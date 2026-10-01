import { fromDate } from '../lib/persianDate';
import { toHijri } from '../lib/hijriDate';

export interface Occasion {
  title: string;
  holiday: boolean;
  calendar: 'solar' | 'lunar' | 'gregorian';
}

type Entry = [month: number, day: number, title: string, holiday: boolean];

// Official public holidays of Iran plus a few widely marked occasions. Mirrored 1:1 in
// app/src/main/java/ir/mhdolatabadi/atid/data/Occasions.kt: change both, and both test suites.
const solar: Entry[] = [
  [1, 1, 'جشن نوروز', true],
  [1, 2, 'عید نوروز', true],
  [1, 3, 'عید نوروز', true],
  [1, 4, 'عید نوروز', true],
  [1, 12, 'روز جمهوری اسلامی', true],
  [1, 13, 'روز طبیعت', true],
  [2, 12, 'روز معلم', false],
  [2, 25, 'روز بزرگداشت فردوسی', false],
  [3, 14, 'رحلت امام خمینی', true],
  [3, 15, 'قیام ۱۵ خرداد', true],
  [5, 14, 'صدور فرمان مشروطیت', false],
  [7, 1, 'آغاز سال تحصیلی', false],
  [7, 8, 'روز بزرگداشت مولوی', false],
  [8, 13, 'روز دانش‌آموز', false],
  [9, 16, 'روز دانشجو', false],
  [9, 30, 'شب یلدا', false],
  [11, 22, 'پیروزی انقلاب اسلامی', true],
  [12, 15, 'روز درختکاری', false],
  [12, 29, 'روز ملی شدن صنعت نفت', true],
];

const lunar: Entry[] = [
  [1, 1, 'آغاز سال هجری قمری', false],
  [1, 9, 'تاسوعای حسینی', true],
  [1, 10, 'عاشورای حسینی', true],
  [2, 20, 'اربعین حسینی', true],
  [2, 28, 'رحلت حضرت رسول اکرم و شهادت امام حسن مجتبی', true],
  [3, 8, 'شهادت امام حسن عسکری', true],
  [3, 17, 'میلاد حضرت رسول اکرم و امام جعفر صادق', true],
  [6, 3, 'شهادت حضرت فاطمه زهرا', true],
  [6, 20, 'ولادت حضرت فاطمه زهرا و روز مادر', false],
  [7, 13, 'ولادت امام علی و روز پدر', true],
  [7, 27, 'مبعث حضرت رسول اکرم', true],
  [8, 3, 'ولادت امام حسین', false],
  [8, 15, 'ولادت حضرت قائم', true],
  [9, 21, 'شهادت امام علی', true],
  [10, 1, 'عید سعید فطر', true],
  [10, 2, 'تعطیل به مناسبت عید سعید فطر', true],
  [10, 25, 'شهادت امام جعفر صادق', true],
  [11, 11, 'ولادت امام رضا', false],
  [12, 9, 'روز عرفه', false],
  [12, 10, 'عید سعید قربان', true],
  [12, 18, 'عید سعید غدیر خم', true],
];

const gregorian: Entry[] = [
  [1, 1, 'آغاز سال نو میلادی', false],
  [12, 25, 'میلاد حضرت عیسی مسیح', false],
];

function match(entries: Entry[], month: number, day: number, calendar: Occasion['calendar']): Occasion[] {
  return entries
    .filter(([m, d]) => m === month && d === day)
    .map(([, , title, holiday]) => ({ title, holiday, calendar }));
}

export function occasionsOn(date: Date): Occasion[] {
  const solarDate = fromDate(date);
  const hijri = toHijri(date);
  const result = [
    ...match(solar, solarDate.month, solarDate.day, 'solar'),
    ...match(lunar, hijri.month, hijri.day, 'lunar'),
  ];
  // Imam Reza's martyrdom is marked on the last day of Safar, whether it has 29 or 30 days.
  const tomorrow = new Date(date.getFullYear(), date.getMonth(), date.getDate() + 1, 12);
  if (hijri.month === 2 && toHijri(tomorrow).month === 3) {
    result.push({ title: 'شهادت امام رضا', holiday: true, calendar: 'lunar' });
  }
  result.push(...match(gregorian, date.getMonth() + 1, date.getDate(), 'gregorian'));
  return result;
}

/** Fridays and official holidays. */
export function isHoliday(date: Date): boolean {
  return date.getDay() === 5 || occasionsOn(date).some((o) => o.holiday);
}
