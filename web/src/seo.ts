import { religiousTexts } from './data/religiousTexts';
import { cities } from './data/cities';

export const SITE_NAME = 'ساعت‌باشی';

export interface PageMeta {
  title: string;
  description: string;
  /** Personal pages are kept out of search results. */
  noindex?: boolean;
  /** schema.org type for the page's JSON-LD. */
  type: 'WebSite' | 'WebPage' | 'Article' | 'CollectionPage';
  faq?: { question: string; answer: string }[];
}

const home: PageMeta = {
  title: 'ساعت‌باشی — تقویم شمسی، قمری و میلادی، اوقات شرعی و مناسبت‌ها',
  description:
    'تقویم کامل شمسی با تاریخ قمری و میلادی هر روز، تعطیلات رسمی و مناسبت‌های ماه، اوقات شرعی امروز و تبدیل تاریخ؛ به‌همراه پیگیری نماز و روزه‌ی قضا.',
  type: 'WebSite',
};

const pages: Record<string, PageMeta> = {
  '/': home,
  '/calendar': {
    title: 'تقویم شمسی؛ تاریخ امروز، مناسبت‌ها و تعطیلات | ساعت‌باشی',
    description: 'راهنمای تقویم شمسی ایران: تاریخ امروز، تقویم ماه جاری، مناسبت‌ها، تعطیلات و نمایش هم‌زمان تاریخ میلادی و قمری.',
    type: 'WebPage',
    faq: [{ question: 'تقویم شمسی چیست؟', answer: 'تقویم هجری شمسی، تقویم رسمی ایران است و سال آن با نوروز آغاز می‌شود.' }],
  },
  '/date-converter': {
    title: 'تبدیل تاریخ شمسی به میلادی و قمری | ساعت‌باشی',
    description: 'تبدیل آنلاین تاریخ شمسی به میلادی و قمری، میلادی به شمسی و قمری به شمسی، همراه با روز هفته.',
    type: 'WebPage',
    faq: [{ question: 'چطور تاریخ شمسی را به میلادی تبدیل کنم؟', answer: 'در صفحهٔ تبدیل تاریخ ساعت‌باشی گزینهٔ «از شمسی» را انتخاب کنید و روز، ماه و سال را وارد کنید.' }],
  },
  '/app/home': {
    title: 'شمارنده‌ی نماز و روزه‌ی قضا | ساعت‌باشی',
    description: 'شمارنده‌ی ساده برای نمازها و روزه‌های قضا؛ اطلاعات فقط در مرورگر خود شما ذخیره می‌شود.',
    noindex: true,
    type: 'WebPage',
  },
  '/app/calendar': {
    title: 'تقویم شمسی ماه جاری | ساعت‌باشی',
    description: 'تقویم ماه جاری شمسی با مشخص بودن امروز و جمعه‌ها.',
    type: 'WebPage',
  },
  '/app/prayer-times': {
    title: 'اوقات شرعی امروز؛ اذان صبح، ظهر و مغرب | ساعت‌باشی',
    description:
      'اوقات شرعی امروز به افق تهران یا موقعیت شما: اذان صبح، طلوع آفتاب، اذان ظهر، عصر، غروب آفتاب، اذان مغرب و عشا.',
    type: 'WebPage',
  },
  '/app/texts': {
    title: 'متون مذهبی؛ زیارت عاشورا، دعای هفتم صحیفه و حدیث کساء | ساعت‌باشی',
    description: 'متن کامل زیارت عاشورا، دعای هفتم صحیفه سجادیه و حدیث شریف کساء.',
    type: 'CollectionPage',
  },
};

for (const text of religiousTexts) {
  pages[`/app/texts/${text.id}`] = {
    title: `متن کامل ${text.title} | ساعت‌باشی`,
    description: `${text.title}: ${text.subtitle}. متن کامل برای خواندن در موبایل و رایانه.`,
    type: 'Article',
  };
}

pages['/prayer-times'] = {
  title: 'اوقات شرعی شهرهای ایران؛ اذان صبح، ظهر و مغرب امروز | ساعت‌باشی',
  description: 'اوقات شرعی امروز مراکز استان‌های ایران: اذان صبح، طلوع آفتاب، اذان ظهر، غروب آفتاب، اذان مغرب و نیمه‌شب شرعی.',
  type: 'CollectionPage',
};

for (const city of cities) {
  pages[`/prayer-times/${city.slug}`] = {
    title: `اوقات شرعی ${city.name} امروز؛ اذان صبح، ظهر و مغرب | ساعت‌باشی`,
    description: `اوقات شرعی امروز به افق ${city.name}: اذان صبح، طلوع آفتاب، اذان ظهر، غروب آفتاب، اذان مغرب و نیمه‌شب شرعی؛ محاسبه‌ی تقریبی به روش تهران.`,
    type: 'WebPage',
  };
}

export const notFoundMeta: PageMeta = {
  title: 'صفحه پیدا نشد | ساعت‌باشی',
  description: 'صفحه‌ای که دنبالش بودید پیدا نشد.',
  noindex: true,
  type: 'WebPage',
};

export function metaFor(pathname: string): PageMeta {
  const clean = pathname.length > 1 ? pathname.replace(/\/+$/, '') : pathname;
  return pages[clean] ?? notFoundMeta;
}

/** Every route that is pre-rendered; indexable ones also go into the sitemap. */
export const prerenderRoutes = Object.keys(pages);
