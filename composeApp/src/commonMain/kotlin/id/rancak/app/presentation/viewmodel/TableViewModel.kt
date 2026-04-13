package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.repository.OperationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TableUiState(
    val tables: List<Table> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TableViewModel(
    private val operationsRepository: OperationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableUiState())
    val uiState: StateFlow<TableUiState> = _uiState.asStateFlow()

    fun loadTables() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = operationsRepository.getTables()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(tables = result.data, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }
}
