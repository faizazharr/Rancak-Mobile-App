package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CashExpenseUiState(
    val cashIns: List<CashIn> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCashInForm: Boolean = false,
    val showExpenseForm: Boolean = false,
    val formAmount: String = "",
    val formDescription: String = "",
    val formSource: String = "",
    val formNote: String = ""
)

class CashExpenseViewModel(
    private val financeRepository: FinanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CashExpenseUiState())
    val uiState: StateFlow<CashExpenseUiState> = _uiState.asStateFlow()

    fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = financeRepository.getCashIns()) {
                is Resource.Success -> _uiState.update { it.copy(cashIns = result.data) }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
            when (val result = financeRepository.getExpenses()) {
                is Resource.Success -> _uiState.update { it.copy(expenses = result.data, isLoading = false) }
                is Resource.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun toggleCashInForm() { _uiState.update { it.copy(showCashInForm = !it.showCashInForm, formAmount = "", formDescription = "", formSource = "", formNote = "") } }
    fun toggleExpenseForm() { _uiState.update { it.copy(showExpenseForm = !it.showExpenseForm, formAmount = "", formDescription = "", formNote = "") } }
    fun onAmountChange(v: String) { _uiState.update { it.copy(formAmount = v) } }
    fun onDescriptionChange(v: String) { _uiState.update { it.copy(formDescription = v) } }
    fun onSourceChange(v: String) { _uiState.update { it.copy(formSource = v) } }
    fun onNoteChange(v: String) { _uiState.update { it.copy(formNote = v) } }

    fun submitCashIn() {
        val s = _uiState.value
        val amount = s.formAmount.toLongOrNull() ?: return
        viewModelScope.launch {
            when (val result = financeRepository.createCashIn(amount, s.formSource, s.formDescription, s.formNote.ifBlank { null })) {
                is Resource.Success -> { toggleCashInForm(); loadAll() }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun submitExpense() {
        val s = _uiState.value
        val amount = s.formAmount.toLongOrNull() ?: return
        viewModelScope.launch {
            when (val result = financeRepository.createExpense(amount, s.formDescription, s.formNote.ifBlank { null })) {
                is Resource.Success -> { toggleExpenseForm(); loadAll() }
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteCashIn(uuid: String) {
        viewModelScope.launch {
            when (val result = financeRepository.deleteCashIn(uuid)) {
                is Resource.Success -> loadAll()
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteExpense(uuid: String) {
        viewModelScope.launch {
            when (val result = financeRepository.deleteExpense(uuid)) {
                is Resource.Success -> loadAll()
                is Resource.Error -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }
}
