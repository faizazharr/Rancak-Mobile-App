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
import id.rancak.app.data.remote.api.addHeldOrderItems
import id.rancak.app.data.remote.api.cancelSale
import id.rancak.app.data.remote.api.createQrPayment
import id.rancak.app.data.remote.api.createSale
import id.rancak.app.data.remote.api.deleteHeldOrderItem
import id.rancak.app.data.remote.api.getOrderBoard
import id.rancak.app.data.remote.api.getQrPaymentStatus
import id.rancak.app.data.remote.api.getReceiptCombined
import id.rancak.app.data.remote.api.getReceiptEscpos
import id.rancak.app.data.remote.api.getReceiptKitchen
import id.rancak.app.data.remote.api.getReceiptQueue
import id.rancak.app.data.remote.api.getSaleDetail
import id.rancak.app.data.remote.api.getSaleReceipt
import id.rancak.app.data.remote.api.getSales
import id.rancak.app.data.remote.api.mergeSale
import id.rancak.app.data.remote.api.moveTable
import id.rancak.app.data.remote.api.openCashDrawer
import id.rancak.app.data.remote.api.payHeldOrder
import id.rancak.app.data.remote.api.refundSale
import id.rancak.app.data.remote.api.reprintSale
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.serveSale
import id.rancak.app.data.remote.api.splitBill
import id.rancak.app.data.remote.api.voidSale
import id.rancak.app.data.remote.dto.sale.CreateSaleRequest
import id.rancak.app.data.sync.SyncScheduler
import id.rancak.app.data.mapper.toSaleItemRequest
import id.rancak.app.data.util.isNetworkError
import id.rancak.app.data.util.safe
import id.rancak.app.data.util.safeUnit
import id.rancak.app.data.util.toNetworkMessage
import id.rancak.app.domain.model.CartItem
import id.rancak.app.domain.model.OrderBoardOrder
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.PaymentMethod
import id.rancak.app.domain.model.QrPayment
import id.rancak.app.domain.model.Receipt
import id.rancak.app.domain.model.Refund
import id.rancak.app.domain.model.RefundItemInput
import id.rancak.app.domain.model.ReprintResult
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Sale
import id.rancak.app.domain.model.SplitBillResult
import id.rancak.app.domain.model.SplitPaymentEntry
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
                items           = items.map { it.toSaleItemRequest() },
                paymentMethod   = paymentMethod.value,
                // QRIS: backend creates sale without requiring paid_amount;
                // payment is confirmed later via Xendit webhook.
                paidAmount      = if (paymentMethod == PaymentMethod.QRIS) null else paidAmount,
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
            if (isNetworkError(e)) {
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
                Resource.Error(e.toNetworkMessage())
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
                items    = items.map { it.toSaleItemRequest() },
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
            Resource.Error(e.toNetworkMessage())
        }
    }

    override suspend fun getSales(dateFrom: String?, dateTo: String?): Resource<List<Sale>> {
        val isFiltered = !dateFrom.isNullOrBlank() || !dateTo.isNullOrBlank()
        return try {
            val response = api.getSales(tenantUuid, dateFrom, dateTo)
            if (response.isSuccess && response.data != null) {
                val sales = response.data.map { it.toDomain() }
                // Only cache unfiltered main list
                if (!isFiltered) {
                    val now = Clock.System.now().toEpochMilliseconds()
                    saleDao.upsertSalesWithItems(
                        sales = sales.map { it.toEntity(now) },
                        items = sales.flatMap { sale ->
                            sale.items.map { it.toEntity(sale.uuid) }
                        }
                    )
                }
                Resource.Success(sales)
            } else {
                serveCachedSales(response.message)
            }
        } catch (e: Exception) {
            serveCachedSales(e.message)
        }
    }

    private suspend fun serveCachedSales(errorMessage: String?): Resource<List<Sale>> {
        val cached = saleDao.getAll()
        return if (cached.isNotEmpty()) {
            Resource.Success(cached.map { entity ->
                entity.toDomain(saleDao.getItemsForSale(entity.uuid))
            })
        } else {
            Resource.Error(errorMessage ?: "Tidak ada koneksi internet")
        }
    }

    override suspend fun getSalesFromCache(): Resource<List<Sale>> {
        val cached = saleDao.getAll()
        return Resource.Success(cached.map { entity ->
            entity.toDomain(saleDao.getItemsForSale(entity.uuid))
        })
    }

    override suspend fun getSaleDetail(saleUuid: String): Resource<Sale> {
        return try {
            val response = api.getSaleDetail(tenantUuid, saleUuid)
            if (response.isSuccess && response.data != null) {
                val sale = response.data.toDomain()
                val now = Clock.System.now().toEpochMilliseconds()
                saleDao.upsertSalesWithItems(
                    sales = listOf(sale.toEntity(now)),
                    items = sale.items.map { it.toEntity(sale.uuid) }
                )
                Resource.Success(sale)
            } else {
                serveCachedSaleDetail(saleUuid, response.message)
            }
        } catch (e: Exception) {
            serveCachedSaleDetail(saleUuid, e.message)
        }
    }

    private suspend fun serveCachedSaleDetail(saleUuid: String, errorMessage: String?): Resource<Sale> {
        val cached = saleDao.findByUuid(saleUuid)
        return if (cached != null) {
            Resource.Success(cached.toDomain(saleDao.getItemsForSale(saleUuid)))
        } else {
            Resource.Error(errorMessage ?: "Tidak ada koneksi internet")
        }
    }

    override suspend fun getSaleDetailFromCache(saleUuid: String): Resource<Sale> {
        val cached = saleDao.findByUuid(saleUuid)
        return if (cached != null) {
            Resource.Success(cached.toDomain(saleDao.getItemsForSale(saleUuid)))
        } else {
            Resource.Error("Penjualan tidak ditemukan di cache")
        }
    }

    override suspend fun serveSale(saleUuid: String): Resource<Sale> = safe(
        block    = { api.serveSale(tenantUuid, saleUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal menyajikan pesanan"
    )

    override suspend fun paySale(saleUuid: String, paymentMethod: PaymentMethod, paidAmount: Long): Resource<Sale> = safe(
        block = {
            api.payHeldOrder(
                tenantUuid, saleUuid,
                id.rancak.app.data.remote.dto.sale.PayHeldOrderRequest(paymentMethod.value, paidAmount)
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal membayar pesanan"
    )

    override suspend fun paySaleWithSplitPayment(
        saleUuid: String,
        payments: List<SplitPaymentEntry>
    ): Resource<Sale> = safe(
        block = {
            api.payHeldOrder(
                tenantUuid, saleUuid,
                id.rancak.app.data.remote.dto.sale.PayHeldOrderRequest(
                    payments = payments.map {
                        id.rancak.app.data.remote.dto.sale.SplitPaymentRequest(it.method.value, it.amount)
                    }
                )
            )
        },
        map = { it.toDomain() },
        errorMsg = "Gagal membayar pesanan dengan split payment"
    )

    override suspend fun splitBill(saleUuid: String, itemIds: List<String>): Resource<SplitBillResult> = safe(
        block = { api.splitBill(tenantUuid, saleUuid, SplitBillRequest(itemIds)) },
        map = {
            SplitBillResult(
                original = it.original.toDomain(),
                newSale  = it.newSale.toDomain()
            )
        },
        errorMsg = "Gagal memisahkan tagihan"
    )

    override suspend fun addItemsToHeldOrder(
        saleUuid: String,
        items: List<CartItem>
    ): Resource<Sale> = safe(
        block    = { api.addHeldOrderItems(
            tenantUuid, saleUuid,
            id.rancak.app.data.remote.dto.sale.AddHeldOrderItemsRequest(items.map { it.toSaleItemRequest() })
        ) },
        map      = { it.toDomain() },
        errorMsg = "Gagal menambah item ke pesanan"
    )

    override suspend fun removeHeldOrderItem(saleUuid: String, itemUuid: String): Resource<Sale> = safe(
        block    = { api.deleteHeldOrderItem(tenantUuid, saleUuid, itemUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal menghapus item dari pesanan"
    )

    override suspend fun voidSale(saleUuid: String, reason: String?): Resource<Sale> = safe(
        block    = { api.voidSale(tenantUuid, saleUuid, reason) },
        map      = { it.toDomain() },
        errorMsg = "Gagal membatalkan penjualan"
    )

    override suspend fun cancelSale(saleUuid: String, reason: String?): Resource<Sale> = safe(
        block    = { api.cancelSale(tenantUuid, saleUuid, reason) },
        map      = { it.toDomain() },
        errorMsg = "Gagal membatalkan pesanan"
    )

    override suspend fun refundSale(
        saleUuid: String,
        items: List<RefundItemInput>,
        reason: String?
    ): Resource<Refund> = safe(
        block = {
            val request = id.rancak.app.data.remote.dto.sale.RefundRequest(
                items = items.map {
                    id.rancak.app.data.remote.dto.sale.RefundItemRequest(
                        saleItemUuid = it.saleItemUuid,
                        qty          = it.qty
                    )
                },
                reason = reason
            )
            api.refundSale(tenantUuid, saleUuid, request)
        },
        map      = { it.toDomain() },
        errorMsg = "Gagal memproses refund"
    )

    override suspend fun moveTable(saleUuid: String, tableUuid: String): Resource<Sale> = safe(
        block    = { api.moveTable(tenantUuid, saleUuid, tableUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal memindahkan meja"
    )

    override suspend fun createQrPayment(saleUuid: String): Resource<QrPayment> {
        return try {
            val response = api.createQrPayment(tenantUuid, saleUuid)
            // 409 = QR already exists for this sale — idempotent, return existing QR
            if ((response.isSuccess || response.statusCode == 409) && response.data != null) {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(response.message ?: "Gagal membuat QR QRIS")
            }
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage())
        }
    }

    override suspend fun getQrPaymentStatus(saleUuid: String): Resource<QrPayment> = safe(
        block    = { api.getQrPaymentStatus(tenantUuid, saleUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal mengecek status QR"
    )

    override suspend fun getSaleReceipt(saleUuid: String): Resource<Receipt> = safe(
        block    = { api.getSaleReceipt(tenantUuid, saleUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal mengambil struk"
    )

    override suspend fun getReceiptEscpos(saleUuid: String): Resource<ByteArray> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val bytes = api.getReceiptEscpos(tenantUuid, saleUuid)
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage("Gagal mengambil data cetak struk"))
        }
    }

    override suspend fun getReceiptKitchen(saleUuid: String): Resource<ByteArray> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val bytes = api.getReceiptKitchen(tenantUuid, saleUuid)
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage("Gagal mengambil tiket dapur"))
        }
    }

    override suspend fun getReceiptCombined(saleUuid: String, kotFirst: Boolean): Resource<ByteArray> {
        val tenantUuid = tokenManager.tenantUuid ?: return Resource.Error("Tenant belum dipilih")
        return try {
            val bytes = api.getReceiptCombined(tenantUuid, saleUuid, kotFirst)
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage("Gagal mengambil data cetak gabungan"))
        }
    }

    override suspend fun batchSales(sales: List<CartItem>): Resource<Unit> {
        // Batch upload ditangani oleh SyncManager via POST /tenants/:id/sales/batch.
        // Method ini hanya placeholder interface — tidak dipanggil langsung dari UI.
        return Resource.Success(Unit)
    }

    override suspend fun getOrderBoard(date: String?, includeDone: Boolean): Resource<List<OrderBoardOrder>> = safe(
        block    = { api.getOrderBoard(tenantUuid, date, includeDone) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal mengambil order board"
    )

    override suspend fun mergeSale(targetUuid: String, sourceUuid: String): Resource<Sale> = safe(
        block    = { api.mergeSale(tenantUuid, targetUuid, sourceUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal menggabungkan order"
    )

    override suspend fun getReceiptQueue(saleUuid: String): Resource<ByteArray> {
        return try {
            val bytes = api.getReceiptQueue(tenantUuid, saleUuid)
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage("Gagal mengambil struk antrian"))
        }
    }

    override suspend fun reprintSale(
        saleUuid: String,
        printType: String,
        reason: String?
    ): Resource<ReprintResult> = safe(
        block = { api.reprintSale(tenantUuid, saleUuid, reason, printType) },
        map = {
            ReprintResult(
                printType = it.printType,
                sale = it.sale.toDomain()
            )
        },
        errorMsg = "Gagal cetak ulang struk"
    )

    override suspend fun openCashDrawer(): Resource<ByteArray> {
        return try {
            val bytes = api.openCashDrawer(tenantUuid)
            Resource.Success(bytes)
        } catch (e: Exception) {
            Resource.Error(e.toNetworkMessage("Gagal membuka laci kas"))
        }
    }
}

