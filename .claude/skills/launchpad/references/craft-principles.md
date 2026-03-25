---
name: launchpad-craft-principles
description: "(ePost) Typography, color, spacing, layout, animation, and signature patterns for landing page craft"
user-invocable: false
disable-model-invocation: true
---

# Craft Principles

Quality floor for landing pages. Apply regardless of brand direction.

## Typography as Personality

Typography IS your content. The personality of letters shapes how the product feels before anyone reads a word.

### Display Typography

```jsx
// Hero headline — massive, commanding, tight
className="text-[clamp(3rem,8vw,6rem)] font-display font-black tracking-tight leading-[0.9]"

// Display — section headers with presence
className="text-[clamp(2rem,5vw,4rem)] font-display font-bold tracking-tight leading-[1.1]"

// Subheadline — supporting but still bold
className="text-[clamp(1.25rem,3vw,1.75rem)] font-display font-semibold"
```

### Body Typography

```jsx
// Lead paragraph — larger, comfortable
className="text-lg md:text-xl text-ink-muted leading-relaxed max-w-2xl"

// Body — readable baseline
className="text-base text-ink-muted leading-relaxed"

// Labels — small, functional
className="text-sm font-medium uppercase tracking-wide text-ink-muted"
```

### Font Pairing

**Display fonts** (personality carriers):
- Space Grotesk (technical, precise)
- Cabinet Grotesk (humanist, warm)
- Clash Display (geometric, modern)
- Satoshi (clean, contemporary)

**Body fonts** (readability with character):
- Inter (neutral, reliable)
- Plus Jakarta Sans (friendly, modern)
- DM Sans (geometric, clean)

**Rule**: If you're using Inter for headlines, you're not designing.

## Color as Emotion

### Brand Color Architecture

```css
:root {
  --brand: #ff6b35;        /* dominant personality */
  --brand-dark: #e55a2b;
  --brand-light: #ff8f66;
  --surface: #0a0a0a;       /* dark backgrounds */
  --surface-alt: #141414;
  --ink: #fafafa;           /* primary text */
  --ink-muted: #a1a1aa;     /* secondary text */
  --accent: #00d4aa;        /* punctuation */
}
```

### One-Color Rule

One dominant brand color owns the page. One accent punctuates. Everything else is surface and ink. Three competing "brand" colors = nothing is brand.

## Spacing for Drama

```jsx
// Hero section — maximum breathing room
className="py-24 md:py-32 lg:py-40"

// Standard sections — generous
className="py-20 md:py-28 lg:py-32"

// Between major elements
className="space-y-8 md:space-y-12"

// Container
className="max-w-7xl mx-auto px-6 md:px-8"

// Narrow content (readable width)
className="max-w-3xl mx-auto px-6"
```

## Layout as Tension

Centered everything = monotony. Create tension with asymmetry.

```jsx
// Asymmetric 60/40 split
className="grid grid-cols-1 lg:grid-cols-5 gap-12"

// Full-bleed breaking container
<div className="max-w-7xl mx-auto px-6">
  <div className="-mx-6 md:-mx-8 lg:-mx-16">
    <img className="w-full" />
  </div>
</div>

// Overlapping elements
className="relative -mt-20 z-10"
```

**Rhythm**: Alternate layouts — full-width → contained, left-aligned → right-aligned, dense → spacious.

## The Signature Element

ONE unforgettable moment per page.

**Types**:
- Visual: distinctive illustration/color/gradient
- Typographic: extreme headline treatment, animated text
- Interactive: magnetic button, cursor-following element
- Structural: unexpected section shape, overlapping layers

**Placement**: Once prominently (hero), echoed subtly (supporting), never repeated to exhaustion.

## Animation with Purpose

Motion reveals, not decorates.

```jsx
// Entrance — fade up
initial={{ opacity: 0, y: 20 }}
animate={{ opacity: 1, y: 0 }}
transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}

// Scroll reveal — once only
whileInView={{ opacity: 1, y: 0 }}
viewport={{ once: true, margin: "-100px" }}

// Hover — fast, responsive
className="transition-all duration-200 hover:scale-105"
```

**Rules**: Once not loop. 200-600ms max. Ease out. Every animation answers "why does this move?"

## CTA Craft

```jsx
// Primary — the ONE action
className="inline-flex items-center px-8 py-4 bg-brand hover:bg-brand-dark text-white font-semibold rounded-full transition-all duration-200 hover:scale-105"

// Secondary — alternative path
className="border border-ink/20 text-ink px-6 py-3 font-medium rounded-full"

// Tertiary — low commitment
className="text-brand underline-offset-4 hover:underline"
```

**Placement**: Hero (always), after each value section (reinforce), final (strong close).

## Responsive

Mobile-first, fluid. Use `clamp()` for typography, `minmax()` for grids.

```
Base (mobile):  Single column, stacked, thumb-friendly (min touch 44px)
md (768px+):    Two-column layouts
lg (1024px+):   Full desktop expression
```

Headlines min 2rem on mobile. CTAs must be thumb-reachable.

## Reference Products

Study for bold, memorable craft:
- **Apple** — Dramatic simplicity, scroll reveals, typography that commands
- **Linear** — Dark mode done right, motion with purpose
- **Arc Browser** — Personality without chaos, playful yet trustworthy

What they share: remove the logo and you still know who made it.
