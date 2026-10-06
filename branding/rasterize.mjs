// Renders the PNG logo files from the SVGs that generate.py writes. Needs Playwright's Chromium:
//   npm i --no-save playwright && node branding/rasterize.mjs
// (set CHROME=/path/to/chrome to use an existing Chromium instead of a downloaded one).
import { chromium } from 'playwright';
import { readFileSync } from 'node:fs';
import { execFileSync } from 'node:child_process';

const tile = (radius) =>
  readFileSync('branding/logo.svg', 'utf8').replace(/rx="[^"]*"/, `rx="${radius}"`);
const uri = (svg) => 'data:image/svg+xml;base64,' + Buffer.from(svg).toString('base64');
const font = (name) => 'data:font/woff2;base64,' + readFileSync(`web/src/assets/fonts/${name}.woff2`).toString('base64');

const browser = await chromium.launch(process.env.CHROME ? { executablePath: process.env.CHROME } : {});

async function render(file, width, height, html) {
  const page = await browser.newPage({ viewport: { width, height } });
  await page.setContent(`<!doctype html><html><head><style>
    @font-face { font-family: V; src: url(${font('vazirmatn_bold')}); font-weight: 700; }
    @font-face { font-family: V; src: url(${font('vazirmatn_medium')}); font-weight: 500; }
    html, body { margin: 0; background: transparent; }
  </style></head><body>${html}</body></html>`);
  await page.evaluate(() => document.fonts.ready);
  await page.screenshot({ path: file, omitBackground: true });
  await page.close();
  console.log('wrote', file);
}

const img = (svg, size) => `<img src="${uri(svg)}" width="${size}" height="${size}" style="display:block">`;

// Web
await render('web/public/apple-touch-icon.png', 180, 180, img(tile(0), 180)); // iOS rounds it itself
await render('web/public/icon-192.png', 192, 192, img(tile(116), 192));
await render('web/public/icon-512.png', 512, 512, img(tile(116), 512));
await render(
  'web/public/og-image.png',
  1200,
  630,
  `<div dir="rtl" style="width:1200px;height:630px;box-sizing:border-box;padding:0 110px;display:flex;align-items:center;gap:64px;
     background:linear-gradient(135deg,#1a1446 0%,#20307e 55%,#2448c9 100%);font-family:V;color:#fff">
     ${img(tile(116), 300)}
     <div>
       <div style="font-size:112px;font-weight:700;color:#ffe7ad;line-height:1.25">ساعت‌باشی</div>
       <div style="font-size:40px;font-weight:500;line-height:1.7;opacity:.92">تقویم شمسی، قمری و میلادی<br>اوقات شرعی و مناسبت‌ها</div>
     </div>
   </div>`,
);

// Android launcher icons for API 24–25 (26+ uses the adaptive vector icon).
const densities = { mdpi: 48, hdpi: 72, xhdpi: 96, xxhdpi: 144, xxxhdpi: 192 };
for (const [density, size] of Object.entries(densities)) {
  const dir = `app/src/main/res/mipmap-${density}`;
  await render(`${dir}/ic_launcher.png`, size, size, img(tile(96), size));
  await render(`${dir}/ic_launcher_round.png`, size, size, img(tile(256), size));
}

await browser.close();

// Keep the PNGs small.
try {
  execFileSync('sh', ['-c', 'command -v optipng >/dev/null && optipng -quiet -o2 web/public/*.png app/src/main/res/mipmap-*/*.png || true']);
} catch {}
