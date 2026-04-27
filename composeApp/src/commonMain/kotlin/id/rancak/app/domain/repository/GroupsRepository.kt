package id.rancak.app.domain.repository

import id.rancak.app.domain.model.BranchReport
import id.rancak.app.domain.model.Group
import id.rancak.app.domain.model.GroupOverview
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.model.Tenant

interface GroupsRepository {
    // ── CRUD ───────────────────────────────────────────────────────────────────
    suspend fun getGroups(): Resource<List<Group>>
    suspend fun getGroup(groupUuid: String): Resource<Group>
    suspend fun createGroup(name: String, description: String? = null): Resource<Group>
    suspend fun updateGroup(groupUuid: String, name: String? = null, description: String? = null): Resource<Group>
    suspend fun deleteGroup(groupUuid: String): Resource<Unit>

    // ── Tenant assignment ──────────────────────────────────────────────────────
    suspend fun getGroupTenants(groupUuid: String): Resource<List<Tenant>>
    suspend fun assignTenantToGroup(groupUuid: String, tenantUuid: String): Resource<Unit>
    suspend fun removeTenantFromGroup(groupUuid: String, tenantUuid: String): Resource<Unit>

    // ── Reports ────────────────────────────────────────────────────────────────
    suspend fun getGroupOverview(groupUuid: String, start: String? = null, end: String? = null): Resource<GroupOverview>
    suspend fun getGroupBranches(groupUuid: String, start: String? = null, end: String? = null): Resource<List<BranchReport>>
    // Raw reports (flexible JSON — callers parse as needed)
    suspend fun getGroupRevenueSeries(groupUuid: String, start: String? = null, end: String? = null): Resource<String>
    suspend fun getGroupTopProducts(groupUuid: String): Resource<String>
    suspend fun getGroupPaymentMethods(groupUuid: String): Resource<String>
    suspend fun getGroupPeakHours(groupUuid: String): Resource<String>
    suspend fun getGroupPnl(groupUuid: String): Resource<String>
    suspend fun getGroupCategories(groupUuid: String): Resource<String>
    suspend fun getGroupCashiers(groupUuid: String): Resource<String>
    suspend fun getGroupDiscounts(groupUuid: String): Resource<String>
    suspend fun getGroupVoidRefund(groupUuid: String): Resource<String>
}
