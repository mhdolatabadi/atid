import { useSyncExternalStore } from 'react';

const noopSubscribe = () => () => {};

/**
 * False while pre-rendering and during the first client render (hydration), true afterwards.
 * Anything that depends on the current time or on this browser's storage waits for it, so the
 * static HTML never carries a stale date and hydration never mismatches.
 */
export function useHydrated(): boolean {
  return useSyncExternalStore(
    noopSubscribe,
    () => true,
    () => false,
  );
}
