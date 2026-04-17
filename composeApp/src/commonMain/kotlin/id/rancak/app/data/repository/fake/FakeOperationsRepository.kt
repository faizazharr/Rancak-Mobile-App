package id.rancak.app.data.repository.fake

import id.rancak.app.domain.model.*
import id.rancak.app.domain.repository.OperationsRepository

class FakeOperationsRepository : OperationsRepository {

    override suspend fun getTables(): Resource<List<Table>> =
        Resource.Success(demoTables.toList())

    override suspend fun getCurrentShift(): Resource<Shift?> =
        Resource.Success(demoShift)

    override suspend fun openShift(openingCash: String): Resource<Shift> {
        val shift = Shift(
            uuid             = "shift-${System.currentTimeMillis()}",
            openedAt         = "2025-06-14 08:00:00",
            closedAt         = null,
            status           = ShiftStatus.OPEN,
            openingCash      = openingCash,
            closingCash      = null,
            expectedCash     = null,
            cashDifference   = null,
            cashierName      = null,
            totalSales       = 0L,
            totalTransactions = 0,
            totalExpenses    = 0L,
            totalCashIn      = 0L
        )
        demoShift = shift
        return Resource.Success(shift)
    }

    override suspend fun closeShift(closingCash: String, note: String?): Resource<Shift> {
        val current = demoShift
            ?: return Resource.Error("Tidak ada shift aktif")
        val closed = current.copy(
            status      = ShiftStatus.CLOSED,
            closedAt    = "2025-06-14 22:00:00",
            closingCash = closingCash
        )
        demoShift = null
        return Resource.Success(closed)
    }

    override suspend fun getKdsOrders(): Resource<List<KdsOrder>> =
        Resource.Success(demoKdsOrders.toList())

    override suspend fun updateKdsStatus(kdsUuid: String, status: KdsStatus): Resource<Unit> {
        val idx = demoKdsOrders.indexOfFirst { it.uuid == kdsUuid }
        return if (idx >= 0) {
            demoKdsOrders[idx] = demoKdsOrders[idx].copy(status = status)
            Resource.Success(Unit)
        } else Resource.Error("Order tidak ditemukan")
    }

    override suspend fun getSurcharges(): Resource<List<Surcharge>> =
        Resource.Success(emptyList())

    override suspend fun getTaxConfigs(): Resource<List<TaxConfig>> =
        Resource.Success(emptyList())

    override suspend fun getDiscountRules(): Resource<List<DiscountRule>> =
        Resource.Success(emptyList())

    override suspend fun validateVoucher(code: String, subtotal: Long): Resource<VoucherValidation> =
        Resource.Error("Tidak tersedia dalam mode demo")

    override suspend fun previewDiscount(total: Long): Resource<DiscountPreview> =
        Resource.Success(DiscountPreview(emptyList(), 0, total))

    override suspend fun syncCatalog(updatedAfter: String?): Resource<Unit> =
        Resource.Success(Unit)

    override suspend fun syncStatus(): Resource<Boolean> =
        Resource.Success(true)
}
