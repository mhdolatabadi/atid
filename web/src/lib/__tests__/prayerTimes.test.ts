import { describe, expect, it } from 'vitest';
import { calculate } from '../prayerTimes';

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
});
