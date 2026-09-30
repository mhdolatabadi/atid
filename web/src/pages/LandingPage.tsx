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
  weekdayName,
} from '../lib/persianDate';
import { hijriMonthName, toHijri } from '../lib/hijriDate';
import { calculateForToday, type PrayerTimes } from '../lib/prayerTimes';
import { requestDeviceLocation, TEHRAN, type Coordinates } from '../lib/location';
import { isHoliday, occasionsOn, type Occasion } from '../data/occasions';
import { ChevronIcon, LocationIcon } from '../components/icons';
import './LandingPage.css';

const weekdayHeaders = ['شنبه', 'یکشنبه', 'دوشنبه', 'سه‌شنبه', 'چهارشنبه', 'پنجشنبه', 'جمعه'];

function sameDay(a: Date, b: Date): boolean {
  return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
}

function solarLabel(date: Date): string {
  const p = fromDate(date);
  return `${weekdayName(p.dayOfWeek)} ${toPersianDigits(p.day)} ${monthName(p.month)} ${toPersianDigits(p.year)}`;
}

function hijriLabel(date: Date): string {
  const h = toHijri(date);
  return `${toPersianDigits(h.day)} ${hijriMonthName(h.month)} ${toPersianDigits(h.year)}`;
}

function gregorianLabel(date: Date): string {
  return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' });
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

// --- Date converter -----------------------------------------------------------------------------

function DateConverterCard() {
  const today = fromDate(new Date());
  const [mode, setMode] = useState<'solar' | 'gregorian'>('solar');
  const [fields, setFields] = useState({ year: String(today.year), month: String(today.month), day: String(today.day) });

  const switchMode = (next: 'solar' | 'gregorian') => {
    const now = new Date();
    setMode(next);
    setFields(
      next === 'solar'
        ? { year: String(today.year), month: String(today.month), day: String(today.day) }
        : { year: String(now.getFullYear()), month: String(now.getMonth() + 1), day: String(now.getDate()) },
    );
  };

  const result = useMemo(() => {
    const y = Number(fields.year);
    const m = Number(fields.month);
    const d = Number(fields.day);
    if (!Number.isInteger(y) || !Number.isInteger(m) || !Number.isInteger(d) || m < 1 || m > 12 || d < 1) return null;
    if (mode === 'solar') {
      if (y < 1300 || y > 1500 || d > daysInMonth(y, m)) return null;
      return toGregorianDate(y, m, d);
    }
    if (y < 1921 || y > 2121) return null;
    const date = new Date(y, m - 1, d, 12);
    return date.getMonth() === m - 1 ? date : null;
  }, [fields, mode]);

  const field = (name: 'year' | 'month' | 'day', label: string) => (
    <label className="ti-converter__field">
      <span>{label}</span>
      <input
        inputMode="numeric"
        value={fields[name]}
        onChange={(e) => setFields((f) => ({ ...f, [name]: e.target.value.replace(/[۰-۹]/g, (c) => String('۰۱۲۳۴۵۶۷۸۹'.indexOf(c))) }))}
      />
    </label>
  );

  return (
    <section className="ti-card ti-converter glass enter" style={{ animationDelay: '200ms' }} aria-label="تبدیل تاریخ">
      <h2 className="ti-card__title">تبدیل تاریخ</h2>
      <div className="ti-segmented" role="tablist">
        <button type="button" role="tab" aria-selected={mode === 'solar'} onClick={() => switchMode('solar')}>
          از شمسی
        </button>
        <button type="button" role="tab" aria-selected={mode === 'gregorian'} onClick={() => switchMode('gregorian')}>
          از میلادی
        </button>
      </div>
      <div className="ti-converter__fields">
        {field('day', 'روز')}
        {field('month', 'ماه')}
        {field('year', 'سال')}
      </div>
      {result ? (
        <dl key={result.toDateString()} className="ti-converter__result">
          <div><dt>شمسی</dt><dd>{solarLabel(result)}</dd></div>
          <div><dt>قمری</dt><dd>{hijriLabel(result)}</dd></div>
          <div><dt>میلادی</dt><dd dir="ltr">{gregorianLabel(result)}</dd></div>
        </dl>
      ) : (
        <p className="ti-note ti-note--error">تاریخ واردشده معتبر نیست.</p>
      )}
    </section>
  );
}

// --- Page ---------------------------------------------------------------------------------------

export function LandingPage() {
  const now = useNow();
  return (
    <div className="ti">
      <header className="ti-header glass">
        <div className="ti-header__inner">
          <Link to="/" className="ti-brand">عتید</Link>
          <nav className="ti-nav" aria-label="بخش‌ها">
            <Link to="/app/calendar">تقویم</Link>
            <Link to="/app/prayer-times">اوقات شرعی</Link>
            <Link to="/app/home">نماز و روزه قضا</Link>
            <Link to="/app/texts">متون مذهبی</Link>
            <a href="https://github.com/mhdolatabadi/atid/releases/latest" target="_blank" rel="noreferrer">
              نسخه اندروید
            </a>
          </nav>
        </div>
      </header>

      <main className="ti-main">
        <TodayPanel now={now} />
        <div className="ti-layout">
          <div className="ti-layout__primary">
            <CalendarSection now={now} />
          </div>
          <aside className="ti-layout__aside">
            <PrayerTimesCard now={now} />
            <DateConverterCard />
          </aside>
        </div>
      </main>

      <footer className="ti-footer">اطلاعات شما فقط در همین مرورگر ذخیره می‌شود.</footer>
    </div>
  );
}
