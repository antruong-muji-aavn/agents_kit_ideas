# klara-theme Audit Standards

Authoritative, enforceable rules for klara-theme component audits. Each rule has a unique ID, severity, pass criterion, and fail criterion. Used by epost-muji during `audit --ui` to evaluate web components.

Also used for integration guidance — see `references/guidance.md` for consumer integration consulting workflow.

**Severity scale:**
- `critical` — breaks library contract, theming, or isolation
- `high` — convention violation affecting consistency
- `medium` — quality gap, maintainability concern
- `low` — style preference, minor improvement

---

## Rule Index

### Library Mode (files inside `libs/klara-theme/` or `libs/common/`)

| Category | Human Name | Rules | Count |
|----------|-----------|-------|:-----:|
| KBLOAD | Live KB Load Gate | KBLOAD-001..003 | 3 |
| STRUCT | Component Structure | STRUCT-001..006 | 6 |
| PROPS | Props & Naming | PROPS-001..008 | 8 |
| TOKEN | Token & Style | TOKEN-001..007 | 7 |
| BIZ | Business Isolation | BIZ-001..005 | 5 |
| A11Y | Accessibility | A11Y-001..005 | 5 |
| TEST | Testing & Documentation | TEST-001..004 | 4 |
| SEC | Security (conditional) | SEC-001..005 | 5 |
| PERF | Performance (conditional) | PERF-001..004 | 4 |
| LDRY | Library DRY | LDRY-001..003 | 3 |
| EMBED | Embedded Components | EMBED-001..005 | 5 |
| | | **Total** | **55** |

### Consumer Mode (files in `app/`, `features/`, `pages/` importing from klara-theme)

| Category | Human Name | Rules | Count |
|----------|-----------|-------|:-----:|
| KBLOAD | Live KB Load Gate | KBLOAD-001..003 | 3 |
| INT | Library Integrity (critical gate) | INT-1..3 | 3 |
| PLACE | Component Placement | PL-1..7 | 7 |
| REUSE | Klara-Theme Reuse | RU-1..8 | 8 |
| TW | Tailwind Compliance | TW-1..5 | 5 |
| DRY | DRY Gating | DRY-1..3 | 3 |
| REACT | React Best Practices | RE-1..8 | 8 |
| POC | Production Maturity | POC-1..7 | 7 |
| PP | Consumer Props | PP-E1..E2 | 2 |
| A11Y | Accessibility | A11Y-001..005 | 5 |
| SEC | Security (conditional) | SEC-001..005 | 5 |
| PERF | Performance (conditional) | PERF-001..004 | 4 |
| EMBED | Embedded Components | EMBED-001..005 | 5 |
| | | **Total** | **65** |

---

# Library Mode Rules

Apply when the file under audit lives inside `libs/klara-theme/` or `libs/common/`.

---

## KBLOAD: Live KB Load Gate — Always First

Before applying any rule below, load the current klara-theme standards from the live KB. This ensures audit checks match the current library version, not cached assumptions.

**Steps:**
1. Read `libs/klara-theme/docs/index.json` (klara-theme KB registry — NOT `docs/index.json` at project root)
2. Load **FEAT-0001** → build `componentCatalog: Set<string>` (current component list)
3. Load task-relevant CONV-* entries → extract current naming conventions, token names, required props
4. If component in scope has a FEAT-* entry: load it → treat documented patterns as conventions, not violations
5. If `index.json` missing: fallback to `Glob libs/klara-theme/docs/**/*.md`, then read directly

**Gate**: Do NOT run STRUCT, PROPS, or TOKEN checks until KB loaded (or degraded-mode logged).

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| KBLOAD-001 | `libs/klara-theme/docs/index.json` read and `componentCatalog` populated | critical | Non-empty catalog built from FEAT-0001 | File missing AND no fallback found — log "KB unavailable" |
| KBLOAD-002 | At least one CONV-* entry loaded for audit scope | high | Relevant CONV entries loaded | No CONV entries loaded — flag as coverage gap |
| KBLOAD-003 | Component-specific FEAT-* entry loaded (if component in scope) | medium | FEAT entry found and loaded | No FEAT entry — note "no KB entry" as docs gap; do not block |

---

## STRUCT: Component Structure

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| STRUCT-001 | Component directory exists under `src/lib/components/{kebab-name}/` | critical | Directory exists with kebab-case name | Missing or wrong location |
| STRUCT-002 | Required files present: `.tsx`, `-styles.ts`, `.stories.tsx`, `.test.tsx`, `.figma.json`, `.mapping.json`, `index.ts` | critical | All 7 files present | Any missing (note: `types.ts` optional) |
| STRUCT-003 | `index.ts` barrel exports default + all named types | high | All public types/components exported | Missing exports |
| STRUCT-004 | Component file starts with `'use client'` directive | high | First line is `'use client'` | Missing directive |
| STRUCT-005 | `displayName` set on component | medium | `Component.displayName = 'Name'` present | Missing displayName |
| STRUCT-006 | Compound components split into separate files per sub-component | medium | Each sub-component has own `.tsx` + `-styles.ts` | Monolithic file with multiple components |

---

## PROPS: Props & Naming

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| PROPS-001 | Props interface named `I{PascalName}Props` | high | `IButtonProps`, `IChipProps` | `ButtonProps`, `Props`, unnamed |
| PROPS-002 | Standard vocab props used: `styling`, `mode`, `size`, `radius`, `className`, `id`, `disabled`, `inverse` | high | Uses standard names for these concepts | Custom names for equivalent concepts (e.g., `variant` instead of `styling`) |
| PROPS-003 | Variant consts use `SCREAMING_SNAKE` + `as const` pattern | medium | `BUTTON_STYLING = {...} as const` | Plain enum, no const assertion |
| PROPS-004 | Type derived from const: `type X = (typeof CONST)[keyof typeof CONST]` | medium | Derived type pattern | Manually duplicated union type |
| PROPS-005 | Boolean feature flags typed as `prop?: true` (not `boolean`) | low | `inverse?: true`, `isBold?: true` | `inverse?: boolean` when only truthy is meaningful |
| PROPS-006 | Internal compound props prefixed with `_` | medium | `_size`, `_mode`, `_themeUILabel` | Unprefixed internal props |
| PROPS-007 | JSDoc on every prop with `@default` annotations | medium | All props have `/** */` docs | Missing JSDoc |
| PROPS-008 | Deprecated props use `@deprecated` annotation with migration guidance | high | `@deprecated Use X instead` in JSDoc | Silently removed prop or undocumented deprecation |

---

## TOKEN: Token & Style

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| TOKEN-001 | ALL Tailwind classes in `*-styles.ts` file, never inline in component | critical | Styles in dedicated file | Tailwind strings in `.tsx` component body |
| TOKEN-002 | `clsx()` for all conditional class assembly | high | `clsx(base, variant && modifier)` | Template literals or string concatenation |
| TOKEN-003 | Semantic color tokens only: `bg-base-*`, `text-base-*`, `signal-*`, `alternate-*` | critical | Only semantic Tailwind classes | Raw colors (`bg-blue-500`), hex values, `rgb()` |
| TOKEN-004 | Size tokens: `size-theme-*`, `px-size-padding`, `gap-size-spacing` for theme dimensions | high | Design scale values | Raw px values (`px-4`, `w-32`) for theme-managed dimensions |
| TOKEN-005 | Variant maps use `Map<string, string>` pattern | medium | `new Map([['primary', '...'], ...])` | Plain object or switch statement for variant-to-class mapping |
| TOKEN-006 | Components use `--color-theme-base-*` vars, never `--color-base-*` directly | high | Theme-tier CSS vars | Base-tier CSS vars bypassing theme layer |
| TOKEN-007 | State layer via shared utility (`STATE_LAYER`), not per-component | medium | Imports from `_utils/state-layer/` | Custom hover/focus/pressed/disabled classes |

---

## BIZ: Business Isolation

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| BIZ-001 | No domain/business types in component | critical | Only UI types (string, number, React types) | Types referencing business entities (Letter, Email, Survey) |
| BIZ-002 | No API calls or data fetching | critical | No fetch, axios, useSWR, useQuery | Any network call in component |
| BIZ-003 | No state management beyond local UI state | critical | Only useState, useRef for local UI | Redux, Zustand, global stores imported |
| BIZ-004 | Theming via BrandedWrapper/InverseWrapper/ThemedBox only | high | Uses provided wrapper components | Custom CSS var injection, manual theme switching |
| BIZ-005 | No app-layer lifecycle artifacts in component dir | medium | Only library files in directory | `CHANGELOG.md`, development guides, app-specific docs |

---

## A11Y: Accessibility

**Delegation rule**: A11Y findings require WCAG expertise beyond the surface checks below.
- **Standalone audit** (not a sub-agent): After collecting violations, delegate A11Y review to **epost-a11y-specialist** via `/audit --a11y`. Pass `finding_ids` from this section. Also use epost-a11y-specialist for integration guidance questions involving keyboard nav, screen readers, or contrast.
- **As sub-agent** (dispatched via Agent tool): Collect all A11Y violations in `## A11Y Findings (for escalation)` section with `finding_id`, `rule_id`, `file:line`, `issue`. The calling agent (code-reviewer) handles delegation.

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| A11Y-001 | `theme-ui-label` attribute on root element | high | `theme-ui-label="component-name"` present | Missing attribute |
| A11Y-002 | Auto-ID via `useId()` with consumer override | high | `const id = useId(); <el id={props.id \|\| id}>` | Hardcoded or missing IDs |
| A11Y-003 | Radix UI for complex interactive primitives | high | Accordion, Tabs, Dialog, Tooltip use Radix | Rolling own keyboard handling for complex widgets |
| A11Y-004 | Standard focus ring via STATE_LAYER utility | medium | `focus-visible:outline focus-visible:outline-focus` from shared utility | Custom focus styles |
| A11Y-005 | Disabled state uses `opacity-disabled pointer-events-none` token | medium | Semantic disabled token | Raw opacity values or custom disabled styling |

---

## TEST: Testing & Documentation

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| TEST-001 | Test file exists: `*.test.tsx` | high | File present with at least "renders without crashing" | Missing test file |
| TEST-002 | Stories file exists: `*.stories.tsx` with `tags: ['autodocs']` | high | Story with meta, argTypes, at least Base story | Missing stories |
| TEST-003 | Standard test coverage: render, props, disabled, interaction, className passthrough | medium | Tests for major prop combos + interactions | Only smoke test |
| TEST-004 | `.figma.json` and `.mapping.json` present and non-empty | medium | Valid JSON with nodeId and prop mappings | Missing or empty Figma artifacts |

---

## SEC: Security — Conditional

**Activation gate**: Component imports fetch/axios/localStorage OR props include URL/apiKey/endpoint OR imports AI SDK. Skip if none match.
**Standalone-component exception**: Pure presentational component with no network, no storage, no external API surface → skip SEC entirely. BIZ rules already enforce isolation.

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| SEC-001 | No API keys/secrets in localStorage/sessionStorage/hardcoded strings | critical | Credentials via env vars or secure store only | API key in localStorage, hardcoded secret string |
| SEC-002 | External/AI data validated before type casting — no bare `as X` on unvalidated JSON | high | Runtime validation (zod, io-ts, type guard) before cast | `response.data as MyType` without validation |
| SEC-003 | External endpoint must have origin allowlist or server-side proxy | high | Fetch routed through API route or allowlisted origins | Raw client-side fetch to user-supplied URL |
| SEC-004 | `javascript:` scheme rejected in any URL-accepting prop or string builder | high | URL sanitized or scheme-checked before use | href/src prop accepts arbitrary string without scheme check |
| SEC-005 | API credentials via headers, not query params | high | Auth in Authorization header or cookie | `?apiKey=xxx` in URL |

---

## PERF: Performance — Conditional

**Activation gate**: 10+ files in scope OR any file >300 LOC. Skip if neither.
**Standalone-component exception**: Single isolated component <300 LOC → skip PERF-001 through PERF-003. Apply PERF-004 always.

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| PERF-001 | Component files <=500 LOC | medium | Under 500 lines | File exceeds 500 LOC — decompose |
| PERF-002 | Hook files <=400 LOC | medium | Under 400 lines | File exceeds 400 LOC — split |
| PERF-003 | Expensive computations wrapped in useMemo/useCallback | medium | Memoized where needed | Heavy computation in render path without memoization |
| PERF-004 | Mock/demo data not in production index.ts | high | Mock data in .stories.tsx or __tests__/ only | >100 lines mock data exported from production module |

---

## LDRY: Library DRY

**Scope note**: LDRY applies to the component directory and immediate dependencies. For a single standalone component with no `_utils/` subdirectory, only LDRY-003 (POC maturity) applies. LDRY-001 and LDRY-002 require at least 2 files in scope to be meaningful.

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| LDRY-001 | No identical utility function bodies in 2+ files | medium | Shared utils in _utils/ | Same function body copy-pasted across files |
| LDRY-002 | No identical type definitions in 2+ files | medium | Shared types in _types/ | Same interface/type duplicated across files |
| LDRY-003 | POC maturity: no console.log, TODO, hardcoded URLs, commented-out blocks in library code | high | Clean production-ready code | POC artifacts left in library source |

---

## EMBED: Embedded Components

When a component renders other components internally, verify those components are library-approved — not overriding library internals or importing external UI libs.

**RAG lookup**: Before running EMBED checks, query RAG for known embedded component patterns:
1. `ToolSearch("web-rag")` → discover `mcp__web-rag-system__*` tools
2. Call `query` with "{component-name} embedded components tokens used"
3. If RAG unavailable: `Grep libs/klara-theme/src/lib/components/{component}/ --glob "*.tsx" --pattern "^import"`

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| EMBED-001 | All embedded components are from `klara-theme` or `libs/common/` — no external UI libs (MUI, Ant Design, etc.) | critical | Only library-internal or lib-approved components embedded | External UI library component inside klara component |
| EMBED-002 | Embedded components used via their public API (props) only — no direct DOM manipulation of embedded component internals | critical | Embedded component API used as documented | `document.querySelector` on embedded component's DOM |
| EMBED-003 | Embedded component tokens/variants match current library catalog (verified via RAG/KB) | high | Token names and variant values match FEAT-* entry for embedded component | Hardcoded variant string that no longer exists in embedded component API |
| EMBED-004 | No overriding embedded component styles via `!important`, direct class injection, or CSS targeting internal structure | high | Style customization via documented `className`/`style` props only | `.wrapper .embedded-component__internal { }` |
| EMBED-005 | Children slots use types declared by the parent — no arbitrary JSX passed where a specific type is expected | medium | Children match documented slot types | Arbitrary JSX passed to a typed slot prop |

---

# Consumer Mode Rules

Apply when the file under audit lives in `app/`, `features/`, `pages/`, or any path that *imports from* `klara-theme` but does not live inside it.

---

## INTEGRITY: Library Integrity — Critical Gate

Runs before all other consumer checks. If any INT-1 or INT-2 violation is found, set `block: true` and stop the audit.

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| INT-1 | No direct edits to `klara-theme/` or `common/` library files from consumer code | critical | Consumer code never modifies library source files | Any `klara-theme/` or `common/` file modified by consumer PR/commit |
| INT-2 | No copy-paste of library component source into consumer code | critical | Consumer code imports from klara-theme; does not duplicate it | Block of code copied verbatim from library source |
| INT-3 | No wrapping library components with non-composable style overrides that break theme | warning | Overrides use provided className/style props or composition patterns | Direct DOM class injection, `!important` hacks on library internals |

---

## PLACE: Component Placement

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| PL-1 | Feature components in `features/<name>/components/`, not root `components/` | high | Component lives under its feature directory | Component in root `components/` but contains feature-specific logic |
| PL-2 | Shared cross-feature components in `components/` with no feature imports | high | Shared component has zero imports from any `features/` path | Shared component imports from a specific feature |
| PL-3 | Page-level components in `app/` or `pages/`, not in `components/` | medium | Route/page components live in `app/` or `pages/` directories | Page component placed in `components/` |
| PL-4 | No business logic in presentational components | high | Presentational components accept data via props; no API calls, no store access | Presentational component calls API or reads from Redux |
| PL-5 | Container/presenter split respected — containers fetch/transform, presenters render | medium | Clear separation between data layer and render layer | Mixed file does both fetching and complex rendering |
| PL-6 | No circular imports between feature modules | critical | Import graph is a DAG; no cycles | Feature A imports Feature B which imports Feature A |
| PL-7 | Index exports present for all public-facing component directories | medium | `index.ts` barrel file in every component directory that is imported externally | External code imports deep paths like `features/x/components/Button/Button.tsx` |

---

## REUSE: Klara-Theme Reuse

Absence of a klara equivalent is a **violation**, not a contribution opportunity. If klara provides the component, use it.

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| RU-1 | Button variants — use `<Button variant="...">` not custom button divs or styled anchors | high | All click/submit interactions use klara `Button` | Custom `<div onClick>` or `<a>` styled as button |
| RU-2 | Form inputs — use klara `Input`/`Select`/`Checkbox`/`Radio`, not raw `<input>`/`<select>` | high | All form fields use klara input components | Raw HTML form elements without klara wrapper |
| RU-3 | Modal/Dialog — use klara `Modal` or `Dialog`, not custom overlay divs | high | All modal surfaces use klara modal primitives | Custom `position: fixed` overlay or portal implementation |
| RU-4 | Typography — use klara `Text`/`Heading` components, not raw `<p>`/`<h1>`–`<h6>` with manual styles | medium | Text rendered via klara typography components | Raw HTML tags with hardcoded font/size classes |
| RU-5 | Icons — use klara `Icon` component, not inline SVG or `<img>` tags for icons | medium | All icons via klara Icon with named variant | Inline `<svg>` or `<img src="icon.png">` for UI icons |
| RU-6 | Loading states — use klara `Spinner`/`Skeleton`, not custom loader divs or CSS animations | medium | All loading feedback uses klara loading components | Custom spinning CSS, DIY skeleton shimmer |
| RU-7 | Toast/notification — use klara Toast system, not custom notification divs | high | All user feedback toasts use klara toast | Custom notification implementation with manual positioning |
| RU-8 | Table/list — use klara `Table`/`List` if the data display matches component capability | low | Data tables use klara Table; simple lists use klara List | Custom table/list when klara equivalent covers the use case |

---

## TW: Tailwind Compliance

Parse `tailwind.config.ts` before running this section. Extract theme scale values to validate against.

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| TW-1 | No arbitrary values `[123px]` when an equivalent theme scale value exists | high | Tailwind classes use theme-defined scale (`p-4` not `p-[16px]`) | Arbitrary bracket values when config defines the equivalent token |
| TW-2 | No arbitrary colors `[#ff0000]` or `[rgb(...)]` when a design token exists | critical | All colors via semantic token classes | Arbitrary color values in class list |
| TW-3 | No `style={}` inline styles when an equivalent Tailwind class exists | high | Styling via className only | `style={{ marginTop: '8px' }}` when `mt-2` exists |
| TW-4 | Layout — use flex/grid patterns; flag `absolute`/`fixed` used as layout hacks | medium | Positioning used for overlays/modals only; standard layout via flex/grid | `absolute` used to align sibling elements instead of flex gap |
| TW-5 | No `!important` via `!` prefix unless in a documented override scenario | high | No `!` prefix classes | `!text-red-500`, `!mt-0` without documented justification |

---

## DRY: DRY Gating

Scan the **whole feature directory** before running REUSE checks. Patterns found in 2+ files are conventions, not violations.

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| DRY-1 | If a UI pattern appears in 2+ files in the feature, treat it as an established convention — suppress downstream REUSE flags | info | Pattern recognized as convention; REUSE finding suppressed with note | REUSE flag raised on a pattern used consistently across the feature |
| DRY-2 | Repeated style combinations applied 3+ times → extract to a shared component or utility class | medium | Common style combos extracted | Same multi-class string repeated 3+ times across files |
| DRY-3 | Repeated logic hooks duplicated in 2+ files → extract to a shared hook | medium | Common logic in a shared hook | Hook body copy-pasted between components |

---

## REACT: React Best Practices

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| RE-1 | No inline object/array literals in JSX props — causes unnecessary re-renders | high | Objects/arrays defined outside render or memoized | `<Comp style={{ color: 'red' }}>` or `<Comp items={[...]}>`  in render |
| RE-2 | `useEffect` dependencies are complete and minimal — no missing or over-specified deps | high | Effect deps match all values referenced inside | ESLint exhaustive-deps violations; stale closure bugs |
| RE-3 | No `useState` for values derivable from props or other state | medium | Derived values computed via `useMemo` or inline expression | `useState` initialized from a prop, never independently updated |
| RE-4 | All list renders have stable, unique `key` props — no index keys on dynamic lists | high | Keys are stable identifiers (ID, slug) | `key={index}` on a list that can reorder or filter |
| RE-5 | Prop drilling beyond 2 levels — suggest context or composition instead | medium | Deep data passed via context or component composition | Same prop threaded through 3+ component layers |
| RE-6 | Large components (>200 lines) — suggest decomposition | medium | Component files under 200 lines of JSX/logic | Single file >200 lines mixing concerns |
| RE-7 | No direct DOM manipulation — use React state and refs correctly | high | DOM interaction via `ref.current` only when unavoidable | `document.querySelector` or `document.getElementById` inside component |
| RE-8 | Error boundaries present around async data components | high | Async data components wrapped in `ErrorBoundary` | No error boundary around components that fetch or throw |

---

## POC: Production Maturity

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| POC-1 | No hardcoded API URLs or environment-specific strings | critical | All URLs from environment variables or config | `"https://api.staging.example.com"` literal in code |
| POC-2 | No `console.log`, `console.error`, `debugger` statements | high | Clean console — logging via structured logger only | `console.log('DEBUG:', data)` left in production code |
| POC-3 | No `TODO`/`FIXME`/`HACK` comments — file issues instead | medium | No inline TODO markers | `// TODO: fix this later` in source |
| POC-4 | No placeholder text (`Lorem ipsum`, `test123`, `fake@email.com`) | high | All visible strings are real or internationalized | Placeholder content visible in UI |
| POC-5 | No commented-out code blocks longer than 3 lines | medium | Dead code removed, not commented | Multi-line commented blocks left in source |
| POC-6 | No `any` TypeScript type overuse — max 1 per file, documented with justification | high | Types are specific; `any` used sparingly with comment | `as any`, `any[]`, `: any` repeated throughout file |
| POC-7 | All async operations have error handling | critical | Every `async/await` wrapped in try/catch or `.catch()` | `await fetch(...)` with no error handler |

---

## Consumer Props Enhancements

Additional PROPS rules that apply when auditing consumer code:

| Rule ID | Rule | Severity | Pass | Fail |
|---------|------|----------|------|------|
| PP-E1 | All required props are passed — scan the component's TypeScript Props interface and flag any required prop missing at call sites | high | All non-optional props provided at every usage | Required prop omitted at a call site |
| PP-E2 | Props are passed with correct types — no string-where-number, no wrong enum value | high | Prop types match interface at all call sites | `<Comp count="5">` where `count: number` |

---

# Mode Applicability

| Section | Library Mode | Consumer Mode | Notes |
|---------|-------------|---------------|-------|
| KBLOAD | Y | Y | Always first — blocks STRUCT/PROPS/TOKEN until loaded |
| INTEGRITY | — | Y | Consumer only — blocks on direct library edits |
| STRUCT, PROPS, TOKEN, BIZ | Y | — | Library only |
| A11Y | Y | Y | Both modes; delegate to epost-a11y-specialist for full WCAG |
| TEST | Y | — | Library only (figma artifacts, stories) |
| PLACE | — | Y | Consumer only |
| REUSE | — | Y | Consumer only |
| TW | Y | Y | Both modes parse tailwind.config.ts |
| DRY | — | Y | Consumer only; gates REUSE false positives |
| REACT | — | Y | Consumer only |
| POC | — | Y | Consumer only (LDRY-003 covers POC for library code) |
| SEC | Y (conditional) | Y | Skip for standalone presentational components |
| PERF | Y (conditional) | Y | Skip PERF-001–003 for standalone component <300 LOC |
| LDRY | Y | — | Library only; LDRY-003 only for single standalone component |
| EMBED | Y | Y | Both modes; RAG lookup required for EMBED-003 |

**Mode detection**: file inside `libs/klara-theme/` or `libs/common/` → Library mode. File importing from those paths but living in `app/`, `features/`, `pages/` → Consumer mode.

---

# Consumer Scoring Formulas

```
placementScore    = (passed_PL_rules / total_PL_rules) * 10
reuseRate         = (klara_components_used / total_reusable_ui_elements) * 10
twComplianceRate  = (classes_using_project_tokens / total_tw_classes) * 10
reactScore        = (passed_RE_rules / total_RE_rules) * 10
pocScore          = (total_POC_rules - poc_indicator_count) / total_POC_rules * 10
```

Update denominators when rules are added/removed.

---

# Anti-Patterns

Known violations from production component analysis:

| Anti-Pattern | Description | Rule Violations |
|-------------|-------------|-----------------|
| Internal subdirectory tree | `_constants/`, `_hooks/`, `_types/`, `_utils/` inside component dir = app-layer structure, not library | BIZ-005 |
| Domain type leak | Importing `ElementType`, `ScreenMode`, `Letter` from app layer | BIZ-001 |
| Inline styles in TSX | Tailwind classes or style objects mixed into component body | TOKEN-001 |
| Separate CHANGELOG | `CHANGELOG.md` in component dir implies separate versioning lifecycle | BIZ-005 |
| Standalone app files | `*-app.tsx` files inside component dir — organisms, not reusable components | STRUCT-002, BIZ-005 |
| Non-semantic colors | `bg-blue-500`, `text-[#FF0000]`, inline `style={{ color: '...' }}` | TOKEN-003 |
| Raw sizing | `px-4`, `w-32`, `h-8` for theme-managed dimensions | TOKEN-004 |
| Custom state styles | Per-component `hover:bg-gray-100` etc. instead of STATE_LAYER | TOKEN-007 |
