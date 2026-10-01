import { StrictMode } from 'react';
import { renderToString } from 'react-dom/server';
import { StaticRouter } from 'react-router-dom';
import App from './App.tsx';

export { metaFor, prerenderRoutes, notFoundMeta, SITE_NAME } from './seo';
export { getReligiousTextById } from './data/religiousTexts';

/** Renders one route to static HTML for scripts/prerender.mjs. */
export function render(url: string): string {
  return renderToString(
    <StrictMode>
      <StaticRouter location={url}>
        <App />
      </StaticRouter>
    </StrictMode>,
  );
}
