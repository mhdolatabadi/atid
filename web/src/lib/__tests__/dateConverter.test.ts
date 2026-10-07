import { describe, expect, it } from 'vitest';
import { convert, fromGregorian, fromHijri, fromSolar, yearRanges } from '../dateConverter';
import { toHijri } from '../hijriDate';

function iso(date: Date | null): string | null {
  if (!date) return null;
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

// Umm al-Qura reference values (ICU `islamic-umalqura`, same as JDK HijrahDate in the Android tests).
const hijriReferences: [gregorian: string, year: number, month: number, day: number][] = [
  ['1979-02-11', 1399, 3, 14],
  ['2000-01-01', 1420, 9, 24],
  ['2024-03-11', 1445, 9, 1],
  ['2024-07-07', 1446, 1, 1],
  ['2025-07-05', 1447, 1, 10],
  ['2026-09-30', 1448, 4, 19],
  ['2099-12-31', 1523, 10, 19],
];

describe('fromHijri', () => {
  it.each(hijriReferences)('%s is %i/%i/%i', (gregorian, year, month, day) => {
    expect(iso(fromHijri(year, month, day))).toBe(gregorian);
  });

  it('inverts toHijri for every day 1925–2100', () => {
    const mismatches: string[] = [];
    for (let date = new Date(1925, 0, 1, 12); date.getFullYear() <= 2100; date.setDate(date.getDate() + 1)) {
      const h = toHijri(date);
      const back = fromHijri(h.year, h.month, h.day);
      if (iso(back) !== iso(date)) mismatches.push(iso(date)!);
    }
    expect(mismatches).toEqual([]);
  });

  it('rejects day 30 of a 29-day month', () => {
    // Ramadan 1445 had 30 days, Shawwal 1445 had 29 (Umm al-Qura).
    expect(fromHijri(1445, 9, 30)).not.toBeNull();
    expect(fromHijri(1445, 10, 30)).toBeNull();
  });

  it('rejects out-of-range input', () => {
    expect(fromHijri(1445, 13, 1)).toBeNull();
    expect(fromHijri(1445, 1, 31)).toBeNull();
    expect(fromHijri(yearRanges.hijri[0] - 1, 1, 1)).toBeNull();
    expect(fromHijri(1445.5, 1, 1)).toBeNull();
  });
});

describe('fromSolar', () => {
  it('converts and validates month length', () => {
    expect(iso(fromSolar(1405, 7, 8))).toBe('2026-09-30');
    expect(iso(fromSolar(1403, 12, 30))).toBe('2025-03-20'); // leap year
    expect(fromSolar(1404, 12, 30)).toBeNull();
    expect(fromSolar(1405, 7, 31)).toBeNull();
    expect(fromSolar(1299, 1, 1)).toBeNull();
  });
});

describe('fromGregorian', () => {
  it('converts and validates month length', () => {
    expect(iso(fromGregorian(2024, 2, 29))).toBe('2024-02-29');
    expect(fromGregorian(2025, 2, 29)).toBeNull();
    expect(fromGregorian(2026, 4, 31)).toBeNull();
    expect(fromGregorian(1920, 1, 1)).toBeNull();
  });
});

describe('convert', () => {
  it('dispatches by calendar', () => {
    expect(iso(convert('solar', 1405, 7, 8))).toBe('2026-09-30');
    expect(iso(convert('gregorian', 2026, 9, 30))).toBe('2026-09-30');
    expect(iso(convert('hijri', 1448, 4, 19))).toBe('2026-09-30');
  });
});
