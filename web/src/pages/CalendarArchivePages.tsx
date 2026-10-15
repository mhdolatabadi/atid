import { Link, useParams } from 'react-router-dom';
import { PublicLayout } from '../components/SiteChrome';
import { isHoliday, occasionsOn } from '../data/occasions';
import {
  calendarYears, gregorianDayLabel, hijriDayLabel, monthDays, monthPath, monthSlugs, monthSpan, monthTitle,
  parseMonth, parseYear, shiftMonth, solarDayLabel, yearPath,
} from '../lib/calendarPages';
import { daysInMonth, monthName, toPersianDigits, weekdayName } from '../lib/persianDate';
import { NotFoundPage } from './NotFoundPage';
import './LandingPage.css';
import './CalendarArchivePages.css';

const LUNAR_NOTE =
  'تاریخ‌های قمری و مناسبت‌های قمری محاسبه‌ای‌اند و ممکن است با تقویم رسمی، که بر پایه‌ی رؤیت هلال است، یک روز اختلاف داشته باشند.';

function MonthNav({ year, month }: { year: number; month: number }) {
  const prev = shiftMonth(year, month, -1);
  const next = shiftMonth(year, month, 1);
  return (
    <nav className="ti-archive__nav" aria-label="ماه‌های دیگر">
      {prev ? <Link to={monthPath(prev.year, prev.month)} rel="prev">→ {monthTitle(prev.year, prev.month)}</Link> : <span />}
      <Link to={yearPath(year)}>تقویم {toPersianDigits(year)}</Link>
      {next ? <Link to={monthPath(next.year, next.month)} rel="next">{monthTitle(next.year, next.month)} ←</Link> : <span />}
    </nav>
  );
}

export function CalendarMonthPage() {
  const params = useParams();
  const year = parseYear(params.year);
  const month = parseMonth(params.month);
  if (!year || !month) return <NotFoundPage />;
  const days = monthDays(year, month);
  const span = monthSpan(year, month);
  const holidays = days.filter((date) => occasionsOn(date).some((o) => o.holiday)).length;
  const title = monthTitle(year, month);
  return (
    <PublicLayout>
      <main className="ti-main ti-main--narrow">
        <h1 className="ti-page-title">تقویم {title}</h1>
        <section className="ti-about glass" aria-labelledby="month-summary">
          <h2 id="month-summary" className="ti-card__title">{title} در یک نگاه</h2>
          <p>
            {title} {toPersianDigits(daysInMonth(year, month))} روز دارد؛ از {weekdayName(days[0].getDay())}{' '}
            {gregorianDayLabel(days[0])} تا {weekdayName(days[days.length - 1].getDay())}{' '}
            {gregorianDayLabel(days[days.length - 1])}. این ماه با {span.hijri} قمری و {span.gregorian} میلادی هم‌زمان
            است{holidays > 0 ? ` و ${toPersianDigits(holidays)} روز تعطیل رسمی (غیر از جمعه‌ها) دارد` : ' و غیر از جمعه‌ها تعطیل رسمی ندارد'}.
          </p>
          <MonthNav year={year} month={month} />
        </section>

        <section className="ti-card glass ti-archive" aria-labelledby="month-days">
          <h2 id="month-days" className="ti-card__title">روزهای {title}</h2>
          <ol className="ti-archive__days">
            {days.map((date) => {
              const occasions = occasionsOn(date);
              const holiday = isHoliday(date);
              return (
                <li key={date.getDate()} className={'ti-archive__day' + (holiday ? ' ti-archive__day--holiday' : '')}>
                  <div className="ti-archive__date">
                    <span className="ti-archive__solar">
                      {weekdayName(date.getDay())} {solarDayLabel(date)}
                      {holiday && <span className="ti-occasion__tag">تعطیل</span>}
                    </span>
                    <span className="ti-archive__sub">
                      {hijriDayLabel(date)} · {gregorianDayLabel(date)}
                    </span>
                  </div>
                  {occasions.length > 0 && (
                    <ul className="ti-archive__occasions">
                      {occasions.map((o, i) => (
                        <li key={i} className={o.holiday ? 'ti-occasion--holiday' : undefined}>{o.title}</li>
                      ))}
                    </ul>
                  )}
                </li>
              );
            })}
          </ol>
          <p className="ti-note">{LUNAR_NOTE}</p>
        </section>

        <section className="ti-about glass" aria-labelledby="month-more">
          <h2 id="month-more" className="ti-card__title">ابزارهای دیگر</h2>
          <ul className="ti-about__links">
            <li><Link to="/">تقویم و اوقات شرعی امروز</Link></li>
            <li><Link to="/prayer-times">اوقات شرعی شهرهای ایران</Link></li>
            <li><Link to="/date-converter">تبدیل تاریخ</Link></li>
          </ul>
        </section>
      </main>
    </PublicLayout>
  );
}

export function CalendarYearPage() {
  const year = parseYear(useParams().year);
  if (!year) return <NotFoundPage />;
  const nowruz = monthDays(year, 1)[0];
  const holidays = monthSlugs.flatMap((_, i) =>
    monthDays(year, i + 1).flatMap((date) =>
      occasionsOn(date).filter((o) => o.holiday).map((o) => ({ date, title: o.title }))));
  return (
    <PublicLayout>
      <main className="ti-main ti-main--narrow">
        <h1 className="ti-page-title">تقویم {toPersianDigits(year)}</h1>
        <section className="ti-about glass" aria-labelledby="year-summary">
          <h2 id="year-summary" className="ti-card__title">سال {toPersianDigits(year)} هجری شمسی</h2>
          <p>
            سال {toPersianDigits(year)} با نوروز در روز {weekdayName(nowruz.getDay())} {gregorianDayLabel(nowruz)} آغاز
            می‌شود و {toPersianDigits(daysInMonth(year, 12) === 30 ? 366 : 365)} روز دارد
            {daysInMonth(year, 12) === 30 ? '؛ سال کبیسه است و اسفند ۳۰ روز دارد' : ''}. برای دیدن روزبه‌روز هر ماه با
            تاریخ قمری، میلادی و مناسبت‌ها، ماه را انتخاب کنید.
          </p>
          <ul className="ti-archive__months">
            {monthSlugs.map((_, i) => {
              const span = monthSpan(year, i + 1);
              return (
                <li key={i}>
                  <Link to={monthPath(year, i + 1)}>
                    <span className="ti-archive__month-name">{monthName(i + 1)}</span>
                    <span className="ti-archive__sub">{span.gregorian}</span>
                  </Link>
                </li>
              );
            })}
          </ul>
        </section>

        <section className="ti-card glass ti-archive" aria-labelledby="year-holidays">
          <h2 id="year-holidays" className="ti-card__title">تعطیلات رسمی {toPersianDigits(year)}</h2>
          <ul className="ti-occasions__list">
            {holidays.map(({ date, title }, i) => (
              <li key={i} className="ti-occasion ti-occasion--holiday">
                <span className="ti-occasion__day ti-archive__holiday-day">{weekdayName(date.getDay())} {solarDayLabel(date)}</span>
                <span className="ti-occasion__title">{title}</span>
              </li>
            ))}
          </ul>
          <p className="ti-note">{LUNAR_NOTE}</p>
        </section>

        <nav className="ti-about glass" aria-label="سال‌های دیگر">
          <ul className="ti-about__links">
            {calendarYears.map((y) => (
              <li key={y}>
                <Link to={yearPath(y)} aria-current={y === year ? 'page' : undefined}>تقویم {toPersianDigits(y)}</Link>
              </li>
            ))}
          </ul>
        </nav>
      </main>
    </PublicLayout>
  );
}

/** Links to every published year and to this build year's months, for the /calendar guide. */
export function CalendarArchiveLinks({ year }: { year: number }) {
  return (
    <section className="ti-about glass" aria-labelledby="archive-title">
      <h2 id="archive-title" className="ti-card__title">تقویم ماه‌به‌ماه {toPersianDigits(year)}</h2>
      <ul className="ti-about__links">
        {monthSlugs.map((_, i) => (
          <li key={i}><Link to={monthPath(year, i + 1)}>{monthTitle(year, i + 1)}</Link></li>
        ))}
      </ul>
      <ul className="ti-about__links">
        {calendarYears.map((y) => (
          <li key={y}><Link to={yearPath(y)}>تقویم کامل {toPersianDigits(y)}</Link></li>
        ))}
      </ul>
    </section>
  );
}

