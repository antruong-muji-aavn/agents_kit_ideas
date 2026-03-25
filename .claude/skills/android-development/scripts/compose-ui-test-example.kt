// Compose UI Test Example with Espresso and Compose Testing
// Tests: User interactions, UI state, navigation, accessibility

package com.example.feature.screen

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: ExampleViewModel
    private lateinit var uiStateFlow: MutableStateFlow<UiState>

    @Before
    fun setup() {
        viewModel = mockk(relaxed = true)
        uiStateFlow = MutableStateFlow<UiState>(UiState.Loading)
        every { viewModel.uiState } returns uiStateFlow
    }

    @Test
    fun loadingState_displaysProgressIndicator() {
        // Given
        uiStateFlow.value = UiState.Loading

        // When
        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun errorState_displaysErrorMessageAndRetryButton() {
        // Given
        val errorMessage = "Network error occurred"
        uiStateFlow.value = UiState.Error(errorMessage)

        // When
        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule
            .onNodeWithText(errorMessage)
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Retry")
            .assertExists()
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun errorState_clickRetryButton_callsRefresh() {
        // Given
        uiStateFlow.value = UiState.Error("Error")

        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // When
        composeTestRule
            .onNodeWithText("Retry")
            .performClick()

        // Then
        verify { viewModel.refresh() }
    }

    @Test
    fun successState_displaysItemList() {
        // Given
        val items = listOf(
            Item("1", "Item 1", "Description 1"),
            Item("2", "Item 2", "Description 2"),
            Item("3", "Item 3", "Description 3")
        )
        uiStateFlow.value = UiState.Success(items)

        // When
        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // Then - All items displayed
        items.forEach { item ->
            composeTestRule
                .onNodeWithText(item.title)
                .assertExists()
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithText(item.description)
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun successState_clickItem_callsOnItemClick() {
        // Given
        val item = Item("1", "Test Item", "Test Description")
        uiStateFlow.value = UiState.Success(listOf(item))

        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // When
        composeTestRule
            .onNodeWithText("Test Item")
            .performClick()

        // Then
        verify { viewModel.onItemClick(item) }
    }

    @Test
    fun topBar_displaysTitle() {
        // Given
        uiStateFlow.value = UiState.Loading

        // When
        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule
            .onNodeWithText("Example Screen")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun topBar_refreshButton_callsRefresh() {
        // Given
        uiStateFlow.value = UiState.Success(emptyList())

        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // When
        composeTestRule
            .onNodeWithContentDescription("Refresh")
            .performClick()

        // Then
        verify { viewModel.refresh() }
    }

    @Test
    fun successState_scrollThroughList() {
        // Given - Many items
        val items = (1..50).map { i ->
            Item("$i", "Item $i", "Description $i")
        }
        uiStateFlow.value = UiState.Success(items)

        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // When - Scroll to bottom
        composeTestRule
            .onNodeWithText("Item 1")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Item 50")
            .performScrollTo()

        // Then - Last item visible
        composeTestRule
            .onNodeWithText("Item 50")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_displaysNoItemsMessage() {
        // Given
        uiStateFlow.value = UiState.Success(emptyList())

        // When
        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // Then
        composeTestRule
            .onNodeWithText("No items available")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun stateTransition_fromLoadingToSuccess() {
        // Given - Start with loading
        uiStateFlow.value = UiState.Loading

        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertExists()

        // When - Transition to success
        composeTestRule.runOnIdle {
            uiStateFlow.value = UiState.Success(
                listOf(Item("1", "Item 1", "Description"))
            )
        }

        // Then
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithContentDescription("Loading")
                .fetchSemanticsNodes()
                .isEmpty()
        }

        composeTestRule
            .onNodeWithText("Item 1")
            .assertExists()
            .assertIsDisplayed()
    }
}

// Accessibility Tests
@RunWith(AndroidJUnit4::class)
class ExampleScreenAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allInteractiveElements_haveContentDescriptions() {
        // Given
        val viewModel = mockk<ExampleViewModel>(relaxed = true)
        val uiState = MutableStateFlow<UiState>(UiState.Success(emptyList()))
        every { viewModel.uiState } returns uiState

        // When
        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // Then - Verify accessibility
        composeTestRule
            .onNodeWithContentDescription("Refresh")
            .assertExists()

        // All clickable items should have descriptions or text
        composeTestRule
            .onAllNodes(hasClickAction())
            .assertAll(
                hasText() or hasContentDescription()
            )
    }

    @Test
    fun screen_supportsKeyboardNavigation() {
        // Given
        val items = listOf(
            Item("1", "Item 1", "Description 1"),
            Item("2", "Item 2", "Description 2")
        )
        val viewModel = mockk<ExampleViewModel>(relaxed = true)
        val uiState = MutableStateFlow<UiState>(UiState.Success(items))
        every { viewModel.uiState } returns uiState

        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // When - Navigate with tab
        composeTestRule
            .onNodeWithText("Item 1")
            .performKeyInput { pressKey(androidx.compose.ui.input.key.Key.Tab) }

        // Then - Focus moves
        // Verify focus behavior if needed
    }
}

// Integration Test with Real ViewModel
@RunWith(AndroidJUnit4::class)
class ExampleScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var repository: ExampleRepository

    @Before
    fun setup() {
        repository = mockk()
    }

    @Test
    fun fullFlow_loadData_displayItems_clickItem() {
        // Given
        val items = listOf(
            Item("1", "Item 1", "Description 1"),
            Item("2", "Item 2", "Description 2")
        )
        val itemDetails = ItemDetails("1", "Item 1", "Full description", 123L)

        coEvery { repository.getItems() } returns items
        coEvery { repository.getItemDetails("1") } returns itemDetails

        val viewModel = ExampleViewModel(repository)

        // When
        composeTestRule.setContent {
            ExampleScreen(viewModel = viewModel)
        }

        // Then - Wait for loading to complete
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithContentDescription("Loading")
                .fetchSemanticsNodes()
                .isEmpty()
        }

        // Verify items displayed
        composeTestRule
            .onNodeWithText("Item 1")
            .assertExists()
            .performClick()

        // Verify repository called
        coVerify { repository.getItemDetails("1") }
    }
}

// Semantic Matchers for Custom Components
fun hasItemTitle(title: String): SemanticsMatcher {
    return hasText(title) and hasTestTag("item_title")
}

fun hasItemDescription(description: String): SemanticsMatcher {
    return hasText(description) and hasTestTag("item_description")
}

// Usage in tests
@Test
fun customMatcher_findsItemBySemantics() {
    // Given
    val item = Item("1", "Special Item", "Special Description")
    val uiState = MutableStateFlow<UiState>(UiState.Success(listOf(item)))
    val viewModel = mockk<ExampleViewModel>(relaxed = true)
    every { viewModel.uiState } returns uiState

    composeTestRule.setContent {
        ExampleScreen(viewModel = viewModel)
    }

    // When/Then
    composeTestRule
        .onNode(hasItemTitle("Special Item"))
        .assertExists()
}

// Test Helper Extensions
fun ComposeContentTestRule.waitForIdle(timeoutMillis: Long = 1000) {
    waitUntil(timeoutMillis) { true }
}

fun ComposeContentTestRule.waitForText(text: String, timeoutMillis: Long = 5000) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text)
            .fetchSemanticsNodes()
            .isNotEmpty()
    }
}
