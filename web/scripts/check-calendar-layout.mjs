// Start Vite preview, install Playwright locally, then run this optional browser check.
import assert from 'node:assert/strict';
import { chromium } from 'playwright';
const browser = await chromium.launch(process.env.CHROME ? { executablePath: process.env.CHROME } : {});
try {
  const page = await browser.newPage();
  for (const width of [360, 390, 768]) {
    await page.setViewportSize({ width, height: 900 });
    await page.goto(process.env.CHECK_URL ?? 'http://127.0.0.1:4173/');
    await page.evaluate(() => document.fonts.ready);
    await page.waitForSelector('.ti-day');
    for (const colorScheme of ['light', 'dark']) {
      await page.emulateMedia({ colorScheme });
      for (const enlarged of [false, true]) {
        const style = enlarged ? await page.addStyleTag({ content: '.ti-day__sub {font-size:24px!important}' }) : null;
        const geometry = await page.evaluate(() => ({
          overflow: document.documentElement.scrollWidth > innerWidth,
          overlap: [...document.querySelectorAll('.ti-day__sub')].some(sub => {
            const bounds = sub.getBoundingClientRect();
            const [a, b] = [...sub.children].map(el => el.getBoundingClientRect());
            const outside = [a, b].some(r => r.left < bounds.left - 1 || r.right > bounds.right + 1);
            const intersects = a.bottom > b.top && b.bottom > a.top && Math.min(a.right, b.right) > Math.max(a.left, b.left) + 1;
            return outside || intersects;
          }),
        }));
        assert.equal(geometry.overflow, false, `${width}px ${colorScheme}: horizontal overflow`);
        assert.equal(geometry.overlap, false, `${width}px ${colorScheme} enlarged=${enlarged}: secondary dates overlap`);
        if (style) await style.evaluate(el => el.remove());
      }
    }
  }
  console.log('Calendar date layout passed: three widths, two themes, normal/enlarged text');
} finally {
  await browser.close();
}
