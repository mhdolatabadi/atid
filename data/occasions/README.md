# مناسبت‌های تقویم رسمی ایران

`iran.json` مناسبت‌های ایران از مخزن [persian-calendar/events](https://github.com/persian-calendar/events) است (مجوز CC0 1.0). منبع آن تقویم رسمی مرکز تقویم مؤسسه‌ی ژئوفیزیک دانشگاه تهران است ([Calendar-1405.pdf](https://calendar.ut.ac.ir/documents/2139738/7092644/Calendar-1405.pdf)). عنوان‌ها عیناً همان عنوان‌های تقویم رسمی‌اند؛ آن‌ها را ویرایش یا کوتاه نکنید.

بعد از به‌روز کردن `iran.json`، داده‌ی وب و اندروید را دوباره بسازید تا یکسان بمانند:

```bash
python3 data/occasions/generate.py
```

این دستور `web/src/data/occasionsData.ts` و `app/src/main/java/ir/mhdolatabadi/atid/data/OccasionsData.kt` را می‌نویسد.

قاعده‌ها (`rule`):

| قاعده | معنی |
| --- | --- |
| `simple` | روز و ماه ثابت در همان تقویم |
| `end of month` | آخرین روز ماه قمری (۲۹ یا ۳۰) |
| `last weekday of month` | آخرین `weekday` ماه، به‌اضافه‌ی `offset` روز (مثلاً شب آخرین چهارشنبه‌ی سال) |
| `nth weekday of month` | `nth`امین `weekday` ماه |

`weekday` از شنبه = ۰ تا جمعه = ۶ است.
