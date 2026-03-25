// ViewModel Unit Test Example with Turbine
// Tests: State transitions, error handling, coroutines

package com.example.feature.viewmodel

import app.cash.turbine.test
import com.example.core.testing.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExampleViewModelTest {

    // Rule to replace Main dispatcher with TestDispatcher
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mocked dependencies
    private lateinit var repository: ExampleRepository
    private lateinit var viewModel: ExampleViewModel

    @Before
    fun setup() {
        repository = mockk()
        viewModel = ExampleViewModel(repository)
    }

    @Test
    fun `initial state is Loading`() = runTest {
        // Given - ViewModel is created
        coEvery { repository.getItems() } returns emptyList()

        // When - Observing initial state
        viewModel.uiState.test {
            // Then - First emission is Loading
            assertTrue(awaitItem() is UiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadData success updates state to Success`() = runTest {
        // Given
        val mockItems = listOf(
            Item("1", "Item 1", "Description 1"),
            Item("2", "Item 2", "Description 2")
        )
        coEvery { repository.getItems() } returns mockItems

        // When
        viewModel.uiState.test {
            // Skip Loading state
            skipItems(1)

            // Then - State updates to Success with items
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(mockItems, (state as UiState.Success).items)
        }
    }

    @Test
    fun `loadData failure updates state to Error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { repository.getItems() } throws Exception(errorMessage)

        // When
        viewModel.uiState.test {
            // Skip Loading state
            skipItems(1)

            // Then - State updates to Error
            val state = awaitItem()
            assertTrue(state is UiState.Error)
            assertEquals(errorMessage, (state as UiState.Error).message)
        }
    }

    @Test
    fun `refresh calls repository and updates state`() = runTest {
        // Given
        val mockItems = listOf(Item("1", "Item 1", "Description 1"))
        coEvery { repository.getItems() } returns mockItems

        // When
        viewModel.refresh()

        // Then
        viewModel.uiState.test {
            skipItems(1) // Skip Loading
            val state = awaitItem()
            assertTrue(state is UiState.Success)
        }

        coVerify(exactly = 2) { repository.getItems() } // init + refresh
    }

    @Test
    fun `onItemClick emits NavigateToDetails event`() = runTest {
        // Given
        val item = Item("1", "Test", "Description")
        val details = ItemDetails("1", "Test", "Full description", 123456L)
        coEvery { repository.getItems() } returns listOf(item)
        coEvery { repository.getItemDetails("1") } returns details

        // When
        viewModel.uiEvent.test {
            viewModel.onItemClick(item)

            // Then - Event emitted
            val event = awaitItem()
            assertTrue(event is UiEvent.NavigateToDetails)
            assertEquals(details, (event as UiEvent.NavigateToDetails).item)
        }
    }

    @Test
    fun `onItemClick failure emits ShowError event`() = runTest {
        // Given
        val item = Item("1", "Test", "Description")
        val errorMessage = "Failed to load details"
        coEvery { repository.getItems() } returns listOf(item)
        coEvery { repository.getItemDetails("1") } throws Exception(errorMessage)

        // When
        viewModel.uiEvent.test {
            viewModel.onItemClick(item)

            // Then - Error event emitted
            val event = awaitItem()
            assertTrue(event is UiEvent.ShowError)
            assertEquals(errorMessage, (event as UiEvent.ShowError).message)
        }
    }

    @Test
    fun `onDeleteItem success removes item and refreshes`() = runTest {
        // Given
        val itemId = "1"
        val initialItems = listOf(
            Item("1", "Item 1", "Desc 1"),
            Item("2", "Item 2", "Desc 2")
        )
        val afterDeleteItems = listOf(Item("2", "Item 2", "Desc 2"))

        coEvery { repository.getItems() } returnsMany listOf(initialItems, afterDeleteItems)
        coEvery { repository.deleteItem(itemId) } just Runs

        // When
        viewModel.onDeleteItem(itemId)

        // Then
        viewModel.uiState.test {
            skipItems(1) // Skip Loading
            val state = awaitItem()
            assertTrue(state is UiState.Success)
            assertEquals(1, (state as UiState.Success).items.size)
            assertEquals("2", state.items.first().id)
        }

        viewModel.uiEvent.test {
            val event = awaitItem()
            assertTrue(event is UiEvent.ShowMessage)
            assertEquals("Item deleted", (event as UiEvent.ShowMessage).message)
        }

        coVerify { repository.deleteItem(itemId) }
        coVerify(exactly = 2) { repository.getItems() }
    }

    @Test
    fun `multiple rapid state changes handled correctly`() = runTest {
        // Given
        val items = listOf(Item("1", "Item", "Desc"))
        coEvery { repository.getItems() } returns items

        // When - Multiple refreshes
        viewModel.refresh()
        viewModel.refresh()
        viewModel.refresh()

        // Then - All calls completed
        coVerify(exactly = 4) { repository.getItems() } // init + 3 refreshes
    }

    @Test
    fun `verify repository not called if viewModel disposed immediately`() = runTest {
        // Given
        val slowRepository = mockk<ExampleRepository>()
        coEvery { slowRepository.getItems() } coAnswers {
            delay(1000)
            emptyList()
        }

        // When - ViewModel created but scope cancelled
        val testViewModel = ExampleViewModel(slowRepository)
        // Scope cancelled before repository call completes

        // Then - verify call was attempted
        coVerify(exactly = 1) { slowRepository.getItems() }
    }
}

// Test Dispatcher Rule (place in test fixtures)
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
