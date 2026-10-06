import { Link } from 'react-router-dom';
import { PublicLayout } from '../components/SiteChrome';
import './NotFoundPage.css';

export function NotFoundPage() {
  return (
    <PublicLayout>
      <main className="not-found glass enter">
        <h1>صفحه پیدا نشد</h1>
        <p>نشانی‌ای که باز کردید در ساعت‌باشی وجود ندارد.</p>
        <Link to="/" className="not-found__link">
          بازگشت به صفحه‌ی اصلی
        </Link>
      </main>
    </PublicLayout>
  );
}
