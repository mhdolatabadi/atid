import {
  QADA_PRAYERS,
  decrementFast,
  decrementPrayer,
  incrementFast,
  incrementPrayer,
  useFastCount,
  useQadaCounts,
  type QadaPrayer,
} from '../store/qadaStore';
import { toPersianDigits } from '../lib/persianDate';
import './HomePage.css';

const prayerLabels: Record<QadaPrayer, string> = {
  FAJR: 'نماز صبح',
  ZOHR: 'نماز ظهر',
  ASR: 'نماز عصر',
  MAGHRIB: 'نماز مغرب',
  ISHA: 'نماز عشا',
};

function RoundButton({
  symbol,
  filled,
  onClick,
}: {
  symbol: string;
  filled?: boolean;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      className={'round-button' + (filled ? ' round-button--filled' : '')}
      onClick={onClick}
      aria-label={symbol === '+' ? 'افزودن' : 'کاستن'}
    >
      {symbol}
    </button>
  );
}

function QadaRow({
  label,
  count,
  onIncrement,
  onDecrement,
}: {
  label: string;
  count: number;
  onIncrement: () => void;
  onDecrement: () => void;
}) {
  return (
    <div className="qada-row glass">
      <span className="qada-row__label">{label}</span>
      <div className="qada-row__controls">
        <RoundButton symbol="−" onClick={onDecrement} />
        <span key={count} className="qada-row__count" aria-live="polite">
          {toPersianDigits(count)}
        </span>
        <RoundButton symbol="+" filled onClick={onIncrement} />
      </div>
    </div>
  );
}

export function HomePage() {
  const counts = useQadaCounts();
  const fastCount = useFastCount();
  const total = QADA_PRAYERS.reduce((sum, prayer) => sum + counts[prayer], 0) + fastCount;

  return (
    <div className="home-page">
      <h1 className="home-page__title">قضا</h1>

      <div className="home-page__total glass">
        <div className="home-page__total-label">مجموع نماز و روزه قضای باقی‌مانده</div>
        <div key={total} className="home-page__total-value">
          {toPersianDigits(total)}
        </div>
      </div>

      <div className="home-page__section-title">نماز‌های قضا</div>
      {QADA_PRAYERS.map((prayer) => (
        <QadaRow
          key={prayer}
          label={prayerLabels[prayer]}
          count={counts[prayer]}
          onIncrement={() => incrementPrayer(prayer)}
          onDecrement={() => decrementPrayer(prayer)}
        />
      ))}

      <div className="home-page__section-title">روزه قضا</div>
      <QadaRow
        label="روزه"
        count={fastCount}
        onIncrement={incrementFast}
        onDecrement={decrementFast}
      />
    </div>
  );
}
