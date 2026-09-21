// Jalali (Persian/Shamsi) calendar helpers.
//
// There's no reliable built-in Persian-calendar conversion across browsers, so this ports the
// same Gregorian<->Jalali algorithm used by the Android app (Kazimierski/Borkowski, via a Julian
// day number), which was validated against the `jdatetime` reference implementation across every
// day from 1925-2100. Integer division here always truncates toward zero (via `idiv`) to match
// Kotlin/Java's Int division semantics exactly; the `%` operator already matches (both languages
// use truncated-division remainder).

export interface PersianDate {
  year: number;
  month: number; // 1..12
  day: number;
  /** JS Date.getDay() weekday constant: Sunday=0 .. Saturday=6. */
  dayOfWeek: number;
}

export const monthNames = [
  'فروردین', 'اردیبهشت', 'خرداد', 'تیر', 'مرداد', 'شهریور',
  'مهر', 'آبان', 'آذر', 'دی', 'بهمن', 'اسفند',
];

const weekdayNames: Record<number, string> = {
  6: 'شنبه',
  0: 'یک‌شنبه',
  1: 'دوشنبه',
  2: 'سه‌شنبه',
  3: 'چهارشنبه',
  4: 'پنجشنبه',
  5: 'جمعه',
};

function idiv(a: number, b: number): number {
  return Math.trunc(a / b);
}

export function today(): PersianDate {
  return fromDate(new Date());
}

export function fromDate(date: Date): PersianDate {
  const gy = date.getFullYear();
  const gm = date.getMonth() + 1;
  const gd = date.getDate();
  const [jy, jm, jd] = gregorianToJalali(gy, gm, gd);
  return { year: jy, month: jm, day: jd, dayOfWeek: date.getDay() };
}

export function monthName(month: number): string {
  return monthNames[Math.min(Math.max(month - 1, 0), 11)];
}

export function weekdayName(dayOfWeek: number): string {
  return weekdayNames[dayOfWeek] ?? '';
}

export function daysInMonth(year: number, month: number): number {
  if (month <= 6) return 31;
  if (month <= 11) return 30;
  return isLeapJalaaliYear(year) ? 30 : 29;
}

/** The weekday (JS Date.getDay() constant) of the 1st day of [year]/[month]. */
export function firstDayOfWeek(year: number, month: number): number {
  const [gy, gm, gd] = jalaliToGregorian(year, month, 1);
  return new Date(gy, gm - 1, gd).getDay();
}

/** Converts a JS weekday constant (Sunday=0) to a 0..6 index with Saturday first (Iranian week). */
export function weekdayIndexSaturdayFirst(dayOfWeek: number): number {
  return dayOfWeek === 6 ? 0 : dayOfWeek + 1;
}

const persianDigits = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];

export function toPersianDigits(number: number): string {
  return String(number).replace(/[0-9]/g, (c) => persianDigits[Number(c)]);
}

// --- Gregorian <-> Jalali conversion (Julian day number based) ---

const breaks = [
  -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
  1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
];

/** Returns [rawLeapCounter, gregorianYear, marchDayOfNowruz] for Jalali year `jy`. */
function jalCal(jy: number): [number, number, number] {
  const gy = jy + 621;
  let leapJ = -14;
  let jp = breaks[0];
  let jump = 0;
  let i = 1;
  while (i < breaks.length) {
    const jm = breaks[i];
    jump = jm - jp;
    if (jy < jm) break;
    leapJ = leapJ + idiv(jump, 33) * 8 + idiv(jump % 33, 4);
    jp = jm;
    i++;
  }
  let n = jy - jp;
  leapJ = leapJ + idiv(n, 33) * 8 + idiv(n % 33 + 3, 4);
  if (jump % 33 === 4 && jump - n === 4) {
    leapJ += 1;
  }
  const leapG = idiv(gy, 4) - idiv((idiv(gy, 100) + 1) * 3, 4) - 150;
  const march = 20 + leapJ - leapG;
  if (jump - n < 6) {
    n = n - jump + idiv(jump, 33) * 33;
  }
  let leap = (((n + 1) % 33) - 1) % 4;
  if (leap === -1) leap = 4;
  return [leap, gy, march];
}

function isLeapJalaaliYear(jy: number): boolean {
  return jalCal(jy)[0] === 0;
}

function gregorianToJulianDay(gy: number, gm: number, gd: number): number {
  const d = idiv((gy + idiv(gm - 8, 6) + 100100) * 1461, 4) +
    idiv(153 * ((gm + 9) % 12) + 2, 5) +
    gd - 34840408;
  return d - idiv(idiv(gy + 100100 + idiv(gm - 8, 6), 100) * 3, 4) + 752;
}

function julianDayToGregorian(jdn: number): [number, number, number] {
  let j = 4 * jdn + 139361631;
  j += idiv(idiv(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908;
  const i = idiv(j % 1461, 4) * 5 + 308;
  const gd = idiv(i % 153, 5) + 1;
  const gm = idiv(i, 153) % 12 + 1;
  const gy = idiv(j, 1461) - 100100 + idiv(8 - gm, 6);
  return [gy, gm, gd];
}

function jalaliToJulianDay(jy: number, jm: number, jd: number): number {
  const [, gy, march] = jalCal(jy);
  return gregorianToJulianDay(gy, 3, march) + (jm - 1) * 31 - idiv(jm, 7) * (jm - 7) + jd - 1;
}

function julianDayToJalali(jdn: number): [number, number, number] {
  const [gy] = julianDayToGregorian(jdn);
  let jy = gy - 621;
  const [leap, , march] = jalCal(jy);
  const jdn1f = gregorianToJulianDay(gy, 3, march);
  let k = jdn - jdn1f;
  if (k >= 0) {
    if (k <= 185) {
      return [jy, 1 + idiv(k, 31), (k % 31) + 1];
    }
    k -= 186;
  } else {
    jy -= 1;
    k += 179;
    if (leap === 1) k += 1;
  }
  return [jy, 7 + idiv(k, 30), (k % 30) + 1];
}

function gregorianToJalali(gy: number, gm: number, gd: number): [number, number, number] {
  return julianDayToJalali(gregorianToJulianDay(gy, gm, gd));
}

function jalaliToGregorian(jy: number, jm: number, jd: number): [number, number, number] {
  return julianDayToGregorian(jalaliToJulianDay(jy, jm, jd));
}
