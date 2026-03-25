---
name: test-visual-mode
description: "(ePost) Visual regression testing with Playwright screenshot comparison"
user-invocable: false
---

# Visual Regression Testing

Invoked when: `/test --visual [--update] [component name]`

## Step 0 — Flag Handling

- `--update`: update baseline screenshots instead of comparing
- `--no-ci`: skip CI-consistency checks (local dev only)

## Step 1 — Detection

Find visual test files in the project:

```bash
find . -not -path "*/node_modules/*" \( -name "*.visual.spec.ts" -o -name "*.screenshot.spec.ts" \)
```

If no files found: suggest creating one using the template in Step 2, then exit.

## Step 2 — Configuration

Expected `playwright.config.ts` settings for screenshot tests:

```ts
import { defineConfig } from '@playwright/test';

export default defineConfig({
  expect: {
    toHaveScreenshot: {
      maxDiffPixels: 50,           // absolute pixel diff threshold
      maxDiffPixelRatio: 0.02,     // 2% of total pixels
      threshold: 0.2,              // per-pixel color difference (0-1)
      animations: 'disabled',      // freeze animations for consistency
    },
  },
  use: {
    viewport: { width: 1280, height: 720 },  // fixed viewport for CI consistency
  },
  projects: [
    { name: 'chromium', use: { channel: 'chromium' } },
  ],
});
```

If `playwright.config.ts` lacks screenshot settings: output the above config block and prompt user to add it.

## Step 3 — Execution

### Run visual tests (compare mode)

```bash
npx playwright test --grep @visual
# or by file pattern
npx playwright test "*.visual.spec.ts"
```

### Run with update flag

```bash
npx playwright test --update-snapshots "*.visual.spec.ts"
```

### Output interpretation

- `PASS` — screenshot matches baseline within threshold
- `FAIL` — diff exceeds threshold; diff image saved to `test-results/`
- `MISSING SNAPSHOT` — no baseline exists; run with `--update` to create one

## Step 4 — Baseline Management

| Action | Command |
|--------|---------|
| Create initial baselines | `npx playwright test --update-snapshots` |
| Update after intentional change | `npx playwright test --update-snapshots "component.visual.spec.ts"` |
| Reset all baselines | Delete `*.spec.ts-snapshots/` directories, then re-run with `--update-snapshots` |

Baseline files location: `src/**/__snapshots__/*.png` or `e2e/**/*.spec.ts-snapshots/*.png`

## Step 5 — Failure Analysis

When a test fails:

1. Diff images are in `test-results/<test-name>/`:
   - `actual.png` — current screenshot
   - `expected.png` — baseline
   - `diff.png` — pixel difference visualization

2. Common causes:
   - Font rendering differences (CI vs local) → use Docker (see Step 6)
   - Animation not frozen → add `animations: 'disabled'` to config
   - Viewport mismatch → enforce fixed viewport in config
   - Dynamic content → mock date/time in test setup

3. To accept a valid change: run `--update-snapshots` and commit updated snapshots.

## Step 6 — CI Integration

### Docker for consistent rendering

Use a Playwright Docker image to eliminate OS-level font/rendering differences:

```yaml
# .github/workflows/visual.yml
- name: Run visual tests
  uses: docker://mcr.microsoft.com/playwright:v1.40.0-jammy
  with:
    args: npx playwright test "*.visual.spec.ts"
```

### Key CI settings

- Always use fixed `viewport` in config (no dynamic sizing)
- Set `CI=true` — Playwright applies stricter thresholds automatically
- Store snapshot updates as PR artifacts for review
- Run visual tests in a separate CI job (slower; don't block unit test feedback)

## Step 7 — Storybook Integration (optional)

For component-level screenshots with `@storybook/test-runner`:

```bash
npm install --save-dev @storybook/test-runner
npx test-storybook --url http://localhost:6006 --snapshot
```

Screenshot test file pattern for Storybook stories:

```ts
// Button.visual.spec.ts
import { test, expect } from '@playwright/test';

test('Button variants @visual', async ({ page }) => {
  await page.goto('/storybook/iframe.html?id=button--primary');
  await expect(page).toHaveScreenshot('button-primary.png');
});
```

## Test File Template

```ts
// src/components/MyComponent.visual.spec.ts
import { test, expect } from '@playwright/test';

test.describe('MyComponent visual @visual', () => {
  test('default state', async ({ page }) => {
    await page.goto('/components/my-component');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('[data-testid="my-component"]')).toHaveScreenshot();
  });

  test('hover state', async ({ page }) => {
    await page.goto('/components/my-component');
    await page.locator('[data-testid="my-component"]').hover();
    await expect(page.locator('[data-testid="my-component"]')).toHaveScreenshot('hover.png');
  });
});
```
