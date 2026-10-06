#!/usr/bin/env python3
"""Builds every Saatbashi logo file from one geometry, so web and Android never drift apart.

The mark: a golden pocket watch on its chain, with four hour marks and hands at ten past ten, on a
night-sky tile. The sa'at-bashi was the person who kept the hours and announced the times of prayer.

Run from the repository root:  python3 branding/generate.py
Vector outputs are written directly. PNGs are rasterised from the SVG with Chromium; see
branding/README.md for that step.
"""
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

SKY = ("#1a1446", "#20307e", "#2448c9")
GOLD = ("#ffe7ad", "#ffc45c")

# Geometry in a 100x100 box; the whole mark is centred on (50, 50).
CX, CY, R, RING_W = 48, 57, 26, 5  # watch case
BOW = (CX, CY - R - 10.5, 4.6, 3)  # ring on top: centre x, y, radius, stroke
CROWN = (CX - 3.6, CY - R - 6.2, 7.2, 5.4, 1.4)  # x, y, width, height, corner radius
CHAIN = ((CX + 4.6, CY - R - 10.5), (CX + 16, CY - R - 14), (CX + 30, CY - R - 6), (CX + 31, CY - R + 8))
CHAIN_W, CHAIN_ALPHA = 2.2, 0.9
TICK_INNER, TICK_LEN, TICK_W = R - RING_W / 2 - 3.2, 4.2, 3.2  # marks at 12, 3, 6 and 9
HANDS = [(-60, R * 0.42), (60, R * 0.62)]  # (angle from twelve in degrees, length): ten past ten
HAND_W, HUB_R = 5, 3.2


def _n(v):
    return f"{v:.2f}".rstrip("0").rstrip(".")


def _p(x, y, s, ox, oy):
    return f"{_n(x * s + ox)},{_n(y * s + oy)}"


def circle(cx, cy, r, s=1.0, ox=0.0, oy=0.0):
    a, b = _p(cx - r, cy, s, ox, oy), _p(cx + r, cy, s, ox, oy)
    rr = _n(r * s)
    return f"M{a} A{rr},{rr} 0 1,1 {b} A{rr},{rr} 0 1,1 {a} Z"


def rounded_rect(x, y, w, h, r, s=1.0, ox=0.0, oy=0.0):
    rr = _n(r * s)
    return (f"M{_p(x + r, y, s, ox, oy)} H{_n((x + w - r) * s + ox)} A{rr},{rr} 0 0,1 {_p(x + w, y + r, s, ox, oy)} "
            f"V{_n((y + h - r) * s + oy)} A{rr},{rr} 0 0,1 {_p(x + w - r, y + h, s, ox, oy)} "
            f"H{_n((x + r) * s + ox)} A{rr},{rr} 0 0,1 {_p(x, y + h - r, s, ox, oy)} "
            f"V{_n((y + r) * s + oy)} A{rr},{rr} 0 0,1 {_p(x + r, y, s, ox, oy)} Z")


def chain(s=1.0, ox=0.0, oy=0.0):
    a, b, c, d = (_p(x, y, s, ox, oy) for x, y in CHAIN)
    return f"M{a} C{b} {c} {d}"


def ticks(s=1.0, ox=0.0, oy=0.0):
    out = []
    for i in range(4):
        a = math.radians(i * 90)
        x1, y1 = CX + TICK_INNER * math.sin(a), CY - TICK_INNER * math.cos(a)
        x2, y2 = CX + (TICK_INNER - TICK_LEN) * math.sin(a), CY - (TICK_INNER - TICK_LEN) * math.cos(a)
        out.append(f"M{_p(x1, y1, s, ox, oy)} L{_p(x2, y2, s, ox, oy)}")
    return " ".join(out)


def hands(s=1.0, ox=0.0, oy=0.0):
    out = []
    for angle, length in HANDS:
        a = math.radians(angle)
        out.append(f"M{_p(CX, CY, s, ox, oy)} L{_p(CX + length * math.sin(a), CY - length * math.cos(a), s, ox, oy)}")
    return " ".join(out)


def layers(s=1.0, ox=0.0, oy=0.0):
    """(kind, path, width, paint, alpha): paint is "gold" (the gradient) or "light" (the pale gold)."""
    return [
        ("stroke", chain(s, ox, oy), CHAIN_W * s, "light", CHAIN_ALPHA),
        ("stroke", circle(BOW[0], BOW[1], BOW[2], s, ox, oy), BOW[3] * s, "gold", 1),
        ("fill", rounded_rect(*CROWN, s, ox, oy), 0, "gold", 1),
        ("stroke", circle(CX, CY, R, s, ox, oy), RING_W * s, "gold", 1),
        ("stroke", ticks(s, ox, oy), TICK_W * s, "light", 1),
        ("stroke", hands(s, ox, oy), HAND_W * s, "light", 1),
        ("fill", circle(CX, CY, HUB_R, s, ox, oy), 0, "light", 1),
    ]


def tile_svg(size=512, radius=116):
    s = size / 100
    shapes = []
    for kind, path, width, paint, alpha in layers(s):
        colour = "url(#gold)" if paint == "gold" else GOLD[0]
        opacity = f' opacity="{_n(alpha)}"' if alpha != 1 else ""
        if kind == "stroke":
            shapes.append(f'  <path d="{path}" fill="none" stroke="{colour}" stroke-width="{_n(width)}" stroke-linecap="round"{opacity}/>')
        else:
            shapes.append(f'  <path d="{path}" fill="{colour}"{opacity}/>')
    body = "\n".join(shapes)
    return f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {size} {size}">
  <title>ساعت‌باشی</title>
  <defs>
    <linearGradient id="sky" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="{SKY[0]}"/>
      <stop offset="0.55" stop-color="{SKY[1]}"/>
      <stop offset="1" stop-color="{SKY[2]}"/>
    </linearGradient>
    <linearGradient id="gold" x1="0" y1="0" x2="0.4" y2="1">
      <stop offset="0" stop-color="{GOLD[0]}"/>
      <stop offset="1" stop-color="{GOLD[1]}"/>
    </linearGradient>
  </defs>
  <rect width="{size}" height="{size}" rx="{radius}" fill="url(#sky)"/>
{body}
</svg>
'''


# Android adaptive icon: a 108dp canvas whose inner 66dp circle is always visible.
ANDROID_SCALE = 0.86
CONTENT_CENTRE = (50, 50)
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
    for kind, path, width, paint, alpha in layers(ANDROID_SCALE, OX, OY):
        if monochrome:
            colour, a = "#FFFFFFFF", 1
        else:
            colour, a = ("#FFD27A" if paint == "gold" else GOLD[0].upper()), alpha
        if kind == "stroke":
            parts.append(f'''    <path
        android:strokeColor="{colour}"
        android:strokeAlpha="{_n(a)}"
        android:strokeWidth="{_n(width)}"
        android:strokeLineCap="round"
        android:pathData="{path}" />''')
        else:
            parts.append(f'''    <path
        android:fillColor="{colour}"
        android:fillAlpha="{_n(a)}"
        android:pathData="{path}" />''')
    return _vector("\n".join(parts))


def main():
    outputs = {
        "branding/logo.svg": tile_svg(),
        "web/public/favicon.svg": tile_svg(),
        "app/src/main/res/drawable/ic_launcher_background.xml": android_background(),
        "app/src/main/res/drawable/ic_launcher_foreground.xml": android_foreground(),
        "app/src/main/res/drawable/ic_launcher_monochrome.xml": android_foreground(monochrome=True),
    }
    for rel, text in outputs.items():
        (ROOT / rel).write_text(text, encoding="utf-8")
        print("wrote", rel)


if __name__ == "__main__":
    main()
