#!/usr/bin/env node
/**
 * Agent Navigation Header Generator
 *
 * Inserts/updates HTML comment navigation blocks at the top of each agent file
 * (after frontmatter). Includes: summary, intention routing table, handoff targets,
 * and section index with approximate line references.
 *
 * Usage: node generate-agent-nav-headers.cjs [agents-dir1,agents-dir2,...]
 * Default: scans packages/{pkg}/agents/epost-{name}.md
 */

const fs = require('fs');
const path = require('path');

// --- Agent routing data (derived from CLAUDE.md intent map) ---
const ROUTING_DATA = {
  'epost-fullstack-developer': {
    summary: 'Executes implementation phases with strict file ownership. Handles backend, frontend, and infrastructure.',
    routing: [
      { signal: '"cook", "implement", "build", "create", "add", "make", "continue"', source: 'orchestrator', action: 'Execute implementation phase' },
      { signal: 'Plan handoff', source: 'epost-planner', action: 'Receive plan, begin coding' },
      { signal: 'Multi-step delegation', source: 'epost-project-manager', action: 'Execute assigned task' },
    ],
    handoffs: ['epost-code-reviewer (review code)'],
  },
  'epost-planner': {
    summary: 'Creates phased implementation plans with complexity auto-detection (fast/deep/parallel).',
    routing: [
      { signal: '"plan", "design", "architect", "spec", "roadmap"', source: 'orchestrator', action: 'Create implementation plan' },
      { signal: 'Ideation complete', source: 'epost-brainstormer', action: 'Formalize ideas into plan' },
      { signal: 'Research complete', source: 'epost-researcher', action: 'Plan based on findings' },
    ],
    handoffs: ['epost-fullstack-developer (implement plan)'],
  },
  'epost-debugger': {
    summary: 'Finds root causes of bugs, test failures, and runtime errors with systematic investigation.',
    routing: [
      { signal: '"debug", "trace", "inspect", "diagnose"', source: 'orchestrator', action: 'Investigate issue' },
      { signal: '"broken", "error", "crash", "failing"', source: 'orchestrator', action: 'Fix/Debug routing' },
      { signal: 'Build/CI failure', source: 'orchestrator (auto)', action: 'Diagnose failure' },
    ],
    handoffs: ['epost-tester (verify fix)'],
  },
  'epost-code-reviewer': {
    summary: 'Enforces code quality, security audits, and best practices. Read-only — never modifies code.',
    routing: [
      { signal: '"review", "check code", "audit" (code-level)', source: 'orchestrator', action: 'Review code changes' },
      { signal: 'Implementation complete', source: 'epost-fullstack-developer', action: 'Post-implementation review' },
      { signal: 'Hybrid audit flow', source: 'orchestrator (audit skill)', action: 'Review after muji audit' },
    ],
    handoffs: ['epost-git-manager (ship changes)'],
  },
  'epost-tester': {
    summary: 'Writes and runs tests, validates coverage, ensures code quality through comprehensive testing.',
    routing: [
      { signal: '"test", "coverage", "validate", "verify"', source: 'orchestrator', action: 'Write/run tests' },
      { signal: 'Debug fix complete', source: 'epost-debugger', action: 'Verify fix with tests' },
    ],
    handoffs: ['epost-git-manager (ship after tests pass)'],
  },
  'epost-researcher': {
    summary: 'Conducts technology research, best practices analysis, and documentation synthesis.',
    routing: [
      { signal: '"how does X work", "best practices", "compare", "research"', source: 'orchestrator', action: 'Research topic' },
      { signal: 'Planning research fan-out', source: 'epost-planner', action: 'Parallel research task' },
    ],
    handoffs: ['epost-planner (create plan from findings)'],
  },
  'epost-project-manager': {
    summary: 'Tracks progress, routes ambiguous requests, manages plan lifecycle and multi-step workflows.',
    routing: [
      { signal: 'Ambiguous intent (fallback)', source: 'orchestrator', action: 'Classify and route' },
      { signal: 'Multi-step workflow', source: 'orchestrator', action: 'Decompose and delegate' },
      { signal: '"status", "progress", "roadmap"', source: 'orchestrator', action: 'Report project status' },
    ],
    handoffs: ['epost-planner (create plan)', 'epost-fullstack-developer (implement)'],
  },
  'epost-git-manager': {
    summary: 'Automates git workflows: stage, commit, push, PR creation with security scanning.',
    routing: [
      { signal: '"commit", "push", "pr", "merge", "done", "ship"', source: 'orchestrator', action: 'Execute git workflow' },
      { signal: 'Work complete', source: 'any agent (handoff)', action: 'Commit and push changes' },
    ],
    handoffs: [],
  },
  'epost-docs-manager': {
    summary: 'Writes, updates, migrates, and audits project documentation and KB structure.',
    routing: [
      { signal: '"docs", "document", "write docs", "migrate docs"', source: 'orchestrator', action: 'Manage documentation' },
      { signal: 'Docs gap identified', source: 'epost-muji', action: 'Fill documentation gap' },
    ],
    handoffs: ['epost-git-manager (ship docs)'],
  },
  'epost-brainstormer': {
    summary: 'Creative ideation and problem-solving — explores options before formal planning.',
    routing: [
      { signal: '"brainstorm", "think about", "explore options", "trade-offs"', source: 'orchestrator', action: 'Generate ideas' },
      { signal: 'Architecture review', source: 'orchestrator', action: 'Explore design alternatives' },
    ],
    handoffs: ['epost-planner (create plan from ideas)'],
  },
  'epost-journal-writer': {
    summary: 'Documents significant difficulties, failures, and decisions with emotional authenticity.',
    routing: [
      { signal: '3+ test failures (auto)', source: 'internal trigger', action: 'Document failure journal' },
      { signal: 'Critical bug or redesign (auto)', source: 'internal trigger', action: 'Document decision log' },
      { signal: '"journal", "document difficulty"', source: 'orchestrator', action: 'Write journal entry' },
    ],
    handoffs: [],
  },
  'epost-mcp-manager': {
    summary: 'Discovers and manages MCP server integrations — tools, prompts, and resources.',
    routing: [
      { signal: 'MCP tool discovery needed', source: 'orchestrator', action: 'Discover MCP capabilities' },
      { signal: 'Template E delegation (non-RAG)', source: 'epost-muji', action: 'Handle MCP integration' },
    ],
    handoffs: [],
  },
  'epost-a11y-specialist': {
    summary: 'Multi-platform accessibility orchestrator — WCAG 2.1 AA auditing, guidance, and batch fixing.',
    routing: [
      { signal: '"a11y", "accessibility", "wcag", "VoiceOver", "TalkBack", "ARIA"', source: 'orchestrator', action: 'Handle a11y task' },
      { signal: 'A11y escalation', source: 'epost-code-reviewer', action: 'Deep a11y review' },
      { signal: 'A11y findings in UI audit', source: 'epost-muji', action: 'Fix a11y violations' },
    ],
    handoffs: ['epost-fullstack-developer (fix violations)'],
  },
  'epost-muji': {
    summary: 'Design system specialist — component knowledge, Figma-to-code, UI/UX design, landing pages.',
    routing: [
      { signal: '"design", "component", "UI/UX", "figma", "klara-theme"', source: 'orchestrator', action: 'Design system task' },
      { signal: '"landing page", "prototype"', source: 'orchestrator', action: 'Craft landing page' },
      { signal: 'Hybrid audit (Template A+)', source: 'orchestrator (audit skill)', action: 'Component audit' },
    ],
    handoffs: ['epost-fullstack-developer (implement component)'],
  },
  'epost-kit-designer': {
    summary: 'Creates and maintains agents, skills, commands, and hooks for epost_agent_kit packages.',
    routing: [
      { signal: '"create agent", "add skill", "add hook", "kit authoring"', source: 'orchestrator', action: 'Author kit component' },
      { signal: 'Kit maintenance task', source: 'orchestrator', action: 'Update kit ecosystem' },
    ],
    handoffs: [],
  },
};

// --- Header marker for idempotent insert/update ---
const NAV_START = '<!-- AGENT NAVIGATION';
const NAV_END = '-->';

/**
 * Find all agent .md files across packages
 */
function findAgentFiles(baseDirs) {
  const files = [];
  for (const dir of baseDirs) {
    if (!fs.existsSync(dir)) continue;
    const entries = fs.readdirSync(dir);
    for (const entry of entries) {
      if (entry.startsWith('epost-') && entry.endsWith('.md')) {
        files.push(path.join(dir, entry));
      }
    }
  }
  return files.sort();
}

/**
 * Parse frontmatter boundaries (returns index of the closing --- line)
 */
function findFrontmatterEnd(lines) {
  if (lines[0] !== '---') return -1;
  for (let i = 1; i < lines.length; i++) {
    if (lines[i] === '---') return i;
  }
  return -1;
}

/**
 * Extract ## headings with line numbers from content (after header insertion)
 * Skips headings inside fenced code blocks (``` or ~~~)
 * Returns array of { title, line }
 */
function extractSections(lines, startLine) {
  const sections = [];
  let inCodeBlock = false;
  for (let i = startLine; i < lines.length; i++) {
    // Toggle code fence state
    if (lines[i].match(/^```|^~~~/)) {
      inCodeBlock = !inCodeBlock;
      continue;
    }
    if (inCodeBlock) continue;

    const match = lines[i].match(/^## (.+)/);
    if (match) {
      sections.push({ title: match[1], line: i + 1 }); // 1-indexed
    }
  }
  return sections;
}

/**
 * Build the navigation header comment block
 */
function buildNavHeader(agentName, sections) {
  const data = ROUTING_DATA[agentName];
  if (!data) {
    console.warn(`  WARNING: No routing data for ${agentName}, skipping`);
    return null;
  }

  const lines = [];
  lines.push(`${NAV_START}`);
  lines.push(`## ${agentName}`);
  lines.push(`Summary: ${data.summary}`);
  lines.push('');

  // Intention routing table
  lines.push('### Intention Routing');
  lines.push('| Intent Signal | Source | Action |');
  lines.push('|---------------|--------|--------|');
  for (const r of data.routing) {
    lines.push(`| ${r.signal} | ${r.source} | ${r.action} |`);
  }

  // Handoff targets
  if (data.handoffs.length > 0) {
    lines.push('');
    lines.push('### Handoff Targets');
    for (const h of data.handoffs) {
      lines.push(`- → ${h}`);
    }
  }

  // Section index
  if (sections.length > 0) {
    lines.push('');
    lines.push('### Section Index');
    lines.push('| Section | Line |');
    lines.push('|---------|------|');
    for (const s of sections) {
      lines.push(`| ${s.title} | ~L${s.line} |`);
    }
  }

  lines.push(`${NAV_END}`);
  lines.push('');

  return lines.join('\n');
}

/**
 * Remove existing navigation header from content lines
 * Returns cleaned lines array
 */
function removeExistingNav(lines) {
  const cleaned = [];
  let inNav = false;
  let skipBlankAfterNav = false;

  for (let i = 0; i < lines.length; i++) {
    if (lines[i].startsWith(NAV_START)) {
      inNav = true;
      continue;
    }
    if (inNav) {
      if (lines[i].trimEnd() === NAV_END) {
        inNav = false;
        skipBlankAfterNav = true;
        continue;
      }
      continue;
    }
    // Skip one blank line after nav block removal
    if (skipBlankAfterNav && lines[i].trim() === '') {
      skipBlankAfterNav = false;
      continue;
    }
    skipBlankAfterNav = false;
    cleaned.push(lines[i]);
  }
  return cleaned;
}

/**
 * Process a single agent file
 */
function processAgentFile(filePath) {
  const content = fs.readFileSync(filePath, 'utf-8');
  let lines = content.split('\n');

  // Remove existing nav header for idempotent updates
  lines = removeExistingNav(lines);

  const fmEnd = findFrontmatterEnd(lines);
  if (fmEnd === -1) {
    console.warn(`  WARNING: No frontmatter in ${filePath}, skipping`);
    return false;
  }

  // Extract agent name from frontmatter
  const agentName = lines.find(l => l.startsWith('name:'))?.replace('name:', '').trim();
  if (!agentName) {
    console.warn(`  WARNING: No name in frontmatter of ${filePath}, skipping`);
    return false;
  }

  // Extract sections from body (after frontmatter)
  const bodyStart = fmEnd + 1;
  const sections = extractSections(lines, bodyStart);

  // Build header — we need to calculate line numbers AFTER insertion
  // The header will be inserted at bodyStart, pushing everything down
  // First build without sections to get header line count, then adjust
  const tempHeader = buildNavHeader(agentName, []);
  if (!tempHeader) return false;
  // +1 for the blank line we insert before the header
  const headerLineCount = tempHeader.split('\n').length + 1;

  // Adjust section line numbers by header offset
  const adjustedSections = sections.map(s => ({
    title: s.title,
    line: s.line + headerLineCount,
  }));

  const navHeader = buildNavHeader(agentName, adjustedSections);
  if (!navHeader) return false;

  // Insert after frontmatter — strip leading blanks from body to avoid doubles
  const before = lines.slice(0, fmEnd + 1);
  let after = lines.slice(fmEnd + 1);
  // Remove leading blank lines from body (we'll add exactly one)
  while (after.length > 0 && after[0].trim() === '') {
    after.shift();
  }

  const newContent = [...before, '', navHeader, ...after].join('\n');
  fs.writeFileSync(filePath, newContent, 'utf-8');
  return true;
}

// --- Main ---
function main() {
  const rootDir = path.resolve(__dirname, '../../..');
  // Note: when copied to .claude/scripts/, __dirname is shallower — pass rootDir explicitly or use process.cwd()
  const resolvedRoot = fs.existsSync(path.join(rootDir, 'packages')) ? rootDir : path.resolve(__dirname, '../..');
  const defaultDirs = [
    path.join(resolvedRoot, 'packages/core/agents'),
    path.join(resolvedRoot, 'packages/a11y/agents'),
    path.join(resolvedRoot, 'packages/design-system/agents'),
    path.join(resolvedRoot, 'packages/kit/agents'),
  ];

  const dirs = process.argv[2] ? process.argv[2].split(',').map(d => d.trim()) : defaultDirs;
  const files = findAgentFiles(dirs);

  console.log(`Found ${files.length} agent files`);
  let updated = 0;
  let skipped = 0;

  for (const file of files) {
    const name = path.basename(file, '.md');
    process.stdout.write(`  ${name}... `);
    if (processAgentFile(file)) {
      console.log('OK');
      updated++;
    } else {
      console.log('SKIPPED');
      skipped++;
    }
  }

  console.log(`\nDone: ${updated} updated, ${skipped} skipped`);
}

main();
