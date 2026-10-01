import { useSyncExternalStore } from 'react';

export type QadaPrayer = 'FAJR' | 'ZOHR' | 'ASR' | 'MAGHRIB' | 'ISHA';

export const QADA_PRAYERS: QadaPrayer[] = ['FAJR', 'ZOHR', 'ASR', 'MAGHRIB', 'ISHA'];

const prefsKeys: Record<QadaPrayer, string> = {
  FAJR: 'qada_fajr',
  ZOHR: 'qada_zohr',
  ASR: 'qada_asr',
  MAGHRIB: 'qada_maghrib',
  ISHA: 'qada_isha',
};

const FAST_KEY = 'qada_fast';

function readCount(key: string): number {
  // The pre-renderer runs in Node, which has no storage; the page shows counts only in the browser.
  if (typeof localStorage === 'undefined') return 0;
  const raw = localStorage.getItem(key);
  const parsed = raw === null ? 0 : Number.parseInt(raw, 10);
  return Number.isFinite(parsed) ? parsed : 0;
}

function writeCount(key: string, value: number): void {
  localStorage.setItem(key, String(Math.max(0, value)));
}

const listeners = new Set<() => void>();

// useSyncExternalStore requires getSnapshot to return a stable (Object.is-equal) reference
// when nothing changed, or it re-renders in a loop; these caches are only replaced on a write.
let countsSnapshot = computeCountsSnapshot();
let fastSnapshot = readCount(FAST_KEY);

function computeCountsSnapshot(): Record<QadaPrayer, number> {
  const counts = {} as Record<QadaPrayer, number>;
  for (const prayer of QADA_PRAYERS) {
    counts[prayer] = readCount(prefsKeys[prayer]);
  }
  return counts;
}

function notify(): void {
  countsSnapshot = computeCountsSnapshot();
  fastSnapshot = readCount(FAST_KEY);
  listeners.forEach((listener) => listener());
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function getSnapshot(): Record<QadaPrayer, number> {
  return countsSnapshot;
}

function getFastSnapshot(): number {
  return fastSnapshot;
}

export function incrementPrayer(prayer: QadaPrayer): void {
  writeCount(prefsKeys[prayer], readCount(prefsKeys[prayer]) + 1);
  notify();
}

export function decrementPrayer(prayer: QadaPrayer): void {
  writeCount(prefsKeys[prayer], readCount(prefsKeys[prayer]) - 1);
  notify();
}

export function incrementFast(): void {
  writeCount(FAST_KEY, readCount(FAST_KEY) + 1);
  notify();
}

export function decrementFast(): void {
  writeCount(FAST_KEY, readCount(FAST_KEY) - 1);
  notify();
}

export function useQadaCounts(): Record<QadaPrayer, number> {
  return useSyncExternalStore(subscribe, getSnapshot);
}

export function useFastCount(): number {
  return useSyncExternalStore(subscribe, getFastSnapshot);
}
