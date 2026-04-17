package id.rancak.app.data.repository

import id.rancak.app.data.local.OfflineSaleQueue
import id.rancak.app.data.local.PendingSale
import id.rancak.app.data.local.PendingSaleItem
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.local.db.dao.SaleDao
import id.rancak.app.data.local.db.entity.toEntity
import id.rancak.app.data.local.db.entity.toDomain
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.dto.sale.CreateSaleRequest
import id.rancak.app.data.remote.dto.sale.SaleItemRequest
import id.rancak.app.data.sync.SyncManager
import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.CartItem
import id.rancak.app.domain.repository.SaleRepository
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SaleRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager,
    private val offlineQueue: OfflineSaleQueue,
    private val syncManager: SyncManager,
    private val saleDao: SaleDao
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
        hold: Boolean,
        pax: Int,
        discount: Long,
        tax: Long,
        adminFee: Long,
        deliveryFee: Long,
        tip: Long,
        voucherCode: String?
    ): Resource<Sale> {
        val idempotencyKey  = Uuid.random().toString()
        val deviceCreatedAt = Clock.System.now().toString()
        val deviceId        = tokenManager.deviceId

        return try {
            val request = CreateSaleRequest(
                items = items.map { cartItem ->
                    SaleItemRequest(
                        productUuid = cartItem.productUuid,
                        qty         = cartItem.qty,
                        variantUuid = cartItem.variantUuid,
                        note        = cartItem.note
                    )
                },
                paymentMethod   = paymentMethod.value,
                paidAmount      = paidAmount,
                orderType       = orderType.value,
                tableUuid       = tableUuid,
                customerName    = customerName,
                pax             = pax.takeIf { it > 0 },
                discount        = discount.takeIf { it > 0 },
                tax             = tax.takeIf { it > 0 },
                adminFee        = adminFee.takeIf { it > 0 },
                deliveryFee     = deliveryFee.takeIf { it > 0 },
                tip             = tip.takeIf { it > 0 },
                voucherCode     = voucherCode?.takeIf { it.isNotBlank() },
                note            = note,
                hold            = hold,
                deviceCreatedAt = deviceCreatedAt,
                deviceId        = deviceId
            )
            val response = api.createSale(tenantUuid, request, idempotencyKey)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Failed to create sale")
            }
        } catch (e: Exception) {
            val networkError = e.message?.let {
                it.contains("UnknownHostException", ignoreCase = true) ||
                it.contains("ConnectException", ignoreCase = true) ||
                it.contains("SocketTimeoutException", ignoreCase = true) ||
                it.contains("Network is unreachable", ignoreCase = true) ||
                it.contains("Unable to resolve host", ignoreCase = true) ||
                it.contains("failed to connect", ignoreCase = true)
            } ?: false

            if (networkError) {
                offlineQueue.enqueue(
                    PendingSale(
                        idempotencyKey  = idempotencyKey,
                        tenantUuid      = tenantUuid,
                        items           = items.map { PendingSaleItem(it.productUuid, it.qty, it.variantUuid, it.note) },
                        paymentMethod   = paymentMethod.value,
                        paidAmount      = paidAmount,
                        orderType       = orderType.value,
                        tableUuid       = tableUuid,
                        customerName    = customerName,
                        pax             = pax,
                        discount        = discount,
                        tax             = tax,
                        adminFee        = adminFee,
                        deliveryFee     = deliveryFee,
                        tip             = tip,
                        voucherCode     = voucherCode,
                        note            = note,
                        hold            = hold,
                        deviceCreatedAt = deviceCreatedAt,
                        deviceId        = deviceId,
                        enqueuedAt      = Clock.System.now().toEpochMilliseconds()
                    )
                )
                syncManager.scheduleSync()
                Resource.Error("Offline: sale queued for sync (${offlineQueue.size} pending)")
            } else {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    override suspend fun getSales(dateFrom: String?, dateTo: String?): Resource<List<Sale>> {
        return try {
            val response = api.getSales(tenantUuid, dateFrom, dateTo)
            if (response.isSuccess && response.data != null) {
                val sales = response.data.map { it.toDomain() }
                // Cache to Room
                val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                saleDao.upsertSalesWithItems(
                    sales = sales.map { it.toEntity(now) },
                    items = sales.flatMap { sale ->
                        sale.items.map { it.toEntity(sale.uuid) }
                    }
                )
                Resource.Success(sales)
            } else {
                Resource.Error(response.message ?: "Failed to load sales")
            }
        } catch (e: Exception) {
            // Fallback to cached sales
            val cached = saleDao.getAll()
            if (cached.isNotEmpty()) {
                val sales = cached.map { entity ->
                    entity.toDomain(saleDao.getItemsForSale(entity.uuid))
                }
                Resource.Success(sales)
            } else {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    override suspend fun getSaleDetail(saleUuid: String): Resource<Sale> {
        return try {
            val response = api.getSaleDetail(tenantUuid, saleUuid)
            if (response.isSuccess && response.data != null) {
                val sale = response.data.toDomain()
                // Cache to Room
                val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
                saleDao.upsertSalesWithItems(
                    sales = listOf(sale.toEntity(now)),
                    items = sale.items.map { it.toEntity(sale.uuid) }
                )
                Resource.Success(sale)
            } else {
                Resource.Error(response.message ?: "Sale not found")
            }
        } catch (e: Exception) {
            val cached = saleDao.findByUuid(saleUuid)
            if (cached != null) {
                Resource.Success(cached.toDomain(saleDao.getItemsForSale(saleUuid)))
            } else {
                Resource.Error(e.message ?: "Network error")
            }
        }
    }

    override suspend fun serveSale(saleUuid: String): Resource<Sale> {
        return try {
            val response = api.serveSale(tenantUuid, saleUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Failed to serve sale")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun paySale(saleUuid: String, paymentMethod: PaymentMethod, paidAmount: Long): Resource<Sale> {
        return try {
            val request = id.rancak.app.data.remote.dto.sale.PayHeldOrderRequest(
                paymentMethod = paymentMethod.value,
                paidAmount = paidAmount
            )
            val response = api.payHeldOrder(tenantUuid, saleUuid, request)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal membayar pesanan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun voidSale(saleUuid: String, reason: String?): Resource<Sale> {
        return try {
            val response = api.voidSale(tenantUuid, saleUuid, reason)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Failed to void sale")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun cancelSale(saleUuid: String, reason: String?): Resource<Sale> {
        return try {
            val response = api.cancelSale(tenantUuid, saleUuid, reason)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Failed to cancel sale")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun refundSale(saleUuid: String, amount: Long?, reason: String?): Resource<Sale> {
        return try {
            // For now, refund all items — caller should provide specific items for per-item refund
            val saleDetail = api.getSaleDetail(tenantUuid, saleUuid)
            val items = saleDetail.data?.items?.map {
                id.rancak.app.data.remote.dto.sale.RefundItemRequest(
                    saleItemUuid = it.uuid,
                    qty = it.qty.toDoubleOrNull()?.toInt() ?: 1
                )
            } ?: emptyList()
            val request = id.rancak.app.data.remote.dto.sale.RefundRequest(items = items, reason = reason)
            val response = api.refundSale(tenantUuid, saleUuid, request)
            if (response.isSuccess && response.data != null) {
                // Re-fetch the sale to get updated status
                val updated = api.getSaleDetail(tenantUuid, saleUuid)
                if (updated.isSuccess && updated.data != null) {
                    Resource.Success(updated.data.toDomain())
                } else {
                    Resource.Error("Refund berhasil, gagal memuat detail")
                }
            } else {
                Resource.Error(response.message ?: "Gagal melakukan refund")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun moveTable(saleUuid: String, tableUuid: String): Resource<Sale> {
        return try {
            val response = api.moveTable(tenantUuid, saleUuid, tableUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Failed to move table")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun createQrPayment(saleUuid: String): Resource<QrPayment> {
        return try {
            val response = api.createQrPayment(tenantUuid, saleUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal membuat QR QRIS")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun getQrPaymentStatus(saleUuid: String): Resource<QrPayment> {
        return try {
            val response = api.getQrPaymentStatus(tenantUuid, saleUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal mengecek status QR")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }
}

