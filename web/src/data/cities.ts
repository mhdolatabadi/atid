// Iran's 31 provincial capitals. Mirrored 1:1 in app/src/main/java/ir/mhdolatabadi/atid/data/Cities.kt.

export interface City {
  slug: string;
  name: string;
  latitude: number;
  longitude: number;
}

export const cities: City[] = [
  { slug: 'tehran', name: 'تهران', latitude: 35.6892, longitude: 51.389 },
  { slug: 'mashhad', name: 'مشهد', latitude: 36.2972, longitude: 59.6067 },
  { slug: 'isfahan', name: 'اصفهان', latitude: 32.6525, longitude: 51.6746 },
  { slug: 'shiraz', name: 'شیراز', latitude: 29.5918, longitude: 52.5837 },
  { slug: 'tabriz', name: 'تبریز', latitude: 38.08, longitude: 46.2919 },
  { slug: 'karaj', name: 'کرج', latitude: 35.84, longitude: 50.9391 },
  { slug: 'qom', name: 'قم', latitude: 34.6399, longitude: 50.8759 },
  { slug: 'ahvaz', name: 'اهواز', latitude: 31.3183, longitude: 48.6706 },
  { slug: 'kermanshah', name: 'کرمانشاه', latitude: 34.3142, longitude: 47.065 },
  { slug: 'urmia', name: 'ارومیه', latitude: 37.5527, longitude: 45.0761 },
  { slug: 'rasht', name: 'رشت', latitude: 37.2808, longitude: 49.5832 },
  { slug: 'zahedan', name: 'زاهدان', latitude: 29.4963, longitude: 60.8629 },
  { slug: 'hamedan', name: 'همدان', latitude: 34.7983, longitude: 48.5148 },
  { slug: 'kerman', name: 'کرمان', latitude: 30.2839, longitude: 57.0834 },
  { slug: 'yazd', name: 'یزد', latitude: 31.8974, longitude: 54.3569 },
  { slug: 'ardabil', name: 'اردبیل', latitude: 38.2498, longitude: 48.2933 },
  { slug: 'bandar-abbas', name: 'بندرعباس', latitude: 27.1832, longitude: 56.2666 },
  { slug: 'arak', name: 'اراک', latitude: 34.0954, longitude: 49.7013 },
  { slug: 'zanjan', name: 'زنجان', latitude: 36.6765, longitude: 48.4963 },
  { slug: 'sanandaj', name: 'سنندج', latitude: 35.3219, longitude: 46.9862 },
  { slug: 'qazvin', name: 'قزوین', latitude: 36.2797, longitude: 50.0049 },
  { slug: 'khorramabad', name: 'خرم‌آباد', latitude: 33.4878, longitude: 48.3558 },
  { slug: 'gorgan', name: 'گرگان', latitude: 36.8456, longitude: 54.4393 },
  { slug: 'sari', name: 'ساری', latitude: 36.5633, longitude: 53.0601 },
  { slug: 'bushehr', name: 'بوشهر', latitude: 28.9234, longitude: 50.8203 },
  { slug: 'birjand', name: 'بیرجند', latitude: 32.8663, longitude: 59.2211 },
  { slug: 'ilam', name: 'ایلام', latitude: 33.6374, longitude: 46.4227 },
  { slug: 'bojnurd', name: 'بجنورد', latitude: 37.4747, longitude: 57.329 },
  { slug: 'shahrekord', name: 'شهرکرد', latitude: 32.3256, longitude: 50.8644 },
  { slug: 'semnan', name: 'سمنان', latitude: 35.5729, longitude: 53.3971 },
  { slug: 'yasuj', name: 'یاسوج', latitude: 30.6682, longitude: 51.588 },
];

export const DEFAULT_CITY = cities[0];

export function cityBySlug(slug: string | undefined): City | undefined {
  return cities.find((city) => city.slug === slug);
}
