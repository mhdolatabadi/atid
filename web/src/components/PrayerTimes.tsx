import { formatDuration, midnight, type PrayerTimes } from '../lib/prayerTimes';
import { toPersianDigits } from '../lib/persianDate';
import type { City } from '../data/cities';

type RowKey = keyof PrayerTimes | 'midnight';

const rows: { key: RowKey; label: string }[] = [
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

/** Today's times with the next one highlighted; [nowMinutes] is the minute of the day at the place. */
export function PrayerTimesTable({ times, nowMinutes }: { times: PrayerTimes; nowMinutes: number }) {
  const values: Record<RowKey, string> = { ...times, midnight: midnight(times) };
  const nextRow = rows.find((row) => row.key !== 'midnight' && toMinutes(values[row.key]) > nowMinutes);
  const next = nextRow?.key;
  return (
    <>
    <p className="ti-prayer__countdown" aria-live="polite">
      {nextRow
        ? `${formatDuration(toMinutes(values[nextRow.key]) - nowMinutes)} تا ${nextRow.label}`
        : // After the last prayer, count down to tomorrow's dawn (today's time is within a minute or two).
          `${formatDuration(toMinutes(values.fajr) + 1440 - nowMinutes)} تا اذان صبح فردا`}
    </p>
    <dl className="ti-prayer__list">
      {rows.map(({ key, label }) => (
        <div key={key} className={'ti-prayer__row' + (key === next ? ' is-next' : '')}>
          <dt>{label}</dt>
          <dd>{toPersianDigits(values[key])}</dd>
        </div>
      ))}
    </dl>
    </>
  );
}

export function CityPicker({ cities, value, onChange }: { cities: City[]; value: string; onChange: (slug: string) => void }) {
  return (
    <label className="ti-city-picker">
      <span>شهر</span>
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        {value === '' && <option value="">موقعیت فعلی شما</option>}
        {cities.map((city) => (
          <option key={city.slug} value={city.slug}>
            {city.name}
          </option>
        ))}
      </select>
    </label>
  );
}
