package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.billing.InvoiceDto
import id.rancak.app.data.remote.dto.billing.PlanDto
import id.rancak.app.data.remote.dto.billing.SubscriptionStateDto
import id.rancak.app.domain.model.Invoice
import id.rancak.app.domain.model.Plan
import id.rancak.app.domain.model.SubscriptionState

fun PlanDto.toDomain() = Plan(
    uuid = uuid,
    code = code,
    name = name,
    description = description,
    basePrice = basePrice,
    taxRate = taxRate,
    durationDays = durationDays,
    maxUsers = maxUsers,
    isTrial = isTrial,
    totalPrice = totalPrice
)

fun SubscriptionStateDto.toDomain() = SubscriptionState(
    status = status,
    plan = plan,
    startedAt = startedAt,
    expiresAt = expiresAt,
    maxUsers = maxUsers,
    hadTrial = hadTrial
)

fun InvoiceDto.toDomain() = Invoice(
    uuid = uuid,
    invoiceNo = invoiceNo,
    planCode = planCode,
    planName = planName,
    durationDays = durationDays,
    baseAmount = baseAmount,
    taxRate = taxRate,
    taxAmount = taxAmount,
    totalAmount = totalAmount,
    status = status,
    issuedAt = issuedAt,
    dueAt = dueAt,
    paidAt = paidAt,
    cancelledAt = cancelledAt,
    appliedAt = appliedAt,
    createdAt = createdAt,
    xenditQrId = xenditQrId,
    qrString = qrString,
    xenditRefId = xenditRefId,
    usingWebhook = usingWebhook
)
