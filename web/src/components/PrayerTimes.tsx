import { midnight, type PrayerTimes } from '../lib/prayerTimes';
import { nextRowAfter, prayerCountdown, rows, type RowKey } from './prayerRows';
import { toPersianDigits } from '../lib/persianDate';
import type { City } from '../data/cities';

/** Today's times with the next one highlighted; [nowMinutes] is the minute of the day at the place. */
export function PrayerTimesTable({ times, nowMinutes }: { times: PrayerTimes; nowMinutes: number }) {
  const values: Record<RowKey, string> = { ...times, midnight: midnight(times) };
  const next = nextRowAfter(values, nowMinutes)?.key;
  return (
    <>
    <p className="ti-prayer__countdown" aria-live="polite">
      {prayerCountdown(times, nowMinutes)}
    </p>
    <dl className="ti-prayer__list">
      {rows.map(({ key, label }) => (
        <div key={key} className={'ti-prayer__row' + (key === next ? ' is-next' : '')}>
          <dt>{label}{key === next && <span className="prayer-next-label">بعدی</span>}</dt>
          <dd dir="ltr">{toPersianDigits(values[key])}</dd>
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
