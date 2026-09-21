import { useEffect, useMemo, useState } from 'react';
import { calculateForToday, type PrayerTimes } from '../lib/prayerTimes';
import { toPersianDigits } from '../lib/persianDate';
import { requestDeviceLocation, TEHRAN, type Coordinates } from '../lib/location';
import { LocationIcon } from '../components/icons';
import './PrayerTimesPage.css';

const rows: { key: keyof PrayerTimes; label: string }[] = [
  { key: 'fajr', label: 'اذان صبح' },
  { key: 'sunrise', label: 'طلوع آفتاب' },
  { key: 'dhuhr', label: 'اذان ظهر' },
  { key: 'asr', label: 'اذان عصر' },
  { key: 'sunset', label: 'غروب آفتاب' },
  { key: 'maghrib', label: 'اذان مغرب' },
  { key: 'isha', label: 'اذان عشا' },
];

function toPersianTime(hhmm: string): string {
  return hhmm
    .split(':')
    .map((part) => toPersianDigits(Number(part)))
    .join(':');
}

function toMinutesOfDay(hhmm: string): number {
  const [hour, minute] = hhmm.split(':').map(Number);
  return hour * 60 + minute;
}

function computeNextPrayerKey(times: PrayerTimes): keyof PrayerTimes {
  const now = new Date();
  const nowMinutes = now.getHours() * 60 + now.getMinutes();
  const found = rows.find((row) => toMinutesOfDay(times[row.key]) > nowMinutes);
  return (found ?? rows[0]).key;
}

export function PrayerTimesPage() {
  const [coordinates, setCoordinates] = useState<Coordinates>(TEHRAN);
  const [locationError, setLocationError] = useState(false);

  const times = useMemo(
    () => calculateForToday(coordinates.latitude, coordinates.longitude),
    [coordinates],
  );

  const [nextKey, setNextKey] = useState<keyof PrayerTimes>(() => computeNextPrayerKey(times));

  useEffect(() => {
    setNextKey(computeNextPrayerKey(times));
    const interval = setInterval(() => setNextKey(computeNextPrayerKey(times)), 60_000);
    return () => clearInterval(interval);
  }, [times]);

  const useMyLocation = () => {
    setLocationError(false);
    requestDeviceLocation()
      .then(setCoordinates)
      .catch(() => setLocationError(true));
  };

  const locationLabel = coordinates.isDeviceLocation
    ? 'بر اساس موقعیت مکانی شما'
    : 'تهران (پیش‌فرض؛ دسترسی به موقعیت مکانی فعال نیست)';

  return (
    <div className="prayer-times-page">
      <h1 className="prayer-times-page__title">اوقات شرعی امروز</h1>
      <div className="prayer-times-page__location">
        {locationLabel}
        {locationError && ' — دسترسی به موقعیت مکانی رد شد'}
      </div>

      <button type="button" className="prayer-times-page__location-button" onClick={useMyLocation}>
        <LocationIcon />
        استفاده از موقعیت مکانی من
      </button>

      <div className="prayer-times-list">
        {rows.map(({ key, label }) => (
          <div key={key} className={'prayer-times-row' + (key === nextKey ? ' prayer-times-row--next' : '')}>
            <span>{label}</span>
            <span>{toPersianTime(times[key])}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
