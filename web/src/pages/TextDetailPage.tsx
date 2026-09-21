import { useNavigate, useParams } from 'react-router-dom';
import { getReligiousTextById } from '../data/religiousTexts';
import { ArrowRightIcon } from '../components/icons';
import './TextDetailPage.css';

export function TextDetailPage() {
  const { textId } = useParams<{ textId: string }>();
  const navigate = useNavigate();
  const text = textId ? getReligiousTextById(textId) : undefined;

  if (!text) {
    return (
      <div className="text-detail-page">
        <button type="button" className="text-detail-page__back" onClick={() => navigate(-1)}>
          <ArrowRightIcon />
          بازگشت
        </button>
        <p>متن مورد نظر پیدا نشد.</p>
      </div>
    );
  }

  return (
    <div className="text-detail-page">
      <button type="button" className="text-detail-page__back" onClick={() => navigate(-1)}>
        <ArrowRightIcon />
        بازگشت
      </button>

      <h1 className="text-detail-page__title">{text.title}</h1>
      <div className="text-detail-page__subtitle">{text.subtitle}</div>
      <div className="text-detail-page__note">{text.note}</div>
      <div className="text-detail-page__body">{text.body}</div>
    </div>
  );
}
