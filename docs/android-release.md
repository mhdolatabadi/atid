# انتشار نسخه‌ی اندروید ساعت‌باشی

ورک‌فلوی `.github/workflows/release.yml` با push کردن هر تگ `v*` یک APK **release** امضاشده می‌سازد و در GitHub Releases منتشر می‌کند. کلید امضا هرگز در مخزن نیست؛ فقط به‌صورت Secret در GitHub نگه داشته می‌شود.

## ساخت کلید (یک بار، روی کامپیوتر خودتان)

```bash
keytool -genkeypair -v -keystore atid-release.jks -alias atid \
  -keyalg RSA -keysize 4096 -validity 10000
base64 -w0 atid-release.jks > atid-release.jks.b64
```

`keytool` همراه JDK نصب می‌شود. رمز keystore و رمز کلید را جایی امن نگه دارید و از فایل `atid-release.jks` نسخه‌ی پشتیبان بگیرید: **اگر این کلید گم شود، هیچ نسخه‌ی بعدی روی نسخه‌ی نصب‌شده آپدیت نمی‌شود.**

## Secretهای مخزن

در **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | مقدار |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64` | محتوای `atid-release.jks.b64` |
| `ANDROID_KEYSTORE_PASSWORD` | رمز keystore |
| `ANDROID_KEY_ALIAS` | `atid` (همان `-alias`) |
| `ANDROID_KEY_PASSWORD` | رمز کلید |

اگر یکی از این‌ها نباشد، ورک‌فلو با نام Secret ناقص متوقف می‌شود و چیزی منتشر نمی‌کند.

## انتشار نسخه

1. در `app/build.gradle.kts` مقدار `versionCode` را یکی بالا ببرید و `versionName` را عوض کنید (در یک PR).
2. بعد از merge، روی `master` تگ بزنید:

   ```bash
   git tag v1.2.0 && git push origin v1.2.0
   ```

3. ورک‌فلو APK را می‌سازد، امضا را با `apksigner verify` بررسی می‌کند و `Saatbashi-v1.2.0.apk` را در Releases می‌گذارد.

## نکته‌ی مهم برای کاربران فعلی

نسخه‌های ۱.۰.۰ و ۱.۱.۰ با کلید debug تصادفی ساخته شده بودند. اولین نسخه‌ی امضاشده با کلید جدید **روی آن‌ها آپدیت نمی‌شود**؛ کاربران باید یک بار نسخه‌ی قبلی را حذف و نسخه‌ی جدید را نصب کنند. حذف اپ شمارنده‌های قضا را هم پاک می‌کند، پس پیش از آن عددها را یادداشت کنند. از این نسخه به بعد آپدیت‌ها عادی خواهند بود.

بیلدهای محلی (`./gradlew assembleDebug`) تغییری نکرده‌اند و کلید لازم ندارند.
