# SEO verification and next work

## Verified in code

- Every exported route is pre-rendered; page-specific title, description, canonical, social metadata and JSON-LD are in the initial HTML.
- Client navigation updates canonical, robots, social metadata and structured data as well as title/description. Personal qada and unknown pages remain noindex; returning to a public page restores indexability.
- Canonicals preserve the public origin from the initial server document. Production `SITE_URL` must be an HTTPS origin, without a path or query.
- Structured data includes a stable site/page identity and breadcrumb hierarchy for indexable pages. The home page also names the publisher (`Organization`) and the Android app (`SoftwareApplication`); guide pages emit their visible FAQ as `FAQPage` from `web/src/data/faq.ts`. No fabricated dates or ratings are added.
- Every build validates the delivered HTML: one title/description/canonical, a heading, valid JSON-LD, FAQ text that is visible on the page, consistent URLs, private-page exclusions, sitemap coverage and a `lastmod` for every sitemap URL.

Run from `web/`:

```bash
npm run lint
npm test
SITE_URL=https://saatbashi.ir npm run build
```

## Production checks still needed

The public robots.txt and sitemap.xml could not be reached from the development environment on 2026-10-15. Build verification does not prove that production serves the latest image or that Google has indexed it.

1. Check the deployed revision, canonical origin, robots.txt, sitemap.xml and real HTTP 404 behavior through the public proxy.
2. In the site's verified Google Search Console property, inspect representative home, city and converter URLs, then submit the sitemap. Inspect exclusion reasons and duplicate canonical reports before changing URLs.
3. Measure field Core Web Vitals and mobile Lighthouse on production. Prioritize measured LCP/INP/CLS bottlenecks rather than adding scripts speculatively.
4. Use actual query and impression data to improve useful Persian content: calendar explanations, date conversion examples and city prayer-time methodology. Keep computed times client-only to avoid freezing “today” in a static build.

## Primary guidance

- https://developers.google.com/search/docs/crawling-indexing/javascript/javascript-seo-basics
- https://developers.google.com/search/docs/appearance/structured-data/breadcrumb
- https://developers.google.com/search/docs/appearance/structured-data/generate-structured-data-with-javascript
