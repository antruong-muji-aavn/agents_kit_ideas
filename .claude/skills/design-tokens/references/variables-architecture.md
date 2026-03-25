# Figma Variables Architecture

> **Source**: `figma-variables.json` (2.3 MB) — complete export of all Figma design variables from the Vien 2.0 design system.
> **Scope**: 1,059 variables across 42 collections, with full reference chain resolution.
> **Date of analysis**: 2026-02-03

---

## Core Design Philosophy

Five architectural ideas govern how this variable system is structured:

### 1. Design Decisions, Not Design Values

The system stores **decisions**, not raw values. When Mobile uses `gentle.background` instead of Web's `strong.background`, that is a conscious UX decision encoded as a variable reference — not a hardcoded color.

### 2. Change Once, Cascade Everywhere

The deep reference chain architecture (up to 11 hops) means a brand color change at the lowest layer propagates through every component state, size, and platform automatically. This is the core value proposition of the entire system.

### 3. Modes as Dimensions, Not Duplicates

Instead of creating separate token sets for every combination (Web + Light + Hover + CTA = combinatorial explosion), modes compose orthogonally. Each collection handles one dimension of variation independently.

### 4. Platform Abstraction as a Bridge

The Platform layer is where **design language meets implementation**. Above it: abstract design concepts. Below it: concrete code values. This boundary is where a Figma-to-code generator should operate.

### 5. Component Category as the Consumer API

The Component Category layer (with its 28 cross-collection dependencies) is the most complex consumer. This layer aggregates all decisions above it into a single coherent API for each component type — it is the layer that components should reference in code.

---

## External Brand Libraries (Inputs)

Five external Figma libraries feed into the system. They are **abstract inputs** — the internal architecture references them but does not define them.

| Library | Remote Refs | Role |
|---------|------------|------|
| **1. Base Colors** | 500 | Foundational color palette shared across all brands |
| **2. Brand Settings** | 316 | Product-specific overrides (e.g. `brandsettings.post.*` for ePost) |
| **3. Brand Sets** | 691 | Central brand color definitions and brand-set mappings |
| **4. Brands Groups** | 232 | Brand hierarchy groupings |
| **4.1 Brands Group 1** | 325 | First-level brand group variations |
| **Total** | **2,064** | **28.3% of all references point to external libraries** |

**Multi-brand support**: Swapping `brandsettings.post.*` for another brand would cascade through the entire system. Every component re-themes automatically because no internal variable hardcodes a brand value.

---

## Layer 1: Primitives (505 variables)

The foundation. Raw design atoms — single-mode, platform-agnostic, no semantic meaning.

**Collection**: `1. Primitives`
**Variable count**: 505 (47.6% of all variables)
**Modes**: 1 (no variation)

### What it contains

| Domain | Example Variables | Count (approx.) |
|--------|------------------|-----------------|
| Grid units | `grid.010000`, `grid.020000`, `grid-negative.010000` | 69 |
| Base colors | `color.base.foreground`, `color.base.background` | ~147 |
| Typography primitives | `typography.font-size.body.1`, `typography.line-height.*` | ~125 |
| Opacity values | `opacity.100`, `opacity.050` | ~20 |
| Radius values | `radius.100`, `radius.200` | ~26 |
| Boolean flags | Visibility toggles, state flags | ~48 |
| String values | Font family names | ~30 |

### Design role

Primitives are **never referenced directly by components**. They exist solely to be consumed by higher layers. The naming uses raw numeric IDs (e.g. `grid.010000`) rather than semantic names, reinforcing that these are atoms, not decisions.

### Variable type breakdown (system-wide, rooted here)

| Type | Count | % | Primary Use |
|------|-------|---|------------|
| FLOAT | 642 | 60.6% | Sizes, spacing, opacity, line-height, font-size |
| COLOR | 339 | 32.0% | All color tokens |
| BOOLEAN | 48 | 4.5% | Visibility toggles, state flags |
| STRING | 30 | 2.8% | Font families, text content |

---

## Layer 2: Themes & Semantics

Four collections apply **meaning** to primitive values.

### 2a. Signal (8 variables)

**Collection**: `2. Signal`
**Modes**: 5 — `Info 2`, `Info`, `Error`, `Success`, `Warning`

Each signal mode provides 4 variables: `strong.background`, `strong.foreground`, `gentle.background`, `gentle.foreground` — plus inverse variants.

| Signal Mode | Strong (Web) | Gentle (Mobile) | Use Case |
|-------------|-------------|-----------------|----------|
| **Info 2** | Purple (brand-color-1) | Neutral feedback | Brand-highlighted information |
| **Info** | Blue | Blue (muted) | General information |
| **Error** | Red (saturated) | Red (muted) | Destructive actions, validation |
| **Success** | Green (saturated) | Green (muted) | Confirmation, completion |
| **Warning** | Brown (saturated) | Brown (muted) | Alerts, caution |

**Key design decision**: Strong signals use `brandsystem.brandsets.general.signal.*` (brand-generic), while Gentle signals use `brandsystem.brandsets.brands.post.epost.signal.*` (tenant-specific). Signal intensity is coupled to brand personality.

The `strong`/`gentle` distinction maps to platform intensity: Web defaults to `strong`, Mobile to `gentle`. This encodes the decision that mobile interfaces should use subtler feedback.

### 2b. Inverse (32 variables)

**Collection**: `┈ Inverse`
**Modes**: 2 — `False` (normal), `True` (inverted)

A theme toggle mechanism providing normal vs inverted surfaces:

| Sub-layer | Pairs | Purpose |
|-----------|-------|---------|
| Base layer | 6 pairs | `default.base.background` <-> `background-inverse` |
| Highlight layer | 3 pairs | Brand highlight states |
| Branded layer | 8 pairs | Post brand primary surfaces |
| Overlay layer | 4 pairs | Blur/overlay backgrounds (foreground stays white in both modes) |

All 32 variables are COLOR type and reference back to Primitives or Brand libraries.

### 2c. Additional (8 variables)

**Collection**: `3. Additional`
**Modes**: 6

Extended semantic colors: Cyan, Magenta, Mustard, Purple, Red, Teal. Used for UI elements outside the signal system (badges, tags, categories).

### 2d. Color Adjustment (15 variables)

**Collection**: `4. Color Adjustment`

Color transforms — opacity modifications, tint/shade operations applied to base colors. These sit between Primitives and Platform, allowing color manipulation without duplicating the full palette.

---

## Layer 3: Platform & Scaling

This layer bridges abstract design values to concrete implementation targets. It is the **central hub** of the architecture.

### 3a. Platform (132 variables)

**Collection**: `9. Platform`
**Modes**: 2 — `Web`, `Mobile`

The most architecturally significant collection after Primitives.

| Metric | Value |
|--------|-------|
| Total variables | 132 |
| Differentiated (Web !== Mobile) | 50 |
| Identical (Web === Mobile) | 82 |
| Differentiation rate | 37.9% |

**Key differentiations** (Web vs Mobile):

| Property | Web | Mobile |
|----------|-----|--------|
| Default component size | M (40px) | L (48px) |
| Signal intensity | `strong` | `gentle` |
| Touch targets | Smaller | Larger |
| Typography scale | Desktop-optimized | Mobile-optimized |

The 82 identical variables exist to maintain a **uniform API surface** — consumers reference Platform variables without needing to know if a value actually differs per platform.

### 3b. Size Scale (90 variables)

**Collection**: `┈ Size Scale`
**Modes**: 7 — `-3`, `-2`, `-1`, `0`, `+1`, `+2`, `+3`

A relative sizing system where `0` is the default and each step scales uniformly. Used for responsive adjustments and user-preference sizing.

### 3c. Typography Collections

| Collection | Variables | Modes | Purpose |
|-----------|-----------|-------|---------|
| `5. Line Height` | 30 | 2 | Two families: `body.1-6` and `title.1-6`, each with standard and dense variants |
| `6. Font Weight` | 3 | 4 (Black, Bold, Regular, unnamed) | Maps to `brandsettings.post.typography.weights.*` |
| `7. Border Weight` | 1 | 2 | Border thickness tokens |

### 3d. Responsive (16 variables)

**Collection**: `18. Responsive`
**Modes**: 7 — device-specific breakpoints

Maps size and layout decisions to specific viewport ranges.

### 3e. Platform Theme (9 variables)

**Collection**: `10. Platform Theme`
**Modes**: 4 — light/dark/brand variants

Theme-level tokens that sit between Platform and Component layers.

### 3f. Radius Scale (10 variables)

**Collection**: `8. Radius Scale`
**Modes**: 8 — `None`, `XS`, `S`, `M`, `L`, `XL`, `XXL`, `Max`

Each mode provides `.main` and `.outer` variants for border-radius strategies (inner content vs outer container).

### 3g. Additional Platform Collections

| Collection | Variables | Modes | Purpose |
|-----------|-----------|-------|---------|
| `11. Platform Radius` | 4 | 2 | Platform-specific radius overrides |
| `17. Effect` | 5 | 3 (Normal, Strong, Light) | Visual effects |
| `19. Utilities` | 4 | 2 | Miscellaneous utility tokens |

---

## Layer 4: Components

Component-level variables consume everything above and present a **consumer-facing API** for each component type.

### 4a. Component Category (15 variables)

**Collection**: `12. Component Category`
**Modes**: 7 — `Avatar`, `Badge`, `CTA`, `Container`, `Field`, `Picker`, `Toggle`
**Cross-collection dependencies**: 28 (highest in the system)

This is the **most complex consumer**. Each mode defines how a component category maps to semantic/platform tokens. A CTA in `Hover` state at size `M` on `Web` resolves through Category -> State -> Platform -> Signal -> Primitives.

### 4b. Component State (22 variables)

**Collection**: `15. Component State`
**Modes**: 7 — `Default`, `Disabled`, `Dragged`, `Drop-in`, `Focus`, `Hover`, `Pressed`

Interaction state tokens. Each mode provides complete state-specific values for background, foreground, border, opacity, and shadow.

### 4c. Component Size (18 variables)

**Collection**: `14. Component Size`

Per-size tokens that map size labels (XS, S, M, L, XL) to concrete dimensions.

### 4d. Component Position (16 variables)

**Collection**: `13. Component Position`

Spatial positioning tokens for component layout — offsets, alignment, and stacking.

### 4e. Input Validation (10 variables)

**Collection**: `16. Input Validation`

Validation-specific tokens for form inputs: error borders, success indicators, warning backgrounds.

---

## Variant / Mode / Content Sub-System

23 collections prefixed with `┈` form a sub-system for fine-grained component variants.

### Variant collections (6)

| Collection | Variables | Purpose |
|-----------|-----------|---------|
| `┈ Variant / CTA` | 4-6 | Call-to-action button variants |
| `┈ Variant / Container` | 3 | Container layout variants |
| `┈ Variant / Field` | 3 | Input field variants |
| `┈ Variant / Picker` | 3 | Picker/selector variants |
| `┈ Variant / Toggle` | 3 | Toggle switch variants |
| `┈ Variant / Tree` | 1 | Tree view component variants |

### Content collections (7)

| Collection | Variables | Purpose |
|-----------|-----------|---------|
| `┈ Content / Dropdown` | 2 | Dropdown content presentation |
| `┈ Content / Toggle` | 2 | Toggle label/content |
| `┈ Content / Tree Level` | 4 | Tree depth levels (Level 0-3) |
| `┈ Content / Select Mark` | 1 | Selection indicator visibility |
| `┈ Content / Text` | 1 | Text content display |
| `┈ Content / Leading` | 1 | Leading element (icon/avatar) |
| `┈ Content / Trailing` | 1 | Trailing element (icon/action) |

### Mode collections (5)

| Collection | Variables | Purpose |
|-----------|-----------|---------|
| `┈ Mode / Toggle` | 19 | Toggle interaction modes |
| `┈ Mode / Drop-in` | 4 | Drag-and-drop modes |
| `┈ Mode / Input` | 3 | Input field interaction modes |
| `┈ Mode / Picker` | 3 | Picker interaction modes |
| `┈ Mode / Tree Item` | 2 | Tree view item modes |

### Surface collections (1)

| Collection | Variables | Purpose |
|-----------|-----------|---------|
| `┈ Surface / Primary` | 7 | Primary surface styling (4 modes) |

### Scale & system ┈-prefixed collections

| Collection | Variables | Modes | Purpose |
|-----------|-----------|-------|---------|
| `┈ Size Scale` | 90 | 7 | Relative sizing (Layer 3) |
| `┈ Inverse` | 32 | 2 | Theme inversion (Layer 2) |
| `┈ Avatar Scale` | 10 | 9 | Avatar size variants (16-160px) |
| `┈ Avatar Extra Scale` | — | — | Extended avatar sizing |

---

## Reference Chain Architecture

### How chains work

Every variable includes a `$chain` array showing its full resolution path from consumer to primitive. This is the most architecturally significant feature of the system.

### Example: Button background color resolution

```
12. Component Category (main-layer.background)
  -> ┈ Variant / CTA (main-layer.background)
    -> ┈ Surface / Primary (default.surface.background)
      -> 10. Platform Theme (theme.primary.background)
        -> 9. Platform (color.base.foreground)
          -> ┈ Inverse (default.foreground)
            -> 1. Primitives (color.base.foreground)
              -> 3. Brand Sets (brandsystem.brandsets.default.foreground)
                -> 4. Brands Groups (light.surface.foreground)
                  -> 4.1 Brands Group 1 (...)
                    -> 2. Brand Settings (brandsettings.post.colors.brand-color-2)
```

**11 hops** from component usage to final brand color.

### Chain depth distribution

| Depth | % of Variables | Meaning |
|-------|---------------|---------|
| 1-2 | 54% | Simple tokens (primitives, direct references) |
| 3-4 | 29% | Semantic tokens (platform, theme) |
| 5-7 | 15% | Component tokens (state, category) |
| 8-11 | 2% | Deep component variants (maximum complexity) |

### Dependency topology

**Hub nodes** (most depended upon):

| Collection | Dependents |
|-----------|-----------|
| 1. Primitives | 32 collections depend on it |
| 1. Base Colors (external) | 20 collections |
| 9. Platform | 20 collections |
| 3. Brand Sets (external) | 19 collections |
| 2. Brand Settings (external) | 17 collections |

**Leaf nodes** (most dependencies):

| Collection | Dependencies |
|-----------|-------------|
| 12. Component Category | Depends on 28 collections |
| 9. Platform | Depends on 19 collections |
| ┈ Mode / Toggle | Depends on 17 collections |
| ┈ Variant / Toggle | Depends on 14 collections |
| 15. Component State | Depends on 12 collections |

### The "funnel" pattern

Variables flow **upward** from primitives and **downward** from component needs. The Platform layer (Layer 3a) is the **central hub** — it is where design intent meets implementation reality. All chains pass through it.

---

## Multi-Mode Composition

The system uses **mode composition** — a single component instance can simultaneously be in:

| Dimension | Example Value | Collection |
|-----------|--------------|------------|
| Platform | Web | 9. Platform |
| Size scale | +1 | ┈ Size Scale |
| Component state | Hover | 15. Component State |
| Component category | CTA | 12. Component Category |
| Responsive breakpoint | Desktop | 18. Responsive |

This creates a **multiplicative space** without multiplicative token counts:
- 2 platforms x 7 sizes x 7 states x 7 categories x 7 breakpoints = **4,802 theoretical combinations**
- Actual variables defined: **1,059**

The orthogonal mode system reduces token count by **~78%** compared to a flat enumeration.

---

## Variable Types & Scopes

### Type distribution

| Type | Count | % | Primary Use |
|------|-------|---|------------|
| FLOAT | 642 | 60.6% | Sizes, spacing, opacity, line-height, font-size |
| COLOR | 339 | 32.0% | All color tokens |
| BOOLEAN | 48 | 4.5% | Visibility toggles, state flags |
| STRING | 30 | 2.8% | Font families, text content |

### Scope usage (how Figma applies them)

| Scope | Count | What It Targets |
|-------|-------|----------------|
| ALL_SCOPES | 249 | Universal (mostly colors + booleans) |
| WIDTH_HEIGHT | 212 | Component dimensions |
| LINE_HEIGHT | 194 | Typography line spacing |
| GAP | 160 | Flex/grid gaps |
| FONT_SIZE | 134 | Text sizing |
| CORNER_RADIUS | 104 | Border radius |
| STROKE_FLOAT | 98 | Border/stroke widths |
| OPACITY | 81 | Transparency |

---

## Naming Conventions

### Hierarchical pattern

```
{domain}.{category}.{variant}.{property}
```

### Examples

| Variable Name | Domain | Category | Variant | Property |
|--------------|--------|----------|---------|----------|
| `color.signal.default.background` | color | signal | default | background |
| `typography.font-size.body.1` | typography | font-size | body | 1 (scale) |
| `component.spacing.s.gap` | component | spacing | s (small) | gap |
| `grid.010000` | grid | — | — | raw numeric ID |

### Prefix distribution

| Prefix | Count | Domain |
|--------|-------|--------|
| `color` | 147 | Color system |
| `component` | 145 | Component tokens |
| `typography` | 125 | Type system |
| `color-adjustment` | 60 | Color transforms |
| `breakpoints` | 48 | Responsive |
| `grid` / `grid-negative` | 69 | Spacing grid |
| `radius` | 26 | Border radius |

### Separator conventions

- **Dots** (`.`) separate hierarchy levels: `color.signal.default.background`
- **Dashes** (`-`) separate compound words: `font-size`, `line-height`, `brand-color-2`
- **Slashes** (`/`) separate collection namespaces: `┈ Variant / CTA`, `┈ Content / Text`

---

## Collection Inventory

All 42 collections, grouped by layer:

**External brand libraries** (Layer 0 — abstract inputs)

| Library | Remote Refs |
|---------|------------|
| 1. Base Colors | 500 |
| 2. Brand Settings | 316 |
| 3. Brand Sets | 691 |
| 4. Brands Groups | 232 |
| 4.1 Brands Group 1 | 325 |

**Numbered system collections** (Layers 1-4)

| # | Collection | Vars | Modes | Layer | Role |
|---|-----------|------|-------|-------|------|
| 1 | Primitives | 505 | 1 | L1 | Raw design atoms |
| 2 | Signal | 8 | 5 | L2 | Semantic feedback colors |
| 3 | Additional | 8 | 6 | L2 | Supplementary palette |
| 4 | Color Adjustment | 15 | 2 | L2 | Color transforms |
| 5 | Line Height | 30 | 2 | L3 | Typography line spacing |
| 6 | Font Weight | 3 | 4 | L3 | Typography weight |
| 7 | Border Weight | 1 | 2 | L3 | Border thickness |
| 8 | Radius Scale | 10 | 8 | L3 | Border radius scales |
| 9 | Platform | 132 | 2 | L3 | **Central hub** (Web/Mobile) |
| 10 | Platform Theme | 9 | 4 | L3 | Theme layer (light/dark/brand) |
| 11 | Platform Radius | 4 | 2 | L3 | Platform-specific radius |
| 12 | Component Category | 15 | 7 | L4 | **Consumer API** per component type |
| 13 | Component Position | 16 | 7 | L4 | Spatial positioning |
| 14 | Component Size | 18 | 6 | L4 | Size variants (XS-XL) |
| 15 | Component State | 22 | 7 | L4 | Interaction states |
| 16 | Input Validation | 10 | 6 | L4 | Form validation states |
| 17 | Effect | 5 | 3 | L3 | Visual effects (Normal/Strong/Light) |
| 18 | Responsive | 16 | 7 | L3 | Device breakpoints |
| 19 | Utilities | 4 | 2 | L3 | Miscellaneous |

**┈-prefixed sub-system collections**

| Collection | Vars | Modes | Group |
|-----------|------|-------|-------|
| ┈ Size Scale | 90 | 7 | Scale |
| ┈ Inverse | 32 | 2 | System |
| ┈ Mode / Toggle | 19 | 5 | Mode |
| ┈ Avatar Scale | 10 | 9 | Scale |
| ┈ Surface / Primary | 7 | 4 | Surface |
| ┈ Variant / CTA | 4-6 | 2-4 | Variant |
| ┈ Content / Tree Level | 4 | 4 | Content |
| ┈ Mode / Drop-in | 4 | 2 | Mode |
| ┈ Variant / Container | 3 | 2 | Variant |
| ┈ Variant / Field | 3 | 4 | Variant |
| ┈ Variant / Picker | 3 | 2 | Variant |
| ┈ Variant / Toggle | 3 | 4 | Variant |
| ┈ Mode / Input | 3 | 3 | Mode |
| ┈ Mode / Picker | 3 | 5 | Mode |
| ┈ Content / Dropdown | 2 | 2 | Content |
| ┈ Content / Toggle | 2 | 2 | Content |
| ┈ Mode / Tree Item | 2 | 2 | Mode |
| ┈ Content / Select Mark | 1 | 2 | Content |
| ┈ Content / Text | 1 | 2 | Content |
| ┈ Content / Leading | 1 | 2 | Content |
| ┈ Content / Trailing | 1 | 2 | Content |
| ┈ Variant / Tree | 1 | 2 | Variant |
| ┈ Avatar Extra Scale | — | — | Scale |

---

## Summary Statistics

| Metric | Value |
|--------|-------|
| Total variables | 1,059 |
| Total collections | 42 |
| Architecture layers | 4 main + ┈ sub-system |
| External brand libraries | 5 |
| Remote references | 2,064 |
| Max chain depth | 11 hops |
| Platform modes | Web, Mobile |
| Size scale range | -3 to +3 (7 levels) |
| Component categories | 7 (Avatar, Badge, CTA, Container, Field, Picker, Toggle) |
| Component states | 7 (Default, Disabled, Dragged, Drop-in, Focus, Hover, Pressed) |
| Responsive modes | 7 |
| Avatar scale modes | 9 (16px - 160px) |
| Variable types | 4 (FLOAT 60.6%, COLOR 32.0%, BOOLEAN 4.5%, STRING 2.8%) |
| Mode composition combinations | ~4,802 theoretical (reduced from flat enumeration via orthogonal modes) |
