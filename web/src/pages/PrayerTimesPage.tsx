import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { APPROXIMATE_NOTE, type PrayerTimes } from '../lib/prayerTimes';
import { toPersianDigits } from '../lib/persianDate';
import { minutesAt, placeLabel, timesFor, usePlace } from '../lib/place';
import { CityPicker } from '../components/PrayerTimes';
import { LocationIcon } from '../components/icons';
import './LandingPage.css';
import './PrayerTimesPage.css';
import { ClientOnly } from '../components/ClientOnly';

const rows: { key: keyof PrayerTimes; label: string }[] = [
  { key: 'fajr', label: 'اذان صبح' },
  { key: 'sunrise', label: 'طلوع آفتاب' },
  { key: 'dhuhr', label: 'اذان ظهر' },
  { key: 'asr', label: 'اذان عصر' },
  { key: 'sunset', label: 'غروب آفتاب' },
  { key: 'maghrib', label: 'اذان مغرب' },
  { key: 'isha', label: 'اذان عشا' },
];

function toMinutesOfDay(hhmm: string): number {
  const [hour, minute] = hhmm.split(':').map(Number);
  return hour * 60 + minute;
}

function computeNextPrayerKey(times: PrayerTimes, nowMinutes: number): keyof PrayerTimes {
  const found = rows.find((row) => toMinutesOfDay(times[row.key]) > nowMinutes);
  return (found ?? rows[0]).key;
}

function PrayerTimesPageView() {
  const { place, selectCity, useMyLocation, locationError, cities } = usePlace();
  const [now, setNow] = useState(() => new Date());

  useEffect(() => {
    const interval = setInterval(() => setNow(new Date()), 60_000);
    return () => clearInterval(interval);
  }, []);

  const times = timesFor(place, now);
  const nextKey = computeNextPrayerKey(times, minutesAt(place, now));

  return (
    <div>
      <div className="prayer-times-page__controls">
        <CityPicker cities={cities} value={place.kind === 'city' ? place.city.slug : ''} onChange={selectCity} />
        <button type="button" className="prayer-times-page__location-button" onClick={useMyLocation}>
          <LocationIcon />
          موقعیت مکانی من
        </button>
      </div>
      <div className="prayer-times-page__location">
        {placeLabel(place)}
        {locationError && ' — دسترسی به موقعیت مکانی داده نشد'}
      </div>

      <div className="prayer-times-list glass">
        {rows.map(({ key, label }) => (
          <div key={key} className={'prayer-times-row' + (key === nextKey ? ' prayer-times-row--next' : '')}>
            <span>{label}</span>
            <span>{toPersianDigits(times[key])}</span>
          </div>
        ))}
      </div>
      <p className="prayer-times-page__note">
        {APPROXIMATE_NOTE} اوقات شهرهای دیگر را در <Link to="/prayer-times">اوقات شرعی شهرهای ایران</Link> ببینید.
      </p>
    </div>
  );
}

export function PrayerTimesPage() {
  return (
    <div className="prayer-times-page">
      <h1 className="prayer-times-page__title">اوقات شرعی امروز</h1>
      <ClientOnly fallback={<div className="glass page-placeholder page-placeholder--prayer" aria-hidden="true" />}>
        <PrayerTimesPageView />
      </ClientOnly>
    </div>
  );
}
