# AGENTS.md

This file defines the standing rules for every contributor and coding agent working on Saatbashi.

## Product identity and scope

- The product name is **Saatbashi** (ساعت‌باشی). The project was first called Atid; the repository, the Android application ID, the Docker/Compose names (`atid`, `atid-web`) and the domain keep the `atid` identifier on purpose. Do not rename them.
- The supported clients are **Android** (`app/`, Kotlin + Jetpack Compose) and **Web** (`web/`, Vite + React + TypeScript). Do not add iOS-specific code or workflows unless the owner explicitly changes this rule.
- The Android application ID is `ir.mhdolatabadi.atid`.
- Keep Web and Android behavior consistent where platform capabilities allow it. Shared logic (Jalali calendar conversion, prayer-time calculation) is ported 1:1 between the two; a fix in one must be mirrored in the other.
- User-facing copy is Persian and the primary layout direction is RTL.

## Issue-first delivery

- Every independently deliverable change starts with a GitHub issue before implementation.
- Break broad requests into focused issues with a clear outcome and acceptance criteria.
- Use a dedicated branch and pull request for each issue. Reference and close the issue from the pull request.
- Do not mix unrelated cleanup or features into the same pull request.
- Keep the issue and pull request updated when scope, risks, or rollout requirements change.

## UI and UX

- The web home page follows the structure of time.ir: today's clock and dates (solar, lunar, Gregorian), a month calendar that shows all three calendars per day with holidays in red, the month's occasions, and today's prayer times.
- Mobile layouts need generous breathing room, safe-area awareness, and touch targets of at least 44 CSS pixels (48dp on Android).
- Floating controls, bottom navigation, browser chrome, and system insets must never cover the last list item or a primary action.
- Long Persian and Arabic text must wrap or truncate gracefully without horizontal overflow.
- Desktop content must use a centered maximum width and must not stick to the viewport edges.
- Support both light and dark color schemes; colors come from the tokens in `web/src/styles/theme.css`, not raw hex in components.
- Preserve accessibility semantics, visible focus, useful labels, and adequate contrast.
- The web version must stay search-engine friendly: clean URLs, every public route pre-rendered by `web/scripts/prerender.mjs`, per-page metadata in `web/src/seo.ts`. Content that depends on the current time or browser storage renders client-only (`ClientOnly`) so static HTML never carries a stale date. Do not move the web client to a framework that renders without HTML (such as Flutter web).
- Before making substantial UI changes, inspect and follow the relevant guidance stored under `.skills/`.

## Dates, calendars and religious data

- Jalali dates use the Julian-day algorithm in `web/src/lib/persianDate.ts` (validated against `jdatetime` for 1925–2100). Do not replace it with ad-hoc arithmetic.
- Lunar (Hijri) dates can differ by a day from Iran's official calendar, which depends on moon sighting. Never present a computed lunar date as authoritative without that caveat.
- Prayer times are approximate (Tehran angle convention). Say so wherever they are shown.
- Religious texts must be reproduced exactly; do not paraphrase or trim them.

## Privacy

- Qada counters and other personal data stay on the device (`localStorage` on the web). Do not add network calls that send them anywhere.
- Location is requested only after an explicit user action, and the app must keep working when it is denied.

## Testing and quality gates

- Add or update tests for every behavior change and regression fix.
- For web changes, run at minimum `npm run lint`, `npm test` and `npm run build` in `web/`.
- For Android changes, run `./gradlew testDebugUnitTest assembleDebug`.
- Calendar and prayer-time logic is covered on both platforms with the same reference values (`web/src/**/__tests__`, `app/src/test`). A change to that logic must update both tests and both ports together.
- UI changes must include a narrow-screen (≈360px) check and must verify that controls do not obscure content.
- Do not merge while required CI checks are failing.

## Deployment and releases

- The web version is deployed to the owner's server through the repository workflow after CI succeeds; avoid undocumented manual server mutations. See `deploy/README.md`.
- Keep secrets in GitHub Actions secrets or the server environment. Never commit them.
- Validate Docker Compose and Caddy changes before deployment.
- The server is shared with other stacks (nafir). `deploy/compose.yaml` must keep `name: atid` and a uniquely named service (`atid-web`) with no published ports; atid is served through the existing reverse proxy. Never run a deploy that could stop, recreate or remove containers outside the `atid` project. Check what owns ports 80/443 before adding anything that binds them.
- Android releases are built by `.github/workflows/release.yml` from a `v*` tag.

## Safe collaboration

- Preserve unrelated user changes and the current repository state.
- Prefer small, reversible changes.
- Diagnose production incidents with read-only checks first.
- Confirm exact destructive targets before deleting server data, Docker volumes, or user content.
- Document operational commands and rollback notes in the relevant issue or pull request.
