package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.CashCount
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Shift
import id.rancak.app.domain.repository.OperationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ShiftUiState(
    val currentShift: Shift? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val openingCash: String = "",
    val closingCash: String = "",
    val closingNote: String = "",
    val shiftJustOpened: Boolean = false,
    val shiftJustClosed: Boolean = false,
    // ── Cash count (rekonsiliasi kas) ─────────────────────────────────────────
    val cashCounts: List<CashCount> = emptyList(),
    val isCountLoading: Boolean = false,
    val isCountSubmitting: Boolean = false,
    val cashCountError: String? = null,
    val cashCountSuccess: Boolean = false
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

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun clearCashCountError() = _uiState.update { it.copy(cashCountError = null) }
    fun clearCashCountSuccess() = _uiState.update { it.copy(cashCountSuccess = false) }

    fun loadCashCounts(shiftUuid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCountLoading = true, cashCountError = null) }
            when (val result = operationsRepository.getCashCounts(shiftUuid)) {
                is Resource.Success -> _uiState.update { it.copy(cashCounts = result.data, isCountLoading = false) }
                is Resource.Error   -> _uiState.update { it.copy(cashCountError = result.message, isCountLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun submitCashCount(actualCash: Double, note: String? = null) {
        val shiftUuid = _uiState.value.currentShift?.uuid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCountSubmitting = true, cashCountError = null) }
            when (val result = operationsRepository.submitCashCount(shiftUuid, actualCash, note = note)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            cashCounts = it.cashCounts + result.data,
                            isCountSubmitting = false,
                            cashCountSuccess = true
                        )
                    }
                }
                is Resource.Error   -> _uiState.update { it.copy(cashCountError = result.message, isCountSubmitting = false) }
                is Resource.Loading -> {}
            }
        }
    }
}
