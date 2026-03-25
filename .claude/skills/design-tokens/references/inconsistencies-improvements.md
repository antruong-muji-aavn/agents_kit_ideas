# Complexities, Inconsistencies & Improvements

> **Context**: Analysis of the Vien 2.0 Figma variables system (1,059 variables, 42 collections).
> **Date**: 2026-02-03

---

## Complexities

Six structural complexities that are inherent to the architecture and affect tooling, onboarding, and maintainability.

### C1. Chain Depth — Up to 11 Hops

The maximum reference chain depth is **11 hops** (from Component Category down to Brand Settings). While this enables the "change once, cascade everywhere" philosophy, it creates:

- **Debugging difficulty**: Tracing why a button is the wrong color requires following 11 references across 11 different collections.
- **Performance risk**: Any Figma-to-code generator must resolve the full chain to produce a final value.
- **Cognitive load**: New designers must understand the full chain to know where to make changes.

**Evidence**: The button background chain passes through `12. Component Category` -> `┈ Variant / CTA` -> `┈ Surface / Primary` -> `10. Platform Theme` -> `9. Platform` -> `┈ Inverse` -> `1. Primitives` -> `3. Brand Sets` -> `4. Brands Groups` -> `4.1 Brands Group 1` -> `2. Brand Settings`.

### C2. Mode Explosion — 200+ Distinct Modes

The system defines **200+ distinct modes** across all collections:

| Collection | Mode Count |
|-----------|-----------|
| ┈ Avatar Scale | 9 |
| ┈ Size Scale | 7 |
| 15. Component State | 7 |
| 12. Component Category | 7 |
| 18. Responsive | 7 |
| 9. Platform | 2 |
| 2. Signal | 2 |
| ┈ Inverse | 2 |

Combined, these produce a theoretical **4,802 combinations** per variable. Tooling must handle mode composition correctly or risk generating incomplete or incorrect output.

### C3. Primitives Dominance — 47.6% in One Collection

The `1. Primitives` collection holds **505 of 1,059 variables** (47.6%). This concentration means:

- Nearly half the system is in a single flat namespace.
- Any refactoring of Primitives affects the majority of reference chains.
- Search and filtering within Primitives is harder than in smaller, scoped collections.

### C4. Hub Concentration on Platform

The `9. Platform` collection (132 variables) is the **central routing hub** — nearly all chains from Layer 4 (Components) pass through it to reach Layer 1 (Primitives). This creates a single point of structural dependency:

- Changes to Platform variable names break upstream and downstream references.
- The 82 identical Web/Mobile variables (62.1%) add navigational noise without adding differentiation.

### C5. External Library Coupling — 28.3% Remote References

Of all references in the system, **2,064 (28.3%) point to external Figma libraries**. These external libraries are:

- Not version-controlled alongside the internal variables.
- Subject to independent updates that could break chains.
- Not visible in a standalone export without explicit remote reference resolution.

**Impact**: A Figma-to-code pipeline must either resolve remote references at export time or maintain a separate mapping of external library values.

### C6. Redundant Platform Variables — 82 Identical Pairs

Of the 132 Platform variables, **82 have identical values** for Web and Mobile modes. These exist solely to provide a uniform API surface, but they:

- Inflate the variable count by ~7.7%.
- Make it harder to identify which variables actually differ per platform.
- Require maintenance of "pass-through" variables that carry no differentiated information.

---

## Inconsistencies

Eight naming and structural inconsistencies found in the variable system.

### I1. "FIeld" Typo — 24 Variables Affected

The collection `┈ Variant / Field` and related field variables use the misspelling **"FIeld"** (capital I instead of lowercase i) in some contexts.

**Affected variables** (24 across Component Category and Input Validation):

- `12. Component Category/main-layer.background`
- `12. Component Category/main-layer.foreground`
- `12. Component Category/main-layer.border`
- `12. Component Category/state-layer.background`
- `12. Component Category/main-layer.border-weight`
- `12. Component Category/main-layer.separator`
- `16. Input Validation/input.separator` (6 references)
- `16. Input Validation/input.border`
- `16. Input Validation/input.border-weight`
- `16. Input Validation/input.foreground`
- `16. Input Validation/input.background`
- `16. Input Validation/toggle.border`

**Impact**: Automated tooling that pattern-matches on "Field" will miss "FIeld" variants, creating silent failures in code generation.
**Fix**: Rename all instances of "FIeld" to "Field".

### I2. Double-Space After ┈ — 4 Tree-Related Collections

Four collections use **double-space** after the `┈` prefix (U+2448) instead of the standard single-space:

- `┈  Content / Toggle` (double-space)
- `┈  Content / Tree Level` (double-space)
- `┈  Mode / Tree Item` (double-space)
- `┈  Variant / Tree` (double-space)

Compared to the standard: `┈ Mode / Toggle`, `┈ Variant / CTA` (single-space).

All four are tree-related collections — likely a copy-paste artifact.

**Affected**: 4 collection names.
**Impact**: String matching on collection names breaks if the delimiter pattern is assumed to be `┈ ` (┈ + single space).
**Fix**: Normalize all `┈` prefixes to use a single space.

### I3. Mixed Separators — 460 Variables

Variable names mix **dots** and **dashes** as separators without a consistent rule:

| Pattern | Example | Count (approx.) |
|---------|---------|-----------------|
| Dots only | `color.signal.default.background` | ~350 |
| Dashes in compound words | `font-size`, `line-height`, `brand-color-2` | ~460 |
| Dots + dashes mixed | `typography.font-size.body.1` | ~250 |

**Specific examples of mixed separators**:
- `color.effect.overlay-background`, `color.effect.blur-background`
- `color.effect.gradient-background-start`, `color.effect.gradient-background-stop`
- `color.base.branded-theme`
- `typography.line-height.display.1` through `.6`
- `border.outer-distance`, `border.outer-radius`
- `avatar.container-negative`
- `body-dense.1` through `.6`
- `breakpoints.{size}.column-size`, `breakpoints.{size}.padding-negative`
- `color-adjustment.{variant}.{number}` (all 60+ variations)

**Impact**: Parsing variable names into a token hierarchy requires handling both separators, and the boundary between "hierarchy level" (dot) and "compound word" (dash) is implicit, not declared.
**Fix**: Formalize a naming spec: dots = hierarchy, dashes = compound words within a level.

### I4. Content/ Spacing — Inconsistent Slash Spacing

Collection names under the `┈ Content /` namespace have **inconsistent spacing** around the slash:

- `┈ Content / Dropdown` — space before and after slash
- `┈ Content /Text` — occasionally missing trailing space (observed in some exports)

**Affected**: Content sub-system collection names.
**Impact**: Minor, but breaks exact-match lookups in tooling.
**Fix**: Standardize to `┈ Content / {Name}` (space on both sides of slash).

### I5. Numbered vs ┈ Distinction Blurred

The system uses **numbered prefixes** (e.g. `1.`, `9.`, `12.`) for "system" collections and `┈` for "sub-system" collections. However, two `┈`-prefixed collections are clearly **system-level** infrastructure, not component sub-systems:

| Collection | Actual Role | Expected Prefix |
|-----------|------------|----------------|
| `┈ Inverse` (32 vars) | Theme inversion — Layer 2 semantic | Should be numbered (e.g. `3.5. Inverse`) |
| `┈ Size Scale` (90 vars) | Relative sizing — Layer 3 scaling | Should be numbered (e.g. `8.5. Size Scale`) |

**Impact**: The ┈ prefix implies these are small component-specific overrides, but they are actually core system layers with 32 and 90 variables respectively.
**Fix**: Either number these collections or document the naming exception explicitly.

### I6. Single-Variable Collections — 6 Collections

Six collections contain **only 1 variable** each:

| Collection | Variable Count |
|-----------|----------|
| `7. Border Weight` | 1 |
| `┈ Content / Text` | 1 |
| `┈ Content / Leading` | 1 |
| `┈ Content / Trailing` | 1 |
| `┈ Content / Select Mark` | 1 |
| `┈ Variant / Tree` | 1 |

**Impact**: These create collection overhead (metadata, mode definitions) for a single value. They fragment the namespace and increase cognitive load when browsing the collection list.
**Fix**: Consider consolidating into a single `┈ Content` collection with sub-keys, or document why the 1:1 collection-to-variable ratio is intentional.

### I7. Repeated Collections in Chains — 210 Variables

**210 variables** have reference chains where the **same collection appears more than once**. For example, a chain might pass through `1. Primitives` -> `9. Platform` -> `1. Primitives` -> `1. Primitives` (bouncing back).

**Example**: `┈ Size Scale / l.font-size-sub` — chain includes `1. Primitives -> 9. Platform -> 1. Primitives -> 1. Primitives`.

**Affected**: 210 variables (19.8% of all variables).
**Impact**: This creates circular-looking chains (though they resolve to different variables within the same collection). It complicates chain visualization and can confuse dependency analysis tools.
**Fix**: Audit whether these re-entries are architecturally necessary or if intermediate references can be simplified.

### I8. "Info 2" Naming — Inverts Visual Hierarchy

The signal system uses `info` and `info-2` as variable names:

- `color.signal.info.*` — primary informational color
- `color.signal.info-2.*` — secondary informational color

The `info-2` suffix **inverts typical naming hierarchy** where numbers usually indicate levels (e.g. `heading-1` is larger than `heading-2`). In this case, `info-2` is a variant, not a level.

**Affected**: Info signal variables.
**Impact**: Developers may assume `info-2` is a weaker/smaller version of `info`, when it's actually just an alternative.
**Fix**: Rename to `info-alt`, `info-secondary`, or document the convention.

---

## Improvement Possibilities

14 recommendations across architecture, naming, and tooling.

### Architecture (5)

#### A1. Flatten Deep Chains (Target: Max 7 Hops)

**Current**: Max chain depth is 11 hops.
**Recommendation**: Audit chains deeper than 7 hops and evaluate whether intermediate layers can be collapsed. Specifically:
- The `Brand Sets -> Brands Groups -> Brands Group 1 -> Brand Settings` tail (4 hops) could potentially be pre-resolved into a single `Brand` reference.
- The `Surface / Primary -> Platform Theme -> Platform` sequence (3 hops) might collapse to 2.

**Benefit**: Faster resolution, easier debugging, simpler tooling.
**Risk**: Reduces the granularity of cascade control at the brand level.

#### A2. Split Primitives into Sub-Collections

**Current**: 505 variables in one flat collection.
**Recommendation**: Split into domain-specific sub-collections:

| Proposed Collection | Variables (approx.) |
|--------------------|-------------------|
| `1a. Primitives / Color` | ~147 |
| `1b. Primitives / Typography` | ~125 |
| `1c. Primitives / Grid` | ~69 |
| `1d. Primitives / Opacity` | ~20 |
| `1e. Primitives / Boolean` | ~48 |
| `1f. Primitives / String` | ~30 |
| `1g. Primitives / Radius` | ~26 |

**Benefit**: Easier navigation, scoped search, clearer ownership.
**Risk**: Increases collection count; existing references would need updating.

#### A3. Consolidate Single-Variable Collections

**Current**: 6 collections with 1 variable each.
**Recommendation**: Merge into parent collections:
- `┈ Content / Text`, `┈ Content / Leading`, `┈ Content / Trailing` -> merge into `┈ Content` (3 variables).
- Evaluate the 3 other single-variable collections for similar consolidation.

**Benefit**: Reduces collection count by ~5, simplifies browsing.
**Risk**: May lose explicit namespace separation that Figma components rely on.

#### A4. Formalize the Naming Scheme as a Spec

**Current**: Naming conventions are implicit and learned by example.
**Recommendation**: Create a formal naming specification document that defines:
- Separator rules: `.` for hierarchy, `-` for compound words, `/` for collection namespaces.
- Prefix rules: numbered for system layers, `┈` for component sub-systems.
- Required segments: `{domain}.{category}.{variant}.{property}`.

**Benefit**: Enables automated linting, onboards new designers faster, prevents future drift.

#### A5. Reduce Redundant Platform Modes

**Current**: 82 of 132 Platform variables are identical across Web and Mobile.
**Recommendation**: Either:
1. **Remove identical pairs** from the Platform collection and reference Primitives directly (saves 82 variables).
2. **Mark them explicitly** with a `$identical: true` flag in exports so tooling can skip them.

**Benefit**: Clearer signal of what actually differs per platform.
**Risk**: Option 1 breaks the uniform API surface; consumers would need to know whether to reference Platform or Primitives.

### Naming (5)

#### N1. Fix "FIeld" Typo

**Action**: Global find-and-replace `FIeld` -> `Field` across all variable names and collection names.
**Affected**: ~24 variables.
**Priority**: High — this is a silent bug that breaks pattern matching.

#### N2. Normalize ┈ Spacing

**Action**: Replace all `┈  ` (double-space) with `┈ ` (single-space) in collection names.
**Affected**: 4 tree-related collections.
**Priority**: Medium — affects string matching in tooling.

#### N3. Rename "Info 2"

**Action**: Rename `info-2` to `info-alt` or `info-secondary` across signal variables.
**Affected**: Info signal variable set.
**Priority**: Low — documentation can mitigate confusion in the interim.

#### N4. Standardize Separator Usage

**Action**: Audit all 1,059 variable names and enforce:
- `.` = hierarchy separator only
- `-` = compound word joiner only
- No mixing within the same hierarchy level

**Affected**: ~460 variables with mixed separators.
**Priority**: Medium — high impact but large scope of change.

#### N5. Re-prefix System-Level ┈ Collections

**Action**: Rename `┈ Inverse` and `┈ Size Scale` to use numbered prefixes (e.g. `3.5. Inverse`, `8.5. Size Scale`) to match their system-level role.
**Affected**: 2 collections (122 variables total).
**Priority**: Low — cosmetic but improves navigability.

### Tooling (4)

#### T1. Chain Depth Linting

**Recommendation**: Add an automated check to the Figma-to-code pipeline that warns when any variable's chain depth exceeds a configurable threshold (suggested: 7).

**Implementation**: Parse `$chain` arrays in the export and flag violations. Output a report like:
```
WARNING: 21 variables exceed chain depth 7
  - 12. Component Category / main-layer.background: depth 11
  - 12. Component Category / main-layer.foreground: depth 10
  ...
```

**Benefit**: Catches unintentional chain growth before it becomes structural debt.

#### T2. Duplicate Detection

**Recommendation**: Build a tool that identifies:
1. **Identical values across modes** (like the 82 identical Platform pairs).
2. **Identical chains** (variables that resolve to the same final value through different paths).
3. **Unused variables** (defined but never referenced by any other variable).

**Benefit**: Keeps the system lean and identifies consolidation opportunities.

#### T3. Version Tracking

**Recommendation**: Add metadata to the `figma-variables.json` export:
```json
{
  "$meta": {
    "exportDate": "2026-02-03",
    "variableCount": 1059,
    "collectionCount": 42,
    "schemaVersion": "2.0",
    "previousExportDate": "2026-01-15"
  }
}
```

Include a diff summary against the previous export: added/removed/modified variables.

**Benefit**: Enables change tracking, regression detection, and audit trails.

#### T4. Dependency Graph Visualization

**Recommendation**: Generate an interactive dependency graph from the chain data, showing:
- Collections as nodes, references as edges.
- Edge thickness proportional to reference count.
- Clickable drill-down from collection -> variable -> full chain.

**Implementation**: Export chain data to a format compatible with D3.js, Mermaid, or Graphviz. Integrate into the design system documentation site.

**Benefit**: Makes the architecture visible and navigable for both designers and developers. Immediately surfaces structural issues like hub concentration and circular-looking chains.

---

## Additional Low-Severity Issues

### Mode Names with Spaces and Special Characters

Several mode names contain spaces, parentheses, or numeric prefixes that may cause issues in code generation:

- `┈ Size Scale: "-2 (Main Downscale)"` — includes parentheses
- `1. Primitives: "Mode 1"` — generic name with space
- `2. Signal: "Info 2"` — semantic name with space
- `┈ Content / Tree Level: "Level 0", "Level 1", "Level 2", "Level 3"` — numeric with text prefix

**Risk**: Code generators or APIs expecting identifier-safe names will need to sanitize these.

### Overlapping Collection Purposes

Some collections have unclear boundaries:

| Pair | Overlap |
|------|---------|
| `1. Base Colors` (external) vs `1. Primitives` | Both provide foundational colors |
| `2. Signal` vs `3. Additional` | Both define semantic color sets |
| `4. Brands Groups` vs `4.1 Brands Group 1` | Unclear hierarchy relationship |

---

## Structural Health Summary

| Metric | Status | Notes |
|--------|--------|-------|
| Null/Empty Values | CLEAN | No undefined or empty variable values found |
| Orphaned References | CLEAN | All referenced collections exist (external refs are valid) |
| Mode Consistency | CLEAN | Sibling variables have consistent mode definitions |
| Type/Scope Alignment | CLEAN | COLOR, FLOAT, STRING, BOOLEAN all have appropriate scopes |
| Naming Consistency | **POOR** | Typos, mixed separators, inconsistent spacing |
| Chain Depth | **EXCESSIVE** | Max 11 levels (deeply nested resolution chains) |
| Collection Organization | **UNCLEAR** | Numbered vs prefixed distinction not documented |
| Size Distribution | **IMBALANCED** | Primitives dominates; 6 collections have only 1 variable |
