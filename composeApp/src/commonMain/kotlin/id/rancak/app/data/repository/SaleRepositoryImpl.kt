package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.dto.sale.CreateSaleRequest
import id.rancak.app.data.remote.dto.sale.SaleItemRequest
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.CartItem
import id.rancak.app.domain.repository.SaleRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SaleRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : SaleRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("No tenant selected")

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createSale(
        items: List<CartItem>,
        paymentMethod: PaymentMethod,
        paidAmount: Long,
        orderType: OrderType,
        tableUuid: String?,
        customerName: String?,
        note: String?,
        hold: Boolean
    ): Resource<Sale> {
        return try {
            val idempotencyKey = Uuid.random().toString()
            val request = CreateSaleRequest(
                items = items.map { cartItem ->
                    SaleItemRequest(
                        productUuid = cartItem.productUuid,
                        qty = cartItem.qty,
                        variantUuid = cartItem.variantUuid,
                        note = cartItem.note
                    )
                },
                paymentMethod = paymentMethod.value,
                paidAmount = paidAmount,
                orderType = orderType.value,
                tableUuid = tableUuid,
                customerName = customerName,
                note = note,
                hold = hold,
                deviceCreatedAt = null // set by server
            )
            val response = api.createSale(tenantUuid, request, idempotencyKey)
            if (response.status == "ok" && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Failed to create sale")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getSales(dateFrom: String?, dateTo: String?): Resource<List<Sale>> {
        return try {
            val response = api.getSales(tenantUuid, dateFrom, dateTo)
            if (response.status == "ok" && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Failed to load sales")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getSaleDetail(saleUuid: String): Resource<Sale> {
        return try {
            val response = api.getSaleDetail(tenantUuid, saleUuid)
            if (response.status == "ok" && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Sale not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}
