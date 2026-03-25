---
module: composer
last-verified: 2026-02-11
source-path: apps/luz-epost/app/[locale]/(auth)/composer
---

# Composer Module

## Purpose

Content composition tools for creating messages, letters, and communications. Rich text editor with templates, variable interpolation, and preview capabilities.

## Component Inventory

> Populate by scanning `_components/` directory

| Component | File | Key Props | Description |
|-----------|------|-----------|-------------|
| _TBD_ | _scan required_ | — | — |

## Hook Inventory

| Hook | File | Returns | Used By |
|------|------|---------|---------|
| _TBD_ | _scan required_ | — | — |

## Store Shape (Redux)

> Populate by scanning `_stores/` directory

## API Endpoints

| Endpoint | Method | Request | Response | Service File |
|----------|--------|---------|----------|-------------|
| _TBD_ | — | — | — | — |

## Cross-Module Dependencies

| Dependency | Direction | Purpose |
|------------|-----------|---------|
| Contacts | imports from | Recipient selection |
| Smart Send | exports to | Campaign message content |
| Unified Inbox | exports to | Reply/forward composition |

## Known Patterns & Conventions

- Rich text editor (likely TipTap or similar)
- Template management
- Variable/merge field interpolation
- Preview mode (desktop/mobile)
- Attachment handling
