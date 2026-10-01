import { calculateForToday } from './prayerTimes';
import { TEHRAN } from './location';

export type Sky = 'dawn' | 'day' | 'dusk' | 'night';

function minutes(hhmm: string): number {
  const [h, m] = hhmm.split(':').map(Number);
  return h * 60 + m;
}

/** Which part of the day it is, from today's prayer times (Tehran horizon; no location prompt). */
export function skyAt(now: Date): Sky {
  const t = calculateForToday(TEHRAN.latitude, TEHRAN.longitude);
  const m = now.getHours() * 60 + now.getMinutes();
  if (m >= minutes(t.fajr) && m < minutes(t.sunrise) + 20) return 'dawn';
  if (m >= minutes(t.sunrise) + 20 && m < minutes(t.sunset) - 40) return 'day';
  if (m >= minutes(t.sunset) - 40 && m < minutes(t.maghrib) + 30) return 'dusk';
  return 'night';
}
