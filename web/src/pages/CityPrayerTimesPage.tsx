import { Link, NavLink, useParams } from 'react-router-dom';
import { ClientOnly } from '../components/ClientOnly';
import { PublicLayout } from '../components/SiteChrome';
import { PrayerTimesTable } from '../components/PrayerTimes';
import { APPROXIMATE_NOTE } from '../lib/prayerTimes';
import { cities, cityBySlug, type City } from '../data/cities';
import { minutesAt, timesFor } from '../lib/place';
import { NotFoundPage } from './NotFoundPage';
import './LandingPage.css';

function CityList({ current }: { current?: string }) {
  return (
    <ul className="ti-city-grid">
      {cities.map((city) => (
        <li key={city.slug}>
          <NavLink to={`/prayer-times/${city.slug}`} aria-current={city.slug === current ? 'page' : undefined}>
            {city.name}
          </NavLink>
        </li>
      ))}
    </ul>
  );
}

function TodayFor({ city }: { city: City }) {
  const now = new Date();
  const place = { kind: 'city', city } as const;
  return <PrayerTimesTable times={timesFor(place, now)} nowMinutes={minutesAt(place, now)} />;
}

export function CityPrayerTimesPage() {
  const city = cityBySlug(useParams().city);
  if (!city) return <NotFoundPage />;
  return (
    <PublicLayout>
      <main className="ti-main ti-main--narrow">
        <h1 className="ti-page-title">اوقات شرعی {city.name}</h1>
        <section className="ti-card ti-prayer glass" aria-label={`اوقات شرعی امروز ${city.name}`}>
          <h2 className="ti-card__title">امروز به افق {city.name}</h2>
          <ClientOnly fallback={<div className="ti-placeholder ti-placeholder--table" aria-hidden="true" />}>
            <TodayFor city={city} />
          </ClientOnly>
          <p className="ti-note">{APPROXIMATE_NOTE} ساعت‌ها به وقت رسمی ایران‌اند.</p>
        </section>
        <section className="ti-about glass" aria-labelledby="cities-title">
          <h2 id="cities-title" className="ti-card__title">شهرهای دیگر</h2>
          <CityList current={city.slug} />
        </section>
      </main>
    </PublicLayout>
  );
}

export function PrayerTimesIndexPage() {
  return (
    <PublicLayout>
      <main className="ti-main ti-main--narrow">
        <h1 className="ti-page-title">اوقات شرعی شهرهای ایران</h1>
        <section className="ti-about glass" aria-labelledby="cities-title">
          <h2 id="cities-title" className="ti-card__title">شهر خود را انتخاب کنید</h2>
          <CityList />
        </section>
        <article className="ti-about glass">
          <h2>اوقات شرعی چگونه محاسبه می‌شود؟</h2>
          <p>
            اذان صبح، طلوع آفتاب، اذان ظهر، غروب آفتاب، اذان مغرب و نیمه‌شب شرعی هر شهر از روی موقعیت جغرافیایی آن و به
            روش تهران (زاویه‌ی ۱۷٫۷ درجه برای صبح و ۴٫۵ درجه برای مغرب) محاسبه می‌شود. این اوقات تقریبی‌اند و ممکن است
            حدود یک دقیقه با جدول رسمی تفاوت داشته باشند. برای شهرهای دیگر می‌توانید در <Link to="/app/prayer-times">صفحه‌ی
            اوقات شرعی</Link> از موقعیت مکانی خود استفاده کنید.
          </p>
        </article>
      </main>
    </PublicLayout>
  );
}
