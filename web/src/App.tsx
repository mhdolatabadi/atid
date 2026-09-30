import { HashRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { SkyBackground } from './components/SkyBackground';
import { LandingPage } from './pages/LandingPage';
import { HomePage } from './pages/HomePage';
import { CalendarPage } from './pages/CalendarPage';
import { PrayerTimesPage } from './pages/PrayerTimesPage';
import { TextsPage } from './pages/TextsPage';
import { TextDetailPage } from './pages/TextDetailPage';

export default function App() {
  return (
    <HashRouter>
      <SkyBackground />
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/app" element={<AppShell />}>
          <Route index element={<Navigate to="home" replace />} />
          <Route path="home" element={<HomePage />} />
          <Route path="calendar" element={<CalendarPage />} />
          <Route path="prayer-times" element={<PrayerTimesPage />} />
          <Route path="texts" element={<TextsPage />} />
          <Route path="texts/:textId" element={<TextDetailPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </HashRouter>
  );
}
