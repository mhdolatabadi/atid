import { Link } from 'react-router-dom';
import { ClientOnly } from '../components/ClientOnly';
import { DateConverter } from '../components/DateConverter';
import { PublicLayout } from '../components/SiteChrome';
import './LandingPage.css';

const faq = {
  calendar: [
    ['تقویم شمسی چیست؟', 'تقویم هجری شمسی، تقویم رسمی ایران است و آغاز سال آن با نوروز هم‌زمان می‌شود. در عتید می‌توانید تاریخ شمسی، میلادی و قمری را کنار هم ببینید.'],
    ['امروز چندم است؟', 'صفحهٔ اصلی عتید تاریخ امروز را به‌صورت زنده نشان می‌دهد. این صفحه راهنمایی ماندگار برای استفاده از تقویم شمسی و مناسبت‌ها است.'],
  ],
  converter: [
    ['چطور تاریخ شمسی را به میلادی تبدیل کنم؟', 'در همین صفحه گزینهٔ «از شمسی» را انتخاب کنید و روز، ماه و سال را وارد کنید تا معادل میلادی و قمری آن را ببینید.'],
    ['آیا تاریخ قمری قطعی است؟', 'تاریخ قمری محاسبه‌شده ممکن است با تقویم رسمی مبتنی بر رؤیت هلال تا یک روز اختلاف داشته باشد.'],
  ],
};

function Faq({ items }: { items: string[][] }) {
  return <section className="ti-about glass" aria-labelledby="faq-title"><h2 id="faq-title" className="ti-card__title">پرسش‌های پرتکرار</h2>{items.map(([question, answer]) => <div key={question}><h3>{question}</h3><p>{answer}</p></div>)}</section>;
}

export function CalendarGuidePage() {
  return <PublicLayout><main className="ti-main"><article className="ti-about glass"><h1 className="ti-card__title">تقویم شمسی؛ تاریخ امروز، مناسبت‌ها و تعطیلات</h1><p>تقویم عتید برای دیدن تاریخ امروز در سه گاه‌شماری شمسی، میلادی و قمری طراحی شده است. ماه جاری، جمعه‌ها، مناسبت‌ها و اوقات شرعی را در یک نمای خوانا می‌بینید.</p><h2>تقویم امروز و ماه جاری</h2><p>برای تقویم زنده، روزهای ماه و تاریخ امروز به <Link to="/">صفحهٔ اصلی عتید</Link> بروید. برای مرور ماه با کنترل‌های جابه‌جایی، <Link to="/app/calendar">تقویم ماه جاری</Link> در دسترس است.</p><h2>تقویم شمسی چگونه کار می‌کند؟</h2><p>سال هجری شمسی با نوروز آغاز می‌شود؛ شش ماه نخست ۳۱ روز، پنج ماه بعدی ۳۰ روز و اسفند ۲۹ یا در سال کبیسه ۳۰ روز دارد. عتید تاریخ‌های میلادی و قمری را هم کنار تاریخ شمسی نمایش می‌دهد تا برنامه‌ریزی روزمره ساده‌تر شود.</p></article><Faq items={faq.calendar} /></main></PublicLayout>;
}

export function DateConverterGuidePage() {
  return (
    <PublicLayout>
      <main className="ti-main ti-main--narrow">
        <h1 className="ti-page-title">تبدیل تاریخ شمسی، قمری و میلادی</h1>
        <ClientOnly fallback={<div className="ti-card glass ti-placeholder ti-placeholder--converter" aria-hidden="true" />}>
          <DateConverter />
        </ClientOnly>
        <article className="ti-about glass">
          <h2>مبدل تاریخ آنلاین</h2>
          <p>با ابزار تبدیل تاریخ عتید می‌توانید معادل یک تاریخ شمسی را در تقویم میلادی و قمری ببینید، تاریخ میلادی را به شمسی تبدیل کنید یا بفهمید یک روز قمری، مثلاً دهم محرم، امسال چه روزی از هفته است. روز، ماه و سال را وارد کنید؛ نتیجه با نام روز هفته نمایش داده می‌شود.</p>
          <h2>تفاوت تقویم قمری و شمسی</h2>
          <p>تقویم شمسی با فصل‌ها هماهنگ است؛ تقویم قمری بر پایهٔ ماه است و هر سال حدود یازده روز کوتاه‌تر از سال شمسی است. بنابراین مناسبت‌های قمری هر سال در تقویم شمسی جابه‌جا می‌شوند. برای دیدن ماه جاری، <Link to="/">تقویم عتید</Link> را ببینید.</p>
        </article>
        <Faq items={faq.converter} />
      </main>
    </PublicLayout>
  );
}
