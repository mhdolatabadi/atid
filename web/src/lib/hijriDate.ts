// Lunar (Hijri) dates from the browser's Umm al-Qura calendar. Iran's official lunar calendar
// follows moon sighting, so a computed date can be a day off from the published one; every
// place that shows these dates says so.

export interface HijriDate {
  year: number;
  month: number; // 1..12
  day: number;
}

export const hijriMonthNames = [
  'محرم', 'صفر', 'ربیع‌الاول', 'ربیع‌الثانی', 'جمادی‌الاول', 'جمادی‌الثانی',
  'رجب', 'شعبان', 'رمضان', 'شوال', 'ذی‌القعده', 'ذی‌الحجه',
];

const formatter = new Intl.DateTimeFormat('en-US-u-ca-islamic-umalqura-nu-latn', {
  day: 'numeric',
  month: 'numeric',
  year: 'numeric',
});

const cache = new Map<string, HijriDate>();

export function toHijri(date: Date): HijriDate {
  const key = `${date.getFullYear()}-${date.getMonth()}-${date.getDate()}`;
  const cached = cache.get(key);
  if (cached) return cached;
  // Noon keeps the conversion on the same civil day regardless of DST.
  const noon = new Date(date.getFullYear(), date.getMonth(), date.getDate(), 12);
  const parts = formatter.formatToParts(noon);
  const value = (type: string) => Number(parts.find((p) => p.type === type)?.value);
  const result = { year: value('year'), month: value('month'), day: value('day') };
  cache.set(key, result);
  return result;
}

export function hijriMonthName(month: number): string {
  return hijriMonthNames[Math.min(Math.max(month - 1, 0), 11)];
}
