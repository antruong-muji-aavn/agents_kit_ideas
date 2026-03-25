---
name: domain/b2c/app-patterns
description: "Consumer app patterns and conventions"
---

# B2C App Patterns

## Purpose

Defines the core features and platform-specific conventions for the epost consumer mobile apps.

## Table of Contents

- [Core Features](#core-features)
- [Platform-Specific](#platform-specific)
- [Shared Patterns](#shared-patterns)
- [Related Documents](#related-documents)

## Core Features

| Feature | Description |
|---------|-------------|
| Mailbox | Receive and read digital mail |
| Documents | Store and manage received documents |
| Notifications | Push notifications for new mail |
| Profile | User settings, security, preferences |
| Authentication | BankID / ID-porten login |

## Platform-Specific

### iOS

- SwiftUI primary, UIKit where needed
- Swift 6 strict concurrency
- Combine for reactive streams
- Core Data for local cache
- Push via APNs

### Android

- Jetpack Compose UI
- Kotlin Coroutines + Flow
- Room for local cache
- Firebase Cloud Messaging

## Shared Patterns

- **Authentication**: OAuth 2.0 with ID-porten (Norwegian digital ID)
- **API**: REST backend (see platform-backend package)
- **Offline**: Local cache with sync-on-connect
- **Accessibility**: WCAG 2.1 AA compliance required
- **Localization**: Norwegian Bokmal (nb) primary

## Related Documents

- `SKILL.md` — Parent skill index
- `../../../CLAUDE.snippet.md` — Integration snippet
