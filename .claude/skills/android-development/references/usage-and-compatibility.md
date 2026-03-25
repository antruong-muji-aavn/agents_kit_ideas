# Android Usage Instructions & Version Compatibility

## Usage Instructions

### 1. Starting New Android Project

Use app module template:
```kotlin
// Copy build-gradle-app.kts
// Configure namespace, applicationId, version
// Add version catalog (libs.versions.toml)
```

### 2. Creating a Feature

Use the template stack:
```kotlin
// 1. Define domain model (data class)
// 2. Create Repository interface (from hilt-module-template.kt)
// 3. Implement ViewModel (from viewmodel-template.kt)
// 4. Build Compose UI (from compose-screen-template.kt)
// 5. Add navigation (from navigation-template.kt)
// 6. Write tests (from test examples)
```

### 3. Setting Up Database

Use Room template:
```kotlin
// Copy room-entity-dao-template.kt
// Define entities with @Entity
// Create DAOs with suspend functions and Flow
// Add migrations for schema changes
// Inject via Hilt module
```

### 4. Setting Up API Client

Use Retrofit template:
```kotlin
// Copy retrofit-service-template.kt
// Define API endpoints
// Add interceptors (auth, logging)
// Use Kotlinx Serialization
// Wrap responses in Result type
```

### 5. Implementing Error Handling

Follow error-handling.md:
```kotlin
// Use Result<T> wrapper
// Define domain exceptions
// Convert to user messages
// Add retry logic for network calls
// Validate input early
```

### 6. Writing Tests

Use test templates:
```kotlin
// ViewModels: Test state transitions with Turbine
// Repositories: Use fakes or MockK
// UI: Test semantics, interactions, accessibility
// Aim for 80% overall coverage, 90% for ViewModels
```

## Version Compatibility

**Tested with:**
- Kotlin 2.0.21+
- Compose BOM 2024.12.01
- Android Gradle Plugin 8.7.3
- Hilt 2.51
- Room 2.6.1
- Retrofit 2.9.0

**Minimum Requirements:**
- Android Studio Ladybug | 2024.2.1+
- Gradle 8.9+
- JDK 17+
