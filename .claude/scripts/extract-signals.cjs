#!/usr/bin/env node
/**
 * Signal Extractor — Parse journal entries and audit reports for skill improvement signals
 *
 * Reads: docs/journal/**\/*.md, reports/*.md
 * Writes: docs/proposals/signals.json
 *
 * Usage:
 *   node .claude/scripts/extract-signals.cjs           # run with console output
 *   node .claude/scripts/extract-signals.cjs --silent  # suppress output (hook mode)
 *
 * Idempotent: signals are keyed by (source+excerpt) hash — re-running is safe.
 */

'use strict';

const fs   = require('fs');
const path = require('path');
const crypto = require('crypto');

const SILENT = process.argv.includes('--silent');
const log = (...args) => { if (!SILENT) console.log(...args); };
const warn = (...args) => { if (!SILENT) console.warn(...args); };

const ROOT         = process.cwd();
const JOURNAL_DIR  = path.join(ROOT, 'docs', 'journal');
const REPORTS_DIR  = path.join(ROOT, 'reports');
const PROPOSALS_DIR = path.join(ROOT, 'docs', 'proposals');
const SIGNALS_FILE = path.join(PROPOSALS_DIR, 'signals.json');
const SKILL_INDEX  = path.join(ROOT, '.claude', 'skills', 'skill-index.json');

// --- Workaround pattern keywords ------------------------------------------
const WORKAROUND_PATTERNS = [
  /workaround[:\s]/i,
  /used\s+\S+\s+instead\s+of/i,
  /had\s+to\s+use\s+\S+\s+instead/i,
  /couldn['']t\s+use/i,
  /bypassed/i,
  /fell\s+back\s+to/i,
  /fallback\s+to/i,
];

// --- Audit failure patterns ------------------------------------------------
const AUDIT_FAIL_PATTERNS = [
  /\bFAIL\b/,
  /\bfailed\b/i,
  /audit.*verdict.*fail/i,
  /\bWARNING\b.*critical/i,
];

// --- Load known skill names from skill-index.json -------------------------
function loadSkillNames() {
  try {
    const idx = JSON.parse(fs.readFileSync(SKILL_INDEX, 'utf-8'));
    return (idx.skills || []).map(s => s.name);
  } catch {
    // Fallback list of core skills when index not available
    return [
      'skill-discovery', 'knowledge-capture', 'journal', 'cook', 'plan',
      'debug', 'core', 'kit', 'fix', 'test', 'docs', 'audit', 'review',
      'git', 'research', 'web-frontend', 'web-nextjs', 'web-api-routes',
      'ios-development', 'android-development', 'backend-javaee',
      'design-tokens', 'figma', 'a11y', 'ios-a11y', 'android-a11y', 'web-a11y',
    ];
  }
}

// --- Infer target skill from text -----------------------------------------
function inferTargetSkill(text, skillNames) {
  const lower = text.toLowerCase();
  // Prefer longer skill names (more specific) to avoid partial matches
  const sorted = [...skillNames].sort((a, b) => b.length - a.length);
  for (const name of sorted) {
    if (lower.includes(name.toLowerCase())) return name;
  }
  // Keyword fallbacks
  if (/hook|settings\.json|permission/i.test(text)) return 'core';
  if (/epost-kit|package\.yaml|bundles\.yaml/i.test(text)) return 'kit';
  return 'unknown';
}

// --- Hash a signal to a stable 8-char ID ----------------------------------
function signalId(source, excerpt) {
  return 'sig-' + crypto
    .createHash('sha256')
    .update(source + '|' + excerpt.slice(0, 100))
    .digest('hex')
    .slice(0, 8);
}

// --- Recursively find files matching a pattern ----------------------------
function findFiles(dir, predicate, results = []) {
  if (!fs.existsSync(dir)) return results;
  for (const entry of fs.readdirSync(dir)) {
    const full = path.join(dir, entry);
    try {
      const stat = fs.statSync(full);
      if (stat.isDirectory()) {
        findFiles(full, predicate, results);
      } else if (predicate(entry)) {
        results.push(full);
      }
    } catch { /* skip unreadable */ }
  }
  return results;
}

// --- Extract section content between two ## headers ----------------------
function extractSection(lines, headerText) {
  const sections = [];
  let capturing = false;
  let buffer = [];

  for (const line of lines) {
    if (/^##\s/.test(line)) {
      if (capturing) {
        const text = buffer.join('\n').trim();
        if (text) sections.push(text);
        buffer = [];
      }
      capturing = line.toLowerCase().includes(headerText.toLowerCase());
      continue;
    }
    if (capturing) buffer.push(line);
  }
  if (capturing) {
    const text = buffer.join('\n').trim();
    if (text) sections.push(text);
  }
  return sections;
}

// --- Parse journal entry for signals --------------------------------------
function parseJournalSignals(filePath, skillNames) {
  const signals = [];
  let content;
  try { content = fs.readFileSync(filePath, 'utf-8'); } catch { return signals; }
  const lines = content.split('\n');
  const relPath = path.relative(ROOT, filePath);

  // Extract "What almost went wrong" sections
  const wrongSections = extractSection(lines, 'what almost went wrong');
  for (const text of wrongSections) {
    if (text.length < 20) continue; // too short to be meaningful
    signals.push({
      id: signalId(relPath, text),
      type: 'journal-flag',
      confidence: 'low',  // upgraded later by occurrence counting
      source: relPath,
      excerpt: text.slice(0, 300),
      targetSkill: inferTargetSkill(text, skillNames),
      suggestedAction: '',
      detectedAt: new Date().toISOString(),
      status: 'new',
    });
  }

  // Detect workaround patterns anywhere in the file
  const allText = content;
  for (const pattern of WORKAROUND_PATTERNS) {
    const match = allText.match(pattern);
    if (!match) continue;
    // Extract surrounding sentence (~100 chars)
    const idx = match.index;
    const excerpt = allText.slice(Math.max(0, idx - 30), idx + 120).trim();
    signals.push({
      id: signalId(relPath, excerpt),
      type: 'workaround',
      confidence: 'low',
      source: relPath,
      excerpt: excerpt.slice(0, 300),
      targetSkill: inferTargetSkill(excerpt, skillNames),
      suggestedAction: '',
      detectedAt: new Date().toISOString(),
      status: 'new',
    });
    break; // one workaround signal per file per pattern is enough
  }

  return signals;
}

// --- Parse audit/planner report for signals --------------------------------
function parseReportSignals(filePath, skillNames) {
  const signals = [];
  let content;
  try { content = fs.readFileSync(filePath, 'utf-8'); } catch { return signals; }
  const lines = content.split('\n');
  const relPath = path.relative(ROOT, filePath);

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (!AUDIT_FAIL_PATTERNS.some(p => p.test(line))) continue;
    // Extract a snippet around the failure
    const start = Math.max(0, i - 1);
    const end = Math.min(lines.length - 1, i + 3);
    const excerpt = lines.slice(start, end + 1).join('\n').trim();
    if (excerpt.length < 20) continue;

    signals.push({
      id: signalId(relPath, excerpt),
      type: 'audit-failure',
      confidence: 'low',
      source: relPath,
      excerpt: excerpt.slice(0, 300),
      targetSkill: inferTargetSkill(excerpt, skillNames),
      suggestedAction: '',
      detectedAt: new Date().toISOString(),
      status: 'new',
    });
    i += 2; // skip ahead to avoid duplicate snippets
  }

  return signals;
}

// --- Upgrade confidence based on occurrence counts -------------------------
// Rules: 3+ audit-failure for same targetSkill → high
//        2+ journal-flag or workaround for same targetSkill → medium
//        1 occurrence → low
function upgradeConfidence(signals) {
  // Count (type, targetSkill) occurrences
  const counts = {};
  for (const s of signals) {
    const key = `${s.type}|${s.targetSkill}`;
    counts[key] = (counts[key] || 0) + 1;
  }

  return signals.map(s => {
    const key = `${s.type}|${s.targetSkill}`;
    const count = counts[key] || 1;
    let confidence = 'low';
    if (s.type === 'audit-failure' && count >= 3) confidence = 'high';
    else if (s.type === 'audit-failure' && count >= 2) confidence = 'medium';
    else if ((s.type === 'journal-flag' || s.type === 'workaround') && count >= 2) confidence = 'medium';
    return { ...s, confidence };
  });
}

// --- Compute summary table ------------------------------------------------
function computeSummary(signals) {
  const bySkill = {};
  const byType = { 'journal-flag': 0, 'audit-failure': 0, 'workaround': 0 };
  let newCount = 0, proposedCount = 0, dismissedCount = 0;

  for (const s of signals) {
    byType[s.type] = (byType[s.type] || 0) + 1;
    if (!bySkill[s.targetSkill]) {
      bySkill[s.targetSkill] = { total: 0, high: 0, medium: 0, low: 0 };
    }
    bySkill[s.targetSkill].total++;
    bySkill[s.targetSkill][s.confidence]++;
    if (s.status === 'new') newCount++;
    if (s.status === 'proposed') proposedCount++;
    if (s.status === 'dismissed') dismissedCount++;
  }

  return {
    total: signals.length,
    new: newCount,
    proposed: proposedCount,
    dismissed: dismissedCount,
    bySkill,
    byType,
  };
}

// --- Merge new signals into existing (dedup by id) -------------------------
function mergeSignals(existing, incoming) {
  const map = new Map(existing.map(s => [s.id, s]));
  let added = 0;
  for (const s of incoming) {
    if (!map.has(s.id)) {
      map.set(s.id, s);
      added++;
    }
    // Preserve status of existing signals (don't reset proposed → new)
  }
  return { merged: [...map.values()], added };
}

// --- Print summary table --------------------------------------------------
function printSummary(summary) {
  log('\n┌─ Signal Summary ──────────────────────────────────┐');
  log(`│  Total: ${summary.total}  New: ${summary.new}  Proposed: ${summary.proposed}  Dismissed: ${summary.dismissed}`);
  log('│');
  log('│  By type:');
  for (const [type, count] of Object.entries(summary.byType)) {
    if (count > 0) log(`│    ${type.padEnd(20)} ${count}`);
  }
  log('│');
  log('│  By skill (new signals only):');
  const skills = Object.entries(summary.bySkill)
    .filter(([, v]) => v.total > 0)
    .sort(([, a], [, b]) => b.total - a.total);
  if (skills.length === 0) {
    log('│    (none)');
  } else {
    log(`│    ${'Skill'.padEnd(28)} ${'Total'.padEnd(6)} High   Med   Low`);
    log(`│    ${'-'.repeat(52)}`);
    for (const [skill, counts] of skills) {
      const name = skill.length > 26 ? skill.slice(0, 25) + '…' : skill;
      log(`│    ${name.padEnd(28)} ${String(counts.total).padEnd(6)} ${String(counts.high).padEnd(6)} ${String(counts.medium).padEnd(6)} ${counts.low}`);
    }
  }
  log('└────────────────────────────────────────────────────┘\n');
}

// --- Main -----------------------------------------------------------------
function main() {
  // Ensure proposals dir exists
  if (!fs.existsSync(PROPOSALS_DIR)) fs.mkdirSync(PROPOSALS_DIR, { recursive: true });

  const skillNames = loadSkillNames();
  log(`Loaded ${skillNames.length} skill names`);

  // Collect raw signals
  const rawSignals = [];

  // Parse journal entries
  const journalFiles = findFiles(JOURNAL_DIR, f => f.endsWith('.md') && f !== 'README.md');
  log(`Scanning ${journalFiles.length} journal files...`);
  for (const f of journalFiles) {
    const sigs = parseJournalSignals(f, skillNames);
    rawSignals.push(...sigs);
  }

  // Parse report files (planner/researcher reports, not fullstack-developer reports which may not have audit content)
  const reportFiles = findFiles(REPORTS_DIR, f => f.endsWith('.md'));
  log(`Scanning ${reportFiles.length} report files...`);
  for (const f of reportFiles) {
    const sigs = parseReportSignals(f, skillNames);
    rawSignals.push(...sigs);
  }

  log(`Found ${rawSignals.length} raw signals (before dedup)`);

  // Upgrade confidence based on occurrence counts
  const scoredSignals = upgradeConfidence(rawSignals);

  // Load existing signals
  let existing = [];
  try {
    const stored = JSON.parse(fs.readFileSync(SIGNALS_FILE, 'utf-8'));
    existing = stored.signals || [];
  } catch { /* first run */ }

  // Merge (preserve existing statuses, add new)
  const { merged, added } = mergeSignals(existing, scoredSignals);
  log(`Added ${added} new signals (${merged.length} total after merge)`);

  // Recompute summary over all merged signals
  const summary = computeSummary(merged);

  // Write output
  const output = {
    generated: new Date().toISOString(),
    summary,
    signals: merged,
  };

  fs.writeFileSync(SIGNALS_FILE, JSON.stringify(output, null, 2), 'utf-8');
  log(`\nWrote ${merged.length} signals to ${path.relative(ROOT, SIGNALS_FILE)}`);

  printSummary(summary);
}

try {
  main();
  process.exit(0);
} catch (err) {
  if (!SILENT) console.error('Fatal:', err.message);
  process.exit(1);
}
