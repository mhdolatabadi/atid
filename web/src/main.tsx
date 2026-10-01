import { StrictMode } from 'react';
import { createRoot, hydrateRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './styles/theme.css';
import App from './App.tsx';
import { skyAt } from './lib/sky';

// Links from before clean URLs looked like /#/app/texts/...; send them to the real page.
if (window.location.hash.startsWith('#/')) {
  window.location.replace(window.location.hash.slice(1));
} else {
  // Set before the first paint so the sky starts on the right colours instead of gliding to them.
  document.documentElement.dataset.sky = skyAt(new Date());

  const container = document.getElementById('root')!;
  const app = (
    <StrictMode>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </StrictMode>
  );
  // Built pages arrive pre-rendered and are hydrated; the dev server serves an empty root.
  if (container.hasChildNodes()) {
    hydrateRoot(container, app);
  } else {
    createRoot(container).render(app);
  }
}
