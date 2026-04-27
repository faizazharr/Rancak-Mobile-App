package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.groups.BranchReportDto
import id.rancak.app.data.remote.dto.groups.GroupDto
import id.rancak.app.data.remote.dto.groups.GroupOverviewDto
import id.rancak.app.domain.model.BranchReport
import id.rancak.app.domain.model.Group
import id.rancak.app.domain.model.GroupOverview

fun GroupDto.toDomain() = Group(
    uuid = uuid,
    name = name,
    description = description,
    tenantCount = tenantCount,
    createdAt = createdAt
)

fun GroupOverviewDto.toDomain() = GroupOverview(
    totalRevenue = totalRevenue,
    totalTransactions = totalTransactions,
    totalOutlets = totalOutlets,
    avgOrderValue = avgOrderValue,
    growthPct = growthPct,
    periodStart = period?.start,
    periodEnd = period?.end
)

fun BranchReportDto.toDomain() = BranchReport(
    tenantUuid = tenantUuid,
    name = name,
    revenue = revenue,
    transactions = transactions,
    aov = aov
)
