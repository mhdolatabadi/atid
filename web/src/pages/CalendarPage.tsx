import { useMemo, useState } from 'react';
import {
  daysInMonth,
  firstDayOfWeek,
  monthName,
  today,
  toPersianDigits,
  weekdayIndexSaturdayFirst,
  weekdayName,
} from '../lib/persianDate';
import { ChevronIcon } from '../components/icons';
import './CalendarPage.css';
import { ClientOnly } from '../components/ClientOnly';

const weekdayLabels = ['ش', 'ی', 'د', 'س', 'چ', 'پ', 'ج'];
const FRIDAY_INDEX = 6;

function CalendarPageView() {
  const currentDate = useMemo(() => today(), []);
  const [displayedYear, setDisplayedYear] = useState(currentDate.year);
  const [displayedMonth, setDisplayedMonth] = useState(currentDate.month);
  const [direction, setDirection] = useState(0);

  const goToPreviousMonth = () => {
    setDirection(-1);
    if (displayedMonth === 1) {
      setDisplayedMonth(12);
      setDisplayedYear((y) => y - 1);
    } else {
      setDisplayedMonth((m) => m - 1);
    }
  };

  const goToNextMonth = () => {
    setDirection(1);
    if (displayedMonth === 12) {
      setDisplayedMonth(1);
      setDisplayedYear((y) => y + 1);
    } else {
      setDisplayedMonth((m) => m + 1);
    }
  };

  const days = useMemo(() => {
    const count = daysInMonth(displayedYear, displayedMonth);
    const leadingBlanks = weekdayIndexSaturdayFirst(firstDayOfWeek(displayedYear, displayedMonth));
    const cells: (number | null)[] = Array.from({ length: leadingBlanks }, () => null);
    for (let day = 1; day <= count; day++) cells.push(day);
    return cells;
  }, [displayedYear, displayedMonth]);

  const isDisplayingCurrentMonth =
    displayedYear === currentDate.year && displayedMonth === currentDate.month;

  const todayLabel =
    `امروز: ${weekdayName(currentDate.dayOfWeek)} ` +
    `${toPersianDigits(currentDate.day)} ${monthName(currentDate.month)} ` +
    toPersianDigits(currentDate.year);

  return (
    <div>
      <div className="calendar-page__today">{todayLabel}</div>

      <div className="calendar-page__card glass">
        <div className="calendar-page__header">
          <button type="button" className="calendar-nav-button" onClick={goToNextMonth} aria-label="ماه بعد">
            <ChevronIcon className="calendar-nav-button__icon--flip" />
          </button>
          <span key={`${displayedYear}-${displayedMonth}`} className="calendar-page__month-label">
            {monthName(displayedMonth)} {toPersianDigits(displayedYear)}
          </span>
          <button type="button" className="calendar-nav-button" onClick={goToPreviousMonth} aria-label="ماه قبل">
            <ChevronIcon />
          </button>
        </div>

        <div className="calendar-weekdays">
          {weekdayLabels.map((label, index) => (
            <div
              key={label}
              className={'calendar-weekdays__cell' + (index === FRIDAY_INDEX ? ' calendar-weekdays__cell--friday' : '')}
            >
              {label}
            </div>
          ))}
        </div>

        <div
          key={`${displayedYear}-${displayedMonth}`}
          className={'calendar-grid' + (direction > 0 ? ' is-from-next' : direction < 0 ? ' is-from-prev' : '')}
        >
          {days.map((day, index) => {
            const indexInWeek = index % 7;
            const isToday = isDisplayingCurrentMonth && day === currentDate.day;
            const isFriday = indexInWeek === FRIDAY_INDEX;
            return (
              <div key={index} className="calendar-day">
                {day !== null && (
                  <div
                    className={
                      'calendar-day__circle' +
                      (isToday ? ' calendar-day__circle--today' : isFriday ? ' calendar-day__circle--friday' : '')
                    }
                  >
                    {toPersianDigits(day)}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

export function CalendarPage() {
  return (
    <div className="calendar-page">
      <h1 className="calendar-page__title">تقویم</h1>
      <ClientOnly fallback={<div className="glass page-placeholder page-placeholder--calendar" aria-hidden="true" />}>
        <CalendarPageView />
      </ClientOnly>
    </div>
  );
}
