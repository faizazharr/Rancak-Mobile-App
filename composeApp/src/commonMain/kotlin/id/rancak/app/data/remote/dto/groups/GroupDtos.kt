package id.rancak.app.data.remote.dto.groups

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupDto(
    val uuid: String,
    val name: String,
    val description: String? = null,
    @SerialName("tenant_count") val tenantCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)

// ── Group reports (raw JSON — structure varies per endpoint, using JsonObject) ──
// Returned as generic Map from server; domain layer exposes typed wrappers below.

@Serializable
data class GroupOverviewDto(
    @SerialName("total_revenue") val totalRevenue: Double = 0.0,
    @SerialName("total_transactions") val totalTransactions: Int = 0,
    @SerialName("total_outlets") val totalOutlets: Int = 0,
    @SerialName("avg_order_value") val avgOrderValue: Double = 0.0,
    @SerialName("growth_pct") val growthPct: Double? = null,
    val period: PeriodDto? = null
)

@Serializable
data class PeriodDto(
    val start: String? = null,
    val end: String? = null
)

@Serializable
data class BranchReportDto(
    @SerialName("tenant_uuid") val tenantUuid: String,
    val name: String,
    val revenue: Double = 0.0,
    val transactions: Int = 0,
    val aov: Double = 0.0
)
