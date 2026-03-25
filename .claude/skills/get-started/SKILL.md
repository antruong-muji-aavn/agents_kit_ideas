---
name: get-started
description: (ePost) Use when user says "get started", "I'm new to this project", "onboard me", "what is this codebase", or "help me understand this project" — discovers project state and delivers a structured onboarding experience
user-invocable: true
context: inline
metadata:
  argument-hint: "[project path or question]"
  keywords:
    - onboard
    - get-started
    - begin
    - new-project
    - what-is-this
    - existing-project
  agent-affinity:
    - epost-researcher
  platforms:
    - all
  connections:
    enhances: [docs-init, docs-update]
---

# Get Started

Full onboarding pipeline — detect project state, then dispatch researcher → documenter → implementer subagents in sequence.

## Arguments

- CONTEXT: $ARGUMENTS (optional — project path, specific question, or empty)

## Step 1 — Detect Documentation State

Gather signals (read-only, no file creation):

```
index_json = Glob("docs/index.json")
docs_files = Glob("docs/**/*.md")
readme = Glob("README*")
markers = Glob for: package.json, pom.xml, Package.swift, build.gradle.kts, Cargo.toml
```

Branch:
- `index_json` exists → Step 2a (has KB structure)
- `docs_files` not empty but no `index.json` → Step 2b (has flat docs)
- `docs_files` empty → Step 2c (no docs)

## Step 2a — Has Knowledge Base

Audit KB coverage and health:

1. **Read `docs/index.json`** — parse entries array
2. **Count by category**:
   | Category | Count | Example IDs |
   |----------|-------|-------------|
   | decisions (ADR) | N | ADR-0001, ... |
   | architecture (ARCH) | N | ... |
   | patterns (PATTERN) | N | ... |
   | conventions (CONV) | N | ... |
   | features (FEAT) | N | ... |
   | findings (FINDING) | N | ... |
3. **Check staleness** — for each entry, check if referenced files still exist via Glob/Grep. Flag issues:
   - `BROKEN` — entry references file that doesn't exist
   - `GAP` — major code areas (routes, modules, deps) with no doc coverage
4. **Read project markers** — extract tech stack, scripts
5. **Present** → Step 3

## Step 2b — Has Flat Docs (force migrate)

Read flat docs and build migration manifest for Phase 2:

1. **Read each doc file** (first 50 lines) — write 1-2 line summary + classify target category (ADR/ARCH/PATTERN/CONV/FEAT/FINDING/GUIDE)
2. **Read project markers** (README, package.json, configs) — extract tech stack, scripts
3. **Build migration manifest** — list every flat doc with: `source path → target category → proposed ID`. Include this in the researcher report so Phase 2 has an explicit migration plan.
4. **Present** → Step 3

**Note:** Flat docs are always migrated — never left as-is. Phase 2 will convert them to structured KB with `index.json`.

## Step 2c — No Docs

Read project markers only (do NOT create files):

1. **Read** README, package.json/pom.xml/etc, tsconfig, Dockerfile — extract project name, tech stack, scripts, entry points
2. **Scan** directory structure (top 2 levels via `ls`)
3. **Present** → Step 3

## Step 3 — Present Insights

```markdown
## Project: {name}

**Tech Stack**: {framework} / {language} / {build tool}
**Key Commands**: `{dev}` | `{build}` | `{test}`

### Directory Structure
{top 2 levels}

### Entry Points
- {main files}

### Documentation Status
{one of:}
- "KB structure with N entries across M categories" + coverage table + any issues
- "Flat docs found (N files)" + list with summaries + migration suggestion
- "No docs/ directory found"
```

## Step 4 — Orchestrate Subagents

**CRITICAL: You MUST execute ALL 4 phases below in sequence. Do NOT stop after any phase to present "Next Steps" or ask the user what to do. Do NOT present choices. Run Phase 1 → Phase 2 → Phase 3 → Phase 4 automatically without pausing.**

Define shared report path before dispatching:
```
RESEARCH_REPORT = reports/{YYMMDD-HHMM}-get-started-research.md
```

Record the detected docs state from Step 2 as `DOCS_STATE`:
- `index.json` found → `"kb"`
- Flat docs found, no `index.json` → `"flat"`
- No docs → `"none"`

### Phase 1 — Research (conditional)

**Skip condition**: If `DOCS_STATE = "kb"` (index.json exists with entries), the KB already contains project context. Skip the researcher and proceed directly to Phase 2.

**Run condition**: If `DOCS_STATE = "flat"` or `DOCS_STATE = "none"`, dispatch the researcher first — the docs-manager needs codebase context to generate a meaningful KB.

```
if DOCS_STATE != "kb":
  Agent(
    subagent_type: "epost-researcher"
    description: "Research codebase for onboarding"
    prompt: """
    Explore the codebase at {CWD}. Read-only — do NOT create or edit files.

    Goals:
    1. Read README, package.json/pom.xml/Cargo.toml/Package.swift, Dockerfile, CI configs
    2. Scan top 3 directory levels (ls)
    3. Identify: tech stack, language, framework, key entry points, major modules
    4. Extract build + run commands (install, dev, build, test, start)
    5. Identify env requirements (.env.example, required secrets/vars)
    6. Note existing docs structure (docs/index.json, flat docs, or none)

    Write concise report to: {RESEARCH_REPORT}

    ## Tech Stack
    ## Entry Points
    ## Build & Run Commands
    ## Env Requirements
    ## Docs Status
    ## Key Findings (anything unusual or important)
    """
  )
  WAIT for Agent to complete, then READ {RESEARCH_REPORT}.
else:
  # KB exists — read docs/index.json directly for project context
  READ docs/index.json → extract tech stack, categories, entry count
  # No researcher needed — proceed to Phase 2
```
**Then immediately proceed to Phase 2 — do NOT stop here.**

### Phase 2 — Documentation (epost-docs-manager)

Use the Agent tool to dispatch docs agent with mode derived from DOCS_STATE:

```
Agent(
  subagent_type: "epost-docs-manager"
  description: "Generate/update KB docs"
  prompt: """
  Read the researcher report at: {RESEARCH_REPORT}

  Docs state: {DOCS_STATE}

  Based on docs state, apply the matching workflow:
  - DOCS_STATE = "none"  → run docs-init workflow: generate full KB structure in docs/ with index.json
  - DOCS_STATE = "flat"  → FORCE MIGRATE: convert ALL flat docs to KB structure (ADR/ARCH/PATTERN/CONV/FEAT/FINDING/GUIDE + index.json). Use migration manifest from researcher report. Move files, do not leave originals. Every flat doc must end up categorized in the KB.
  - DOCS_STATE = "kb"    → run docs-update --verify workflow: check all entries, flag STALE/BROKEN/GAP

  Apply templates from knowledge-retrieval skill. Keep all files under 800 LOC.
  Update docs/index.json after all changes.
  """
)
```
WAIT for Agent to complete.
**Then immediately proceed to Phase 3 — do NOT stop here.**

### Phase 3 — Environment Setup & Run (epost-fullstack-developer)

Use the Agent tool to dispatch implementer to prepare the environment and get the project running.
The implementer should **actively run steps** — not just report them. Go as far as possible automatically.

```
Agent(
  subagent_type: "epost-fullstack-developer"
  description: "Setup env, install deps, build, run project on simulator/device"
  prompt: """
  Read the researcher report at: {RESEARCH_REPORT}

  Your job: get this project running locally. Run every step below. If a step requires sudo and
  fails, SKIP it and continue — do NOT stop. Collect all sudo-blocked steps for the final report.

  ## Step 1 — Install missing tools
  Check what's missing and install (WITHOUT sudo first — if blocked, skip and continue):
  - `mvn` not found but `./mvnw` exists → use `./mvnw` (no install needed)
  - `mvn` not found, no wrapper → `brew install maven`
  - `node`/`npm` not found → `brew install node`
  - `java` not found → `brew install openjdk@{version from pom.xml}`
  - `docker` not found → note it, skip (requires Docker Desktop)
  - `bundle` not found → `gem install bundler` (no sudo)
  - `xcode-select` misconfigured → try `xcode-select -s /Applications/Xcode.app/Contents/Developer` (needs sudo — if blocked, skip and proceed)

  ## Step 2 — Install project dependencies
  - Node: `npm install` / `yarn` / `bun install`
  - Ruby/Fastlane: `bundle install` (if Gemfile present)
  - CocoaPods: `pod install` (if Podfile present)
  - Maven: `./mvnw dependency:resolve`
  - Gradle: `./gradlew dependencies`
  - Swift: `swift package resolve`
  - Skip if already installed (node_modules/, Pods/, .m2/)

  ## Step 3 — Environment variables
  - If `.env.example` exists but `.env` missing: copy `.env.example` → `.env`, list vars needing real values
  - Do NOT fill in real secrets

  ## Step 4 — Build
  - Web: `npm run build` (or yarn/bun equivalent)
  - iOS: `xcodebuild -scheme '{scheme}' -destination 'platform=iOS Simulator,name=iPhone 16 Pro' -sdk iphonesimulator build`
    - Use scheme from researcher report (prefer "Development" or "Dev" variant)
    - If `fastlane` available: `fastlane ios DEV` (equivalent, cleaner)
  - Android: `./gradlew assembleDebug`
  - Backend: `./mvnw package -DskipTests`
  - Note exact error if build fails

  ## Step 5 — Start (web/backend only)
  - Web: run dev command, confirm "ready on port X"
  - Backend: start server, confirm startup message
  - Skip for iOS/Android (handled in Step 6/7)

  ## Step 6 — Launch on iOS Simulator (iOS projects only)
  Only if `*.xcodeproj` or `*.xcworkspace` found in Step 1 detection:
  1. List available simulators: `xcrun simctl list devices available --json`
  2. Boot target simulator: `xcrun simctl boot "iPhone 16 Pro"` (or use already-booted device)
  3. Open Simulator app: `open -a Simulator`
  4. Find built .app: search DerivedData for `{scheme}-*.app`
  5. Install: `xcrun simctl install booted {app_path}`
  6. Get bundle ID from Info.plist: `defaults read {app_path}/Info.plist CFBundleIdentifier`
  7. Launch: `xcrun simctl launch booted {bundle_id}`
  8. Confirm: `xcrun simctl get_app_container booted {bundle_id}` (exit 0 = running)
  Note: For advanced simulator lifecycle management, use `/simulator` skill.

  ## Step 7 — Launch on Android Emulator/Device (Android projects only)
  Only if `build.gradle` or `build.gradle.kts` found:
  1. Check connected devices: `adb devices`
  2. If device connected: `./gradlew installDebug`, then `adb shell am start -n {package}/{main_activity}`
  3. If no device: list AVDs with `emulator -list-avds`, boot first: `emulator -avd {avd_name} &`, wait for boot, then install
  4. Confirm launch via `adb shell dumpsys activity | grep {package}`

  ## Output
  For each step: what ran, what succeeded, what was blocked (sudo or otherwise) and why.
  Final line: "App launch: running on {device/simulator name}" OR "Not launched — {reason}"
  Manual steps (sudo required): {list, or "none"}
  """
)
```
WAIT for Agent to complete.
**Then immediately proceed to Phase 4 — do NOT stop here.**

### Phase 4 — Final Summary

Present a consolidated onboarding summary (this is the ONLY place you stop).
If the implementer reported blockers, include a **Setup Guide** with exact commands the user needs to run manually:

```markdown
## Onboarded: {project-name}

**Tech Stack**: {from researcher}
**Running**: {status — simulator/device name and app status, or URL:port, or "not started"}

### What Was Done
- {tools installed, deps resolved, build result, launch result}

### Docs
{count} entries generated/updated in docs/index.json

### Manual Steps Required (if any)
Steps that couldn't run automatically (need sudo or manual action):

1. {e.g., "Fix xcode-select: `sudo xcode-select -s /Applications/Xcode.app/Contents/Developer`"}
2. {e.g., "Then build: `fastlane ios DEV` or `xcodebuild -scheme 'ePost Development' ...`"}
3. {e.g., "Authenticate to GCP Artifact Registry: `gcloud auth configure-docker ...`"}
(omit section entirely if no manual steps needed)

### Next Steps
- `/simulator` — manage iOS simulators
- `/{cook|fix|plan}` to start coding
```

## Rules

- **MUST run all 4 phases** — do NOT stop, present choices, or ask user between phases
- **Fast detection** — Steps 1–3 are lightweight scan only, < 15s
- **Sequential dispatch** — use Agent tool for each phase, wait for completion before next
- **Shared report** — researcher writes to `reports/`, other agents read from it
- Only stop early if the user has a specific question (answer from what was read, suggest `/scout` for deeper exploration)
