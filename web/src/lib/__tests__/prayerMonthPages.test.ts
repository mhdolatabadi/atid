import { describe, expect, it } from 'vitest';
import { BUILD_YEAR, cityTimesOn, monthDays, prayerMonthPath, prayerYears } from '../calendarPages';
import { calculate, midnight } from '../prayerTimes';
import { cities, DEFAULT_CITY } from '../../data/cities';
import { metaFor, prerenderRoutes, structuredDataFor } from '../../seo';

describe('monthly prayer-time tables', () => {
  it('publishes every city for this and next year', () => {
    expect(prayerYears).toEqual([BUILD_YEAR, BUILD_YEAR + 1]);
    const tables = prerenderRoutes.filter((path) => /^\/prayer-times\/[a-z-]+\/\d{4}\/[a-z]+$/.test(path));
    expect(tables).toHaveLength(cities.length * 2 * 12);
    expect(prerenderRoutes).toContain(prayerMonthPath('tehran', BUILD_YEAR, 7));
    expect(metaFor(prayerMonthPath('tehran', BUILD_YEAR - 1, 7)).noindex).toBe(true);
    expect(metaFor(prayerMonthPath('atlantis', BUILD_YEAR, 7)).noindex).toBe(true);
  });

  it('computes each row with the shared calculator on Iran time', () => {
    const [first] = monthDays(1405, 7); // 1 Mehr 1405 = 23 September 2026
    const expected = calculate(2026, 9, 23, DEFAULT_CITY.latitude, DEFAULT_CITY.longitude, 3.5);
    expect(cityTimesOn(DEFAULT_CITY, first)).toEqual({ ...expected, midnight: midnight(expected) });
  });

  it('nests tables under the city in breadcrumbs', () => {
    const path = prayerMonthPath('tehran', BUILD_YEAR, 1);
    const crumbs = structuredDataFor(path, 'https://example.test')['@graph'][1] as { itemListElement: { item: string }[] };
    expect(crumbs.itemListElement.map((c) => c.item)).toEqual([
      'https://example.test/', 'https://example.test/prayer-times', 'https://example.test/prayer-times/tehran',
      `https://example.test${path}`,
    ]);
  });
});
