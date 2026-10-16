import { describe, expect, it } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { PrayerTimesTable } from '../PrayerTimes';
const times = { fajr: '05:00', sunrise: '06:30', dhuhr: '12:00', asr: '15:00', sunset: '18:00', maghrib: '18:20', isha: '19:00' };
describe('prayer table accessibility', () => {
  it('identifies the next row with visible text and isolates clock direction', () => {
    const html = renderToStaticMarkup(<PrayerTimesTable times={times} nowMinutes={600} />);
    expect(html).toContain('اذان ظهر<span class="prayer-next-label">بعدی</span>');
    expect(html.match(/prayer-next-label/g)).toHaveLength(1);
    expect(html.match(/<dd dir="ltr">/g)).toHaveLength(6);
  });
  it('does not mark an elapsed row after the final listed time', () => {
    expect(renderToStaticMarkup(<PrayerTimesTable times={times} nowMinutes={1439} />)).not.toContain('prayer-next-label');
  });
});
