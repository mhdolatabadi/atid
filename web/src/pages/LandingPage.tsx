import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  daysInMonth,
  firstDayOfWeek,
  fromDate,
  gregorianMonthNames,
  monthName,
  toArabicDigits,
  toGregorianDate,
  toPersianDigits,
  weekdayIndexSaturdayFirst,
} from '../lib/persianDate';
import { hijriMonthName, toHijri } from '../lib/hijriDate';
import { calculateForToday, type PrayerTimes } from '../lib/prayerTimes';
import { requestDeviceLocation, TEHRAN, type Coordinates } from '../lib/location';
import { isHoliday, occasionsOn, type Occasion } from '../data/occasions';
import { ChevronIcon, LocationIcon } from '../components/icons';
import { ClientOnly } from '../components/ClientOnly';
import { PublicLayout } from '../components/SiteChrome';
import { DateConverter } from '../components/DateConverter';
import { gregorianLabel, hijriLabel, solarLabel } from '../lib/dateLabels';
import './LandingPage.css';

const weekdayHeaders = ['شنبه', 'یکشنبه', 'دوشنبه', 'سه‌شنبه', 'چهارشنبه', 'پنجشنبه', 'جمعه'];

function sameDay(a: Date, b: Date): boolean {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

function useNow(): Date {
  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(id);
  }, []);
  return now;
}

// --- Today --------------------------------------------------------------------------------------

/** Each digit remounts when it changes, so only the digits that tick roll into place. */
function Rolling({ value, className }: { value: number; className?: string }) {
  const digits = toPersianDigits(String(value).padStart(2, '0'));
  return (
    <span className={'ti-roll' + (className ? ` ${className}` : '')}>
      {[...digits].map((digit, index) => (
        <span key={`${index}-${digit}`} className="ti-roll__digit">
          {digit}
        </span>
      ))}
    </span>
  );
}

function TodayPanel({ now }: { now: Date }) {
  return (
    <section className="ti-today glass enter" aria-label="امروز">
      <div className="ti-today__clock" role="timer" aria-label={now.toLocaleTimeString('fa-IR')}>
        <Rolling value={now.getHours()} />
        <span className="ti-today__colon">:</span>
        <Rolling value={now.getMinutes()} />
        <span className="ti-today__seconds">
          <span className="ti-today__colon">:</span>
          <Rolling value={now.getSeconds()} />
        </span>
      </div>
      <div className="ti-today__dates">
        <div className="ti-today__date ti-today__date--solar">
          <span className="ti-today__label">هجری شمسی</span>
          <span className="ti-today__value">{solarLabel(now)}</span>
        </div>
        <div className="ti-today__date">
          <span className="ti-today__label">هجری قمری</span>
          <span className="ti-today__value">{hijriLabel(now)}</span>
        </div>
        <div className="ti-today__date">
          <span className="ti-today__label">میلادی</span>
          <span className="ti-today__value" dir="ltr">{gregorianLabel(now)}</span>
        </div>
      </div>
    </section>
  );
}

// --- Calendar -----------------------------------------------------------------------------------

interface Cell {
  date: Date;
  solarDay: number;
  inMonth: boolean;
}

function buildCells(year: number, month: number): Cell[] {
  const leading = weekdayIndexSaturdayFirst(firstDayOfWeek(year, month));
  const count = daysInMonth(year, month);
  const first = toGregorianDate(year, month, 1);
  const total = Math.ceil((leading + count) / 7) * 7;
  const cells: Cell[] = [];
  for (let i = 0; i < total; i++) {
    const date = new Date(first.getFullYear(), first.getMonth(), first.getDate() - leading + i, 12);
    cells.push({ date, solarDay: fromDate(date).day, inMonth: i >= leading && i < leading + count });
  }
  return cells;
}

function rangeLabel(first: Date, last: Date): { hijri: string; gregorian: string } {
  const h1 = toHijri(first);
  const h2 = toHijri(last);
  const hijri =
    h1.month === h2.month
      ? `${hijriMonthName(h1.month)} ${toPersianDigits(h1.year)}`
      : `${hijriMonthName(h1.month)}${h1.year !== h2.year ? ` ${toPersianDigits(h1.year)}` : ''} – ${hijriMonthName(h2.month)} ${toPersianDigits(h2.year)}`;
  const g1 = gregorianMonthNames[first.getMonth()];
  const g2 = gregorianMonthNames[last.getMonth()];
  const gregorian =
    first.getMonth() === last.getMonth()
      ? `${g1} ${toPersianDigits(first.getFullYear())}`
      : `${g1}${first.getFullYear() !== last.getFullYear() ? ` ${toPersianDigits(first.getFullYear())}` : ''} – ${g2} ${toPersianDigits(last.getFullYear())}`;
  return { hijri, gregorian };
}

function OccasionList({ items }: { items: { date?: Date; occasion: Occasion }[] }) {
  if (items.length === 0) return <p className="ti-occasions__empty">مناسبتی ثبت نشده است.</p>;
  return (
    <ul className="ti-occasions__list">
      {items.map(({ date, occasion }, index) => (
        <li key={index} className={'ti-occasion' + (occasion.holiday ? ' ti-occasion--holiday' : '')}>
          {date && (
            <span className="ti-occasion__day">
              {toPersianDigits(fromDate(date).day)} {monthName(fromDate(date).month)}
            </span>
          )}
          <span className="ti-occasion__title">
            {occasion.title}
            {occasion.holiday && <span className="ti-occasion__tag">تعطیل</span>}
          </span>
        </li>
      ))}
    </ul>
  );
}

function CalendarSection({ now }: { now: Date }) {
  const current = fromDate(now);
  const [view, setView] = useState({ year: current.year, month: current.month, direction: 0 });
  const [selected, setSelected] = useState<Date>(now);

  const cells = useMemo(() => buildCells(view.year, view.month), [view.year, view.month]);
  const monthDays = cells.filter((c) => c.inMonth);
  const range = rangeLabel(monthDays[0].date, monthDays[monthDays.length - 1].date);

  const shift = (delta: number) => {
    setView(({ year, month }) => {
      const index = year * 12 + (month - 1) + delta;
      return { year: Math.floor(index / 12), month: (index % 12) + 1, direction: delta };
    });
  };

  const goToday = () => {
    const delta = current.year * 12 + current.month - (view.year * 12 + view.month);
    setView({ year: current.year, month: current.month, direction: Math.sign(delta) });
    setSelected(now);
  };

  const monthOccasions = monthDays.flatMap(({ date }) =>
    occasionsOn(date).map((occasion) => ({ date, occasion })),
  );

  return (
    <>
      <section className="ti-card ti-calendar glass enter" style={{ animationDelay: '80ms' }} aria-label="تقویم">
        <header className="ti-calendar__header">
          <button type="button" className="ti-icon-button" onClick={() => shift(-1)} aria-label="ماه قبل">
            <ChevronIcon />
          </button>
          <div className="ti-calendar__titles">
            <h2 key={`${view.year}-${view.month}`} className="ti-calendar__month">
              {monthName(view.month)} {toPersianDigits(view.year)}
            </h2>
            <div className="ti-calendar__range">
              <span>{range.hijri}</span>
              <span className="ti-calendar__dot" aria-hidden="true" />
              <span>{range.gregorian}</span>
            </div>
          </div>
          <button type="button" className="ti-icon-button ti-icon-button--flip" onClick={() => shift(1)} aria-label="ماه بعد">
            <ChevronIcon />
          </button>
        </header>

        <div className="ti-calendar__weekdays" aria-hidden="true">
          {weekdayHeaders.map((label, index) => (
            <div key={label} className={'ti-calendar__weekday' + (index === 6 ? ' is-holiday' : '')}>
              <span className="ti-calendar__weekday-full">{label}</span>
              <span className="ti-calendar__weekday-short">{label[0]}</span>
            </div>
          ))}
        </div>
        <div
          key={`${view.year}-${view.month}`}
          className={
            'ti-calendar__grid' +
            (view.direction > 0 ? ' is-from-next' : view.direction < 0 ? ' is-from-prev' : '')
          }
          role="group"
          aria-label={`${monthName(view.month)} ${toPersianDigits(view.year)}`}
        >
          {cells.map((cell) => {
            const holiday = isHoliday(cell.date);
            const classes = [
              'ti-day',
              cell.inMonth ? '' : 'is-outside',
              holiday ? 'is-holiday' : '',
              sameDay(cell.date, now) ? 'is-today' : '',
              sameDay(cell.date, selected) ? 'is-selected' : '',
            ].filter(Boolean).join(' ');
            return (
              <button
                key={cell.date.toDateString()}
                type="button"
                className={classes}
                aria-label={solarLabel(cell.date)}
                aria-pressed={sameDay(cell.date, selected)}
                onClick={() => {
                  setSelected(cell.date);
                  if (!cell.inMonth) {
                    const p = fromDate(cell.date);
                    const delta = p.year * 12 + p.month - (view.year * 12 + view.month);
                    setView({ year: p.year, month: p.month, direction: Math.sign(delta) });
                  }
                }}
              >
                <span className="ti-day__solar">{toPersianDigits(cell.solarDay)}</span>
                <span className="ti-day__sub">
                  <span className="ti-day__hijri">{toArabicDigits(toHijri(cell.date).day)}</span>
                  <span className="ti-day__gregorian">{cell.date.getDate()}</span>
                </span>
              </button>
            );
          })}
        </div>

        <div className="ti-calendar__footer">
          <button type="button" className="ti-button" onClick={goToday}>
            برو به امروز
          </button>
          <span className="ti-calendar__legend">
            <span className="ti-legend-dot" aria-hidden="true" /> تعطیل رسمی
          </span>
        </div>

        <div key={selected.toDateString()} className="ti-selected">
          <div className="ti-selected__dates">
            <strong>{solarLabel(selected)}</strong>
            <span>{hijriLabel(selected)}</span>
            <span dir="ltr">{gregorianLabel(selected)}</span>
          </div>
          <OccasionList items={occasionsOn(selected).map((occasion) => ({ occasion }))} />
        </div>
      </section>

      <section className="ti-card ti-occasions glass enter" style={{ animationDelay: '160ms' }} aria-label="مناسبت‌های ماه">
        <h2 className="ti-card__title">مناسبت‌های {monthName(view.month)}</h2>
        <OccasionList items={monthOccasions} />
        <p className="ti-note">
          تاریخ‌های قمری محاسبه‌ای‌اند و ممکن است با تقویم رسمی کشور یک روز اختلاف داشته باشند.
        </p>
      </section>
    </>
  );
}

// --- Prayer times -------------------------------------------------------------------------------

const prayerRows: { key: keyof PrayerTimes | 'midnight'; label: string }[] = [
  { key: 'fajr', label: 'اذان صبح' },
  { key: 'sunrise', label: 'طلوع آفتاب' },
  { key: 'dhuhr', label: 'اذان ظهر' },
  { key: 'sunset', label: 'غروب آفتاب' },
  { key: 'maghrib', label: 'اذان مغرب' },
  { key: 'midnight', label: 'نیمه‌شب شرعی' },
];

function toMinutes(hhmm: string): number {
  const [h, m] = hhmm.split(':').map(Number);
  return h * 60 + m;
}

function fromMinutes(total: number): string {
  const t = ((Math.round(total) % 1440) + 1440) % 1440;
  return `${String(Math.floor(t / 60)).padStart(2, '0')}:${String(t % 60).padStart(2, '0')}`;
}

/** Shar'i midnight (Jafari): halfway between sunset and the next dawn. */
function midnight(times: PrayerTimes): string {
  const sunset = toMinutes(times.sunset);
  const nextFajr = toMinutes(times.fajr) + 1440;
  return fromMinutes(sunset + (nextFajr - sunset) / 2);
}

function PrayerTimesCard({ now }: { now: Date }) {
  const [coordinates, setCoordinates] = useState<Coordinates>(TEHRAN);
  const [locationError, setLocationError] = useState(false);
  // A handful of trig calls; cheap enough to redo on every tick and it rolls over at midnight.
  const times = calculateForToday(coordinates.latitude, coordinates.longitude);
  const values = { ...times, midnight: midnight(times) };
  const nowMinutes = now.getHours() * 60 + now.getMinutes();
  const next = prayerRows.find((row) => row.key !== 'midnight' && toMinutes(values[row.key]) > nowMinutes)?.key;

  const useMyLocation = () => {
    setLocationError(false);
    requestDeviceLocation().then(setCoordinates).catch(() => setLocationError(true));
  };

  return (
    <section className="ti-card ti-prayer glass enter" style={{ animationDelay: '120ms' }} aria-label="اوقات شرعی">
      <h2 className="ti-card__title">اوقات شرعی</h2>
      <div className="ti-prayer__place">
        {coordinates.isDeviceLocation ? 'موقعیت فعلی شما' : 'به افق تهران'}
        <button type="button" className="ti-link-button" onClick={useMyLocation}>
          <LocationIcon /> موقعیت من
        </button>
      </div>
      {locationError && <p className="ti-note ti-note--error">دسترسی به موقعیت مکانی داده نشد؛ اوقات به افق تهران است.</p>}
      <dl className="ti-prayer__list">
        {prayerRows.map(({ key, label }) => (
          <div key={key} className={'ti-prayer__row' + (key === next ? ' is-next' : '')}>
            <dt>{label}</dt>
            <dd>{toPersianDigits(values[key])}</dd>
          </div>
        ))}
      </dl>
      <p className="ti-note">محاسبه‌ی تقریبی؛ ممکن است حدود یک دقیقه با جدول رسمی تفاوت داشته باشد.</p>
      <Link to="/app/prayer-times" className="ti-more">
        جزئیات بیشتر
      </Link>
    </section>
  );
}

// --- Page ---------------------------------------------------------------------------------------

/** Everything that depends on the current moment; rendered only in the browser. */
function LiveHome() {
  const now = useNow();
  return (
    <>
      <TodayPanel now={now} />
      <div className="ti-layout">
        <div className="ti-layout__primary">
          <CalendarSection now={now} />
        </div>
        <aside className="ti-layout__aside">
          <PrayerTimesCard now={now} />
          <DateConverter style={{ animationDelay: '200ms' }} />
        </aside>
      </div>
    </>
  );
}

/** Same footprint as LiveHome, so nothing jumps when the live content replaces it. */
function HomePlaceholder() {
  return (
    <div aria-hidden="true">
      <div className="ti-today glass ti-placeholder ti-placeholder--today" />
      <div className="ti-layout">
        <div className="ti-layout__primary">
          <div className="ti-card glass ti-placeholder ti-placeholder--calendar" />
        </div>
        <div className="ti-layout__aside">
          <div className="ti-card glass ti-placeholder ti-placeholder--prayer" />
        </div>
      </div>
    </div>
  );
}

export function LandingPage() {
  return (
    <PublicLayout>
      <main className="ti-main">
        <h1 className="sr-only">تقویم شمسی، قمری و میلادی، اوقات شرعی و مناسبت‌های امروز</h1>
        <ClientOnly fallback={<HomePlaceholder />}>
          <LiveHome />
        </ClientOnly>

        <section className="ti-about glass" aria-labelledby="ti-about-title">
          <h2 id="ti-about-title" className="ti-card__title">درباره‌ی ساعت‌باشی</h2>
          <p>
            ساعت‌باشی تقویم کامل شمسی را همراه با تاریخ قمری و میلادی هر روز نشان می‌دهد؛ جمعه‌ها و تعطیلات رسمی
            به رنگ قرمزند و مناسبت‌های هر ماه زیر تقویم آمده است. اوقات شرعی امروز (اذان صبح، طلوع آفتاب، اذان
            ظهر، غروب آفتاب، اذان مغرب و نیمه‌شب شرعی) به افق تهران یا موقعیت شما محاسبه می‌شود و با مبدل تاریخ
            می‌توانید تاریخ شمسی و میلادی را به هم تبدیل کنید.
          </p>
          <ul className="ti-about__links">
            <li><Link to="/app/prayer-times">اوقات شرعی امروز</Link></li>
            <li><Link to="/app/calendar">تقویم ماه جاری</Link></li>
            <li><Link to="/app/texts/ziyarat_ashura">متن کامل زیارت عاشورا</Link></li>
            <li><Link to="/app/texts/sahifa_dua_7">دعای هفتم صحیفه سجادیه</Link></li>
            <li><Link to="/app/texts/hadith_kisa">حدیث کساء</Link></li>
            <li><Link to="/app/home">شمارنده‌ی نماز و روزه‌ی قضا</Link></li>
          </ul>
        </section>
      </main>

    </PublicLayout>
  );
}
