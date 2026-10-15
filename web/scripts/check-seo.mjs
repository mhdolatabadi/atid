// Validate the actual HTML delivered to crawlers, not only metadata source objects.
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
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
  }
}
const missing = await readFile('dist/404.html', 'utf8');
assert.match(missing, /name="robots" content="noindex"/);
console.log(`SEO HTML checks passed for ${prerenderRoutes.length} routes + 404`);

}
