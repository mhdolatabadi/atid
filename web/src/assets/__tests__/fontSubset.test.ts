import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';

// The Vazirmatn files are subset (scripts/subset-fonts.sh); copy that uses a character outside
// those ranges would silently fall back to another font.
const root = join(__dirname, '..', '..', '..');
const ranges = [...readFileSync(join(root, 'scripts', 'subset-fonts.sh'), 'utf8').matchAll(/U\+([0-9A-F]{4})(?:-([0-9A-F]{4}))?/g)]
  .map(([, from, to]) => [parseInt(from, 16), parseInt(to ?? from, 16)]);

function sources(dir: string): string[] {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    if (statSync(path).isDirectory()) return name === '__tests__' ? [] : sources(path);
    return /\.(tsx?|css)$/.test(name) ? [path] : [];
  });
}

describe('font subset', () => {
  it('covers every character the web copy uses', () => {
    const missing = new Set<string>();
    for (const file of sources(join(root, 'src'))) {
      for (const char of readFileSync(file, 'utf8')) {
        const code = char.codePointAt(0)!;
        if (code > 0x7e && !ranges.some(([from, to]) => code >= from && code <= to)) missing.add(`${char} U+${code.toString(16)} ${file}`);
      }
    }
    expect([...missing]).toEqual([]);
  });
});
