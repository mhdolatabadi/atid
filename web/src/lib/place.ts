import { useState } from 'react';
import { cities, cityBySlug, DEFAULT_CITY, type City } from '../data/cities';
import { calculateForToday, calculateForTodayInIran, iranClock, type PrayerTimes } from './prayerTimes';
import { requestDeviceLocation } from './location';

/** Where prayer times are computed for: a city (on Iran time) or the device's position (on its own clock). */
export type Place = { kind: 'city'; city: City } | { kind: 'device'; latitude: number; longitude: number };

const STORAGE_KEY = 'saatbashi.city';

export function timesFor(place: Place, now: Date = new Date()): PrayerTimes {
  return place.kind === 'city'
    ? calculateForTodayInIran(place.city.latitude, place.city.longitude, now)
    : calculateForToday(place.latitude, place.longitude);
}

/** Minute of the day at the place, to pick the next prayer. */
export function minutesAt(place: Place, now: Date = new Date()): number {
  return place.kind === 'city' ? iranClock(now).minutes : now.getHours() * 60 + now.getMinutes();
}

export function placeLabel(place: Place): string {
  return place.kind === 'city' ? `به افق ${place.city.name}` : 'موقعیت فعلی شما';
}

function savedCity(): City {
  try {
    return cityBySlug(localStorage.getItem(STORAGE_KEY) ?? undefined) ?? DEFAULT_CITY;
  } catch {
    return DEFAULT_CITY;
  }
}

/** The chosen city (remembered on this device only) or, after the user asks, the device location. */
export function usePlace() {
  const [place, setPlace] = useState<Place>(() => ({ kind: 'city', city: savedCity() }));
  const [locationError, setLocationError] = useState(false);

  const selectCity = (slug: string) => {
    const city = cityBySlug(slug);
    if (!city) return;
    setLocationError(false);
    setPlace({ kind: 'city', city });
    try {
      localStorage.setItem(STORAGE_KEY, city.slug);
    } catch {
      // Storage can be unavailable (private mode); the choice then lasts for this page only.
    }
  };

  const useMyLocation = () => {
    setLocationError(false);
    requestDeviceLocation()
      .then(({ latitude, longitude }) => setPlace({ kind: 'device', latitude, longitude }))
      .catch(() => setLocationError(true));
  };

  return { place, selectCity, useMyLocation, locationError, cities };
}
