// ViewModel Template with StateFlow and Error Handling
// Pattern: Sealed UiState, suspend functions, proper coroutine scope

package com.example.feature.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val repository: ExampleRepository
) : ViewModel() {

    // UI State Management
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // User Actions
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    fun onItemClick(item: Item) {
        viewModelScope.launch {
            try {
                val details = repository.getItemDetails(item.id)
                _uiEvent.emit(UiEvent.NavigateToDetails(details))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    fun onDeleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                repository.deleteItem(itemId)
                loadData() // Refresh list
                _uiEvent.emit(UiEvent.ShowMessage("Item deleted"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError("Failed to delete: ${e.message}"))
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val items = repository.getItems()
                _uiState.value = UiState.Success(items)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Failed to load data"
                )
            }
        }
    }
}

// Sealed UI State
sealed interface UiState {
    data object Loading : UiState
    data class Success(val items: List<Item>) : UiState
    data class Error(val message: String) : UiState
}

// One-Time Events
sealed interface UiEvent {
    data class NavigateToDetails(val item: ItemDetails) : UiEvent
    data class ShowMessage(val message: String) : UiEvent
    data class ShowError(val message: String) : UiEvent
}

// Domain Models
data class Item(
    val id: String,
    val title: String,
    val description: String
)

data class ItemDetails(
    val id: String,
    val title: String,
    val fullDescription: String,
    val timestamp: Long
)

// Repository Interface (inject implementation via Hilt)
interface ExampleRepository {
    suspend fun getItems(): List<Item>
    suspend fun getItemDetails(id: String): ItemDetails
    suspend fun deleteItem(id: String)
}
