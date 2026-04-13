package id.rancak.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.repository.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val orderType: OrderType = OrderType.DINE_IN,
    val tableUuid: String? = null,
    val customerName: String = "",
    val note: String = ""
) {
    val subtotal: Long get() = items.sumOf { it.subtotal }
    val itemCount: Int get() = items.sumOf { it.qty }
    val isEmpty: Boolean get() = items.isEmpty()
}

class CartViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun addProduct(product: Product, variantUuid: String? = null, variantName: String? = null) {
        _uiState.update { state ->
            val existingIndex = state.items.indexOfFirst {
                it.productUuid == product.uuid && it.variantUuid == variantUuid
            }
            if (existingIndex >= 0) {
                val updated = state.items.toMutableList()
                val existing = updated[existingIndex]
                updated[existingIndex] = existing.copy(qty = existing.qty + 1)
                state.copy(items = updated)
            } else {
                state.copy(
                    items = state.items + CartItem(
                        productUuid = product.uuid,
                        productName = product.name,
                        qty = 1,
                        price = product.price,
                        variantUuid = variantUuid,
                        variantName = variantName,
                        imageUrl = product.imageUrl
                    )
                )
            }
        }
    }

    fun updateQuantity(productUuid: String, variantUuid: String?, qty: Int) {
        _uiState.update { state ->
            if (qty <= 0) {
                state.copy(items = state.items.filter {
                    !(it.productUuid == productUuid && it.variantUuid == variantUuid)
                })
            } else {
                state.copy(items = state.items.map {
                    if (it.productUuid == productUuid && it.variantUuid == variantUuid) {
                        it.copy(qty = qty)
                    } else it
                })
            }
        }
    }

    fun removeItem(productUuid: String, variantUuid: String?) {
        updateQuantity(productUuid, variantUuid, 0)
    }

    fun updateItemNote(productUuid: String, variantUuid: String?, note: String) {
        _uiState.update { state ->
            state.copy(items = state.items.map {
                if (it.productUuid == productUuid && it.variantUuid == variantUuid) {
                    it.copy(note = note)
                } else it
            })
        }
    }

    fun setOrderType(orderType: OrderType) {
        _uiState.update { it.copy(orderType = orderType) }
    }

    fun setTable(tableUuid: String?) {
        _uiState.update { it.copy(tableUuid = tableUuid) }
    }

    fun setCustomerName(name: String) {
        _uiState.update { it.copy(customerName = name) }
    }

    fun setNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun clearCart() {
        _uiState.value = CartUiState()
    }
}
