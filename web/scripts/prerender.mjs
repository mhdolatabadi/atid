// Turns the client build into one static HTML page per route, so search engines and link
// previews get real content and per-page metadata before any JavaScript runs. Runs after
// `vite build` (client, into dist/) and `vite build --ssr` (renderer, into dist-server/).
import { mkdir, readFile, readdir, rm, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { checkSeo } from './check-seo.mjs';
import { fileURLToPath, pathToFileURL } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const dist = join(root, 'dist');
const serverDir = join(root, 'dist-server');

const { render, metaFor, prerenderRoutes, notFoundMeta, SITE_NAME, structuredDataFor } = await import(
  pathToFileURL(join(serverDir, 'entry-server.js')).href
);

// Absolute URLs need the public address; without it canonical/og:url and the sitemap are skipped.
const siteUrl = (process.env.SITE_URL ?? '').replace(/\/+$/, '');
if (siteUrl) {
  const parsed = new URL(siteUrl);
  if (parsed.protocol !== 'https:' || parsed.origin !== siteUrl) {
    throw new Error('SITE_URL must be a public HTTPS origin without path, query, fragment or credentials.');
  }
}
if (!siteUrl) {
  console.warn('prerender: SITE_URL is not set; skipping canonical URLs, og:url and sitemap.xml');
}

const template = await readFile(join(dist, 'index.html'), 'utf8');
const assets = await readdir(join(dist, 'assets'));
const regularFont = assets.find((name) => /^vazirmatn_regular-.*\.woff2$/.test(name));

const escapeHtml = (value) =>
  value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

function headTags(route, meta) {
  const url = siteUrl ? `${siteUrl}${route === '/' ? '/' : route}` : null;
  const tags = [
    meta.noindex ? '<meta name="robots" content="noindex" />' : null,
    url ? `<link rel="canonical" href="${url}" />` : null,
    `<meta property="og:site_name" content="${SITE_NAME}" />`,
    '<meta property="og:locale" content="fa_IR" />',
    `<meta property="og:type" content="${meta.type === 'Article' ? 'article' : 'website'}" />`,
    `<meta property="og:title" content="${escapeHtml(meta.title)}" />`,
    `<meta property="og:description" content="${escapeHtml(meta.description)}" />`,
    url ? `<meta property="og:url" content="${url}" />` : null,
    siteUrl ? `<meta property="og:image" content="${siteUrl}/og-image.png" />` : null,
    siteUrl ? '<meta property="og:image:width" content="1200" />' : null,
    siteUrl ? '<meta property="og:image:height" content="630" />' : null,
    `<meta name="twitter:card" content="${siteUrl ? 'summary_large_image' : 'summary'}" />`,
    `<meta name="twitter:title" content="${escapeHtml(meta.title)}" />`,
    `<meta name="twitter:description" content="${escapeHtml(meta.description)}" />`,
    siteUrl ? `<meta name="twitter:image" content="${siteUrl}/og-image.png" />` : null,
    '<meta property="og:image:alt" content="نشان ساعت‌باشی؛ همراه روزها و وقت‌ها" />',
    regularFont
      ? `<link rel="preload" href="/assets/${regularFont}" as="font" type="font/woff2" crossorigin />`
      : null,
    siteUrl ? `<script type="application/ld+json">${JSON.stringify(structuredDataFor(route, siteUrl)).replace(/</g, '\\u003c')}</script>` : null,
  ];
  return tags.filter(Boolean).join('\n    ');
}

function page(route, meta, html) {
  return template
    .replace(/<title>[\s\S]*?<\/title>/, `<title>${escapeHtml(meta.title)}</title>`)
    .replace(/<meta name="description" content="[^"]*" \/>/, `<meta name="description" content="${escapeHtml(meta.description)}" />`)
    .replace('<!--app-head-->', headTags(route, meta))
    .replace('<!--app-html-->', html);
}

for (const route of prerenderRoutes) {
  const file = route === '/' ? join(dist, 'index.html') : join(dist, route, 'index.html');
  await mkdir(dirname(file), { recursive: true });
  await writeFile(file, page(route, metaFor(route), render(route)));
}

// Served by nginx for unknown paths, with a real 404 status.
await writeFile(join(dist, '404.html'), page('/404', notFoundMeta, render('/__not_found__')));

const indexable = prerenderRoutes.filter((route) => !metaFor(route).noindex);
// The qada page carries noindex; blocking it here would hide that tag from crawlers.
const robots = ['User-agent: *', 'Allow: /'];
if (siteUrl) robots.push(`Sitemap: ${siteUrl}/sitemap.xml`);
await writeFile(join(dist, 'robots.txt'), robots.join('\n') + '\n');

if (siteUrl) {
  const urls = indexable
    .map((route) => `  <url><loc>${siteUrl}${route === '/' ? '/' : route}</loc></url>`)
    .join('\n');
  await writeFile(
    join(dist, 'sitemap.xml'),
    `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${urls}\n</urlset>\n`,
  );
}

await checkSeo({ metaFor, prerenderRoutes, origin: siteUrl });

await rm(serverDir, { recursive: true, force: true });
console.log(`prerender: ${prerenderRoutes.length} pages + 404${siteUrl ? ' + sitemap.xml' : ''}`);
