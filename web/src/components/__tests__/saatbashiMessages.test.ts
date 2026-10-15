import { describe, expect, it } from 'vitest';
import { saatbashiMessage } from '../saatbashiMessages';

describe('Saatbashi contextual guidance', () => {
  it('keeps the prayer approximation caveat', () => {
    expect(saatbashiMessage('/app/prayer-times')).toContain('تقریبی');
    expect(saatbashiMessage('/prayer-times/tehran')).toContain('تقریبی');
  });
  it('offers relevant guidance on tools', () => {
    expect(saatbashiMessage('/date-converter')).toContain('معادل');
    expect(saatbashiMessage('/app/calendar')).toContain('مناسبت');
  });
  it('introduces the guide on public pages', () => {
    expect(saatbashiMessage('/')).toContain('ساعت‌باشی');
  });
});
