import { describe, expect, it } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { MemoryRouter } from 'react-router-dom';
import { AppShell } from '../AppShell';
import { PublicLayout } from '../SiteChrome';

describe('unobstructed page chrome', () => {
  it('keeps navigation without adding a repeated guide panel', () => {
    const html = renderToStaticMarkup(<MemoryRouter><AppShell /></MemoryRouter>);
    expect(html).toContain('aria-label="بخش‌ها"');
    expect(html).not.toContain('saatbashi-guide');
    expect(html).not.toContain('saatbashi-character.webp');
  });
  it('preserves public content and the compact brand mark', () => {
    const html = renderToStaticMarkup(<MemoryRouter><PublicLayout><main>محتوای صفحه</main></PublicLayout></MemoryRouter>);
    expect(html).toContain('محتوای صفحه');
    expect(html).toContain('/favicon.svg');
    expect(html).not.toContain('saatbashi-guide');
  });
});
