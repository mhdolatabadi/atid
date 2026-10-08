import { describe, expect, it } from 'vitest';
import { calculate, calculateForTodayInIran, iranClock, midnight } from '../prayerTimes';

const TEHRAN = { latitude: 35.6892, longitude: 51.389, utcOffset: 3.5 };

// Fixed outputs for Tehran. The Android port asserts the very same values
// (PrayerTimesCalculatorTest), so the two implementations cannot drift apart.
const references = [
  [[2026, 9, 30], { fajr: '04:35', sunrise: '05:59', dhuhr: '11:54', asr: '15:17', sunset: '17:50', maghrib: '18:08', isha: '18:55' }],
  [[2026, 3, 21], { fajr: '04:43', sunrise: '06:06', dhuhr: '12:12', asr: '15:39', sunset: '18:17', maghrib: '18:35', isha: '19:22' }],
  [[2026, 6, 21], { fajr: '03:02', sunrise: '04:49', dhuhr: '12:06', asr: '15:55', sunset: '19:24', maghrib: '19:45', isha: '20:44' }],
  [[2026, 12, 21], { fajr: '05:40', sunrise: '07:10', dhuhr: '12:03', asr: '14:37', sunset: '16:55', maghrib: '17:15', isha: '18:06' }],
] as const;

const minutes = (hhmm: string) => Number(hhmm.slice(0, 2)) * 60 + Number(hhmm.slice(3));

describe('prayer times', () => {
  it.each(references)('Tehran %j', ([y, m, d], expected) => {
    expect(calculate(y, m, d, TEHRAN.latitude, TEHRAN.longitude, TEHRAN.utcOffset)).toEqual(expected);
  });

  it('keeps the times in order through the year', () => {
    for (let month = 1; month <= 12; month++) {
      const t = calculate(2026, month, 15, TEHRAN.latitude, TEHRAN.longitude, TEHRAN.utcOffset);
      const order = [t.fajr, t.sunrise, t.dhuhr, t.asr, t.sunset, t.maghrib, t.isha].map(minutes);
      expect(order).toEqual([...order].sort((a, b) => a - b));
    }
  });

  it('computes city times on Iran time, whatever the browser time zone', () => {
    // 30 Sep 2026 at 21:00 UTC is already 1 Oct 00:30 in Tehran; 20:00 UTC is still 30 Sep 23:30.
    expect(iranClock(new Date(Date.UTC(2026, 8, 30, 21, 0)))).toEqual({ year: 2026, month: 10, day: 1, minutes: 30 });
    expect(iranClock(new Date(Date.UTC(2026, 8, 30, 20, 0)))).toMatchObject({ day: 30, minutes: 23 * 60 + 30 });
    expect(calculateForTodayInIran(TEHRAN.latitude, TEHRAN.longitude, new Date(Date.UTC(2026, 8, 30, 8, 0)))).toEqual(references[0][1]);
  });

  it('puts shar\'i midnight halfway between sunset and the next dawn', () => {
    expect(midnight(references[0][1])).toBe('23:13'); // 17:50 → 04:35: midpoint 23:12:30, rounded
  });
});
