import { NavLink, Outlet } from 'react-router-dom';
import { QadaIcon, CalendarIcon, ClockIcon, BookIcon } from './icons';
import './AppShell.css';

const tabs = [
  { to: '/app/home', label: 'قضا', Icon: QadaIcon },
  { to: '/app/calendar', label: 'تقویم', Icon: CalendarIcon },
  { to: '/app/prayer-times', label: 'اوقات شرعی', Icon: ClockIcon },
  { to: '/app/texts', label: 'متون مذهبی', Icon: BookIcon },
];

export function AppShell() {
  return (
    <div className="app-shell">
      <main className="app-shell__content">
        <Outlet />
      </main>
      <nav className="app-shell__nav">
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
