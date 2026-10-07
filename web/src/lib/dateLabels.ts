import { fromDate, monthName, toPersianDigits, weekdayName } from './persianDate';
import { hijriMonthName, toHijri } from './hijriDate';

export function solarLabel(date: Date): string {
  const p = fromDate(date);
  return `${weekdayName(p.dayOfWeek)} ${toPersianDigits(p.day)} ${monthName(p.month)} ${toPersianDigits(p.year)}`;
}

export function hijriLabel(date: Date): string {
  const h = toHijri(date);
  return `${toPersianDigits(h.day)} ${hijriMonthName(h.month)} ${toPersianDigits(h.year)}`;
}

export function gregorianLabel(date: Date): string {
  return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' });
}
