#!/usr/bin/env python3
"""Resize the approved illustration; never redraw the character during a build.
Requires Pillow. Run from any directory: python3 branding/build_character.py
"""
from pathlib import Path
from base64 import b64encode
from io import BytesIO
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
source = Image.open(ROOT / 'branding/source/saatbashi-approved.png').convert('RGB')
# The approved full-body portrait is kept intact in the guide artwork.
source.resize((512, 768), Image.Resampling.LANCZOS).save(ROOT / 'web/public/saatbashi-character.webp', quality=88)
source.resize((512, 768), Image.Resampling.LANCZOS).save(ROOT / 'app/src/main/res/drawable-nodpi/saatbashi_character.webp', quality=88)
# Close portrait: preserve bald crown, curls, glasses and welcoming expression.
portrait = source.crop((170, 42, 728, 600)).resize((512, 512), Image.Resampling.LANCZOS)
buf = BytesIO()
portrait.save(buf, format='PNG', optimize=True)
svg = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><title>ساعت‌باشی</title><image width="512" height="512" href="data:image/png;base64,' + b64encode(buf.getvalue()).decode() + '"/></svg>\n'
for path in ['branding/logo.svg', 'web/public/favicon.svg']:
    (ROOT / path).write_text(svg)
for name, size in [('icon-192',192),('icon-512',512),('apple-touch-icon',180)]:
    portrait.resize((size,size), Image.Resampling.LANCZOS).save(ROOT / f'web/public/{name}.png', optimize=True)
for density, size in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    tile=portrait.resize((size,size),Image.Resampling.LANCZOS)
    tile.save(ROOT / f'app/src/main/res/mipmap-{density}/ic_launcher.png', optimize=True)
    mask=Image.new('L',(size,size)); ImageDraw.Draw(mask).ellipse((0,0,size-1,size-1),fill=255)
    rounded=tile.convert('RGBA'); rounded.putalpha(mask)
    rounded.save(ROOT / f'app/src/main/res/mipmap-{density}/ic_launcher_round.png',optimize=True)
# Adaptive icon occupies the central safe zone; broad cream padding survives every mask.
adaptive=Image.new('RGB',(432,432),source.getpixel((0,0)))
adaptive.paste(portrait.resize((248,248),Image.Resampling.LANCZOS),(92,92))
adaptive.save(ROOT / 'app/src/main/res/drawable-nodpi/saatbashi_launcher.webp',quality=92)
# Sharing art uses the same approved face. Accessible Persian metadata supplies the title.
share=Image.new('RGB',(1200,630),(27,33,96))
share.paste(portrait.resize((490,490),Image.Resampling.LANCZOS),(355,70))
share.save(ROOT / 'web/public/og-image.png',optimize=True)
print('Built approved character assets for web and Android')
