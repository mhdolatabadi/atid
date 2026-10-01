import { religiousTexts } from './data/religiousTexts';

export const SITE_NAME = 'عتید';

export interface PageMeta {
  title: string;
  description: string;
  /** Personal pages are kept out of search results. */
  noindex?: boolean;
  /** schema.org type for the page's JSON-LD. */
  type: 'WebSite' | 'WebPage' | 'Article' | 'CollectionPage';
}

const home: PageMeta = {
  title: 'عتید — تقویم شمسی، قمری و میلادی، اوقات شرعی و مناسبت‌ها',
  description:
    'تقویم کامل شمسی با تاریخ قمری و میلادی هر روز، تعطیلات رسمی و مناسبت‌های ماه، اوقات شرعی امروز و تبدیل تاریخ؛ به‌همراه پیگیری نماز و روزه‌ی قضا.',
  type: 'WebSite',
};

const pages: Record<string, PageMeta> = {
  '/': home,
  '/app/home': {
    title: 'شمارنده‌ی نماز و روزه‌ی قضا | عتید',
    description: 'شمارنده‌ی ساده برای نمازها و روزه‌های قضا؛ اطلاعات فقط در مرورگر خود شما ذخیره می‌شود.',
    noindex: true,
    type: 'WebPage',
  },
  '/app/calendar': {
    title: 'تقویم شمسی ماه جاری | عتید',
    description: 'تقویم ماه جاری شمسی با مشخص بودن امروز و جمعه‌ها.',
    type: 'WebPage',
  },
  '/app/prayer-times': {
    title: 'اوقات شرعی امروز؛ اذان صبح، ظهر و مغرب | عتید',
    description:
      'اوقات شرعی امروز به افق تهران یا موقعیت شما: اذان صبح، طلوع آفتاب، اذان ظهر، عصر، غروب آفتاب، اذان مغرب و عشا.',
    type: 'WebPage',
  },
  '/app/texts': {
    title: 'متون مذهبی؛ زیارت عاشورا، دعای هفتم صحیفه و حدیث کساء | عتید',
    description: 'متن کامل زیارت عاشورا، دعای هفتم صحیفه سجادیه و حدیث شریف کساء.',
    type: 'CollectionPage',
  },
};

for (const text of religiousTexts) {
  pages[`/app/texts/${text.id}`] = {
    title: `متن کامل ${text.title} | عتید`,
    description: `${text.title}: ${text.subtitle}. متن کامل برای خواندن در موبایل و رایانه.`,
    type: 'Article',
  };
}

export const notFoundMeta: PageMeta = {
  title: 'صفحه پیدا نشد | عتید',
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
