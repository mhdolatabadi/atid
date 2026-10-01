import type { ReactNode } from 'react';
import { useHydrated } from '../lib/hydration';

/** Renders `children` only in the browser after hydration; `fallback` in the static HTML. */
export function ClientOnly({ children, fallback = null }: { children: ReactNode; fallback?: ReactNode }) {
  return useHydrated() ? children : fallback;
}
