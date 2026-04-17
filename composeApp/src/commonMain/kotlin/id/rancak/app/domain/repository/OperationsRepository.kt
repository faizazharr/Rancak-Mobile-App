package id.rancak.app.domain.repository

import id.rancak.app.domain.model.*

interface OperationsRepository {
    suspend fun getTables(): Resource<List<Table>>
    suspend fun getCurrentShift(): Resource<Shift?>
    suspend fun openShift(openingCash: String): Resource<Shift>
    suspend fun closeShift(closingCash: String, note: String?): Resource<Shift>
    suspend fun getKdsOrders(): Resource<List<KdsOrder>>
    suspend fun updateKdsStatus(kdsUuid: String, status: KdsStatus): Resource<Unit>
}
