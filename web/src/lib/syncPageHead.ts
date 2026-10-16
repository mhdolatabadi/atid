import { canonicalPath, metaFor, SITE_NAME, structuredDataFor } from '../seo';

/** Keep server and client metadata aligned without replacing the configured public origin. */
export function syncPageHead(pathname: string, doc: Document = document) {
  const path = canonicalPath(pathname);
  const meta = metaFor(path);
  const previousCanonical = doc.querySelector<HTMLLinkElement>('link[rel="canonical"]');
  const origin = new URL(previousCanonical?.href ?? doc.location.origin).origin;
  const url = `${origin}${path}`;
  doc.title = meta.title;
  function tag(attribute: 'name' | 'property', key: string, value: string) {
    let element = doc.querySelector<HTMLMetaElement>(`meta[${attribute}="${key}"]`);
    if (!element) { element = doc.createElement('meta'); element.setAttribute(attribute, key); doc.head.append(element); }
    element.content = value;
  }
  tag('name', 'description', meta.description);
  tag('name', 'robots', meta.noindex ? 'noindex' : 'index, follow');
  tag('property', 'og:site_name', SITE_NAME);
  tag('property', 'og:title', meta.title);
  tag('property', 'og:description', meta.description);
  tag('property', 'og:url', url);
  tag('property', 'og:type', 'website');
  tag('property', 'og:image', `${origin}/og-image.png`);
  tag('name', 'twitter:title', meta.title);
  tag('name', 'twitter:description', meta.description);
  tag('name', 'twitter:image', `${origin}/og-image.png`);
  let canonical = previousCanonical;
  if (!canonical) { canonical = doc.createElement('link'); canonical.rel = 'canonical'; doc.head.append(canonical); }
  canonical.href = url;
  let schema = doc.querySelector<HTMLScriptElement>('script[type="application/ld+json"]');
  if (!schema) { schema = doc.createElement('script'); schema.type = 'application/ld+json'; doc.head.append(schema); }
  schema.textContent = JSON.stringify(structuredDataFor(path, origin));
}
