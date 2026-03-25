#!/usr/bin/env node
/**
 * kit-session-check.cjs — SessionStart Hook
 *
 * At session start, checks if skill-index.json is stale relative to any SKILL.md
 * under packages/. Warns if stale; always exits 0.
 *
 * Only active in kit repo context (packages/ dir must exist).
 *
 * Exit Codes:
 *   0 - Always (non-blocking)
 */

try {

const fs = require('fs');
const path = require('path');

/**
 * Recursively find all SKILL.md files under a directory.
 * Returns the newest mtime in ms, or null if none found.
 */
function newestSkillMdMtime(dir) {
  let newest = null;
  try {
    const entries = fs.readdirSync(dir, { withFileTypes: true });
    for (const entry of entries) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) {
        const sub = newestSkillMdMtime(full);
        if (sub !== null && (newest === null || sub > newest)) newest = sub;
      } else if (entry.isFile() && entry.name === 'SKILL.md') {
        try {
          const mtime = fs.statSync(full).mtimeMs;
          if (newest === null || mtime > newest) newest = mtime;
        } catch { /* skip unreadable */ }
      }
    }
  } catch { /* skip unreadable dir */ }
  return newest;
}

function main() {
  // Guard: only active when packages/ dir exists
  const packagesDir = path.resolve(process.cwd(), 'packages');
  try {
    const stat = fs.statSync(packagesDir);
    if (!stat.isDirectory()) process.exit(0);
  } catch {
    process.exit(0);
  }

  const indexPath = path.resolve(process.cwd(), 'packages/core/skills/skill-index.json');

  let indexMtime = null;
  try {
    indexMtime = fs.statSync(indexPath).mtimeMs;
  } catch {
    // Index doesn't exist yet — not a stale warning, could be fresh clone
    process.exit(0);
  }

  const newestSkillMd = newestSkillMdMtime(packagesDir);

  if (newestSkillMd !== null && newestSkillMd > indexMtime) {
    console.log(
      '[Kit Dev] skill-index.json is stale — a SKILL.md was modified after the last index build.\n' +
      'Run: node .claude/scripts/generate-skill-index.cjs'
    );
  }

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
