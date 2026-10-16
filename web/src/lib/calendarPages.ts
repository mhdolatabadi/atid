// Static yearly and monthly calendar pages (/calendar/1405, /calendar/1405/mehr). Their content
// depends only on the year and month, never on "today", so it is safe to pre-render in full.

import { daysInMonth, fromDate, gregorianMonthNames, monthName, toGregorianDate, toPersianDigits } from './persianDate';
import { hijriMonthName, toHijri } from './hijriDate';
import { calculate, IRAN_UTC_OFFSET, midnight } from './prayerTimes';
import type { City } from '../data/cities';

export const monthSlugs = [
  'farvardin', 'ordibehesht', 'khordad', 'tir', 'mordad', 'shahrivar',
  'mehr', 'aban', 'azar', 'dey', 'bahman', 'esfand',
] as const;

/** The Jalali year of the build (vite.config.ts), so server and browser agree on which pages exist. */
export const BUILD_YEAR: number = __BUILD_JALALI_YEAR__;

/** Published years: last year, this year and next year. */
export const calendarYears = [BUILD_YEAR - 1, BUILD_YEAR, BUILD_YEAR + 1];

/** Years with monthly prayer-time tables per city: this year and next year. */
export const prayerYears = [BUILD_YEAR, BUILD_YEAR + 1];

export function isCalendarYear(year: number): boolean {
  return calendarYears.includes(year);
}

export function parseYear(value: string | undefined): number | undefined {
  if (!value || !/^\d{4}$/.test(value)) return undefined;
  const year = Number(value);
  return isCalendarYear(year) ? year : undefined;
}

export function parseMonth(value: string | undefined): number | undefined {
  const index = monthSlugs.indexOf(value as (typeof monthSlugs)[number]);
  return index >= 0 ? index + 1 : undefined;
}

export function yearPath(year: number): string {
  return `/calendar/${year}`;
}

export function monthPath(year: number, month: number): string {
  return `/calendar/${year}/${monthSlugs[month - 1]}`;
}

/** The neighbouring month, or undefined when it falls outside the published years. */
export function shiftMonth(
  year: number, month: number, delta: number, years: number[] = calendarYears,
): { year: number; month: number } | undefined {
  const index = year * 12 + (month - 1) + delta;
  const next = { year: Math.floor(index / 12), month: (index % 12) + 1 };
  return years.includes(next.year) ? next : undefined;
}

export function prayerMonthPath(citySlug: string, year: number, month: number): string {
  return `/prayer-times/${citySlug}/${year}/${monthSlugs[month - 1]}`;
}

export function monthDays(year: number, month: number): Date[] {
  return Array.from({ length: daysInMonth(year, month) }, (_, i) => toGregorianDate(year, month, i + 1));
}

/** «۲۳ سپتامبر ۲۰۲۶»: deterministic on server and browser, unlike toLocaleDateString. */
export function gregorianDayLabel(date: Date): string {
  return `${toPersianDigits(date.getDate())} ${gregorianMonthNames[date.getMonth()]} ${toPersianDigits(date.getFullYear())}`;
}

export function hijriDayLabel(date: Date): string {
  const h = toHijri(date);
  return `${toPersianDigits(h.day)} ${hijriMonthName(h.month)} ${toPersianDigits(h.year)}`;
}

/** «مهر ۱۴۰۵» */
export function monthTitle(year: number, month: number): string {
  return `${monthName(month)} ${toPersianDigits(year)}`;
}

/** Lunar and Gregorian months a Jalali month spans, e.g. «ربیع‌الثانی – جمادی‌الاول ۱۴۴۸». */
export function monthSpan(year: number, month: number): { hijri: string; gregorian: string } {
  const days = monthDays(year, month);
  const first = days[0];
  const last = days[days.length - 1];
  const h1 = toHijri(first);
  const h2 = toHijri(last);
  const hijri = h1.month === h2.month && h1.year === h2.year
    ? `${hijriMonthName(h1.month)} ${toPersianDigits(h1.year)}`
    : `${hijriMonthName(h1.month)}${h1.year !== h2.year ? ` ${toPersianDigits(h1.year)}` : ''} – ${hijriMonthName(h2.month)} ${toPersianDigits(h2.year)}`;
  const g1 = gregorianMonthNames[first.getMonth()];
  const g2 = gregorianMonthNames[last.getMonth()];
  const gregorian = first.getMonth() === last.getMonth()
    ? `${g1} ${toPersianDigits(first.getFullYear())}`
    : `${g1}${first.getFullYear() !== last.getFullYear() ? ` ${toPersianDigits(first.getFullYear())}` : ''} – ${g2} ${toPersianDigits(last.getFullYear())}`;
  return { hijri, gregorian };
}

export function solarDayLabel(date: Date): string {
  const p = fromDate(date);
  return `${toPersianDigits(p.day)} ${monthName(p.month)}`;
}

/** A city's prayer times on one day, on Iran time, including shar'i midnight. */
export function cityTimesOn(city: City, date: Date) {
  const times = calculate(date.getFullYear(), date.getMonth() + 1, date.getDate(), city.latitude, city.longitude, IRAN_UTC_OFFSET);
  return { ...times, midnight: midnight(times) };
}
