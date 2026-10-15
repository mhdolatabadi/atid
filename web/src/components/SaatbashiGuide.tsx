import './SaatbashiGuide.css';
import { useLocation } from 'react-router-dom';
import { saatbashiMessage } from './saatbashiMessages';

export function SaatbashiGuide({ message }: { message?: string }) {
  const { pathname } = useLocation();
  return <aside className="saatbashi-guide glass" aria-label="راهنمای ساعت‌باشی">
    <img src="/saatbashi-character.webp" width="80" height="120" alt="ساعت‌باشی، پیرمرد مهربان با سر طاس و موهای فرفری سفید" />
    <div><strong>همراهِ روزهای تو</strong><p>{message ?? saatbashiMessage(pathname)}</p></div>
  </aside>;
}
