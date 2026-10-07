import { fromDate } from '../lib/persianDate';
import { toHijri } from '../lib/hijriDate';
import { occasionRules } from './occasionsData';

export type OccasionCalendar = 'solar' | 'lunar' | 'gregorian';

export interface Occasion {
  title: string;
  holiday: boolean;
  calendar: OccasionCalendar;
}

/**
 * One row of the official calendar (data/occasions/iran.json, generated into occasionsData.ts):
 * calendar, rule, month, day, weekday (Saturday = 0 .. Friday = 6), nth, offset in days, holiday, title.
 * The engine is mirrored 1:1 in app/src/main/java/ir/mhdolatabadi/atid/data/Occasions.kt.
 */
export type OccasionRule = [
  calendar: OccasionCalendar,
  rule: 'date' | 'monthEnd' | 'lastWeekday' | 'nthWeekday',
  month: number,
  day: number,
  weekday: number,
  nth: number,
  offset: number,
  holiday: boolean,
  title: string,
];

const calendarOrder: Record<OccasionCalendar, number> = { solar: 0, lunar: 1, gregorian: 2 };
const rules = [...occasionRules].sort((a, b) => calendarOrder[a[0]] - calendarOrder[b[0]]);

function addDays(date: Date, days: number): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate() + days, 12);
}

function partsOf(date: Date, calendar: OccasionCalendar): { month: number; day: number } {
  if (calendar === 'solar') return fromDate(date);
  if (calendar === 'lunar') return toHijri(date);
  return { month: date.getMonth() + 1, day: date.getDate() };
}

/** Saturday-first weekday (the data's convention) to Date.getDay(). */
function jsWeekday(saturdayFirst: number): number {
  return (saturdayFirst + 6) % 7;
}

function applies([calendar, rule, month, day, weekday, nth, offset]: OccasionRule, date: Date): boolean {
  switch (rule) {
    case 'date': {
      const p = partsOf(date, calendar);
      return p.month === month && p.day === day;
    }
    case 'monthEnd':
      // The month's last day, whether it has 29 or 30 days.
      return partsOf(date, calendar).month === month && partsOf(addDays(date, 1), calendar).month !== month;
    case 'lastWeekday': {
      // e.g. Quds day (last Friday of Ramadan); offset -1 marks the eve of that weekday.
      const target = addDays(date, -offset);
      return (
        target.getDay() === jsWeekday(weekday) &&
        partsOf(target, calendar).month === month &&
        partsOf(addDays(target, 7), calendar).month !== month
      );
    }
    case 'nthWeekday': {
      const p = partsOf(date, calendar);
      return p.month === month && date.getDay() === jsWeekday(weekday) && Math.ceil(p.day / 7) === nth;
    }
  }
}

/** Every official occasion on this day: solar first, then lunar, then Gregorian. */
export function occasionsOn(date: Date): Occasion[] {
  const noon = addDays(date, 0);
  return rules.filter((rule) => applies(rule, noon)).map(([calendar, , , , , , , holiday, title]) => ({ title, holiday, calendar }));
}

/** Fridays and official holidays. */
export function isHoliday(date: Date): boolean {
  return date.getDay() === 5 || occasionsOn(date).some((o) => o.holiday);
}
