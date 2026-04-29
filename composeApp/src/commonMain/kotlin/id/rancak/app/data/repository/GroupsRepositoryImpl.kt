package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.assignTenantToGroup
import id.rancak.app.data.remote.api.createGroup
import id.rancak.app.data.remote.api.deleteGroup
import id.rancak.app.data.remote.api.getGroup
import id.rancak.app.data.remote.api.getGroupBranches
import id.rancak.app.data.remote.api.getGroupCashiers
import id.rancak.app.data.remote.api.getGroupCategories
import id.rancak.app.data.remote.api.getGroupDiscounts
import id.rancak.app.data.remote.api.getGroupOverview
import id.rancak.app.data.remote.api.getGroupPaymentMethods
import id.rancak.app.data.remote.api.getGroupPeakHours
import id.rancak.app.data.remote.api.getGroupPnl
import id.rancak.app.data.remote.api.getGroupRevenueSeries
import id.rancak.app.data.remote.api.getGroupTenants
import id.rancak.app.data.remote.api.getGroupTopProducts
import id.rancak.app.data.remote.api.getGroupVoidRefund
import id.rancak.app.data.remote.api.getGroups
import id.rancak.app.data.remote.api.removeTenantFromGroup
import id.rancak.app.data.remote.api.updateGroup
import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.util.safe
import id.rancak.app.data.util.safeUnit
import id.rancak.app.domain.model.BranchReport
import id.rancak.app.domain.model.Group
import id.rancak.app.domain.model.GroupOverview
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Tenant
import id.rancak.app.domain.repository.GroupsRepository
import kotlinx.serialization.json.JsonElement

class GroupsRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : GroupsRepository {

    override suspend fun getGroups(): Resource<List<Group>> = safe(
        block = { api.getGroups() },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat grup"
    )

    override suspend fun getGroup(groupUuid: String): Resource<Group> = safe(
        block = { api.getGroup(groupUuid) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat detail grup"
    )

    override suspend fun createGroup(name: String, description: String?): Resource<Group> = safe(
        block = { api.createGroup(name, description) },
        map = { it.toDomain() },
        errorMsg = "Gagal membuat grup"
    )

    override suspend fun updateGroup(groupUuid: String, name: String?, description: String?): Resource<Group> = safe(
        block = { api.updateGroup(groupUuid, name, description) },
        map = { it.toDomain() },
        errorMsg = "Gagal mengupdate grup"
    )

    override suspend fun deleteGroup(groupUuid: String): Resource<Unit> = safeUnit(
        block = { api.deleteGroup(groupUuid) },
        errorMsg = "Gagal menghapus grup"
    )

    override suspend fun getGroupTenants(groupUuid: String): Resource<List<Tenant>> = safe(
        block = { api.getGroupTenants(groupUuid) },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat outlet grup"
    )

    override suspend fun assignTenantToGroup(groupUuid: String, tenantUuid: String): Resource<Unit> = safeUnit(
        block = { api.assignTenantToGroup(groupUuid, tenantUuid) },
        errorMsg = "Gagal menambahkan outlet ke grup"
    )

    override suspend fun removeTenantFromGroup(groupUuid: String, tenantUuid: String): Resource<Unit> = safeUnit(
        block = { api.removeTenantFromGroup(groupUuid, tenantUuid) },
        errorMsg = "Gagal melepas outlet dari grup"
    )

    override suspend fun getGroupOverview(groupUuid: String, start: String?, end: String?): Resource<GroupOverview> = safe(
        block = { api.getGroupOverview(groupUuid, start, end) },
        map = { it.toDomain() },
        errorMsg = "Gagal memuat ringkasan grup"
    )

    override suspend fun getGroupBranches(groupUuid: String, start: String?, end: String?): Resource<List<BranchReport>> = safe(
        block = { api.getGroupBranches(groupUuid, start, end) },
        map = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat laporan cabang"
    )

    override suspend fun getGroupRevenueSeries(groupUuid: String, start: String?, end: String?): Resource<String> =
        safeJson { api.getGroupRevenueSeries(groupUuid, start, end) }

    override suspend fun getGroupTopProducts(groupUuid: String): Resource<String> =
        safeJson { api.getGroupTopProducts(groupUuid) }

    override suspend fun getGroupPaymentMethods(groupUuid: String): Resource<String> =
        safeJson { api.getGroupPaymentMethods(groupUuid) }

    override suspend fun getGroupPeakHours(groupUuid: String): Resource<String> =
        safeJson { api.getGroupPeakHours(groupUuid) }

    override suspend fun getGroupPnl(groupUuid: String): Resource<String> =
        safeJson { api.getGroupPnl(groupUuid) }

    override suspend fun getGroupCategories(groupUuid: String): Resource<String> =
        safeJson { api.getGroupCategories(groupUuid) }

    override suspend fun getGroupCashiers(groupUuid: String): Resource<String> =
        safeJson { api.getGroupCashiers(groupUuid) }

    override suspend fun getGroupDiscounts(groupUuid: String): Resource<String> =
        safeJson { api.getGroupDiscounts(groupUuid) }

    override suspend fun getGroupVoidRefund(groupUuid: String): Resource<String> =
        safeJson { api.getGroupVoidRefund(groupUuid) }

    private suspend fun safeJson(block: suspend () -> ApiResponse<JsonElement>): Resource<String> = try {
        val response = block()
        if (response.isSuccess && response.data != null) Resource.Success(response.data.toString())
        else Resource.Error(response.message ?: "Gagal memuat data")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Kesalahan jaringan")
    }
}
