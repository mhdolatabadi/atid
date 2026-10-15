#!/usr/bin/env bash
# Re-creates the subset Vazirmatn files in src/assets/fonts from the upstream release
# (Vazirmatn v33.003, fonts/webfonts/Vazirmatn-{Regular,Medium,SemiBold,Bold}.woff2).
# Keeps Basic Latin, Latin-1, Arabic and its presentation forms, general punctuation and arrows:
# everything the Persian UI uses, about a third smaller than the full fonts.
# Needs fonttools and brotli: python3 -m pip install fonttools brotli
# Usage: scripts/subset-fonts.sh <directory with the upstream .woff2 files>
set -euo pipefail
src="$1"
out="$(dirname "$0")/../src/assets/fonts"
for pair in Regular:regular Medium:medium SemiBold:semibold Bold:bold; do
  python3 -m fontTools.subset "$src/Vazirmatn-${pair%%:*}.woff2" \
    --unicodes='U+0020-007E,U+00A0-00FF,U+0600-06FF,U+2000-206F,U+2190-21FF,U+2212,U+FB50-FDFF,U+FE70-FEFF' \
    --layout-features='*' --flavor=woff2 --output-file="$out/vazirmatn_${pair##*:}.woff2"
done
