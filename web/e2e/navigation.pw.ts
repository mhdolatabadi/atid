import { test, expect } from '@playwright/test';

for (const colorScheme of ['light', 'dark'] as const) {
  test(`focused navigation at 360px in ${colorScheme}`, async ({ page }, testInfo) => {
    await page.emulateMedia({ colorScheme });
    await page.goto('/app');
    await expect(page).toHaveURL(/\/app\/calendar$/);
    const nav = page.locator('.app-shell__nav');
    await expect(nav.getByRole('link')).toHaveText(['تقویم', 'اوقات شرعی']);
    await nav.getByRole('link', { name: 'اوقات شرعی', exact: true }).click();
    await expect(page).toHaveURL(/\/app\/prayer-times$/);
    await nav.getByRole('link', { name: 'تقویم', exact: true }).click();
    await expect(page).toHaveURL(/\/app\/calendar$/);
    await expect(page.getByRole('heading', { name: 'تقویم', exact: true })).toBeVisible();
    await page.evaluate(() => document.fonts.ready);
    await page.locator('.page-enter').evaluate(async el => {
      await Promise.all(el.getAnimations().map(animation => animation.finished));
    });
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
    const links = await nav.getByRole('link').all();
    for (const link of links) {
      const bounds = await link.boundingBox();
      expect(bounds?.height).toBeGreaterThanOrEqual(48);
    }
    await page.evaluate(() => scrollTo(0, document.documentElement.scrollHeight));
    const contentBottom = await page.locator('.app-shell__content').evaluate(el => el.lastElementChild!.getBoundingClientRect().bottom);
    const navTop = (await nav.boundingBox())!.y;
    expect(contentBottom).toBeLessThanOrEqual(navTop);
    await page.screenshot({ path: testInfo.outputPath(`navigation-${colorScheme}.png`) });
  });
}

test('legacy qada remains readable and navigation preserves its data', async ({ page }) => {
  await page.goto('/');
  await page.evaluate(() => { localStorage.setItem('qada_fajr', '7'); localStorage.setItem('qada_fast', '4'); });
  await page.goto('/app/home');
  await expect(page.getByRole('heading', { name: 'قضا', exact: true })).toBeVisible();
  await expect(page.locator('.app-shell__indicator')).toHaveCount(0);
  await page.locator('.app-shell__nav').getByRole('link', { name: 'تقویم', exact: true }).click();
  await expect(page).toHaveURL(/\/app\/calendar$/);
  expect(await page.evaluate(() => [localStorage.getItem('qada_fajr'), localStorage.getItem('qada_fast')])).toEqual(['7', '4']);
});
