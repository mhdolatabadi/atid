// Validate the actual HTML delivered to crawlers, not only metadata source objects.
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const escapeHtml = (value) =>
  value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

export async function checkSeo({ metaFor, prerenderRoutes, origin }) {
const sitemap = origin ? await readFile('dist/sitemap.xml', 'utf8') : '';
for (const path of prerenderRoutes) {
  const html = await readFile(`dist${path === '/' ? '' : path}/index.html`, 'utf8');
  const meta = metaFor(path);
  assert.equal([...html.matchAll(/<title>/g)].length, 1, `one title: ${path}`);
  assert.equal([...html.matchAll(/<meta name="description"/g)].length, 1, `one description: ${path}`);
  assert.match(html, /<h1[\s>]/, `heading in server HTML: ${path}`);
  assert.equal(/<meta name="robots" content="noindex"/.test(html), !!meta.noindex, `robots: ${path}`);
  if (origin) {
    const url = `${origin}${path}`;
    assert.equal([...html.matchAll(/rel="canonical"/g)].length, 1, `one canonical: ${path}`);
    assert.ok(html.includes(`rel="canonical" href="${url}"`), `canonical target: ${path}`);
    assert.ok(html.includes(`property="og:url" content="${url}"`), `social URL: ${path}`);
    const blocks = [...html.matchAll(/<script type="application\/ld\+json">([\s\S]*?)<\/script>/g)];
    assert.equal(blocks.length, 1, `one schema graph: ${path}`);
    const schema = JSON.parse(blocks[0][1]);
    assert.equal(schema['@graph'][0].url, url, `schema URL: ${path}`);
    assert.equal(sitemap.includes(`<loc>${url}</loc>`), !meta.noindex, `sitemap coverage: ${path}`);
    for (const faq of schema['@graph'].filter((node) => node['@type'] === 'FAQPage')) {
      for (const { name, acceptedAnswer } of faq.mainEntity) {
        assert.ok(html.includes(escapeHtml(name)) && html.includes(escapeHtml(acceptedAnswer.text)), `FAQ visible: ${path}`);
      }
    }
  }
}
if (origin) {
  const urls = sitemap.match(/<url>/g)?.length ?? 0;
  assert.equal(sitemap.match(/<lastmod>\d{4}-\d{2}-\d{2}<\/lastmod>/g)?.length ?? 0, urls, 'sitemap lastmod');
}
const missing = await readFile('dist/404.html', 'utf8');
assert.match(missing, /name="robots" content="noindex"/);
console.log(`SEO HTML checks passed for ${prerenderRoutes.length} routes + 404`);

}
