#!/usr/bin/env node
/**
 * error-parser.cjs - Build error parsing module
 *
 * Parses build output (stderr+stdout) to extract structured error info.
 * Used by build-gate.cjs to provide actionable failure output.
 *
 * Usage:
 *   const { parseErrors } = require('./error-parser.cjs');
 *   const result = parseErrors(combinedOutput);
 *   // result: { errors: [{file, line, col, message, category}], suggestion: string }
 */

'use strict';

/** @typedef {'typescript'|'eslint'|'import'|'runtime'|'unknown'} ErrorCategory */

/**
 * @typedef {Object} ParsedError
 * @property {string|null} file
 * @property {number|null} line
 * @property {number|null} col
 * @property {string} message
 * @property {ErrorCategory} category
 */

/**
 * @typedef {Object} ParseResult
 * @property {ParsedError[]} errors
 * @property {string} suggestion
 */

// ─── Regex Patterns ───

const PATTERNS = [
  {
    // TypeScript: "src/foo.tsx(12,3): error TS2345: ..."
    regex: /(\S+\.tsx?)\((\d+),(\d+)\):\s*error\s+TS\d+:\s*(.+)/g,
    category: /** @type {ErrorCategory} */ ('typescript'),
    extract: (m) => ({ file: m[1], line: parseInt(m[2]), col: parseInt(m[3]), message: m[4].trim() }),
  },
  {
    // TypeScript alt: "src/foo.ts:12:3 - error TS2345: ..."
    regex: /(\S+\.tsx?):(\d+):(\d+)\s*-\s*error\s+TS\d+:\s*(.+)/g,
    category: /** @type {ErrorCategory} */ ('typescript'),
    extract: (m) => ({ file: m[1], line: parseInt(m[2]), col: parseInt(m[3]), message: m[4].trim() }),
  },
  {
    // ESLint: "src/foo.tsx\n  12:3  error  message  rule-name" (filename on separate line, indented errors)
    regex: /^\s*(\d+):(\d+)\s+error\s+(.+?)\s{2,}(\S+)\s*$/gm,
    category: /** @type {ErrorCategory} */ ('eslint'),
    extract: (m) => ({ file: null, line: parseInt(m[1]), col: parseInt(m[2]), message: `${m[3].trim()} (${m[4]})` }),
  },
  {
    // ESLint compact: "src/foo.tsx: line 12, col 3, Error - message (rule-name)"
    regex: /^(\S+\.(?:tsx?|jsx?|mjs|cjs)):\s*line\s+(\d+),\s*col\s+(\d+),\s*Error\s*-\s*(.+)/gm,
    category: /** @type {ErrorCategory} */ ('eslint'),
    extract: (m) => ({ file: m[1], line: parseInt(m[2]), col: parseInt(m[3]), message: m[4].trim() }),
  },
  {
    // Next.js / Webpack build error with file ref: "Error in ./src/foo.tsx"
    regex: /Error(?:\s+in)?\s+(\.\/\S+\.tsx?)\s*\n?\s*(.+)/g,
    category: /** @type {ErrorCategory} */ ('runtime'),
    extract: (m) => ({ file: m[1], line: null, col: null, message: m[2].trim() }),
  },
  {
    // Import/module errors
    regex: /(Module not found|Cannot find module|Failed to resolve import)[:\s]+['"]?([^'">\n]+)['"]?/g,
    category: /** @type {ErrorCategory} */ ('import'),
    extract: (m) => ({ file: null, line: null, col: null, message: `${m[1]}: ${m[2].trim()}` }),
  },
];

// ─── Suggestion Map ───

/** @type {Record<ErrorCategory, string>} */
const SUGGESTIONS = {
  typescript: '/fix --types',
  eslint: 'fix lint errors (run: bun run lint --fix)',
  import: 'check imports and module paths',
  runtime: '/fix (runtime error — check stack trace above)',
  unknown: '/fix (review errors above)',
};

// ─── Helpers ───

/**
 * Deduplicate errors by file+line+message fingerprint.
 * @param {ParsedError[]} errors
 * @returns {ParsedError[]}
 */
function dedup(errors) {
  const seen = new Set();
  return errors.filter(e => {
    const key = `${e.file}:${e.line}:${e.message.slice(0, 60)}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

/**
 * Determine the dominant error category for the suggestion.
 * @param {ParsedError[]} errors
 * @returns {ErrorCategory}
 */
function dominantCategory(errors) {
  /** @type {Record<string, number>} */
  const counts = {};
  for (const e of errors) {
    counts[e.category] = (counts[e.category] || 0) + 1;
  }
  const priority = ['typescript', 'import', 'eslint', 'runtime', 'unknown'];
  for (const cat of priority) {
    if (counts[cat] > 0) return /** @type {ErrorCategory} */ (cat);
  }
  return 'unknown';
}

// ─── Main Export ───

/**
 * Parse build output and extract structured errors.
 * @param {string} output - Combined stderr+stdout from build command
 * @returns {ParseResult}
 */
function parseErrors(output) {
  if (!output || typeof output !== 'string') {
    return { errors: [], suggestion: SUGGESTIONS.unknown };
  }

  /** @type {ParsedError[]} */
  const errors = [];

  for (const pattern of PATTERNS) {
    // Reset regex state
    pattern.regex.lastIndex = 0;
    let match;
    while ((match = pattern.regex.exec(output)) !== null) {
      const extracted = pattern.extract(match);
      errors.push({
        file: extracted.file || null,
        line: extracted.line || null,
        col: extracted.col || null,
        message: extracted.message,
        category: pattern.category,
      });
      // Guard against infinite loops on zero-length matches
      if (pattern.regex.lastIndex === match.index) {
        pattern.regex.lastIndex++;
      }
    }
  }

  const unique = dedup(errors);
  const cat = unique.length > 0 ? dominantCategory(unique) : 'unknown';

  return {
    errors: unique,
    suggestion: SUGGESTIONS[cat] || SUGGESTIONS.unknown,
  };
}

module.exports = { parseErrors };
