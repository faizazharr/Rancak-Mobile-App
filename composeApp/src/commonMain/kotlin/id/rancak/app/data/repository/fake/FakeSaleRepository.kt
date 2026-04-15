package id.rancak.app.data.repository.fake

import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.CartItem
import id.rancak.app.domain.repository.SaleRepository

class FakeSaleRepository : SaleRepository {

    private var invoiceCounter = 13  // lanjut dari data demo

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
        val subtotal  = items.sumOf { it.subtotal }
        val effectiveTax = if (tax > 0) tax else 0L
        val total     = subtotal - discount + effectiveTax + adminFee + deliveryFee + tip
        val change    = if (paymentMethod == PaymentMethod.CASH) (paidAmount - total).coerceAtLeast(0L) else 0L
        val invoiceNo = "INV-2025-%04d".format(invoiceCounter++)

        val sale = Sale(
            uuid           = "sale-${invoiceCounter.toString().padStart(3, '0')}",
            invoiceNo      = invoiceNo,
            orderType      = orderType,
            queueNumber    = invoiceCounter,
            status         = if (hold) SaleStatus.HELD else SaleStatus.PAID,
            subtotal       = subtotal,
            discount       = discount,
            surcharge      = 0L,
            tax            = effectiveTax,
            total          = total,
            paymentMethod  = if (hold) null else paymentMethod,
            paidAmount     = if (hold) 0L else paidAmount,
            changeAmount   = if (hold) 0L else change,
            items          = items.map { cart ->
                SaleItem(
                    uuid        = "si-${cart.productUuid}",
                    productName = cart.productName,
                    qty         = cart.qty.toString(),
                    price       = cart.price,
                    subtotal    = cart.subtotal,
                    variantName = cart.variantName,
                    note        = cart.note
                )
            },
            createdAt = "2025-06-14 ${(15..20).random()}:${(0..59).random().toString().padStart(2,'0')}:00"
        )
        demoSales.add(0, sale)  // tambah ke depan agar tampil di atas riwayat
        return Resource.Success(sale)
    }

    override suspend fun getSales(dateFrom: String?, dateTo: String?): Resource<List<Sale>> =
        Resource.Success(demoSales.toList())

    override suspend fun getSaleDetail(saleUuid: String): Resource<Sale> {
        val sale = demoSales.firstOrNull { it.uuid == saleUuid }
        return if (sale != null)
            Resource.Success(sale)
        else
            Resource.Error("Transaksi tidak ditemukan")
    }

    override suspend fun paySale(
        saleUuid: String,
        paymentMethod: PaymentMethod,
        paidAmount: Long
    ): Resource<Sale> {
        val idx  = demoSales.indexOfFirst { it.uuid == saleUuid }
        return if (idx >= 0) {
            val sale    = demoSales[idx]
            val change  = (paidAmount - sale.total).coerceAtLeast(0L)
            val updated = sale.copy(
                status        = SaleStatus.PAID,
                paymentMethod = paymentMethod,
                paidAmount    = paidAmount,
                changeAmount  = change
            )
            demoSales[idx] = updated
            Resource.Success(updated)
        } else {
            Resource.Error("Transaksi tidak ditemukan")
        }
    }

    override suspend fun serveSale(saleUuid: String): Resource<Sale> {
        val idx = demoSales.indexOfFirst { it.uuid == saleUuid }
        return if (idx >= 0) {
            val updated = demoSales[idx].copy(status = SaleStatus.SERVED)
            demoSales[idx] = updated
            Resource.Success(updated)
        } else Resource.Error("Transaksi tidak ditemukan")
    }

    override suspend fun voidSale(saleUuid: String, reason: String?): Resource<Sale> {
        val idx = demoSales.indexOfFirst { it.uuid == saleUuid }
        return if (idx >= 0) {
            val updated = demoSales[idx].copy(status = SaleStatus.VOID)
            demoSales[idx] = updated
            Resource.Success(updated)
        } else Resource.Error("Transaksi tidak ditemukan")
    }

    override suspend fun cancelSale(saleUuid: String, reason: String?): Resource<Sale> {
        val idx = demoSales.indexOfFirst { it.uuid == saleUuid }
        return if (idx >= 0) {
            val updated = demoSales[idx].copy(status = SaleStatus.CANCELLED)
            demoSales[idx] = updated
            Resource.Success(updated)
        } else Resource.Error("Transaksi tidak ditemukan")
    }

    override suspend fun refundSale(saleUuid: String, amount: Long?, reason: String?): Resource<Sale> =
        getSaleDetail(saleUuid)

    override suspend fun moveTable(saleUuid: String, tableUuid: String): Resource<Sale> =
        getSaleDetail(saleUuid)

    // ── QRIS / Xendit (fake — simulasi langsung succeeded) ────────────────────

    private val fakeQrString = "00020101021226610014ID.CO.QRIS.WWW0215ID20230001234560303UMI51440014ID.CO.QRIS.WWW0215ID2023000123456"

    override suspend fun createQrPayment(saleUuid: String): Resource<QrPayment> {
        val sale = demoSales.firstOrNull { it.uuid == saleUuid }
        return Resource.Success(
            QrPayment(
                uuid         = "qr-fake-$saleUuid",
                saleUuid     = saleUuid,
                qrString     = fakeQrString,
                amount       = sale?.total ?: 0L,
                status       = QrPaymentStatus.PENDING,
                usingWebhook = false
            )
        )
    }

    override suspend fun getQrPaymentStatus(saleUuid: String): Resource<QrPayment> {
        // Fake: selalu kembalikan PENDING (di dev bisa diubah ke SUCCEEDED untuk testing)
        val sale = demoSales.firstOrNull { it.uuid == saleUuid }
        return Resource.Success(
            QrPayment(
                uuid         = "qr-fake-$saleUuid",
                saleUuid     = saleUuid,
                qrString     = fakeQrString,
                amount       = sale?.total ?: 0L,
                status       = QrPaymentStatus.PENDING,
                usingWebhook = false
            )
        )
    }
}
