package id.rancak.app.data.repository

import id.rancak.app.data.local.OfflineSaleQueue
import id.rancak.app.data.local.PendingSale
import id.rancak.app.data.local.PendingSaleItem
import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.local.db.dao.SaleDao
import id.rancak.app.data.local.db.entity.toEntity
import id.rancak.app.data.local.db.entity.toDomain
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.dto.sale.SplitBillRequest
import id.rancak.app.domain.repository.SplitBillResult
import id.rancak.app.domain.repository.SplitPaymentEntry
import id.rancak.app.data.remote.api.splitBill
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.*
import id.rancak.app.data.remote.dto.sale.CreateSaleRequest
import id.rancak.app.data.remote.dto.sale.SaleItemRequest
import id.rancak.app.data.sync.SyncScheduler
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
    private val syncManager: SyncScheduler,
    private val saleDao: SaleDao
) : SaleRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

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
            if ((response.isSuccess || response.statusCode == 409) && response.data != null) {
                // 409 = idempotency duplicate — treat as success
                Resource.Success(response.data.toDomain())
            } else if (response.statusCode == 409) {
                // 409 tanpa data — penjualan sudah ada
                Resource.Error(response.message ?: "Penjualan sudah tercatat sebelumnya")
            } else {
                Resource.Error(response.message ?: "Gagal membuat penjualan")
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
                // QRIS membutuhkan koneksi internet — tidak bisa di-queue offline
                if (paymentMethod == PaymentMethod.QRIS) {
                    return Resource.Error("QRIS membutuhkan koneksi internet. Periksa jaringan Anda.")
                }
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
                Resource.Error("Offline: penjualan tersimpan, akan dikirim saat online (${offlineQueue.size} antrian)")
            } else {
                Resource.Error(e.message ?: "Kesalahan jaringan")
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun createSaleWithSplitPayment(
        items: List<CartItem>,
        payments: List<SplitPaymentEntry>,
        orderType: OrderType,
        tableUuid: String?,
        customerName: String?,
        note: String?,
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
                payments = payments.map {
                    id.rancak.app.data.remote.dto.sale.SplitPaymentRequest(
                        method = it.method.value,
                        amount = it.amount
                    )
                },
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
                hold            = false,
                deviceCreatedAt = deviceCreatedAt,
                deviceId        = deviceId
            )
            val response = api.createSale(tenantUuid, request, idempotencyKey)
            if ((response.isSuccess || response.statusCode == 409) && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal membuat penjualan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
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
                Resource.Error(response.message ?: "Gagal memuat daftar penjualan")
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
                Resource.Error(e.message ?: "Kesalahan jaringan")
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
                Resource.Error(response.message ?: "Penjualan tidak ditemukan")
            }
        } catch (e: Exception) {
            val cached = saleDao.findByUuid(saleUuid)
            if (cached != null) {
                Resource.Success(cached.toDomain(saleDao.getItemsForSale(saleUuid)))
            } else {
                Resource.Error(e.message ?: "Kesalahan jaringan")
            }
        }
    }

    override suspend fun serveSale(saleUuid: String): Resource<Sale> {
        return try {
            val response = api.serveSale(tenantUuid, saleUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal menyajikan pesanan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
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
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun paySaleWithSplitPayment(
        saleUuid: String,
        payments: List<SplitPaymentEntry>
    ): Resource<Sale> {
        return try {
            val request = id.rancak.app.data.remote.dto.sale.PayHeldOrderRequest(
                payments = payments.map {
                    id.rancak.app.data.remote.dto.sale.SplitPaymentRequest(
                        method = it.method.value,
                        amount = it.amount
                    )
                }
            )
            val response = api.payHeldOrder(tenantUuid, saleUuid, request)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal membayar pesanan dengan split payment")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun splitBill(saleUuid: String, itemIds: List<String>): Resource<SplitBillResult> {
        return try {
            val request = SplitBillRequest(itemIds = itemIds)
            val response = api.splitBill(tenantUuid, saleUuid, request)
            if (response.isSuccess && response.data != null) {
                Resource.Success(
                    SplitBillResult(
                        original = response.data.original.toDomain(),
                        newSale  = response.data.newSale.toDomain()
                    )
                )
            } else {
                Resource.Error(response.message ?: "Gagal memisahkan tagihan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun addItemsToHeldOrder(
        saleUuid: String,
        items: List<CartItem>
    ): Resource<Sale> {
        return try {
            val request = id.rancak.app.data.remote.dto.sale.AddHeldOrderItemsRequest(
                items = items.map { cartItem ->
                    SaleItemRequest(
                        productUuid = cartItem.productUuid,
                        qty         = cartItem.qty,
                        variantUuid = cartItem.variantUuid,
                        note        = cartItem.note
                    )
                }
            )
            val response = api.addHeldOrderItems(tenantUuid, saleUuid, request)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal menambah item ke pesanan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun removeHeldOrderItem(saleUuid: String, itemUuid: String): Resource<Sale> {
        return try {
            val response = api.deleteHeldOrderItem(tenantUuid, saleUuid, itemUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal menghapus item dari pesanan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun voidSale(saleUuid: String, reason: String?): Resource<Sale> {
        return try {
            val response = api.voidSale(tenantUuid, saleUuid, reason)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal membatalkan penjualan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun cancelSale(saleUuid: String, reason: String?): Resource<Sale> {
        return try {
            val response = api.cancelSale(tenantUuid, saleUuid, reason)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal membatalkan pesanan")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
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
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun moveTable(saleUuid: String, tableUuid: String): Resource<Sale> {
        return try {
            val response = api.moveTable(tenantUuid, saleUuid, tableUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal memindahkan meja")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
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
            Resource.Error(e.message ?: "Kesalahan jaringan")
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
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getSaleReceipt(saleUuid: String): Resource<Receipt> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getSaleReceipt(tenantUuid, saleUuid)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal mengambil struk")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }

    override suspend fun getReceiptEscpos(saleUuid: String): Resource<ByteArray> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val bytes = api.getReceiptEscpos(tenantUuid, saleUuid)
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil data cetak struk")
        }
    }

    override suspend fun getReceiptKitchen(saleUuid: String): Resource<ByteArray> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val bytes = api.getReceiptKitchen(tenantUuid, saleUuid)
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil tiket dapur")
        }
    }

    override suspend fun getReceiptCombined(saleUuid: String, kotFirst: Boolean): Resource<ByteArray> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val bytes = api.getReceiptCombined(tenantUuid, saleUuid, kotFirst)
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Gagal mengambil data cetak gabungan")
        }
    }

    override suspend fun batchSales(sales: List<CartItem>): Resource<Unit> {
        // Batch upload ditangani oleh SyncManager via POST /tenants/:id/sales/batch.
        // Method ini hanya placeholder interface — tidak dipanggil langsung dari UI.
        return Resource.Success(Unit)
    }

    override suspend fun getOrderBoard(date: String?, includeDone: Boolean): Resource<List<OrderBoardOrder>> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val response = api.getOrderBoard(tenantUuid, date, includeDone)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal mengambil order board")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }
    }
}

