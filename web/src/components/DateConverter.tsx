import { useMemo, useState, type CSSProperties } from 'react';
import { fromDate, toPersianDigits } from '../lib/persianDate';
import { toHijri } from '../lib/hijriDate';
import { convert, yearRanges, type CalendarKind } from '../lib/dateConverter';
import { gregorianLabel, hijriLabel, solarLabel } from '../lib/dateLabels';

type Fields = { year: string; month: string; day: string };

const modes: { kind: CalendarKind; label: string }[] = [
  { kind: 'solar', label: 'از شمسی' },
  { kind: 'hijri', label: 'از قمری' },
  { kind: 'gregorian', label: 'از میلادی' },
];

function todayIn(kind: CalendarKind): Fields {
  const now = new Date();
  const parts =
    kind === 'solar' ? fromDate(now) : kind === 'hijri' ? toHijri(now) : { year: now.getFullYear(), month: now.getMonth() + 1, day: now.getDate() };
  return { year: String(parts.year), month: String(parts.month), day: String(parts.day) };
}

function toLatinDigits(value: string): string {
  return value.replace(/[۰-۹]/g, (c) => String('۰۱۲۳۴۵۶۷۸۹'.indexOf(c))).replace(/[٠-٩]/g, (c) => String('٠١٢٣٤٥٦٧٨٩'.indexOf(c)));
}

/** Converts a day between the solar, lunar and Gregorian calendars. Defaults to today, so render it client-only. */
export function DateConverter({ headingLevel = 2, style }: { headingLevel?: 2 | 3; style?: CSSProperties }) {
  const [mode, setMode] = useState<CalendarKind>('solar');
  const [fields, setFields] = useState<Fields>(() => todayIn('solar'));
  const Heading = headingLevel === 2 ? 'h2' : 'h3';

  const switchMode = (next: CalendarKind) => {
    setMode(next);
    setFields(todayIn(next));
  };

  const result = useMemo(
    () => convert(mode, Number(fields.year), Number(fields.month), Number(fields.day)),
    [fields, mode],
  );

  const field = (name: keyof Fields, label: string) => (
    <label className="ti-converter__field">
      <span>{label}</span>
      <input
        inputMode="numeric"
        autoComplete="off"
        value={fields[name]}
        onChange={(e) => setFields((f) => ({ ...f, [name]: toLatinDigits(e.target.value) }))}
      />
    </label>
  );

  const [minYear, maxYear] = yearRanges[mode];

  return (
    <section className="ti-card ti-converter glass enter" style={style} aria-label="تبدیل تاریخ">
      <Heading className="ti-card__title">تبدیل تاریخ</Heading>
      <div className="ti-segmented" role="group" aria-label="تقویم ورودی">
        {modes.map(({ kind, label }) => (
          <button key={kind} type="button" aria-pressed={mode === kind} onClick={() => switchMode(kind)}>
            {label}
          </button>
        ))}
      </div>
      <div className="ti-converter__fields">
        {field('day', 'روز')}
        {field('month', 'ماه')}
        {field('year', 'سال')}
      </div>
      <div aria-live="polite">
        {result ? (
          <dl key={result.toDateString()} className="ti-converter__result">
            <div><dt>شمسی</dt><dd>{solarLabel(result)}</dd></div>
            <div><dt>قمری</dt><dd>{hijriLabel(result)}</dd></div>
            <div><dt>میلادی</dt><dd dir="ltr">{gregorianLabel(result)}</dd></div>
          </dl>
        ) : (
          <p className="ti-note ti-note--error">
            تاریخ واردشده معتبر نیست. سال باید بین {toPersianDigits(minYear)} و{' '}
            {toPersianDigits(maxYear)} باشد.
          </p>
        )}
      </div>
      <p className="ti-note">تاریخ قمری بر پایه‌ی تقویم ام‌القری محاسبه می‌شود و ممکن است با تقویم رسمی ایران (رؤیت هلال) یک روز اختلاف داشته باشد.</p>
    </section>
  );
}
