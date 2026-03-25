# Android Error Handling Patterns

## Result Wrapper Pattern

Use a sealed class to wrap success/error states.

### Basic Result Type

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}

// Extension functions
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success
fun <T> Result<T>.isError(): Boolean = this is Result.Error
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    else -> null
}

fun <T> Result<T>.getOrThrow(): T = when (this) {
    is Result.Success -> data
    is Result.Error -> throw exception
    is Result.Loading -> throw IllegalStateException("Result is still loading")
}
```

### Usage in Repository

```kotlin
class UserRepository @Inject constructor(
    private val apiService: ApiService,
    private val userDao: UserDao
) {
    suspend fun getUser(id: String): Result<User> {
        return try {
            val userDto = apiService.getUser(id)
            val user = userDto.toDomain()
            userDao.insertUser(user.toEntity())
            Result.Success(user)
        } catch (e: IOException) {
            // Try local cache on network error
            val cachedUser = userDao.getUserById(id)
            if (cachedUser != null) {
                Result.Success(cachedUser.toDomain())
            } else {
                Result.Error(NetworkException("No network and no cache", e))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun observeUser(id: String): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            userDao.observeUserById(id)
                .map { entity ->
                    if (entity != null) {
                        Result.Success(entity.toDomain())
                    } else {
                        Result.Error(NotFoundException("User not found"))
                    }
                }
                .collect { emit(it) }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
```

## Custom Exception Types

Define domain-specific exceptions.

```kotlin
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : AppException(message, cause)
    class AuthenticationException(message: String = "Authentication failed") : AppException(message)
    class NotFoundException(message: String) : AppException(message)
    class ValidationException(val errors: Map<String, String>) : AppException("Validation failed")
    class ServerException(val code: Int, message: String) : AppException(message)
    class UnknownException(cause: Throwable) : AppException("Unknown error", cause)
}

// Convert HTTP errors to domain exceptions
fun Throwable.toDomainException(): AppException = when (this) {
    is IOException -> AppException.NetworkException("Network error", this)
    is HttpException -> when (code()) {
        401, 403 -> AppException.AuthenticationException()
        404 -> AppException.NotFoundException("Resource not found")
        in 500..599 -> AppException.ServerException(code(), message())
        else -> AppException.UnknownException(this)
    }
    is AppException -> this
    else -> AppException.UnknownException(this)
}
```

## User-Facing Error Messages

Convert exceptions to user-friendly messages.

```kotlin
fun AppException.toUserMessage(): String = when (this) {
    is AppException.NetworkException -> "No internet connection. Please check your network."
    is AppException.AuthenticationException -> "Session expired. Please log in again."
    is AppException.NotFoundException -> "The requested item was not found."
    is AppException.ValidationException -> {
        errors.values.firstOrNull() ?: "Invalid input. Please check your data."
    }
    is AppException.ServerException -> "Server error. Please try again later."
    is AppException.UnknownException -> "Something went wrong. Please try again."
}

// Usage in ViewModel
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.getUser(userId)) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is Result.Error -> {
                    val exception = result.exception.toDomainException()
                    _uiState.value = UiState.Error(exception.toUserMessage())
                }
                is Result.Loading -> {
                    _uiState.value = UiState.Loading
                }
            }
        }
    }
}
```

## Retry Logic

Implement automatic retry with exponential backoff.

```kotlin
suspend fun <T> retryWithExponentialBackoff(
    times: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    shouldRetry: (Throwable) -> Boolean = { it is IOException },
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Throwable) {
            if (!shouldRetry(e)) throw e
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // Last attempt
}

// Usage in repository
suspend fun fetchUserWithRetry(userId: String): Result<User> {
    return try {
        val user = retryWithExponentialBackoff(
            times = 3,
            shouldRetry = { it is IOException }
        ) {
            apiService.getUser(userId)
        }
        Result.Success(user.toDomain())
    } catch (e: Exception) {
        Result.Error(e.toDomainException())
    }
}
```

## Error Boundary Pattern

Catch errors at screen level and provide fallback UI.

```kotlin
@Composable
fun ErrorBoundary(
    modifier: Modifier = Modifier,
    onError: (Throwable) -> Unit = {},
    content: @Composable () -> Unit
) {
    var error by remember { mutableStateOf<Throwable?>(null) }

    if (error != null) {
        ErrorScreen(
            error = error!!,
            onRetry = { error = null },
            modifier = modifier
        )
    } else {
        Box(modifier = modifier) {
            try {
                content()
            } catch (e: Throwable) {
                error = e
                onError(e)
            }
        }
    }
}

@Composable
fun ErrorScreen(
    error: Throwable,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error.toDomainException().toUserMessage(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

// Usage
@Composable
fun UserScreen(userId: String) {
    ErrorBoundary {
        UserContent(userId = userId)
    }
}
```

## Logging Strategy

Structure logging for debugging and crash reporting.

```kotlin
object Logger {
    private const val TAG = "AppLogger"

    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(throwable: Throwable, message: String? = null, tag: String = TAG) {
        Log.e(tag, message ?: throwable.message ?: "Unknown error", throwable)

        // Send to crash reporting (Firebase Crashlytics, Sentry, etc.)
        if (!BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().apply {
                message?.let { log(it) }
                recordException(throwable)
            }
        }
    }

    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
    }
}

// Usage in repository
suspend fun getUser(id: String): Result<User> {
    return try {
        Logger.d("Fetching user: $id")
        val user = apiService.getUser(id)
        Logger.d("User fetched successfully: ${user.email}")
        Result.Success(user.toDomain())
    } catch (e: Exception) {
        Logger.e(e, "Failed to fetch user: $id")
        Result.Error(e.toDomainException())
    }
}
```

## Validation Errors

Handle form validation with detailed error messages.

```kotlin
data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
) {
    fun errorFor(field: String): String? = errors[field]
}

object Validators {
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 8 -> "Password must be at least 8 characters"
            !password.any { it.isUpperCase() } -> "Password must contain uppercase letter"
            !password.any { it.isDigit() } -> "Password must contain a digit"
            else -> null
        }
    }

    fun validateForm(
        email: String,
        password: String
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        validateEmail(email)?.let { errors["email"] = it }
        validatePassword(password)?.let { errors["password"] = it }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

// Usage in ViewModel
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors: StateFlow<Map<String, String>> = _validationErrors.asStateFlow()

    fun login(email: String, password: String) {
        val validation = Validators.validateForm(email, password)
        if (!validation.isValid) {
            _validationErrors.value = validation.errors
            return
        }

        _validationErrors.value = emptyMap()
        // Proceed with login
        viewModelScope.launch {
            // ...
        }
    }
}
```

## Best Practices

1. **Use Result wrapper for all async operations** - Consistent error handling
2. **Define domain-specific exceptions** - Clear error classification
3. **Convert to user-friendly messages** - Never show raw exception messages
4. **Log errors with context** - Include operation details for debugging
5. **Implement retry for network errors** - Improve reliability
6. **Validate input early** - Prevent invalid requests
7. **Cache data locally** - Provide fallback when network fails
8. **Use error boundaries** - Prevent app crashes from UI errors
9. **Test error scenarios** - Verify error handling works correctly
10. **Report crashes** - Use Crashlytics or similar for production monitoring
