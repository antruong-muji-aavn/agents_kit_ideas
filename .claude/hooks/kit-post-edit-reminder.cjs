#!/usr/bin/env node
/**
 * kit-post-edit-reminder.cjs — PostToolUse(Edit|Write|MultiEdit) Hook
 *
 * General post-edit dispatcher for kit-repo context. Self-guards via packages/ dir check.
 * Dispatches reminders based on what was edited:
 *
 *   SKILL.md under packages/         → skill index stale warning
 *   hooks/*.cjs under packages/      → re-init reminder (throttled) + stale reference scan
 *   scripts/*.cjs under packages/    → stale reference scan
 *   agents/*.md, package.yaml        → re-init reminder (throttled)
 *
 * Exit Codes:
 *   0 - Always (non-blocking)
 */

try {

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const THROTTLE_FILE = '/tmp/epost-pkg-edit-reminded.json';
const THROTTLE_MS = 10 * 60 * 1000; // 10 minutes

function isThrottled() {
  try {
    const data = JSON.parse(fs.readFileSync(THROTTLE_FILE, 'utf-8'));
    return Date.now() - data.ts < THROTTLE_MS;
  } catch { return false; }
}

function setThrottle() {
  try { fs.writeFileSync(THROTTLE_FILE, JSON.stringify({ ts: Date.now() })); } catch { /* non-critical */ }
}

/**
 * Scan for stale references to the given basename across packages/ and .claude/.
 * Returns an array of file paths that contain the reference.
 */
function findStaleReferences(baseName, cwd) {
  try {
    const result = execSync(
      `grep -rl "${baseName}" "${path.join(cwd, 'packages')}" "${path.join(cwd, '.claude')}" --include="*.cjs" --include="*.json" --include="*.md" 2>/dev/null || true`,
      { encoding: 'utf-8', timeout: 5000 }
    ).trim();
    return result ? result.split('\n').filter(Boolean) : [];
  } catch { return []; }
}

function main() {
  // Guard: only active in kit repo context (packages/ dir must exist)
  const cwd = process.cwd();
  const packagesDir = path.resolve(cwd, 'packages');
  try {
    if (!fs.statSync(packagesDir).isDirectory()) process.exit(0);
  } catch {
    process.exit(0);
  }

  let hookData;
  try {
    hookData = JSON.parse(fs.readFileSync(0, 'utf-8'));
  } catch {
    process.exit(0);
  }

  const filePath = (hookData.tool_input || {}).file_path || '';
  if (!filePath.includes('/packages/')) process.exit(0);

  const basename = path.basename(filePath);
  const isSkillMd = basename === 'SKILL.md';
  const isHook = filePath.includes('/hooks/') && basename.endsWith('.cjs');
  const isScript = filePath.includes('/scripts/') && basename.endsWith('.cjs');
  const isPackageYaml = basename === 'package.yaml';
  const isAgentMd = filePath.includes('/agents/') && basename.endsWith('.md');

  const messages = [];

  // SKILL.md edited → skill index is stale
  if (isSkillMd) {
    messages.push(
      '[Kit Dev] SKILL.md edited under packages/. Skill discovery index is now stale.\n' +
      'Run: node .claude/scripts/generate-skill-index.cjs'
    );
  }

  // Hook or script edited → scan for stale references
  if (isHook || isScript) {
    const nameWithoutExt = basename.replace(/\.cjs$/, '');
    const refs = findStaleReferences(nameWithoutExt, cwd);
    // Exclude the file itself from results
    const absFilePath = path.resolve(cwd, filePath);
    const otherRefs = refs.filter(r => path.resolve(r) !== absFilePath);

    if (otherRefs.length > 0) {
      messages.push(
        `[Kit Dev] Hook/script edited: ${basename}. Found ${otherRefs.length} reference(s) — verify none are stale:\n` +
        otherRefs.map(r => `  ${path.relative(cwd, r)}`).join('\n')
      );
    } else {
      messages.push(
        `[Kit Dev] Hook/script edited: ${basename}. No stale references found in packages/ or .claude/.`
      );
    }
  }

  // Package-level file edited → re-init reminder (throttled)
  if ((isHook || isAgentMd || isPackageYaml) && !isThrottled()) {
    setThrottle();
    messages.push(
      '[Kit Dev] packages/ file edited. .claude/ won\'t reflect this until re-initialized.\n' +
      'Run: epost-kit init'
    );
  }

  if (messages.length === 0) process.exit(0);

  const response = {
    additionalContext: messages.join('\n\n')
  };
  process.stdout.write(JSON.stringify(response) + '\n');
  process.exit(0);
}

main();

} catch (e) {
  try {
    const fs = require('fs');
    const p = require('path');
    const logDir = p.join(__dirname, '.logs');
    if (!fs.existsSync(logDir)) fs.mkdirSync(logDir, { recursive: true });
    fs.appendFileSync(
      p.join(logDir, 'hook-log.jsonl'),
      JSON.stringify({ ts: new Date().toISOString(), hook: p.basename(__filename, '.cjs'), status: 'crash', error: e.message }) + '\n'
    );
  } catch (_) {}
  process.exit(0); // fail-open
}
