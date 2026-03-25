# MVVM Architecture Pattern for Android

## Overview

MVVM (Model-View-ViewModel) is the recommended architecture for Android apps using Jetpack Compose. It separates concerns, improves testability, and handles lifecycle properly.

## Layer Diagram

```
┌─────────────────────────────────────────┐
│          UI Layer (View)                │
│  - Composables                          │
│  - Collects StateFlow                   │
│  - Emits user actions                   │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│       ViewModel Layer                   │
│  - Holds UI state (StateFlow)           │
│  - Processes user actions               │
│  - Calls use cases/repositories         │
│  - Survives configuration changes       │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│       Domain Layer                      │
│  - Use cases (business logic)           │
│  - Domain models                        │
│  - Repository interfaces                │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│       Data Layer                        │
│  - Repository implementations           │
│  - Data sources (Room, Retrofit)        │
│  - DTOs and mappers                     │
└─────────────────────────────────────────┘
```

## Responsibility Boundaries

### UI Layer (Composables)
**DO:**
- Display data from ViewModel
- Collect StateFlow/SharedFlow
- Call ViewModel functions for user actions
- Handle UI-only state (scroll position, text field focus)
- Show loading/error/success states

**DON'T:**
- Perform business logic
- Access repositories directly
- Hold long-lived state
- Make network/database calls

### ViewModel Layer
**DO:**
- Hold UI state in StateFlow
- Process user actions
- Call use cases or repositories
- Handle errors and loading states
- Emit one-time events via SharedFlow
- Use viewModelScope for coroutines

**DON'T:**
- Hold Activity/Fragment context
- Access Views directly
- Perform complex business logic (delegate to use cases)

### Domain Layer
**DO:**
- Implement business rules
- Define repository interfaces
- Create domain models
- Coordinate multiple repositories
- Handle business validation

**DON'T:**
- Know about Android framework
- Access UI layer
- Hold state

### Data Layer
**DO:**
- Implement repository interfaces
- Handle data fetching/caching
- Map DTOs to domain models
- Manage Room/Retrofit instances
- Handle data synchronization

**DON'T:**
- Implement business logic
- Expose DTOs to domain layer
- Hold UI-related state

## Data Flow

### Unidirectional Data Flow

```
User Action
    ↓
Composable calls ViewModel function
    ↓
ViewModel calls Use Case/Repository
    ↓
Repository fetches from Data Source
    ↓
Data flows back through layers
    ↓
ViewModel updates StateFlow
    ↓
Composable recomposes with new state
```

## Example Implementation

### 1. Domain Model
```kotlin
data class User(
    val id: String,
    val name: String,
    val email: String
)
```

### 2. Repository Interface (Domain Layer)
```kotlin
interface UserRepository {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: String): User
    fun observeUsers(): Flow<List<User>>
}
```

### 3. Repository Implementation (Data Layer)
```kotlin
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val apiService: ApiService
) : UserRepository {
    override suspend fun getUsers(): List<User> {
        return try {
            val users = apiService.getUsers()
            userDao.insertUsers(users.map { it.toEntity() })
            users.map { it.toDomain() }
        } catch (e: Exception) {
            userDao.getUsers().map { it.toDomain() }
        }
    }

    override fun observeUsers(): Flow<List<User>> {
        return userDao.observeUsers().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
```

### 4. ViewModel
```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val users = repository.getUsers()
                _uiState.value = UiState.Success(users)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val users: List<User>) : UiState
        data class Error(val message: String) : UiState
    }
}
```

### 5. Composable
```kotlin
@Composable
fun UserScreen(
    viewModel: UserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorMessage(state.message)
        is UiState.Success -> UserList(state.users)
    }
}
```

## Best Practices

1. **Use sealed interfaces for UI state** - Type-safe state representation
2. **Separate UI state from UI events** - StateFlow for state, SharedFlow for one-time events
3. **Keep ViewModels Android-agnostic** - No Activity/Context references
4. **Use Hilt for dependency injection** - Simplifies ViewModel creation
5. **Map DTOs to domain models** - Keep data layer isolated
6. **Handle errors gracefully** - Always provide user-friendly messages
7. **Use Flow for reactive data** - Observe database changes efficiently
8. **Test each layer independently** - Mock dependencies appropriately
