import { useEffect } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { AppShell } from './components/AppShell';
import { SkyBackground } from './components/SkyBackground';
import { LandingPage } from './pages/LandingPage';
import { HomePage } from './pages/HomePage';
import { CalendarPage } from './pages/CalendarPage';
import { PrayerTimesPage } from './pages/PrayerTimesPage';
import { TextsPage } from './pages/TextsPage';
import { TextDetailPage } from './pages/TextDetailPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { metaFor } from './seo';

/** Keeps the title and description right when navigating without a page load. */
function HeadSync() {
  const { pathname } = useLocation();
  useEffect(() => {
    const meta = metaFor(pathname);
    document.title = meta.title;
    document.querySelector('meta[name="description"]')?.setAttribute('content', meta.description);
  }, [pathname]);
  return null;
}

/** The route table, shared by the browser (BrowserRouter) and the pre-renderer (StaticRouter). */
export default function App() {
  return (
    <>
      <HeadSync />
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
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </>
  );
}
