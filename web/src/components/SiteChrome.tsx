import type { ReactNode } from 'react';
import { Link, NavLink } from 'react-router-dom';
import { religiousTexts } from '../data/religiousTexts';
import './SiteChrome.css';

const ANDROID_RELEASE = 'https://github.com/mhdolatabadi/atid/releases/latest';

const navLinks = [
  { to: '/app/calendar', label: 'تقویم' },
  { to: '/app/prayer-times', label: 'اوقات شرعی' },
  { to: '/date-converter', label: 'تبدیل تاریخ' },
  { to: '/app/home', label: 'نماز و روزه قضا' },
  { to: '/app/texts', label: 'متون مذهبی' },
];

export function SiteHeader() {
  return (
    <header className="ti-header glass">
      <div className="ti-header__inner">
        <Link to="/" className="ti-brand">
          <img src="/favicon.svg" width="32" height="32" alt="" />
          ساعت‌باشی
        </Link>
        <nav className="ti-nav" aria-label="بخش‌ها">
          {navLinks.map(({ to, label }) => (
            <NavLink key={to} to={to} end>
              {label}
            </NavLink>
          ))}
          <a href={ANDROID_RELEASE} target="_blank" rel="noreferrer">
            نسخه اندروید
          </a>
        </nav>
      </div>
    </header>
  );
}

export function SiteFooter() {
  return (
    <footer className="ti-footer glass">
      <div className="ti-footer__groups">
        <section aria-labelledby="footer-tools">
          <h2 id="footer-tools">تقویم و ابزارها</h2>
          <ul>
            <li><Link to="/">تقویم امروز</Link></li>
            <li><Link to="/calendar">راهنمای تقویم شمسی</Link></li>
            <li><Link to="/date-converter">تبدیل تاریخ شمسی، قمری و میلادی</Link></li>
            <li><Link to="/app/prayer-times">اوقات شرعی امروز</Link></li>
          </ul>
        </section>
        <section aria-labelledby="footer-texts">
          <h2 id="footer-texts">متون مذهبی</h2>
          <ul>
            {religiousTexts.map((text) => (
              <li key={text.id}><Link to={`/app/texts/${text.id}`}>{text.title}</Link></li>
            ))}
          </ul>
        </section>
        <section aria-labelledby="footer-more">
          <h2 id="footer-more">ساعت‌باشی</h2>
          <ul>
            <li><Link to="/app/home">شمارنده‌ی نماز و روزه‌ی قضا</Link></li>
            <li><a href={ANDROID_RELEASE} target="_blank" rel="noreferrer">دریافت نسخه اندروید</a></li>
          </ul>
        </section>
      </div>
      <p className="ti-footer__note">
        اطلاعات شما فقط در همین مرورگر ذخیره می‌شود. اوقات شرعی تقریبی‌اند و تاریخ قمری ممکن است با تقویم رسمی
        (رؤیت هلال) یک روز اختلاف داشته باشد.
      </p>
    </footer>
  );
}

/** Header, page content and footer for the public (search-facing) pages. */
export function PublicLayout({ children }: { children: ReactNode }) {
  return (
    <div className="ti">
      <SiteHeader />
      {children}
      <SiteFooter />
    </div>
  );
}
