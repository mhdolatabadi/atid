import type { CSSProperties } from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { QadaIcon, CalendarIcon, ClockIcon, BookIcon } from './icons';
import './AppShell.css';

const tabs = [
  { to: '/app/home', label: 'قضا', Icon: QadaIcon },
  { to: '/app/calendar', label: 'تقویم', Icon: CalendarIcon },
  { to: '/app/prayer-times', label: 'اوقات شرعی', Icon: ClockIcon },
  { to: '/app/texts', label: 'متون مذهبی', Icon: BookIcon },
];

export function AppShell() {
  const { pathname } = useLocation();
  const active = Math.max(0, tabs.findIndex((tab) => pathname.startsWith(tab.to)));
  // Keyed by path so every navigation, including texts → a text, plays the page entrance.
  return (
    <div className="app-shell">
      <main key={pathname} className="app-shell__content page-enter">
        <Outlet />
      </main>
      <nav className="app-shell__nav glass" aria-label="بخش‌ها" style={{ '--active': active } as CSSProperties}>
        <span className="app-shell__indicator" aria-hidden="true" />
        {tabs.map(({ to, label, Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) => 'app-shell__tab' + (isActive ? ' app-shell__tab--active' : '')}
          >
            <Icon />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
