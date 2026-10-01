import { describe, expect, it } from 'vitest';
import { isHoliday, occasionsOn } from '../occasions';
import { toHijri } from '../../lib/hijriDate';

const noon = (y: number, m: number, d: number) => new Date(y, m - 1, d, 12);
const titles = (date: Date) => occasionsOn(date).map((o) => o.title);

describe('lunar dates', () => {
  it('uses the Umm al-Qura calendar', () => {
    expect(toHijri(noon(2026, 9, 30))).toEqual({ year: 1448, month: 4, day: 19 });
  });
});

describe('occasions and holidays', () => {
  it('marks Nowruz', () => {
    expect(titles(noon(2025, 3, 21))).toContain('جشن نوروز');
    expect(isHoliday(noon(2025, 3, 21))).toBe(true);
  });

  it('treats every Friday as a holiday', () => {
    expect(isHoliday(noon(2026, 10, 2))).toBe(true); // Friday
    expect(isHoliday(noon(2026, 10, 3))).toBe(false); // Saturday, no occasion
  });

  it('lists non-holiday occasions without making the day a holiday', () => {
    expect(titles(noon(2026, 9, 30))).toEqual(['روز بزرگداشت مولوی']);
    expect(isHoliday(noon(2026, 9, 30))).toBe(false);
  });

  it("marks Imam Reza's martyrdom on the last day of Safar, whatever its length", () => {
    // Walk a few lunar years: the occasion appears exactly once per Safar, on the day before 1 Rabi I.
    let found = 0;
    for (let day = noon(2025, 1, 1); day < noon(2028, 1, 1); day = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1, 12)) {
      if (titles(day).includes('شهادت امام رضا')) {
        found++;
        const next = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1, 12);
        expect(toHijri(day).month).toBe(2);
        expect(toHijri(next)).toMatchObject({ month: 3, day: 1 });
      }
    }
    expect(found).toBe(3);
  });
});
