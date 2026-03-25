#!/usr/bin/env node
/**
 * kit-write-guard.cjs — PreToolUse(Write|Edit) Hook
 *
 * Hard-blocks writes to .claude/ — which is generated output wiped on epost-kit init.
 * Only active in kit repo context (packages/ dir must exist).
 *
 * Exit Codes:
 *   0 - Allow (not a kit repo, or file not under .claude/)
 *   2 - Block (file targets .claude/ — must edit under packages/ instead)
 */

try {

const fs = require('fs');
const path = require('path');
const { isHookEnabled } = require('./lib/epost-config-utils.cjs');

if (!isHookEnabled('packagesGuard')) process.exit(0);

function main() {
  // Guard: only active when packages/ dir exists (kit repo context)
  const packagesDir = path.resolve(process.cwd(), 'packages');
  try {
    const stat = fs.statSync(packagesDir);
    if (!stat.isDirectory()) process.exit(0);
  } catch {
    process.exit(0); // no packages/ dir — user project, exit safe
  }

  let input = '';
  try {
    input = fs.readFileSync(0, 'utf-8');
  } catch {
    process.exit(0);
  }

  let hookData;
  try {
    hookData = JSON.parse(input);
  } catch {
    process.exit(0);
  }

  const filePath = (hookData.tool_input || {}).file_path;
  if (!filePath) process.exit(0);

  const absFilePath = path.resolve(process.cwd(), filePath);
  const claudeDir = path.resolve(process.cwd(), '.claude') + path.sep;

  // Block if file is under .claude/
  if (absFilePath.startsWith(claudeDir) || absFilePath === path.resolve(process.cwd(), '.claude')) {
    const response = {
      decision: 'block',
      reason: [
        '.claude/ is generated output — wiped on next epost-kit init.',
        'Edit under packages/ instead, then run: epost-kit init'
      ].join('\n')
    };
    process.stdout.write(JSON.stringify(response) + '\n');
    process.exit(2);
  }

  process.exit(0);
}

main();

} catch (e) {
  // Minimal crash logging — only Node builtins, no lib/ deps
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
