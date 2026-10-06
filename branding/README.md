# لوگوی ساعت‌باشی

نشان ساعت‌باشی یک ساعت ساده‌ی طلایی است: یک حلقه و دو عقربه روی ده و ده دقیقه (ساعت‌باشی کسی بود که ساعت و تقویم را نگه می‌داشت و وقت نماز را اعلام می‌کرد). زمینه کاشی آسمان شب با رنگ‌های خود سایت است.

همه‌ی فایل‌ها از یک هندسه در `generate.py` ساخته می‌شوند تا وب و اندروید هیچ‌وقت از هم جدا نشوند. فایل‌های ساخته‌شده را دستی ویرایش نکنید.

## ساختن دوباره

```bash
python3 branding/generate.py            # SVG و vectorهای اندروید
npm i --no-save playwright && node branding/rasterize.mjs   # PNGها
```

| خروجی | کجا |
| --- | --- |
| `branding/logo.svg`، `web/public/favicon.svg` | منبع و favicon وب (هدر سایت هم همین را نشان می‌دهد) |
| `web/public/icon-192.png`، `icon-512.png`، `apple-touch-icon.png` | آیکون‌های PNG وب |
| `web/public/og-image.png` | تصویر پیش‌نمایش اشتراک‌گذاری (۱۲۰۰×۶۳۰) |
| `app/src/main/res/drawable/ic_launcher_{background,foreground,monochrome}.xml` | آیکون adaptive اندروید ۸ به بالا، با لایه‌ی تک‌رنگ برای themed icons |
| `app/src/main/res/mipmap-*/ic_launcher{,_round}.png` | آیکون اندروید ۷ (API 24–25) |
