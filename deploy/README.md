# دیپلوی نسخه وب اتید روی سرور

نسخه وب یک سایت استاتیک است: ایمیج `web` آن را با nginx سرو می‌کند و Caddy گواهی HTTPS را خودکار می‌گیرد و تمدید می‌کند. ساختار همان ساختار دیپلوی نفیر است.

## راه‌اندازی یک‌باره روی سرور

1. یک رکورد A (و در صورت نیاز AAAA) برای دامنه‌ی سایت، مثلاً `atid.example.com`، به IP سرور بسازید.
2. پورت‌های ۸۰ و ۴۴۳ را در فایروال سرور باز کنید.
3. Docker و افزونه‌ی Compose را نصب کنید و مخزن را روی سرور clone کنید، مثلاً در `/opt/atid`.
4. فایل تنظیمات را بسازید:

   ```bash
   cd /opt/atid/deploy
   cp .env.example .env
   chmod 600 .env
   # ATID_DOMAIN را به دامنه‌ی واقعی تغییر دهید
   ```

5. بالا آوردن:

   ```bash
   docker compose up -d --build
   ```

بعد از اینکه DNS به سرور اشاره کرد، `https://<ATID_DOMAIN>` باز می‌شود.

> **اگر روی همین سرور Caddy یا سرویس دیگری (مثل نفیر) پورت ۸۰/۴۴۳ را گرفته است**، سرویس `caddy` این فایل اجرا نمی‌شود. در این حالت فقط سرویس `web` را بالا بیاورید و یک بلوک برای `ATID_DOMAIN` به reverse proxy موجود اضافه کنید که به کانتینر `web` این پروژه پراکسی کند (هر دو باید در یک شبکه‌ی Docker باشند).

## ایمیج‌ها

ورک‌فلوی **Images** با هر push به `master` ایمیج `web` را می‌سازد و با تگ commit و `latest` در `ghcr.io/mhdolatabadi/atid/web` منتشر می‌کند. سرور فقط ایمیج را pull می‌کند و نیازی به Node ندارد.

اگر `docker compose pull` خطای `unauthorized` داد، در بخش **Packages** مخزن visibility پکیج را public کنید، یا روی سرور با توکنی که `read:packages` دارد `docker login ghcr.io` بزنید.

## به‌روزرسانی

`deploy/deploy.sh` روی سرور، commit موردنظر (پیش‌فرض `master`) را checkout می‌کند، ایمیج همان commit را pull می‌کند، سرویس‌ها را دوباره بالا می‌آورد و تا جواب گرفتن از `https://<ATID_DOMAIN>/` صبر می‌کند.

دیپلوی خودکار: بعد از موفق شدن **Quality checks** روی `master`، ورک‌فلوی **Deploy** منتظر ایمیج همان commit می‌ماند و از طریق SSH اسکریپت را روی سرور اجرا می‌کند. اجرای دستی هم از **Actions → Deploy → Run workflow** ممکن است.

این Secretها باید در environment به نام `production` مخزن تعریف شوند:

| Secret | مقدار |
| --- | --- |
| `DEPLOY_HOST` | IP یا hostname سرور |
| `DEPLOY_USER` | کاربر SSH که اجازه‌ی اجرای `docker` دارد |
| `DEPLOY_PATH` | مسیر مطلق clone مخزن روی سرور، مثلاً `/opt/atid` |
| `DEPLOY_SSH_KEY` | کلید خصوصی یک جفت‌کلید مخصوص دیپلوی؛ کلید عمومی را به `~/.ssh/authorized_keys` همان کاربر اضافه کنید |
| `DEPLOY_KNOWN_HOSTS` | خروجی `ssh-keyscan <host>` که با fingerprint سرور چک شده باشد |
| `DEPLOY_PORT` | (اختیاری) پورت SSH، پیش‌فرض ۲۲ |

## برگرداندن نسخه

```bash
deploy/deploy.sh <commit-قبلی>
```

## نکات امنیتی

- فایل `.env` را هرگز commit نکنید.
- کلید SSH دیپلوی را فقط برای همین کار بسازید و جای دیگری استفاده نکنید.
