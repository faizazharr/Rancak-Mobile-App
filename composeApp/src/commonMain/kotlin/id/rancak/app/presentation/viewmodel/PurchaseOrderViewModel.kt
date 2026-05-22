package id.rancak.app.presentation.viewmodel

import androidx.compose.runtime.Immutable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.rancak.app.domain.model.POItemEntry
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.PurchaseOrder
import id.rancak.app.domain.model.PurchaseOrderItem
import id.rancak.app.domain.model.ReceiveItemEntry
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Supplier
import id.rancak.app.domain.repository.InventoryRepository
import id.rancak.app.domain.repository.ProductRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class PurchaseOrderUiState(
    val orders: ImmutableList<PurchaseOrder> = persistentListOf(),
    val suppliers: ImmutableList<Supplier> = persistentListOf(),
    val products: ImmutableList<Product> = persistentListOf(),
    val selectedOrder: PurchaseOrder? = null,
    val isLoading: Boolean = false,
    val isLoadingDetail: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showCreateDialog: Boolean = false,
    val showCancelDialog: Boolean = false,
    /** Filter status: null = semua. */
    val statusFilter: String? = null,
    // Create / Edit PO header form fields
    val formSupplierUuid: String? = null,
    val formOrderDate: String = "",
    val formExpectedDate: String = "",
    val formNotes: String = "",
    // Edit header
    val showEditHeaderDialog: Boolean = false,
    // Add / Edit item form
    val showAddItemDialog: Boolean = false,
    val showEditItemDialog: Boolean = false,
    val editingItem: PurchaseOrderItem? = null,
    val formItemProductUuid: String = "",
    val formItemQty: String = "",
    val formItemUnitCost: String = "",
    val formItemNotes: String = "",
    // Receive form
    val showReceiveDialog: Boolean = false,
    val receiveEntries: PersistentMap<String, String> = persistentMapOf(),
    val formReceiveNotes: String = ""
)

class PurchaseOrderViewModel(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseOrderUiState())
    val uiState: StateFlow<PurchaseOrderUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders(status: String? = _uiState.value.statusFilter) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val ordersDeferred = async { inventoryRepository.getPurchaseOrders(status = status) }
            val suppliersDeferred = async { inventoryRepository.getSuppliers() }
            val productsDeferred = async { productRepository.getProducts() }

            val ordersRes = ordersDeferred.await()
            val suppliersRes = suppliersDeferred.await()
            val productsRes = productsDeferred.await()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    orders = (ordersRes as? Resource.Success)?.data?.toImmutableList() ?: state.orders,
                    suppliers = (suppliersRes as? Resource.Success)?.data?.toImmutableList() ?: state.suppliers,
                    products = (productsRes as? Resource.Success)?.data?.toImmutableList() ?: state.products,
                    error = (ordersRes as? Resource.Error)?.message
                )
            }
        }
    }

    fun setStatusFilter(status: String?) {
        _uiState.update { it.copy(statusFilter = status) }
        loadOrders(status)
    }

    fun selectOrder(order: PurchaseOrder) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetail = true) }
            when (val result = inventoryRepository.getPurchaseOrderDetail(order.uuid)) {
                is Resource.Success -> _uiState.update { it.copy(selectedOrder = result.data, isLoadingDetail = false) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoadingDetail = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun closeDetail() {
        _uiState.update { it.copy(selectedOrder = null) }
    }

    fun openCreateDialog() {
        _uiState.update { it.copy(
            showCreateDialog  = true,
            formSupplierUuid  = null,
            formOrderDate     = "",
            formExpectedDate  = "",
            formNotes         = ""
        ) }
    }

    fun closeCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun onFormSupplierChange(supplierUuid: String?) {
        _uiState.update { it.copy(formSupplierUuid = supplierUuid) }
    }

    fun onFormOrderDateChange(date: String) {
        _uiState.update { it.copy(formOrderDate = date) }
    }

    fun onFormExpectedDateChange(date: String) {
        _uiState.update { it.copy(formExpectedDate = date) }
    }

    fun onFormNotesChange(notes: String) {
        _uiState.update { it.copy(formNotes = notes) }
    }

    fun createPurchaseOrder() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = inventoryRepository.createPurchaseOrder(
                supplierUuid  = state.formSupplierUuid,
                orderDate     = state.formOrderDate.ifBlank { null },
                expectedDate  = state.formExpectedDate.ifBlank { null },
                notes         = state.formNotes.ifBlank { null }
            )
            when (result) {
                is Resource.Success -> {
                    loadOrders()
                    _uiState.update { it.copy(
                        isSaving         = false,
                        showCreateDialog = false,
                        successMessage   = "Purchase order berhasil dibuat"
                    ) }
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun sendOrder(poId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = inventoryRepository.sendPurchaseOrder(poId)) {
                is Resource.Success -> {
                    loadOrders()
                    _uiState.update { it.copy(
                        isSaving       = false,
                        selectedOrder  = result.data,
                        successMessage = "PO berhasil dikirim ke supplier"
                    ) }
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun openCancelDialog() {
        _uiState.update { it.copy(showCancelDialog = true) }
    }

    fun closeCancelDialog() {
        _uiState.update { it.copy(showCancelDialog = false) }
    }

    fun cancelOrder() {
        val order = _uiState.value.selectedOrder ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = inventoryRepository.cancelPurchaseOrder(order.uuid)) {
                is Resource.Success -> {
                    loadOrders()
                    _uiState.update { it.copy(
                        isSaving         = false,
                        showCancelDialog = false,
                        selectedOrder    = result.data,
                        successMessage   = "PO berhasil dibatalkan"
                    ) }
                }
                is Resource.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
    fun clearError()          = _uiState.update { it.copy(error = null) }

    // ── Edit PO Header ───────────────────────────────────────────────────────

    fun openEditHeaderDialog() {
        val po = _uiState.value.selectedOrder ?: return
        _uiState.update { it.copy(
            showEditHeaderDialog = true,
            formSupplierUuid     = po.supplierUuid,
            formOrderDate        = po.orderDate,
            formExpectedDate     = po.expectedDate ?: "",
            formNotes            = po.notes ?: ""
        ) }
    }

    fun closeEditHeaderDialog() {
        _uiState.update { it.copy(showEditHeaderDialog = false) }
    }

    fun updatePOHeader() {
        val state = _uiState.value
        val poId  = state.selectedOrder?.uuid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = inventoryRepository.updatePurchaseOrder(
                poId         = poId,
                supplierUuid = state.formSupplierUuid,
                orderDate    = state.formOrderDate.ifBlank { null },
                expectedDate = state.formExpectedDate.ifBlank { null },
                notes        = state.formNotes.ifBlank { null }
            )) {
                is Resource.Success -> {
                    loadOrders()
                    _uiState.update { it.copy(
                        isSaving             = false,
                        showEditHeaderDialog  = false,
                        selectedOrder        = result.data,
                        successMessage       = "PO berhasil diperbarui"
                    ) }
                }
                is Resource.Error   -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Add Item ─────────────────────────────────────────────────────────────

    fun openAddItemDialog() {
        _uiState.update { it.copy(
            showAddItemDialog   = true,
            formItemProductUuid = "",
            formItemQty         = "",
            formItemUnitCost    = "",
            formItemNotes       = ""
        ) }
    }

    fun closeAddItemDialog() {
        _uiState.update { it.copy(showAddItemDialog = false) }
    }

    fun onItemProductChange(uuid: String) { _uiState.update { it.copy(formItemProductUuid = uuid) } }
    fun onItemQtyChange(qty: String)      { _uiState.update { it.copy(formItemQty = qty) } }
    fun onItemUnitCostChange(cost: String){ _uiState.update { it.copy(formItemUnitCost = cost) } }
    fun onItemNotesChange(notes: String)  { _uiState.update { it.copy(formItemNotes = notes) } }

    fun addItem() {
        val state = _uiState.value
        val poId  = state.selectedOrder?.uuid ?: return
        val productUuid = state.formItemProductUuid.ifBlank { return }
        val qty = state.formItemQty.toDoubleOrNull() ?: return
        val unitCost = state.formItemUnitCost.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = inventoryRepository.addPurchaseOrderItem(
                poId = poId,
                item = POItemEntry(
                    productUuid = productUuid,
                    qtyOrdered  = qty,
                    unitCost    = unitCost,
                    notes       = state.formItemNotes.ifBlank { null }
                )
            )) {
                is Resource.Success -> {
                    refreshDetail()
                    _uiState.update { it.copy(
                        isSaving          = false,
                        showAddItemDialog  = false,
                        successMessage    = "Item berhasil ditambahkan"
                    ) }
                }
                is Resource.Error   -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Edit Item ────────────────────────────────────────────────────────────

    fun openEditItemDialog(item: PurchaseOrderItem) {
        _uiState.update { it.copy(
            showEditItemDialog  = true,
            editingItem         = item,
            formItemQty         = item.qtyOrdered.toString(),
            formItemUnitCost    = item.unitCost.toLong().toString(),
            formItemNotes       = item.notes ?: ""
        ) }
    }

    fun closeEditItemDialog() {
        _uiState.update { it.copy(showEditItemDialog = false, editingItem = null) }
    }

    fun updateItem() {
        val state  = _uiState.value
        val poId   = state.selectedOrder?.uuid ?: return
        val itemId = state.editingItem?.uuid ?: return
        val qty    = state.formItemQty.toDoubleOrNull() ?: return
        val cost   = state.formItemUnitCost.toDoubleOrNull() ?: 0.0
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = inventoryRepository.updatePurchaseOrderItem(
                poId       = poId,
                itemId     = itemId,
                qtyOrdered = qty,
                unitCost   = cost,
                notes      = state.formItemNotes.ifBlank { null }
            )) {
                is Resource.Success -> {
                    refreshDetail()
                    _uiState.update { it.copy(
                        isSaving           = false,
                        showEditItemDialog  = false,
                        editingItem        = null,
                        successMessage     = "Item berhasil diperbarui"
                    ) }
                }
                is Resource.Error   -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    fun deleteItem(item: PurchaseOrderItem) {
        val poId = _uiState.value.selectedOrder?.uuid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = inventoryRepository.deletePurchaseOrderItem(poId, item.uuid)) {
                is Resource.Success -> {
                    refreshDetail()
                    _uiState.update { it.copy(isSaving = false, successMessage = "Item berhasil dihapus") }
                }
                is Resource.Error   -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Receive PO ───────────────────────────────────────────────────────────

    fun openReceiveDialog() {
        val items = _uiState.value.selectedOrder?.items ?: return
        _uiState.update { it.copy(
            showReceiveDialog  = true,
            receiveEntries     = items.associate { item -> item.uuid to "" }.toPersistentMap(),
            formReceiveNotes   = ""
        ) }
    }

    fun closeReceiveDialog() {
        _uiState.update { it.copy(showReceiveDialog = false) }
    }

    fun onReceiveQtyChange(itemUuid: String, qty: String) {
        _uiState.update { it.copy(receiveEntries = it.receiveEntries.put(itemUuid, qty)) }
    }

    fun onReceiveNotesChange(notes: String) {
        _uiState.update { it.copy(formReceiveNotes = notes) }
    }

    fun receiveOrder() {
        val state   = _uiState.value
        val poId    = state.selectedOrder?.uuid ?: return
        val entries = state.receiveEntries.entries.mapNotNull { (itemUuid, qtyStr) ->
            val qty = qtyStr.toDoubleOrNull() ?: return@mapNotNull null
            if (qty <= 0) return@mapNotNull null
            ReceiveItemEntry(itemUuid = itemUuid, qtyReceived = qty)
        }
        if (entries.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = inventoryRepository.receivePurchaseOrder(
                poId  = poId,
                items = entries,
                notes = state.formReceiveNotes.ifBlank { null }
            )) {
                is Resource.Success -> {
                    loadOrders()
                    _uiState.update { it.copy(
                        isSaving           = false,
                        showReceiveDialog  = false,
                        selectedOrder      = result.data,
                        successMessage     = "Penerimaan barang berhasil dicatat"
                    ) }
                }
                is Resource.Error   -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun refreshDetail() {
        val order = _uiState.value.selectedOrder ?: return
        viewModelScope.launch {
            when (val result = inventoryRepository.getPurchaseOrderDetail(order.uuid)) {
                is Resource.Success -> _uiState.update { it.copy(selectedOrder = result.data) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message) }
                is Resource.Loading -> {}
            }
        }
    }
}
