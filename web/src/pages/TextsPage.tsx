import { Link } from 'react-router-dom';
import { religiousTexts } from '../data/religiousTexts';
import './TextsPage.css';

export function TextsPage() {
  return (
    <div className="texts-page">
      <h1 className="texts-page__title">متون مذهبی</h1>
      <div className="texts-page__subtitle">زیارت‌ها، ادعیه و احادیث</div>

      {religiousTexts.map((text) => (
        <Link key={text.id} to={`/app/texts/${text.id}`} className="text-card">
          <div className="text-card__title">{text.title}</div>
          <div className="text-card__subtitle">{text.subtitle}</div>
        </Link>
      ))}
    </div>
  );
}
