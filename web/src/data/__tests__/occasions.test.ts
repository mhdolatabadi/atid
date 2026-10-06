import { describe, expect, it } from 'vitest';
import { isHoliday, occasionsOn } from '../occasions';
import { toHijri } from '../../lib/hijriDate';

const noon = (y: number, m: number, d: number) => new Date(y, m - 1, d, 12);
const titles = (date: Date) => occasionsOn(date).map((o) => o.title);

const holidays1405 = [
  '2026-3-21', '2026-3-22', '2026-3-23', '2026-3-24', '2026-4-1', '2026-4-2', '2026-4-13', '2026-5-27',
  '2026-6-4', '2026-6-24', '2026-6-25', '2026-8-3', '2026-8-11', '2026-8-13', '2026-8-30', '2026-12-22',
  '2027-1-5', '2027-1-23', '2027-2-11', '2027-2-28', '2027-3-9', '2027-3-10', '2027-3-20',
];

describe('lunar dates', () => {
  it('uses the Umm al-Qura calendar', () => {
    expect(toHijri(noon(2026, 9, 30))).toEqual({ year: 1448, month: 4, day: 19 });
  });
});

describe('occasions and holidays', () => {
  it('marks Nowruz', () => {
    expect(titles(noon(2025, 3, 21))).toContain('آغاز نوروز');
    expect(isHoliday(noon(2025, 3, 21))).toBe(true);
  });

  it('treats every Friday as a holiday', () => {
    expect(isHoliday(noon(2026, 10, 2))).toBe(true); // Friday
    expect(isHoliday(noon(2026, 10, 3))).toBe(false); // Saturday, no occasion
  });

  it('lists non-holiday occasions without making the day a holiday', () => {
    expect(titles(noon(2026, 9, 30))).toEqual(['روز بزرگداشت مولوی', 'روز جهانی دریانوردی', 'روز جهانی ناشنوایان']);
    expect(isHoliday(noon(2026, 9, 30))).toBe(false);
  });

  it("marks Imam Reza's martyrdom on the last day of Safar, whatever its length", () => {
    // Walk a few lunar years: the occasion appears exactly once per Safar, on the day before 1 Rabi I.
    let found = 0;
    for (let day = noon(2025, 1, 1); day < noon(2028, 1, 1); day = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1, 12)) {
      if (titles(day).some((t) => t.startsWith('شهادت حضرت امام رضا'))) {
        found++;
        const next = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1, 12);
        expect(toHijri(day).month).toBe(2);
        expect(toHijri(next)).toMatchObject({ month: 3, day: 1 });
      }
    }
    expect(found).toBe(3);
  });

  it('marks Quds day on the last Friday of Ramadan', () => {
    // Ramadan 1447 runs to 19 March 2026; its last Friday is 13 March.
    expect(titles(noon(2026, 3, 13))).toContain('روز جهانی قدس (آخرین جمعهٔ رمضان)');
    expect(titles(noon(2026, 3, 6))).not.toContain('روز جهانی قدس (آخرین جمعهٔ رمضان)');
  });

  it('marks the eve of the last Wednesday of the year', () => {
    // The last Wednesday of Esfand 1404 is 27 Esfand (18 March 2026).
    const title = 'روز تکریم همسایگان (شب آخرین چهارشنبهٔ سال)';
    expect(titles(noon(2026, 3, 17))).toContain(title);
    expect(titles(noon(2026, 3, 10))).not.toContain(title);
  });

  it('marks the second Friday of Mehr', () => {
    // 10 Mehr 1405 (2 October 2026).
    expect(titles(noon(2026, 10, 2))).toContain('آیین مذهبی قالیشویان اردهال (دومین جمعهٔ مهر)');
    expect(titles(noon(2026, 9, 25))).not.toContain('آیین مذهبی قالیشویان اردهال (دومین جمعهٔ مهر)');
  });

  it('keeps the official holiday set', () => {
    // Every non-Friday holiday in solar year 1405 (21 March 2026 – 20 March 2027); same list in OccasionsTest.kt.
    const holidays: string[] = [];
    for (let day = noon(2026, 3, 21); day <= noon(2027, 3, 20); day = new Date(day.getFullYear(), day.getMonth(), day.getDate() + 1, 12)) {
      if (day.getDay() !== 5 && isHoliday(day)) holidays.push(`${day.getFullYear()}-${day.getMonth() + 1}-${day.getDate()}`);
    }
    expect(holidays).toEqual(holidays1405);
  });
});
