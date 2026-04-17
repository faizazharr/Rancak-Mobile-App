package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Shift
import id.rancak.app.domain.repository.OperationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShiftUiState(
    val currentShift: Shift? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val openingCash: String = "",
    val closingCash: String = "",
    val closingNote: String = "",
    val shiftJustOpened: Boolean = false,
    val shiftJustClosed: Boolean = false
)

class ShiftViewModel(
    private val operationsRepository: OperationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShiftUiState())
    val uiState: StateFlow<ShiftUiState> = _uiState.asStateFlow()

    fun loadCurrentShift() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = operationsRepository.getCurrentShift()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(currentShift = result.data, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun onOpeningCashChange(value: String) {
        _uiState.update { it.copy(openingCash = value.filter { c -> c.isDigit() }) }
    }

    fun onClosingCashChange(value: String) {
        _uiState.update { it.copy(closingCash = value.filter { c -> c.isDigit() }) }
    }

    fun onClosingNoteChange(value: String) {
        _uiState.update { it.copy(closingNote = value) }
    }

    fun openShift() {
        val cash = _uiState.value.openingCash
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = operationsRepository.openShift(cash)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            currentShift = result.data,
                            isLoading = false,
                            shiftJustOpened = true,
                            openingCash = ""
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun closeShift() {
        val cash = _uiState.value.closingCash
        val note = _uiState.value.closingNote.takeIf { it.isNotBlank() }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = operationsRepository.closeShift(cash, note)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            currentShift = null,
                            isLoading = false,
                            shiftJustClosed = true,
                            closingCash = "",
                            closingNote = ""
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
