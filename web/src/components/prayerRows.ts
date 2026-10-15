import { formatDuration, midnight, type PrayerTimes } from '../lib/prayerTimes';

export type RowKey = keyof PrayerTimes | 'midnight';

export const rows: { key: RowKey; label: string }[] = [
  { key: 'fajr', label: 'اذان صبح' },
  { key: 'sunrise', label: 'طلوع آفتاب' },
  { key: 'dhuhr', label: 'اذان ظهر' },
  { key: 'sunset', label: 'غروب آفتاب' },
  { key: 'maghrib', label: 'اذان مغرب' },
  { key: 'midnight', label: 'نیمه‌شب شرعی' },
];

function toMinutes(hhmm: string): number {
  const [h, m] = hhmm.split(':').map(Number);
  return h * 60 + m;
}

export function nextRowAfter(values: Record<RowKey, string>, nowMinutes: number) {
  return rows.find((row) => row.key !== 'midnight' && toMinutes(values[row.key]) > nowMinutes);
}

/** Time left to the next prayer after [nowMinutes] (minute of the day at the place). */
export function prayerCountdown(times: PrayerTimes, nowMinutes: number): string {
  const values: Record<RowKey, string> = { ...times, midnight: midnight(times) };
  const nextRow = nextRowAfter(values, nowMinutes);
  // After the last prayer, count down to tomorrow's dawn (today's time is within a minute or two).
  if (!nextRow) return `${formatDuration(toMinutes(times.fajr) + 1440 - nowMinutes)} تا اذان صبح فردا`;
  return `${formatDuration(toMinutes(values[nextRow.key]) - nowMinutes)} تا ${nextRow.label}`;
}
