// Optional browser regression check. Start `npm run preview -- --host 127.0.0.1`,
// install Playwright locally, then run: node scripts/check-responsive.mjs
import assert from 'node:assert/strict';
import { chromium } from 'playwright';

const browser = await chromium.launch(process.env.CHROME ? { executablePath: process.env.CHROME } : {});
try {
  const page = await browser.newPage();
  for (const width of [360, 640, 700, 768, 820, 900, 1024, 1440]) {
    await page.setViewportSize({ width, height: 900 });
    await page.goto(process.env.CHECK_URL ?? 'http://127.0.0.1:4173/');
    await page.evaluate(() => document.fonts.ready);
    await page.waitForSelector('.ti-today');
    for (const enlarged of [false, true]) {
      if (enlarged) await page.addStyleTag({ content: '.ti-nav a { font-size: 29px; }' });
      const geometry = await page.evaluate(() => {
        const frame = document.querySelector('.ti-header').getBoundingClientRect();
        const sections = ['main.ti-main', '.ti-today', '.ti-about', '.ti-footer'].map(s => document.querySelector(s).getBoundingClientRect());
        return {
          overflow: document.documentElement.scrollWidth > innerWidth,
          aligned: sections.every(r => Math.abs(r.left - frame.left) < 1 && Math.abs(r.width - frame.width) < 1),
          contained: [...document.querySelectorAll('.ti-nav a')].every(a => {
            const r = a.getBoundingClientRect();
            return r.left >= frame.left && r.right <= frame.right && r.height >= 44;
          }),
          brandVisible: document.querySelector('.ti-brand img').naturalWidth > 0,
        };
      });
      assert.equal(geometry.overflow, false, `${width}px enlarged=${enlarged}: overflow`);
      assert.ok(geometry.aligned, `${width}px: shared frame alignment`);
      assert.ok(geometry.contained, `${width}px enlarged=${enlarged}: navigation/touch targets`);
      assert.ok(geometry.brandVisible, `${width}px: character mark loaded`);
    }
  }
  console.log('Responsive landing checks passed at 8 widths, normal and enlarged text');
} finally {
  await browser.close();
}
