/** Shared character voice: warm, factual and never judgmental. */
export function saatbashiMessage(path: string): string {
  if (path.includes('prayer-times')) return 'بیا وقت‌های امروز را ببینیم؛ اوقات شرعی تقریبی‌اند.';
  if (path.includes('date-converter')) return 'یک روز، چند جور تاریخ؛ بیا معادلش را پیدا کنیم.';
  if (path.includes('/home')) return 'قدم‌به‌قدم پیش می‌رویم؛ حساب روزهایت همین‌جا محفوظ است.';
  if (path.includes('calendar')) return 'بیا روزها و مناسبت‌های این ماه را با هم بشناسیم.';
  return 'من ساعت‌باشی‌ام؛ همراه تو برای شناخت روزها و وقت‌ها.';
}
