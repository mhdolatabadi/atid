# سنجش سئو با Google Search Console

این راهنما نشان می‌دهد چطور ببینیم ساعت‌باشی در گوگل چطور دیده می‌شود و از داده‌ها برای بهتر کردن محتوا استفاده کنیم. **هیچ جایگاهی در نتایج جست‌وجو تضمین‌شده نیست**؛ این ابزار فقط می‌گوید چه اتفاقی می‌افتد تا بر اساس آن تصمیم بگیریم.

## ۱. تأیید مالکیت دامنه (یک بار، توسط صاحب سایت)

1. به [search.google.com/search-console](https://search.google.com/search-console) بروید و **Add property** را بزنید.
2. نوع **Domain** را انتخاب کنید و `atid.mhdolatabadi.ir` (یا کل `mhdolatabadi.ir`) را وارد کنید.
3. گوگل یک رکورد `TXT` می‌دهد. آن را در پنل DNS دامنه اضافه کنید و **Verify** را بزنید. ممکن است چند دقیقه تا چند ساعت طول بکشد.

نوع Domain همه‌ی زیردامنه‌ها و هر دو `http`/`https` را پوشش می‌دهد و به تغییر کد سایت نیاز ندارد.

## ۲. ثبت sitemap

در **Sitemaps** نشانی زیر را ثبت کنید:

```
https://atid.mhdolatabadi.ir/sitemap.xml
```

این فایل موقع build از روی `web/src/seo.ts` ساخته می‌شود و فقط صفحه‌های قابل‌ایندکس را دارد (صفحه‌ی شخصی قضا `noindex` است و در آن نیست). وضعیت باید «Success» شود و تعداد آدرس‌های کشف‌شده با فایل یکی باشد.

## ۳. درخواست ایندکس صفحه‌های اولویت‌دار

در **URL Inspection** هر کدام از این‌ها را بررسی کنید و اگر ایندکس نشده بود **Request indexing** را بزنید:

- `https://atid.mhdolatabadi.ir/` (تقویم و اوقات شرعی امروز)
- `https://atid.mhdolatabadi.ir/app/prayer-times`
- `https://atid.mhdolatabadi.ir/app/texts/ziyarat_ashura`
- `https://atid.mhdolatabadi.ir/app/texts/sahifa_dua_7`
- `https://atid.mhdolatabadi.ir/app/texts/hadith_kisa`
- `https://atid.mhdolatabadi.ir/app/calendar`

در همین ابزار، **View crawled page** نشان می‌دهد گوگل چه HTMLای دیده است؛ متن کامل صفحه باید آنجا باشد (صفحه‌ها موقع build از قبل ساخته می‌شوند).

## ۴. معیارهایی که باید دنبال کرد

| معیار | کجا | چه می‌گوید |
| --- | --- | --- |
| Impressions | Performance | چند بار سایت در نتایج نمایش داده شده |
| Clicks | Performance | چند بار روی نتیجه کلیک شده |
| CTR | Performance | نسبت کلیک به نمایش؛ اگر نمایش زیاد و CTR کم است، عنوان یا توضیح صفحه را در `web/src/seo.ts` بهتر کنید |
| Average position | Performance | میانگین جایگاه؛ روندش مهم است، نه یک عدد |
| Indexed pages | Pages (Indexing) | چند صفحه ایندکس شده و چرا بقیه نشده‌اند |
| Crawl errors | Pages / Settings → Crawl stats | خطاهای ۴۰۴ یا ۵xx هنگام خزیدن |
| Core Web Vitals | Experience | سرعت و پایداری بارگذاری روی موبایل |

## ۵. چرخه‌ی بهبود

هر چند هفته یک بار:

1. در **Performance → Queries** ببینید کاربران با چه عبارت‌هایی سایت را پیدا می‌کنند (مثلاً «اوقات شرعی تهران»، «متن زیارت عاشورا»، «تقویم مهر ۱۴۰۵»).
2. عبارت‌هایی که نمایش دارند ولی کلیک کم، نشانه‌ی عنوان یا توضیح ضعیف‌اند؛ آن‌ها را در `web/src/seo.ts` اصلاح کنید.
3. عبارت‌های پرجست‌وجویی که صفحه‌ای برایشان نداریم، ایده‌ی محتوای جدید هستند؛ برایشان issue بسازید.
4. بعد از هر تغییر مهم، صفحه‌ی مربوط را در URL Inspection دوباره بررسی و درخواست ایندکس کنید.

## Bing (اختیاری)

[Bing Webmaster Tools](https://www.bing.com/webmasters) می‌تواند تنظیمات Search Console را مستقیم وارد کند (**Import from Google Search Console**) و همان sitemap را می‌خواند.
