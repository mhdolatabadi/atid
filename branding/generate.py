#!/usr/bin/env python3
"""Builds every Saatbashi logo file from one geometry, so web and Android never drift apart.

The mark: a kind bald elder with curly white side hair, round glasses and a pocket watch. The sa'at-bashi
was the person who kept the hours and announced the times of prayer.

Run from the repository root:  python3 branding/generate.py
Vector outputs are written directly. PNGs are rasterised from the SVG with Chromium; see
branding/README.md for that step.
"""
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

SKY = ("#1a1446", "#20307e", "#2448c9")
GOLD = ("#ffe2a0", "#f7b84b")
NAVY = "#1b2160"

# Geometry in a 100x100 box.
CX, CY, R = 50, 50, 28  # the disc
HANDS = [(-60, 11), (60, 17)]  # (angle from twelve in degrees, length): ten past ten
HAND_W, HUB_R = 6, 4
MONO_RING_W = 5  # themed (monochrome) icons can't cut the hands out of the disc, so they use a ring


def _n(v):
    return f"{v:.2f}".rstrip("0").rstrip(".")


def _p(x, y, s, ox, oy):
    return f"{_n(x * s + ox)},{_n(y * s + oy)}"


def circle(cx, cy, r, s=1.0, ox=0.0, oy=0.0):
    a, b = _p(cx - r, cy, s, ox, oy), _p(cx + r, cy, s, ox, oy)
    rr = _n(r * s)
    return f"M{a} A{rr},{rr} 0 1,1 {b} A{rr},{rr} 0 1,1 {a} Z"


def hands(s=1.0, ox=0.0, oy=0.0):
    out = []
    for angle, length in HANDS:
        a = math.radians(angle)
        out.append(f"M{_p(CX, CY, s, ox, oy)} L{_p(CX + length * math.sin(a), CY - length * math.cos(a), s, ox, oy)}")
    return " ".join(out)


def layers(s=1.0, ox=0.0, oy=0.0, monochrome=False):
    """(kind, path, width, paint): paint is "gold" (the gradient), "navy" or "mono"."""
    def path(commands):
        return " ".join(cmd + " " + " ".join(_p(x, y, s, ox, oy) for x, y in points) for cmd, points in commands)
    def fill_circle(x, y, r, paint):
        return ("fill", circle(x, y, r, s, ox, oy), 0, paint)
    def line(commands, width, paint="navy"):
        return ("stroke", path(commands), width * s, paint)
    out = [
        fill_circle(50, 65, 19, "navy"),
        fill_circle(50, 43, 19, "skin"),
    ]
    # Deliberately bare crown, with scalloped curls only at the sides.
    for x, y, r in [(31,34,5),(29,41,5),(30,48,5),(34,52,5),
                     (69,34,5),(71,41,5),(70,48,5),(66,52,5)]:
        out.append(fill_circle(x,y,r,"hair"))
    out += [
        line([("M",[(36,37)]),("Q",[(40,34),(44,37)])],1.6),
        line([("M",[(56,37)]),("Q",[(60,34),(64,37)])],1.6),
        ("stroke",circle(41,43,7,s,ox,oy),1.5*s,"navy"),
        ("stroke",circle(59,43,7,s,ox,oy),1.5*s,"navy"),
        line([("M",[(48,43)]),("L",[(52,43)])],1.5),
        fill_circle(41,43,1.5,"navy"),fill_circle(59,43,1.5,"navy"),
        fill_circle(35,50,2.5,"rose"),fill_circle(65,50,2.5,"rose"),
        fill_circle(50,49,3,"skin"),
        line([("M",[(43,54)]),("Q",[(50,61),(57,54)])],2),
        fill_circle(41,59,4,"hair"),fill_circle(59,59,4,"hair"),
        fill_circle(50,72,9,"gold"),
        line([("M",[(50,67)]),("L",[(50,72),(54,74)])],1.8),
        line([("M",[(73,23)]),("L",[(73,29)])],1.6,"gold"),
        line([("M",[(70,26)]),("L",[(76,26)])],1.6,"gold"),
    ]
    if monochrome:
        # Keep face features as outline so the themed icon remains recognizable.
        return [("stroke" if kind == "fill" and paint in ("skin","navy") else kind,
                 data, 1.8*s if kind == "fill" and paint in ("skin","navy") else width,
                 "mono") for kind,data,width,paint in out]
    return out


def tile_svg(size=512, radius=116):
    s = size / 100
    shapes = []
    for kind, path, width, paint in layers(s):
        colour = "url(#gold)" if paint == "gold" else {"gold": GOLD[1], "navy": NAVY, "skin": "#f6cca5", "hair": "#fff3db", "rose": "#df8e86"}[paint]
        if kind == "stroke":
            shapes.append(f'  <path d="{path}" fill="none" stroke="{colour}" stroke-width="{_n(width)}" stroke-linecap="round"/>')
        else:
            shapes.append(f'  <path d="{path}" fill="{colour}"/>')
    body = "\n".join(shapes)
    return f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {size} {size}">
  <title>ساعت‌باشی</title>
  <defs>
    <linearGradient id="sky" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="{SKY[0]}"/>
      <stop offset="0.55" stop-color="{SKY[1]}"/>
      <stop offset="1" stop-color="{SKY[2]}"/>
    </linearGradient>
    <linearGradient id="gold" x1="0" y1="0" x2="0.5" y2="1">
      <stop offset="0" stop-color="{GOLD[0]}"/>
      <stop offset="1" stop-color="{GOLD[1]}"/>
    </linearGradient>
  </defs>
  <rect width="{size}" height="{size}" rx="{radius}" fill="url(#sky)"/>
{body}
</svg>
'''


# Android adaptive icon: a 108dp canvas whose inner 66dp circle is always visible.
ANDROID_SCALE = 1.0
CONTENT_CENTRE = (CX, CY)
OX = 54 - CONTENT_CENTRE[0] * ANDROID_SCALE
OY = 54 - CONTENT_CENTRE[1] * ANDROID_SCALE


def _vector(body, extra_ns=False):
    ns = ' xmlns:aapt="http://schemas.android.com/aapt"' if extra_ns else ""
    return f'''<?xml version="1.0" encoding="utf-8"?>
<!-- Generated by branding/generate.py; edit the geometry there, not here. -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"{ns}
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
{body}
</vector>
'''


def android_background():
    return _vector(f'''    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="linear"
                android:startX="0"
                android:startY="0"
                android:endX="108"
                android:endY="108">
                <item android:offset="0" android:color="{SKY[0].upper()}" />
                <item android:offset="0.55" android:color="{SKY[1].upper()}" />
                <item android:offset="1" android:color="{SKY[2].upper()}" />
            </gradient>
        </aapt:attr>
    </path>''', extra_ns=True)


def android_foreground(monochrome=False):
    parts = []
    for kind, path, width, paint in layers(ANDROID_SCALE, OX, OY, monochrome):
        if kind == "stroke":
            colour = "#FFFFFFFF" if paint == "mono" else {"gold": GOLD[1], "navy": NAVY, "skin": "#f6cca5", "hair": "#fff3db", "rose": "#df8e86"}[paint].upper()
            parts.append(f'''    <path
        android:strokeColor="{colour}"
        android:strokeWidth="{_n(width)}"
        android:strokeLineCap="round"
        android:pathData="{path}" />''')
        elif paint == "gold":
            parts.append(f'''    <path android:pathData="{path}">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="linear"
                android:startX="{_n(OX + (CX - R * 0.5) * ANDROID_SCALE)}"
                android:startY="{_n(OY + (CY - R) * ANDROID_SCALE)}"
                android:endX="{_n(OX + (CX + R * 0.5) * ANDROID_SCALE)}"
                android:endY="{_n(OY + (CY + R) * ANDROID_SCALE)}">
                <item android:offset="0" android:color="{GOLD[0].upper()}" />
                <item android:offset="1" android:color="{GOLD[1].upper()}" />
            </gradient>
        </aapt:attr>
    </path>''')
        else:
            colour = "#FFFFFFFF" if paint == "mono" else {"gold": GOLD[1], "navy": NAVY, "skin": "#f6cca5", "hair": "#fff3db", "rose": "#df8e86"}[paint].upper()
            parts.append(f'''    <path
        android:fillColor="{colour}"
        android:pathData="{path}" />''')
    return _vector("\n".join(parts), extra_ns=not monochrome)


def main():
    # Primary icons now use the approved illustration. This generator only keeps
    # the themed monochrome mark and sky background as native vectors.
    outputs = {
        "app/src/main/res/drawable/ic_launcher_background.xml": android_background(),
        "app/src/main/res/drawable/ic_launcher_monochrome.xml": android_foreground(monochrome=True),
    }
    for rel, text in outputs.items():
        (ROOT / rel).write_text(text, encoding="utf-8")
        print("wrote", rel)


if __name__ == "__main__":
    main()
