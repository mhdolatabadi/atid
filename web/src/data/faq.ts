// Visible FAQ of the public guide pages; seo.ts emits the same entries as FAQPage JSON-LD,
// so structured data always matches what readers see.

export interface FaqEntry {
  question: string;
  answer: string;
}

export const faqs: Record<string, FaqEntry[]> = {
  '/calendar': [
    { question: 'تقویم شمسی چیست؟', answer: 'تقویم هجری شمسی، تقویم رسمی ایران است و آغاز سال آن با نوروز هم‌زمان می‌شود. در ساعت‌باشی می‌توانید تاریخ شمسی، میلادی و قمری را کنار هم ببینید.' },
    { question: 'امروز چندم است؟', answer: 'صفحهٔ اصلی ساعت‌باشی تاریخ امروز را به‌صورت زنده نشان می‌دهد. این صفحه راهنمایی ماندگار برای استفاده از تقویم شمسی و مناسبت‌ها است.' },
  ],
  '/date-converter': [
    { question: 'چطور تاریخ شمسی را به میلادی تبدیل کنم؟', answer: 'در همین صفحه گزینهٔ «از شمسی» را انتخاب کنید و روز، ماه و سال را وارد کنید تا معادل میلادی و قمری آن را ببینید.' },
    { question: 'آیا تاریخ قمری قطعی است؟', answer: 'تاریخ قمری محاسبه‌شده ممکن است با تقویم رسمی مبتنی بر رؤیت هلال تا یک روز اختلاف داشته باشد.' },
  ],
};
