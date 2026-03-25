# Jetpack Compose Best Practices

## Recomposition Guidelines

### Understanding Recomposition

Recomposition is when Compose re-executes composables that may have changed. This happens automatically when state changes.

**Key Principles:**
- Composables can execute in any order
- Composables can execute in parallel
- Recomposition skips as many composables as possible
- Recomposition is optimistic (may be cancelled)

### Minimize Recomposition

```kotlin
// ❌ Bad: Entire screen recomposes when counter changes
@Composable
fun Screen(counter: Int, list: List<Item>) {
    Column {
        Text("Counter: $counter")  // Changes frequently
        LazyColumn {
            items(list) { item ->
                ItemRow(item)      // Recomposes unnecessarily
            }
        }
    }
}

// ✅ Good: Extract stable composables
@Composable
fun Screen(counter: Int, list: List<Item>) {
    Column {
        CounterDisplay(counter)    // Only this recomposes
        StableItemList(list)       // Stays stable
    }
}

@Composable
fun CounterDisplay(counter: Int) {
    Text("Counter: $counter")
}

@Composable
fun StableItemList(list: List<Item>) {
    LazyColumn {
        items(list, key = { it.id }) { item ->
            ItemRow(item)
        }
    }
}
```

## State Hoisting

### Pattern

Move state up to the highest common ancestor that needs it, and pass down state + callbacks.

```kotlin
// ❌ Bad: State inside reusable component
@Composable
fun SearchBar() {
    var query by remember { mutableStateOf("") }

    TextField(
        value = query,
        onValueChange = { query = it }
    )
}

// ✅ Good: Hoisted state
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
    )
}

// Usage in parent
@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }

    SearchBar(
        query = query,
        onQueryChange = { query = it }
    )
}
```

### When to Hoist

**Hoist when:**
- Multiple composables need the state
- State needs to survive recomposition
- Parent needs to control or observe state
- Testing requires state control

**Don't hoist when:**
- State is purely UI-local (e.g., text field focus)
- State doesn't need to be shared
- Component is completely self-contained

## Side Effects

### LaunchedEffect

Use for triggering suspending functions based on keys.

```kotlin
@Composable
fun UserScreen(userId: String, viewModel: UserViewModel) {
    LaunchedEffect(userId) {
        // Runs when userId changes
        viewModel.loadUser(userId)
    }
}
```

### DisposableEffect

Use for cleanup when composable leaves composition.

```kotlin
@Composable
fun EventListener() {
    DisposableEffect(Unit) {
        val listener = EventCallback { /* handle event */ }
        EventBus.register(listener)

        onDispose {
            EventBus.unregister(listener)
        }
    }
}
```

### derivedStateOf

Use to minimize recompositions when deriving state.

```kotlin
@Composable
fun MessageList(messages: List<Message>) {
    val listState = rememberLazyListState()

    // Only recomposes when first visible item changes
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    Box {
        LazyColumn(state = listState) {
            items(messages) { message ->
                MessageRow(message)
            }
        }

        if (showScrollToTop) {
            ScrollToTopButton(onClick = { /* scroll */ })
        }
    }
}
```

### rememberCoroutineScope

Use for launching coroutines from non-composable callbacks.

```kotlin
@Composable
fun ScrollableContent() {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column {
        Button(onClick = {
            scope.launch {
                listState.animateScrollToItem(0)
            }
        }) {
            Text("Scroll to top")
        }

        LazyColumn(state = listState) {
            // Content
        }
    }
}
```

## Performance Optimization

### Stable Classes

Mark data classes as `@Stable` or `@Immutable` to help Compose skip recomposition.

```kotlin
@Immutable
data class User(
    val id: String,
    val name: String
)

@Stable
interface UserRepository {
    suspend fun getUser(id: String): User
}
```

### Keys in Lists

Always provide keys for items in lazy lists.

```kotlin
// ❌ Bad: No keys
LazyColumn {
    items(users) { user ->
        UserRow(user)
    }
}

// ✅ Good: With keys
LazyColumn {
    items(users, key = { it.id }) { user ->
        UserRow(user)
    }
}
```

### Avoid Heavy Operations

```kotlin
// ❌ Bad: Expensive operation in composable body
@Composable
fun UserProfile(user: User) {
    val processedData = heavyComputation(user)  // Runs on every recomposition
    Text(processedData)
}

// ✅ Good: Use remember
@Composable
fun UserProfile(user: User) {
    val processedData = remember(user) {
        heavyComputation(user)
    }
    Text(processedData)
}
```

## Modifier Best Practices

### Order Matters

```kotlin
// Modifier order affects behavior
Box(
    modifier = Modifier
        .clickable { }      // Click area is 100dp
        .size(100.dp)
        .padding(16.dp)     // Padding inside click area
        .background(Color.Blue)
)

Box(
    modifier = Modifier
        .size(100.dp)
        .padding(16.dp)
        .background(Color.Blue)
        .clickable { }      // Click area is 68dp (100 - 32)
)
```

### Always Accept Modifier Parameter

```kotlin
// ✅ Good: Flexible composable
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier  // Always default to Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier  // Apply first in chain
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text)
    }
}
```

## Material 3 Patterns

### Theme Usage

```kotlin
@Composable
fun ThemedContent() {
    // Use theme colors
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = "Themed text",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
```

### Dynamic Colors (Android 12+)

```kotlin
@Composable
fun App() {
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && isSystemInDarkTheme() ->
            dynamicDarkColorScheme(LocalContext.current)
        dynamicColor && !isSystemInDarkTheme() ->
            dynamicLightColorScheme(LocalContext.current)
        isSystemInDarkTheme() -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme) {
        Content()
    }
}
```

## Testing Considerations

### Write Testable Composables

```kotlin
// ✅ Testable: Pure function with hoisted state
@Composable
fun Counter(
    count: Int,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onIncrement,
        modifier = modifier
    ) {
        Text("Count: $count")
    }
}

// Test
@Test
fun counterDisplaysCorrectValue() {
    composeTestRule.setContent {
        Counter(count = 5, onIncrement = {})
    }

    composeTestRule
        .onNodeWithText("Count: 5")
        .assertExists()
}
```

## Common Anti-Patterns

### 1. Creating State in Loop

```kotlin
// ❌ Bad
@Composable
fun BadList(items: List<Item>) {
    items.forEach { item ->
        var selected by remember { mutableStateOf(false) }  // New state each time
        ItemRow(item, selected)
    }
}

// ✅ Good
@Composable
fun GoodList(items: List<Item>) {
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    items.forEach { item ->
        ItemRow(
            item = item,
            selected = item.id in selectedIds,
            onSelect = { selectedIds = selectedIds + item.id }
        )
    }
}
```

### 2. Using ViewModel in Reusable Composables

```kotlin
// ❌ Bad: Tight coupling
@Composable
fun UserCard(viewModel: UserViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsState()
    // ...
}

// ✅ Good: Pass data
@Composable
fun UserCard(
    user: User,
    onUserClick: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    // ...
}
```

### 3. Reading State Without Tracking

```kotlin
// ❌ Bad: Won't recompose
@Composable
fun BadCounter(state: MutableState<Int>) {
    Text("Count: ${state.value}")  // Not tracked
}

// ✅ Good: Properly tracked
@Composable
fun GoodCounter(state: State<Int>) {
    val count by state  // Tracked via delegation
    Text("Count: $count")
}
```
