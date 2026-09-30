import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './styles/theme.css';
import App from './App.tsx';
import { skyAt } from './lib/sky';

// Set before the first paint so the sky starts on the right colours instead of gliding to them.
document.documentElement.dataset.sky = skyAt(new Date());

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
