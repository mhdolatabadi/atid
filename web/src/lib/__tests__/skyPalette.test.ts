/// <reference types="node" />
import { describe, expect, it } from 'vitest';
import { readFileSync } from 'node:fs';

// Palette parity prevents the two clients showing different periods in the same theme.
describe('shared sky identity', () => {
  it('keeps all period gradients distinct and mirrors them in Android', () => {
    const css = readFileSync('src/styles/theme.css', 'utf8');
    const kotlin = readFileSync('../app/src/main/java/ir/mhdolatabadi/atid/ui/theme/Color.kt', 'utf8');
    const gradients = [...css.matchAll(/:root\[data-sky='(night|dawn|day|dusk)'\]\s*\{ --sky-top: #([\da-f]+); --sky-mid: #([\da-f]+); --sky-low: #([\da-f]+)/g)];
    expect(gradients).toHaveLength(8);
    expect(new Set(gradients.map(m => m.slice(2).join(''))).size).toBe(8);
    for (const gradient of gradients) {
      const line = `SkyPeriod.${gradient[1].toUpperCase()} -> SkyPalette(${gradient.slice(2).map(c => `Color(0xFF${c.toUpperCase()})`).join(', ')}`;
      expect(kotlin).toContain(line);
    }
  });
});
