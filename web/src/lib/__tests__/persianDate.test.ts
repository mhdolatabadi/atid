import { describe, expect, it } from 'vitest';
import { daysInMonth, fromDate, toGregorianDate, toPersianDigits } from '../persianDate';

// Reference values from the `jdatetime` Python package (also used in the Android tests).
const references: [gregorian: string, year: number, month: number, day: number, weekday: number][] = [
  ['1925-03-21', 1304, 1, 1, 6],
  ['1979-02-11', 1357, 11, 22, 0],
  ['2000-01-01', 1378, 10, 11, 6],
  ['2024-03-19', 1402, 12, 29, 2],
  ['2024-03-20', 1403, 1, 1, 3],
  ['2025-03-20', 1403, 12, 30, 4], // leap year: Esfand 30
  ['2025-03-21', 1404, 1, 1, 5],
  ['2026-09-30', 1405, 7, 8, 3],
  ['2029-03-20', 1408, 1, 1, 2],
  ['2099-12-31', 1478, 10, 11, 4],
];

function localNoon(iso: string): Date {
  const [y, m, d] = iso.split('-').map(Number);
  return new Date(y, m - 1, d, 12);
}

describe('Jalali conversion', () => {
  it.each(references)('%s is %i/%i/%i', (iso, year, month, day, weekday) => {
    expect(fromDate(localNoon(iso))).toEqual({ year, month, day, dayOfWeek: weekday });
  });

  it.each(references)('%s converts back from Jalali', (iso, year, month, day) => {
    const date = toGregorianDate(year, month, day);
    expect([date.getFullYear(), date.getMonth() + 1, date.getDate()]).toEqual(iso.split('-').map(Number));
  });

  it('round-trips every day from 1925 to 2100', () => {
    const day = new Date(1925, 0, 1, 12);
    const end = new Date(2100, 11, 31, 12);
    let previous = fromDate(day);
    for (day.setDate(day.getDate() + 1); day <= end; day.setDate(day.getDate() + 1)) {
      const jalali = fromDate(day);
      const back = toGregorianDate(jalali.year, jalali.month, jalali.day);
      expect(back.toDateString()).toBe(day.toDateString());
      // Consecutive days advance by exactly one Jalali day.
      const advancedInMonth = jalali.month === previous.month && jalali.day === previous.day + 1;
      const startedMonth = jalali.day === 1 && previous.day === daysInMonth(previous.year, previous.month);
      expect(advancedInMonth || startedMonth).toBe(true);
      previous = jalali;
    }
  });

  it('knows the month lengths, including leap Esfand', () => {
    expect(daysInMonth(1405, 1)).toBe(31);
    expect(daysInMonth(1405, 7)).toBe(30);
    expect(daysInMonth(1403, 12)).toBe(30);
    expect(daysInMonth(1404, 12)).toBe(29);
  });
});

describe('toPersianDigits', () => {
  it('keeps leading zeros and separators', () => {
    expect(toPersianDigits('06:05')).toBe('۰۶:۰۵');
    expect(toPersianDigits(1405)).toBe('۱۴۰۵');
  });
});
