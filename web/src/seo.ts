import { cities } from './data/cities';

export const SITE_NAME = 'ساعت‌باشی';

export interface PageMeta {
  title: string;
  description: string;
  /** Personal pages are kept out of search results. */
  noindex?: boolean;
  /** schema.org type for the page's JSON-LD. */
  type: 'WebSite' | 'WebPage' | 'CollectionPage';
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
};

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

/** One canonical path for SSR and in-app navigation; query parameters never become canonicals. */
export function canonicalPath(pathname: string): string {
  const path = pathname.split(/[?#]/, 1)[0];
  return path.replace(/\/+$/, '') || '/';
}

export function structuredDataFor(pathname: string, origin: string) {
  const path = canonicalPath(pathname);
  const meta = metaFor(path);
  const site = `${origin}/`;
  const url = `${origin}${path}`;
  const page: Record<string, unknown> = {
    '@type': meta.type, '@id': `${url}#page`, url,
    name: meta.title, description: meta.description, inLanguage: 'fa',
    ...(path === '/' ? { name: SITE_NAME, alternateName: 'Saatbashi' } :
      { isPartOf: { '@id': `${site}#page` } }),
  };
  const graph: Record<string, unknown>[] = [page];
  if (path !== '/' && !meta.noindex) {
    const paths = ['/'];
    if (path.startsWith('/prayer-times/')) paths.push('/prayer-times');
    paths.push(path);
    const breadcrumbId = `${url}#breadcrumb`;
    page.breadcrumb = { '@id': breadcrumbId };
    graph.push({ '@type': 'BreadcrumbList', '@id': breadcrumbId,
      itemListElement: paths.map((p, i) => ({ '@type': 'ListItem', position: i + 1,
        name: p === '/' ? SITE_NAME : metaFor(p).title.split('|')[0].trim(), item: `${origin}${p}` })) });
  }
  return { '@context': 'https://schema.org', '@graph': graph };
}
