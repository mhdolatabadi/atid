import { describe, expect, it } from 'vitest';
import {
  BUILD_YEAR, calendarYears, gregorianDayLabel, hijriDayLabel, monthDays, monthPath, monthSpan, parseMonth, parseYear,
  shiftMonth,
} from '../calendarPages';
import { metaFor, prerenderRoutes, structuredDataFor } from '../../seo';

describe('static calendar pages', () => {
  it('publishes last, this and next Jalali year of the build', () => {
    expect(calendarYears).toEqual([BUILD_YEAR - 1, BUILD_YEAR, BUILD_YEAR + 1]);
    expect(parseYear(String(BUILD_YEAR))).toBe(BUILD_YEAR);
    expect(parseYear(String(BUILD_YEAR + 5))).toBeUndefined();
    expect(parseYear('14O5')).toBeUndefined();
  });

  it('maps Latin month slugs both ways', () => {
    expect(parseMonth('mehr')).toBe(7);
    expect(parseMonth('esfand')).toBe(12);
    expect(parseMonth('Mehr')).toBeUndefined();
    expect(monthPath(1405, 7)).toBe('/calendar/1405/mehr');
  });

  it('moves across year boundaries but not past the published years', () => {
    expect(shiftMonth(BUILD_YEAR, 12, 1)).toEqual({ year: BUILD_YEAR + 1, month: 1 });
    expect(shiftMonth(BUILD_YEAR, 1, -1)).toEqual({ year: BUILD_YEAR - 1, month: 12 });
    expect(shiftMonth(BUILD_YEAR + 1, 12, 1)).toBeUndefined();
    expect(shiftMonth(BUILD_YEAR - 1, 1, -1)).toBeUndefined();
  });

  it('lists every day of a month with its three dates (same reference as persianDate tests)', () => {
    const days = monthDays(1405, 7);
    expect(days).toHaveLength(30);
    expect(gregorianDayLabel(days[0])).toBe('۲۳ سپتامبر ۲۰۲۶');
    expect(hijriDayLabel(days[0])).toMatch(/ربیع‌الثانی ۱۴۴۸$/);
    expect(monthSpan(1405, 10).gregorian).toBe('دسامبر ۲۰۲۶ – ژانویه ۲۰۲۷');
  });

  it('pre-renders and describes every published year and month', () => {
    for (const year of calendarYears) {
      expect(prerenderRoutes).toContain(`/calendar/${year}`);
      for (let month = 1; month <= 12; month++) expect(prerenderRoutes).toContain(monthPath(year, month));
    }
    expect(metaFor(`/calendar/${BUILD_YEAR + 5}`).noindex).toBe(true);
    expect(metaFor(`/calendar/${BUILD_YEAR}/not-a-month`).noindex).toBe(true);
  });

  it('nests month breadcrumbs under the guide and the year', () => {
    const path = monthPath(BUILD_YEAR, 7);
    const crumbs = structuredDataFor(path, 'https://example.test')['@graph'][1] as { itemListElement: { item: string }[] };
    expect(crumbs.itemListElement.map((c) => c.item)).toEqual([
      'https://example.test/', 'https://example.test/calendar', `https://example.test/calendar/${BUILD_YEAR}`,
      `https://example.test${path}`,
    ]);
  });
});
