import { Link } from 'react-router-dom';
import { QadaIcon, CalendarIcon, ClockIcon, BookIcon } from '../components/icons';
import './LandingPage.css';

const features = [
  {
    Icon: QadaIcon,
    title: 'پیگیری نماز و روزه قضا',
    desc: 'شمارنده‌ای ساده برای هر نماز و روزه‌ی قضا، همیشه در دسترس روی مرورگر.',
  },
  {
    Icon: CalendarIcon,
    title: 'تقویم فارسی',
    desc: 'تقویم جلالی کامل با مشخص بودن امروز و روزهای جمعه.',
  },
  {
    Icon: ClockIcon,
    title: 'اوقات شرعی',
    desc: 'محاسبه‌ی اذان صبح تا عشا بر اساس موقعیت مکانی شما.',
  },
  {
    Icon: BookIcon,
    title: 'متون مذهبی',
    desc: 'زیارت عاشورا، دعای هفتم صحیفه سجادیه و حدیث کساء.',
  },
];

export function LandingPage() {
  return (
    <div className="landing">
      <div className="landing__hero">
        <span className="landing__badge">نسخه وب</span>
        <h1 className="landing__title">اتید</h1>
        <p className="landing__tagline">
          پیگیری نماز و روزه قضا، تقویم فارسی و اوقات شرعی — همه در یک صفحه‌ی ساده، بدون نیاز به نصب.
        </p>
        <div className="landing__actions">
          <Link to="/app" className="landing__button landing__button--primary">
            باز کردن اپ
          </Link>
          <a
            href="https://github.com/mhdolatabadi/atid/releases/latest"
            className="landing__button landing__button--secondary"
            target="_blank"
            rel="noreferrer"
          >
            دانلود نسخه‌ی اندروید
          </a>
        </div>
      </div>

      <div className="landing__features">
        {features.map(({ Icon, title, desc }) => (
          <div key={title} className="landing__feature">
            <div className="landing__feature-icon">
              <Icon />
            </div>
            <div className="landing__feature-title">{title}</div>
            <div className="landing__feature-desc">{desc}</div>
          </div>
        ))}
      </div>

      <div className="landing__footer">اطلاعات شما فقط در همین مرورگر ذخیره می‌شود.</div>
    </div>
  );
}
