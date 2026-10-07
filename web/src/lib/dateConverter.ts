// Turns a day/month/year typed into the converter into a civil date. Each function returns a Date
// at local noon, or null when the input is not a real day in that calendar's supported range.
// Mirrored 1:1 in app/src/main/java/ir/mhdolatabadi/atid/util/DateConverter.kt; change both together.

import { daysInMonth, toGregorianDate } from './persianDate';
import { toHijri } from './hijriDate';

export type CalendarKind = 'solar' | 'gregorian' | 'hijri';

/** Supported input years; together they cover roughly 1921–2121. */
export const yearRanges: Record<CalendarKind, [min: number, max: number]> = {
  solar: [1300, 1500],
  gregorian: [1921, 2121],
  hijri: [1340, 1545],
};

const MS_PER_DAY = 86_400_000;
/** 1 Muharram 1 AH in the proleptic Gregorian calendar. */
const HIJRI_EPOCH = Date.UTC(622, 6, 19);
const MEAN_LUNAR_YEAR = 354.367;
const MEAN_LUNAR_MONTH = 29.5306;

function isWhole(...values: number[]): boolean {
  return values.every((v) => Number.isInteger(v));
}

function inRange(kind: CalendarKind, year: number, month: number, day: number): boolean {
  const [min, max] = yearRanges[kind];
  return isWhole(year, month, day) && year >= min && year <= max && month >= 1 && month <= 12 && day >= 1;
}

export function fromSolar(year: number, month: number, day: number): Date | null {
  if (!inRange('solar', year, month, day) || day > daysInMonth(year, month)) return null;
  return toGregorianDate(year, month, day);
}

export function fromGregorian(year: number, month: number, day: number): Date | null {
  if (!inRange('gregorian', year, month, day)) return null;
  const date = new Date(year, month - 1, day, 12);
  return date.getMonth() === month - 1 ? date : null;
}

/**
 * Umm al-Qura has no closed-form inverse, so this estimates the day from mean month lengths and
 * then searches nearby days with the same `toHijri` used everywhere else, keeping both directions
 * consistent. Day 30 of a 29-day month finds no match and is rejected.
 */
export function fromHijri(year: number, month: number, day: number): Date | null {
  if (!inRange('hijri', year, month, day) || day > 30) return null;
  const estimate = new Date(
    HIJRI_EPOCH + Math.round((year - 1) * MEAN_LUNAR_YEAR + (month - 1) * MEAN_LUNAR_MONTH + (day - 1)) * MS_PER_DAY,
  );
  const base = new Date(estimate.getUTCFullYear(), estimate.getUTCMonth(), estimate.getUTCDate(), 12);
  for (let offset = 0; offset <= 20; offset++) {
    for (const sign of offset === 0 ? [1] : [1, -1]) {
      const candidate = new Date(base.getFullYear(), base.getMonth(), base.getDate() + sign * offset, 12);
      const h = toHijri(candidate);
      if (h.year === year && h.month === month && h.day === day) return candidate;
    }
  }
  return null;
}

export function convert(kind: CalendarKind, year: number, month: number, day: number): Date | null {
  switch (kind) {
    case 'solar':
      return fromSolar(year, month, day);
    case 'gregorian':
      return fromGregorian(year, month, day);
    case 'hijri':
      return fromHijri(year, month, day);
  }
}
