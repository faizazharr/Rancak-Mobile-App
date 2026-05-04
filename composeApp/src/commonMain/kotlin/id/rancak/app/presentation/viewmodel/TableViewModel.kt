package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Table
import id.rancak.app.domain.repository.AdminRepository
import id.rancak.app.domain.repository.OperationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class TableUiState(
    val tables: List<Table> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    // ── Admin mode state ────────────────────────────────────────────────────
    val adminMode: Boolean = false,
    val isSubmitting: Boolean = false,
    val editingTable: Table? = null,
    val showFormDialog: Boolean = false,
    val pendingDelete: Table? = null,
    val snackbarMessage: String? = null
)

/**
 * ViewModel untuk denah meja & manajemen meja (admin).
 *
 * - [OperationsRepository] dipakai untuk membaca list (kasir + admin).
 * - [AdminRepository] dipakai untuk CRUD (hanya admin/owner).
 *
 * State `adminMode` dikontrol dari UI berdasarkan role aktif (lihat
 * `RoleGate`). VM tidak melakukan role check sendiri — itu tanggung jawab UI.
 */
class TableViewModel(
    private val operationsRepository: OperationsRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TableUiState())
    val uiState: StateFlow<TableUiState> = _uiState.asStateFlow()

    fun loadTables() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = operationsRepository.getTables()) {
                is Resource.Success ->
                    _uiState.update {
                        it.copy(
                            tables    = result.data.sortedBy { t -> t.sortOrder },
                            isLoading = false
                        )
                    }
                is Resource.Error ->
                    _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> { /* not used */ }
            }
        }
    }

    // ── Admin mode toggle ──────────────────────────────────────────────────

    fun setAdminMode(enabled: Boolean) {
        _uiState.update { it.copy(adminMode = enabled) }
    }

    // ── Form dialog ─────────────────────────────────────────────────────────

    fun openCreateDialog() {
        _uiState.update { it.copy(editingTable = null, showFormDialog = true) }
    }

    fun openEditDialog(table: Table) {
        _uiState.update { it.copy(editingTable = table, showFormDialog = true) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showFormDialog = false, editingTable = null) }
    }

    // ── Save (create or update) ────────────────────────────────────────────

    fun saveTable(
        name: String,
        area: String?,
        capacity: Int,
        isActive: Boolean,
        sortOrder: Int
    ) {
        val editing = _uiState.value.editingTable
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result = if (editing == null) {
                adminRepository.createTable(
                    name      = name,
                    area      = area,
                    capacity  = capacity,
                    isActive  = isActive,
                    sortOrder = sortOrder
                )
            } else {
                adminRepository.updateTable(
                    tableId   = editing.uuid,
                    name      = name,
                    area      = area,
                    capacity  = capacity,
                    isActive  = isActive,
                    sortOrder = sortOrder
                )
            }
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting    = false,
                            showFormDialog  = false,
                            editingTable    = null,
                            snackbarMessage = if (editing == null) "Meja berhasil ditambahkan" else "Meja diperbarui"
                        )
                    }
                    loadTables()
                }
                is Resource.Error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting    = false,
                            snackbarMessage = result.message
                        )
                    }
                is Resource.Loading -> { /* not used */ }
            }
        }
    }

    // ── Delete ──────────────────────────────────────────────────────────────

    fun requestDelete(table: Table) {
        _uiState.update { it.copy(pendingDelete = table) }
    }

    fun cancelDelete() {
        _uiState.update { it.copy(pendingDelete = null) }
    }

    fun confirmDelete() {
        val target = _uiState.value.pendingDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (val result = adminRepository.deleteTable(target.uuid)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting    = false,
                            pendingDelete   = null,
                            snackbarMessage = "Meja '${target.name}' dihapus"
                        )
                    }
                    loadTables()
                }
                is Resource.Error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting    = false,
                            pendingDelete   = null,
                            snackbarMessage = result.message
                        )
                    }
                is Resource.Loading -> { /* not used */ }
            }
        }
    }

    fun consumeSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
