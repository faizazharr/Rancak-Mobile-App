package id.rancak.app.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Group(
    val uuid: String,
    val name: String,
    val description: String?,
    val tenantCount: Int,
    val createdAt: String?
)

@Immutable
data class GroupOverview(
    val totalRevenue: Double,
    val totalTransactions: Int,
    val totalOutlets: Int,
    val avgOrderValue: Double,
    val growthPct: Double?,
    val periodStart: String?,
    val periodEnd: String?
)

@Immutable
data class BranchReport(
    val tenantUuid: String,
    val name: String,
    val revenue: Double,
    val transactions: Int,
    val aov: Double
)
