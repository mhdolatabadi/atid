# اتید — نسخه وب

نسخه وب اپلیکیشن اتید: پیگیری نماز و روزه قضا، تقویم فارسی و اوقات شرعی، بدون نیاز به نصب.

ساخته‌شده با Vite + React + TypeScript. تمام داده‌ها (شمارنده‌های قضا) در `localStorage` مرورگر ذخیره می‌شوند و جایی ارسال نمی‌شوند.

## توسعه

```bash
npm install
npm run dev
```

## ساخت نسخه‌ی نهایی

```bash
npm run build
```

خروجی در پوشه‌ی `dist/` قرار می‌گیرد و یک سایت کاملاً استاتیک است — روی هر هاست استاتیکی (GitHub Pages، Netlify، Vercel، یا هر سرور فایل ساده) قابل هاست شدن است. مسیرها با `HashRouter` مدیریت می‌شوند تا نیازی به تنظیم rewrite سمت سرور نباشد.

## دیپلوی

با هر push به شاخه‌ی `master` که پوشه‌ی `web/` را تغییر دهد، ورک‌فلوی `.github/workflows/deploy-web.yml` نسخه‌ی وب را build کرده و روی GitHub Pages منتشر می‌کند. اجرای دستی هم از تب Actions (گزینه‌ی Run workflow) ممکن است.

پیش‌نیاز یک‌باره: در تنظیمات مخزن، بخش **Settings → Pages**، مقدار **Source** را روی **GitHub Actions** بگذارید.

آدرس سایت: `https://mhdolatabadi.github.io/atid/`

## ساختار

- `src/lib/persianDate.ts` — تبدیل تقویم میلادی↔جلالی (پورت‌شده از منطق اپ اندروید)
- `src/lib/prayerTimes.ts` — محاسبه‌ی اوقات شرعی (پورت‌شده از منطق اپ اندروید)
- `src/data/religiousTexts.ts` — متون مذهبی
- `src/store/qadaStore.ts` — مخزن شمارنده‌های قضا روی localStorage
- `src/pages/` — صفحات (لندینگ، قضا، تقویم، اوقات شرعی، متون مذهبی)
