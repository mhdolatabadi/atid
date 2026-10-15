import { Link, useParams } from 'react-router-dom';
import { PublicLayout } from '../components/SiteChrome';
import { cityBySlug, type City } from '../data/cities';
import {
  BUILD_YEAR, cityTimesOn, monthDays, monthPath, monthSlugs, monthTitle, parseMonth, prayerMonthPath, prayerYears, shiftMonth,
} from '../lib/calendarPages';
import { fromDate, toPersianDigits, weekdayName } from '../lib/persianDate';
import { APPROXIMATE_NOTE } from '../lib/prayerTimes';
import { NotFoundPage } from './NotFoundPage';
import './LandingPage.css';
import './CalendarArchivePages.css';

const columns = [
  { key: 'fajr', short: 'صبح', label: 'اذان صبح' },
  { key: 'sunrise', short: 'طلوع', label: 'طلوع آفتاب' },
  { key: 'dhuhr', short: 'ظهر', label: 'اذان ظهر' },
  { key: 'sunset', short: 'غروب', label: 'غروب آفتاب' },
  { key: 'maghrib', short: 'مغرب', label: 'اذان مغرب' },
  { key: 'midnight', short: 'نیمه‌شب', label: 'نیمه‌شب شرعی' },
] as const;

function parsePrayerYear(value: string | undefined): number | undefined {
  const year = Number(value);
  return /^\d{4}$/.test(value ?? '') && prayerYears.includes(year) ? year : undefined;
}

export function PrayerMonthPage() {
  const params = useParams();
  const city = cityBySlug(params.city);
  const year = parsePrayerYear(params.year);
  const month = parseMonth(params.month);
  if (!city || !year || !month) return <NotFoundPage />;
  const title = monthTitle(year, month);
  const prev = shiftMonth(year, month, -1, prayerYears);
  const next = shiftMonth(year, month, 1, prayerYears);
  return (
    <PublicLayout>
      <main className="ti-main ti-main--narrow">
        <h1 className="ti-page-title">اوقات شرعی {city.name} در {title}</h1>
        <section className="ti-card glass ti-archive" aria-labelledby="table-title">
          <h2 id="table-title" className="ti-card__title">جدول اذان {city.name}، {title}</h2>
          <div className="ti-prayer-month">
            <table>
              <thead>
                <tr>
                  <th scope="col">روز</th>
                  {columns.map((c) => <th key={c.key} scope="col"><abbr title={c.label}>{c.short}</abbr></th>)}
                </tr>
              </thead>
              <tbody>
                {monthDays(year, month).map((date) => {
                  const times = cityTimesOn(city, date);
                  return (
                    <tr key={date.getDate()} className={date.getDay() === 5 ? 'is-friday' : undefined}>
                      <th scope="row">
                        <span className="ti-prayer-month__weekday">{weekdayName(date.getDay())}</span>{' '}
                        {toPersianDigits(fromDate(date).day)}
                      </th>
                      {columns.map((c) => <td key={c.key}>{toPersianDigits(times[c.key])}</td>)}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
          <p className="ti-note">{APPROXIMATE_NOTE} ساعت‌ها به وقت رسمی ایران‌اند.</p>
          <nav className="ti-archive__nav" aria-label="ماه‌های دیگر">
            {prev ? <Link to={prayerMonthPath(city.slug, prev.year, prev.month)} rel="prev">→ {monthTitle(prev.year, prev.month)}</Link> : <span />}
            <Link to={`/prayer-times/${city.slug}`}>اوقات شرعی امروز {city.name}</Link>
            {next ? <Link to={prayerMonthPath(city.slug, next.year, next.month)} rel="next">{monthTitle(next.year, next.month)} ←</Link> : <span />}
          </nav>
        </section>
        <section className="ti-about glass" aria-labelledby="month-more">
          <h2 id="month-more" className="ti-card__title">بیشتر</h2>
          <ul className="ti-about__links">
            <li><Link to={monthPath(year, month)}>تقویم {title} و مناسبت‌ها</Link></li>
            <li><Link to="/prayer-times">اوقات شرعی شهرهای دیگر</Link></li>
          </ul>
        </section>
      </main>
    </PublicLayout>
  );
}

/** Links to this build year's monthly tables, for a city page. */
export function PrayerMonthLinks({ city }: { city: City }) {
  return (
    <section className="ti-about glass" aria-labelledby="month-tables">
      <h2 id="month-tables" className="ti-card__title">جدول ماهانه‌ی اوقات شرعی {city.name} {toPersianDigits(BUILD_YEAR)}</h2>
      <ul className="ti-about__links">
        {monthSlugs.map((_, i) => (
          <li key={i}><Link to={prayerMonthPath(city.slug, BUILD_YEAR, i + 1)}>{monthTitle(BUILD_YEAR, i + 1)}</Link></li>
        ))}
      </ul>
    </section>
  );
}
