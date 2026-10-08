import { describe, expect, it } from 'vitest';
import { cities, cityBySlug, DEFAULT_CITY } from '../cities';

// Same list as app/src/test/.../data/CitiesTest.kt.
describe('cities', () => {
  it('lists the 31 provincial capitals with unique slugs and names', () => {
    expect(cities).toHaveLength(31);
    expect(new Set(cities.map((c) => c.slug)).size).toBe(31);
    expect(new Set(cities.map((c) => c.name)).size).toBe(31);
    for (const c of cities) expect(c.slug).toMatch(/^[a-z]+(-[a-z]+)*$/);
  });

  it('places every city inside Iran', () => {
    for (const c of cities) {
      expect(c.latitude, c.slug).toBeGreaterThan(25);
      expect(c.latitude, c.slug).toBeLessThan(40);
      expect(c.longitude, c.slug).toBeGreaterThan(44);
      expect(c.longitude, c.slug).toBeLessThan(63.5);
    }
  });

  it('defaults to Tehran and finds cities by slug', () => {
    expect(DEFAULT_CITY.slug).toBe('tehran');
    expect(cityBySlug('mashhad')?.name).toBe('مشهد');
    expect(cityBySlug('nowhere')).toBeUndefined();
  });
});
