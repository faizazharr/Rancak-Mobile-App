package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.operations.AppliedRuleDto
import id.rancak.app.data.remote.dto.operations.DiscountPreviewDto
import id.rancak.app.data.remote.dto.operations.VoucherValidationDto
import id.rancak.app.data.remote.dto.sync.DiscountRuleDto
import id.rancak.app.data.remote.dto.sync.SurchargeDto
import id.rancak.app.data.remote.dto.sync.TaxConfigDto
import id.rancak.app.domain.model.AppliedRule
import id.rancak.app.domain.model.DiscountPreview
import id.rancak.app.domain.model.DiscountRule
import id.rancak.app.domain.model.Surcharge
import id.rancak.app.domain.model.TaxConfig
import id.rancak.app.domain.model.Voucher
import id.rancak.app.domain.model.VoucherValidation

/**
 * DTO → domain mappers for Surcharges, Taxes, Discount rules, Vouchers.
 */

fun SurchargeDto.toDomain() = Surcharge(
    uuid = uuid,
    orderType = orderType,
    name = name,
    amount = amount.toLongOrNull() ?: 0L,
    isPercentage = isPercentage,
    maxAmount = maxAmount?.toLongOrNull(),
    isActive = isActive,
    sortOrder = sortOrder
)

fun TaxConfigDto.toDomain() = TaxConfig(
    uuid = uuid,
    name = name,
    rate = rate.toDoubleOrNull() ?: 0.0,
    applyTo = applyTo ?: "after_discount",
    sortOrder = sortOrder,
    isActive = isActive
)

fun DiscountRuleDto.toDomain() = DiscountRule(
    uuid = uuid,
    name = name,
    description = description,
    ruleType = ruleType ?: "always",
    discountType = discountType ?: "pct",
    discountValue = discountValue?.toDoubleOrNull() ?: 0.0,
    startTime = startTime,
    endTime = endTime,
    applicableDays = applicableDays,
    minPurchaseAmount = minPurchaseAmount?.toLongOrNull(),
    priority = priority,
    stackable = stackable,
    maxDiscount = maxDiscount?.toLongOrNull(),
    isActive = isActive
)

fun VoucherValidationDto.toDomain() = VoucherValidation(
    voucher = Voucher(
        uuid = voucher.uuid,
        code = voucher.code,
        name = voucher.name,
        description = voucher.description,
        discountType = voucher.discountType,
        discountValue = voucher.discountValue.toLongOrNull() ?: 0L,
        maxDiscount = voucher.maxDiscount?.toLongOrNull(),
        minPurchase = voucher.minPurchase.toLongOrNull() ?: 0L,
        usageLimit = voucher.usageLimit,
        usageCount = voucher.usageCount,
        validFrom = voucher.validFrom,
        validUntil = voucher.validUntil,
        isActive = voucher.isActive
    ),
    discountApplied = discountApplied.toLongOrNull() ?: 0L
)

fun DiscountPreviewDto.toDomain() = DiscountPreview(
    appliedRules = appliedRules.map { it.toDomain() },
    totalDiscount = totalDiscount,
    finalTotal = finalTotal
)

fun AppliedRuleDto.toDomain() = AppliedRule(
    uuid = uuid,
    name = name,
    ruleType = ruleType ?: "",
    discount = discount
)
