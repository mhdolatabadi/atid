import { describe, expect, it } from 'vitest';
import { saatbashiMessage } from '../saatbashiMessages';

describe('Saatbashi contextual guidance', () => {
  it('keeps the prayer approximation caveat', () => {
    expect(saatbashiMessage('/app/prayer-times')).toContain('تقریبی');
    expect(saatbashiMessage('/prayer-times/tehran')).toContain('تقریبی');
  });
  it('offers relevant guidance on tools and nested reading routes', () => {
    expect(saatbashiMessage('/date-converter')).toContain('معادل');
    expect(saatbashiMessage('/app/calendar')).toContain('مناسبت');
    expect(saatbashiMessage('/app/texts/hadith_kisa')).toContain('بخوان');
  });
  it('introduces the guide on public pages', () => {
    expect(saatbashiMessage('/')).toContain('ساعت‌باشی');
  });
});
