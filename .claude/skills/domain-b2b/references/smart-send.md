---
module: smart-send
last-verified: 2026-02-11
source-path: apps/luz-epost/app/[locale]/(auth)/smart-send
---

# Smart Send Module

## Purpose

Intelligent bulk messaging system with template management, audience targeting, scheduling, and delivery optimization. Supports multi-channel delivery (email, letter, platform).

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
| Composer | imports from | Message content creation |
| Contacts | imports from | Recipient selection |
| Communities | imports from | Group targeting |
| Monitoring | exports to | Delivery tracking |

## Known Patterns & Conventions

- Multi-step wizard for campaign creation
- Template selection and customization
- Audience builder with filters
- Scheduling and preview
- Delivery status tracking
