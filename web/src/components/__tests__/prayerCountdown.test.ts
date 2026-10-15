import { describe, expect, it } from 'vitest';
import { prayerCountdown } from '../prayerRows';
import type { PrayerTimes } from '../../lib/prayerTimes';

const times: PrayerTimes = {
  fajr: '04:30',
  sunrise: '06:00',
  dhuhr: '12:00',
  asr: '15:30',
  sunset: '18:00',
  maghrib: '18:20',
  isha: '19:30',
};

describe('prayer countdown', () => {
  it('counts down to the next prayer of the day', () => {
    expect(prayerCountdown(times, 11 * 60)).toBe('۱ ساعت تا اذان ظهر');
    expect(prayerCountdown(times, 0)).toBe('۴ ساعت و ۳۰ دقیقه تا اذان صبح');
  });
  it("counts down to tomorrow's dawn after the last prayer", () => {
    expect(prayerCountdown(times, 23 * 60)).toBe('۵ ساعت و ۳۰ دقیقه تا اذان صبح فردا');
    expect(prayerCountdown(times, 18 * 60 + 20)).toBe('۱۰ ساعت و ۱۰ دقیقه تا اذان صبح فردا');
  });
});
