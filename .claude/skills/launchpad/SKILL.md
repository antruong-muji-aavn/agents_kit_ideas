---
name: launchpad
description: (ePost) Use when building landing pages, promotional sites, or marketing pages — bold craft, not templates. Routes to epost-muji for design direction then epost-fullstack-developer for implementation.
user-invocable: true
context: fork
agent: epost-muji
metadata:
  argument-hint: "[product/project description]"
  agent-affinity: [epost-muji, epost-fullstack-developer]
  keywords: [landing-page, launchpad, marketing, promotional, showcase, launch, website, homepage]
  platforms: [web]
  triggers: [landing page, launch page, marketing page, promotional site, showcase site]
  connections:
    enhances: [cook]
---

# Launchpad

Build landing pages with craft, boldness, and conversion focus.

**Adapted from** [vichannnnn/design-suite/launchpad](https://github.com/vichannnnn/design-suite) with epost_agent_kit orchestration.

## Scope

**Use for:** Landing pages, product launches, waitlist pages, promotional sites, single-product showcases, email capture pages, project homepages.

**Not for:** Dashboards, admin panels, multi-page apps → route to `/cook` instead.

## Stack

- **Astro** — Static-first, zero JS by default, content-focused
- **React** (Astro islands) — Interactive components only where needed
- **Tailwind CSS** — Utility-first, pure Tailwind, responsive with prefixes
- **Framer Motion** (optional) — Scroll-triggered animations, entrance effects

## The Anti-Template Mandate

> If another AI given similar context produces substantially the same output — you have failed.

Landing pages reward boldness. Generic = forgettable. Every choice must be a choice, not a default.

### Where Defaults Hide

- **Hero sections** feel like templates (headline + subtext + CTA)
- **CTAs** feel like buttons (style it, move on)
- **Social proof** feels like a section (add logos, done)
- **Typography** feels like font selection (pick something nice)
- **Color** feels like a palette (primary/secondary/accent)

The trap: thinking conversion and creativity are separate. The most converting pages are the most memorable ones.

## Step 0 — Intent First

Before touching code, answer these explicitly:

1. **What is the ONE action?** Not "explore." The verb. Sign up. Download. Star the repo.
2. **Who lands here?** The actual person. Where did they come from? What's their skepticism?
3. **What must they feel?** Not "interested." The emotion. Urgency? Curiosity? Trust? Excitement?
4. **What makes this unforgettable?** The moment. What will they remember tomorrow?

If you cannot answer with specifics — ask the user. Do not guess.

## Step 1 — Brand Domain Exploration

**Do not propose any direction until you produce all four:**

| Output | What | Min |
|--------|------|-----|
| **Brand world** | Concepts, metaphors, emotions from this brand's territory | 5+ |
| **Color world** | Colors that exist naturally in this brand's domain | 5+ |
| **Signature** | One element that could only exist for THIS brand | 1 |
| **Defaults** | Obvious choices to deliberately reject | 3 |

Present to user. Get confirmation before building.

## Step 2 — Build

Apply `references/craft-principles.md` throughout. Key rules:

### Drama Through Contrast
- **Scale**: Hero 6rem, body 1rem — dramatic jumps, not gradual steps
- **Color**: One element demands attention, everything else supports
- **Pace**: Dense sections → breathing room → fast reveals → slow moments
- **Motion**: Static elements make animated elements matter

### Sections as Chapters
- **Hero**: The promise. What world are we entering?
- **Problem/Stakes**: Why does this matter?
- **Solution**: How does this change things?
- **Proof**: Why should I believe you?
- **Action**: What do I do now?

### Typography
```jsx
// Hero — massive, commanding
className="text-[clamp(3rem,8vw,6rem)] font-display font-black tracking-tight leading-[0.9]"
// Display — section headers
className="text-[clamp(2rem,5vw,4rem)] font-display font-bold tracking-tight"
// Body — readable, comfortable
className="text-lg md:text-xl text-ink-muted leading-relaxed max-w-2xl"
```

### Brand Tokens (Tailwind config)
```js
colors: {
  brand: { DEFAULT: 'var(--brand)', dark: 'var(--brand-dark)', light: 'var(--brand-light)' },
  surface: { DEFAULT: 'var(--surface)', alt: 'var(--surface-alt)' },
  ink: { DEFAULT: 'var(--ink)', muted: 'var(--ink-muted)' },
  accent: 'var(--accent)',
}
```

### Spacing (Generous)
```
Section padding:    py-24 md:py-32 lg:py-40
Between elements:   space-y-6 to space-y-12
Container:          max-w-7xl mx-auto px-6
```

## Step 3 — Validate

Run these checks before presenting:

| Check | Question | Fail = iterate |
|-------|----------|---------------|
| **Generic** | Remove logo — could this be any company? | Yes |
| **Scroll** | Is there a reason to keep scrolling? | No |
| **Signature** | Can you point to the ONE unforgettable element? | No |
| **Feel** | Does every section reinforce the stated emotion? | No |
| **Action** | Is the ONE action unmistakably clear? | No |

## Avoid

- Purple-to-blue gradients (clearest sign of AI-generated)
- Floating blobs and abstract shapes
- Inter/Roboto for headlines (no personality)
- Centered everything (no tension)
- Feature grids with generic icons
- "Clean and modern" (means nothing)
- Multiple competing CTAs
- Same section rhythm (predictable = forgettable)
- Bright saturated neon colors (prefer sophisticated, muted tones)

## Aspect Files

| File | Purpose |
|------|---------|
| `references/craft-principles.md` | Typography, color, spacing, signatures, animation patterns |

## Workflow

1. **Explore brand** — Produce all four required outputs (Step 1)
2. **Propose direction** — Must reference all four outputs
3. **Confirm** — Get user buy-in before building
4. **Build** — Apply principles with Astro + React + Tailwind
5. **Validate** — Run the 5 checks (Step 3)
6. **Ship** — Deploy to GitHub Pages or static host
