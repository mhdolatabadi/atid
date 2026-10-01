# دیپلوی نسخه وب عتید روی سرور

نسخه وب یک سایت استاتیک است که ایمیج `atid-web` آن را با nginx سرو می‌کند. عتید **Caddy جداگانه ندارد**: روی سرور، Caddy دیگری (مثلاً Caddy نفیر) پورت‌های ۸۰ و ۴۴۳ را در اختیار دارد و یک بلوک سایت در آن، دامنه‌ی عتید را به `atid-web:80` می‌فرستد.

## قاعده‌ی مهم: پروژه‌ی Compose جدا

`compose.yaml` با `name: atid` نام پروژه را ثابت می‌کند. بدون این خط، Docker Compose نام پوشه (`deploy`) را برمی‌دارد که با پروژه‌ی نفیر یکی است، و `up --remove-orphans` کانتینرهای نفیر را حذف می‌کند (اتفاقی که در اولین دیپلوی افتاد؛ issue شماره‌ی ۱۸). `deploy.sh` و CI هر دو این خط را بررسی می‌کنند. **آن را حذف یا عوض نکنید.**

نام سرویس هم `atid-web` است، نه `web`، چون روی شبکه‌ی مشترک proxy نام سرویس همان نام DNS است و `web` مال نفیر است.

## راه‌اندازی یک‌باره روی سرور

1. یک رکورد A برای دامنه‌ی عتید به IP سرور بسازید.
2. مخزن را clone کنید، مثلاً در `/home/apps/atid`، و تنظیمات را بسازید:

   ```bash
   cd /home/apps/atid/deploy
   cp .env.example .env && chmod 600 .env
   nano .env   # ATID_DOMAIN و در صورت نیاز ATID_PROXY_NETWORK
   ```

   `ATID_PROXY_NETWORK` شبکه‌ی Docker همان Caddy است که پورت ۴۴۳ را دارد. برای پیدا کردنش:

   ```bash
   docker inspect "$(docker ps --filter publish=443 --format '{{.Names}}')" \
     --format '{{range $k,$v := .NetworkSettings.Networks}}{{$k}} {{end}}'
   ```

   برای نفیر معمولاً `deploy_edge` است (پیش‌فرض).

3. **بلوک سایت عتید را به Caddyfile همان proxy اضافه کنید** (برای نفیر: `deploy/Caddyfile` در مخزن نفیر) و Caddy را دوباره بارگذاری کنید:

   ```caddy
   atid.example.com {
     encode zstd gzip
     reverse_proxy atid-web:80
   }
   ```

   ```bash
   docker exec <نام کانتینر caddy> caddy reload --config /etc/caddy/Caddyfile
   ```

   بهتر است این بلوک در خود مخزن نفیر commit شود تا دیپلوی بعدی نفیر آن را پاک نکند.

## ایمیج‌ها

ورک‌فلوی **Images** با هر push به `master` ایمیج را می‌سازد و با تگ commit و `latest` در `ghcr.io/mhdolatabadi/atid/web` منتشر می‌کند. پکیج باید public باشد، یا سرور با توکن `read:packages` روی `ghcr.io` لاگین کرده باشد.

پیش از اولین build، در **Settings → Secrets and variables → Actions → Variables** متغیر `ATID_SITE_URL` را برابر نشانی عمومی سایت (مثلاً `https://atid.example.com`) بگذارید تا آدرس‌های canonical و `sitemap.xml` ساخته شوند.

## به‌روزرسانی

`deploy/deploy.sh [revision]` روی سرور:

1. commit موردنظر (پیش‌فرض `master`) را checkout می‌کند و **نسخه‌ی همان commit** از خودش را اجرا می‌کند؛
2. وجود `.env`، `name: atid` و شبکه‌ی proxy را بررسی می‌کند و در غیر این صورت بدون هیچ تغییری متوقف می‌شود؛
3. ایمیج همان commit را pull می‌کند، فقط پروژه‌ی `atid` را بالا می‌آورد و تا وقتی پاسخ `https://<ATID_DOMAIN>/` هدر `X-Atid-Revision` برابر همان commit را برگرداند صبر می‌کند. بنابراین HTTP 200 از backend یا cache قدیمی، deploy موفق محسوب نمی‌شود.

دیپلوی خودکار: بعد از موفق شدن **Quality checks** روی `master`، ورک‌فلوی **Deploy** منتظر ایمیج همان commit می‌ماند و از طریق SSH اسکریپت را اجرا می‌کند. اجرای دستی: **Actions → Deploy → Run workflow**.

Secretهای environment به نام `production`:

| Secret | مقدار |
| --- | --- |
| `DEPLOY_HOST` | IP یا hostname سرور |
| `DEPLOY_USER` | کاربر SSH که اجازه‌ی اجرای `docker` دارد |
| `DEPLOY_PATH` | مسیر مطلق clone مخزن، مثلاً `/home/apps/atid` (بدون فاصله یا `/` اضافه در انتها) |
| `DEPLOY_SSH_KEY` | کلید خصوصی یک جفت‌کلید مخصوص دیپلوی |
| `DEPLOY_KNOWN_HOSTS` | خروجی `ssh-keyscan -t ed25519,ecdsa,rsa <DEPLOY_HOST>` |
| `DEPLOY_PORT` | (اختیاری) پورت SSH، پیش‌فرض ۲۲ |

## اولین دیپلوی بعد از این تغییر

روی سرور یک بار نسخه‌ی جدید اسکریپت را بیاورید، چون ورک‌فلو اسکریپتی را اجرا می‌کند که الان روی سرور است:

```bash
cd /home/apps/atid && git fetch origin master && git checkout --detach origin/master
```

سپس بلوک Caddy (مرحله‌ی ۳ بالا) را اضافه کنید و ورک‌فلوی Deploy را دوباره فعال و اجرا کنید.

## برگرداندن نسخه

```bash
deploy/deploy.sh <commit-قبلی>
```

## نکات امنیتی

- فایل `.env` را هرگز commit نکنید.
- کلید SSH دیپلوی را فقط برای همین کار بسازید.
