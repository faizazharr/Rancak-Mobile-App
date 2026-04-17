package id.rancak.app.data.mapper

import id.rancak.app.data.remote.api.KdsItemDto
import id.rancak.app.data.remote.api.KdsOrderDto
import id.rancak.app.data.remote.dto.auth.LoginResponse
import id.rancak.app.data.remote.dto.auth.MyTenantDto
import id.rancak.app.data.remote.dto.auth.ReceiptSettingsDto
import id.rancak.app.data.remote.dto.auth.SessionDto
import id.rancak.app.data.remote.dto.auth.TenantMembershipDto
import id.rancak.app.data.remote.dto.auth.TenantSettingsDto
import id.rancak.app.data.remote.dto.auth.UserDto
import id.rancak.app.data.remote.dto.operations.CashInDto
import id.rancak.app.data.remote.dto.operations.ExpenseDto
import id.rancak.app.data.remote.dto.operations.ShiftSummaryDto
import id.rancak.app.data.remote.dto.operations.PaymentMethodReportDto
import id.rancak.app.data.remote.dto.operations.OrderBoardOrderDto
import id.rancak.app.data.remote.dto.operations.OrderBoardItemDto
import id.rancak.app.data.remote.dto.operations.VoucherValidationDto
import id.rancak.app.data.remote.dto.operations.DiscountPreviewDto
import id.rancak.app.data.remote.dto.operations.AppliedRuleDto
import id.rancak.app.data.remote.dto.operations.MySalesReportDto
import id.rancak.app.data.remote.dto.operations.StockReportDto
import id.rancak.app.data.remote.dto.operations.LowStockDto
import id.rancak.app.data.remote.dto.operations.StockAlertDto
import id.rancak.app.data.remote.dto.operations.ExpiringBatchDto
import id.rancak.app.data.remote.dto.operations.DailyCategoryReportDto
import id.rancak.app.data.remote.dto.operations.ReceiptDto
import id.rancak.app.data.remote.dto.operations.ReceiptItemDto
import id.rancak.app.data.remote.dto.operations.BundleDto
import id.rancak.app.data.remote.dto.operations.ModifierDto
import id.rancak.app.data.remote.dto.product.FavoriteProductDto
import id.rancak.app.data.remote.dto.product.Product86Dto
import id.rancak.app.data.remote.dto.sync.SurchargeDto
import id.rancak.app.data.remote.dto.sync.TaxConfigDto
import id.rancak.app.data.remote.dto.sync.DiscountRuleDto
import id.rancak.app.data.remote.dto.product.CategoryDto
import id.rancak.app.data.remote.dto.product.ProductDto
import id.rancak.app.data.remote.dto.product.VariantGroupDto
import id.rancak.app.data.remote.dto.product.VariantDto
import id.rancak.app.data.remote.dto.product.ProductBatchDto
import id.rancak.app.data.remote.dto.sale.QrPaymentDto
import id.rancak.app.data.remote.dto.sale.RefundResponseDto
import id.rancak.app.data.remote.dto.sale.RefundItemResponseDto
import id.rancak.app.data.remote.dto.sale.SaleDto
import id.rancak.app.data.remote.dto.sale.SaleItemDto
import id.rancak.app.data.remote.dto.sale.SaleItemAddonDto
import id.rancak.app.data.remote.dto.sale.SalePaymentDto
import id.rancak.app.data.remote.dto.sale.DeliveryResponseDto
import id.rancak.app.data.remote.dto.sync.ShiftDto
import id.rancak.app.data.remote.dto.sync.TableDto
import id.rancak.app.domain.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

// ── Auth Mappers ──

fun UserDto.toDomain() = User(
    uuid = uuid,
    name = name,
    email = email,
    tenants = tenants.map { it.toDomain() }
)

fun TenantMembershipDto.toDomain() = Tenant(uuid = uuid, name = name)

fun MyTenantDto.toDomain() = Tenant(
    uuid = uuid,
    name = name,
    address = address,
    phone = phone,
    role = role,
    subscriptionStatus = subscriptionStatus,
    subscriptionExpiresAt = subscriptionExpiresAt
)

fun TenantSettingsDto.toDomain() = TenantSettings(
    uuid = uuid,
    name = name,
    address = address,
    phone = phone,
    isActive = isActive,
    subscriptionStatus = subscriptionStatus,
    subscriptionPlan = subscriptionPlan,
    subscriptionExpiresAt = subscriptionExpiresAt,
    maxUsers = maxUsers,
    currentUsers = currentUsers
)

fun ReceiptSettingsDto.toDomain() = ReceiptSettings(
    logoUrl = logoUrl,
    email = email,
    website = website,
    npwp = npwp,
    receiptHeader = receiptHeader,
    receiptFooter = receiptFooter,
    receiptFooter2 = receiptFooter2,
    logoPosition = logoPosition,
    logoSizePct = logoSizePct,
    receiptNameSize = receiptNameSize,
    separatorStyle = separatorStyle,
    separatorCount = separatorCount,
    footerPosition = footerPosition,
    receiptInstagram = receiptInstagram,
    receiptFacebook = receiptFacebook,
    receiptWifiSsid = receiptWifiSsid,
    receiptWifiPassword = receiptWifiPassword
)

fun SessionDto.toDomain() = Session(
    sessionId = sessionId,
    userAgent = userAgent,
    issuedAt = issuedAt,
    lastUsedAt = lastUsedAt,
    expiresAt = expiresAt,
    current = current
)

fun LoginResponse.toLoginResult() = LoginResult(
    tokens = AuthTokens(
        accessToken = accessToken,
        refreshToken = refreshToken,
        expiresIn = expiresIn
    ),
    user = user.toDomain()
)

// ── Product Mappers ──

fun ProductDto.toDomain() = Product(
    uuid = uuid,
    sku = sku,
    barcode = barcode,
    name = name,
    description = description,
    category = category?.let { Category(it.uuid, it.name, null) },
    price = price,
    stock = stock,
    unit = unit,
    imageUrl = imageUrl,
    isActive = isActive,
    hasExpiry = hasExpiry,
    updatedAt = updatedAt
)

fun CategoryDto.toDomain() = Category(
    uuid = uuid,
    name = name,
    description = description
)

fun VariantGroupDto.toDomain() = VariantGroup(
    uuid = uuid,
    name = name,
    isRequired = isRequired,
    sortOrder = sortOrder,
    variants = variants.map { it.toDomain() }
)

fun VariantDto.toDomain() = Variant(
    uuid = uuid,
    name = name,
    priceAdjustment = priceAdjustment.toLongOrNull() ?: 0L,
    isDefault = isDefault,
    isActive = isActive
)

fun ProductBatchDto.toDomain() = ProductBatch(
    uuid = uuid,
    quantityInitial = quantityInitial,
    quantityRemaining = quantityRemaining,
    quantityUsed = quantityUsed,
    costPrice = costPrice,
    expiryDate = expiryDate,
    batchNumber = batchNumber,
    note = note,
    receivedAt = receivedAt,
    isExhausted = isExhausted,
    isExpired = isExpired
)

// ── Sale Mappers ──

fun SaleDto.toDomain() = Sale(
    uuid = uuid,
    invoiceNo = invoiceNo,
    orderType = OrderType.from(orderType),
    queueNumber = queueNumber,
    status = SaleStatus.from(status),
    customerName = customerName,
    subtotal = subtotal,
    discount = discount,
    surcharge = surcharge,
    voucherDiscount = voucherDiscount,
    voucherCode = voucherCode,
    autoDiscount = autoDiscount,
    autoDiscountLabel = autoDiscountLabel,
    tax = tax,
    deliveryFee = deliveryFee,
    tip = tip,
    adminFee = adminFee,
    total = total,
    paymentMethod = PaymentMethod.from(paymentMethod),
    paidAmount = paidAmount,
    changeAmount = changeAmount,
    items = items.map { it.toDomain() },
    payments = payments.map { it.toDomain() },
    delivery = delivery?.toDomain(),
    createdAt = createdAt,
    servedAt = servedAt
)

fun SaleItemDto.toDomain() = SaleItem(
    uuid = uuid,
    productUuid = productUuid,
    productName = productName,
    qty = qty,
    price = price,
    discount = discount,
    subtotal = subtotal,
    variantName = variantName,
    note = note,
    addons = addons.map { it.toDomain() }
)

fun SaleItemAddonDto.toDomain() = SaleItemAddon(
    name = name,
    price = price,
    qty = qty,
    subtotal = subtotal
)

fun SalePaymentDto.toDomain() = SalePayment(
    uuid = uuid,
    method = method,
    amount = amount,
    note = note
)

fun DeliveryResponseDto.toDomain() = Delivery(
    uuid = uuid,
    courierName = courierName,
    recipientName = recipientName,
    address = address,
    lat = lat,
    lng = lng,
    note = note
)

fun QrPaymentDto.toDomain() = QrPayment(
    uuid         = uuid,
    saleUuid     = saleUuid,
    qrString     = qrString,
    amount       = amount,
    status       = QrPaymentStatus.from(status),
    expiresAt    = expiresAt,
    usingWebhook = usingWebhook
)

fun RefundResponseDto.toDomain() = Refund(
    uuid         = uuid,
    saleUuid     = saleUuid,
    refundAmount = refundAmount,
    reason       = reason,
    items        = items.map { it.toDomain() },
    createdAt    = createdAt
)

fun RefundItemResponseDto.toDomain() = RefundItem(
    saleItemUuid = saleItemUuid,
    productName  = productName,
    qty          = qty,
    refundAmount = refundAmount
)

// ── Operations Mappers ──

fun TableDto.toDomain() = Table(
    uuid = uuid,
    name = name,
    area = area,
    capacity = capacity,
    status = TableStatus.from(status),
    isActive = isActive,
    sortOrder = sortOrder,
    activeSaleUuid = activeSaleUuid
)

fun ShiftDto.toDomain() = Shift(
    uuid = uuid,
    openedAt = openedAt,
    closedAt = closedAt,
    status = ShiftStatus.from(status),
    openingCash = openingCash,
    closingCash = closingCash,
    expectedCash = expectedCash,
    cashDifference = cashDifference,
    cashierName = cashierName,
    totalSales = totalSales,
    totalTransactions = totalTransactions,
    totalExpenses = totalExpenses,
    totalCashIn = totalCashIn
)

fun KdsOrderDto.toDomain() = KdsOrder(
    uuid = uuid,
    invoiceNo = invoiceNo,
    orderType = OrderType.from(orderType),
    tableName = tableName,
    queueNumber = queueNumber,
    customerName = customerName,
    note = note,
    status = KdsStatus.from(status),
    items = items.map { it.toDomain() },
    createdAt = createdAt
)

fun KdsItemDto.toDomain() = KdsItem(
    uuid = uuid,
    productName = productName,
    qty = qty,
    variantName = variantName,
    note = note,
    status = KdsItemStatus.from(status)
)

// ── Finance Mappers ──

fun CashInDto.toDomain() = CashIn(
    uuid = uuid,
    amount = amount,
    source = source,
    description = description,
    note = note,
    cashierUuid = cashierUuid,
    cashierName = cashierName,
    shiftUuid = shiftUuid,
    cashInDate = cashInDate,
    createdAt = createdAt
)

fun ExpenseDto.toDomain() = Expense(
    uuid = uuid,
    amount = amount,
    description = description,
    note = note,
    categoryUuid = categoryUuid,
    categoryName = categoryName,
    cashierUuid = cashierUuid,
    cashierName = cashierName,
    expenseDate = expenseDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ShiftSummaryDto.toDomain() = ShiftSummary(
    uuid = uuid,
    openedAt = openedAt,
    closedAt = closedAt,
    status = status,
    openingCash = openingCash,
    closingCash = closingCash,
    expectedCash = expectedCash,
    cashDifference = cashDifference,
    cashierName = cashierName,
    totalSales = totalSales,
    totalTransactions = totalTransactions,
    totalExpenses = totalExpenses,
    totalCashIn = totalCashIn,
    paymentSummary = paymentSummary.map { it.toDomain() }
)

fun PaymentMethodReportDto.toDomain() = PaymentMethodReport(
    method = method,
    total = total,
    count = count
)

// ── Pricing Mappers ──

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

// ── Product Extras ──

fun FavoriteProductDto.toDomain() = FavoriteProduct(
    uuid = uuid,
    name = name,
    sku = sku,
    price = price.toLongOrNull() ?: 0L,
    categoryName = categoryName,
    imageUrl = imageUrl,
    stock = stock.toDoubleOrNull() ?: 0.0,
    soldCount = soldCount,
    isLowStock = isLowStock
)

fun Product86Dto.toDomain() = Product86(
    uuid = uuid,
    productUuid = productUuid,
    productName = productName,
    sku = sku,
    reason = reason,
    date = date ?: "",
    createdAt = createdAt
)

// ── Order Board Mapper ──

fun OrderBoardOrderDto.toDomain() = OrderBoardOrder(
    uuid = uuid,
    invoiceNo = invoiceNo,
    queueNumber = queueNumber,
    orderType = OrderType.from(orderType),
    customerName = customerName,
    status = SaleStatus.from(status),
    createdAt = createdAt,
    servedAt = servedAt,
    items = items.map { it.toDomain() }
)

fun OrderBoardItemDto.toDomain() = OrderBoardItem(
    productName = productName,
    qty = qty,
    note = note
)

// ── Report Mappers ──

fun MySalesReportDto.toDomain() = MySalesReport(
    totalSales = totalSales.toLongOrNull() ?: 0L,
    totalTransactions = totalTransactions,
    cashTotal = cashTotal.toLongOrNull() ?: 0L
)

fun StockReportDto.toDomain() = StockReport(
    productUuid = productUuid,
    sku = sku,
    name = name,
    stock = stock,
    stockAlertThreshold = stockAlertThreshold
)

fun LowStockDto.toDomain() = LowStock(
    productUuid = productUuid,
    productName = productName,
    sku = sku,
    currentStock = currentStock,
    threshold = threshold
)

fun StockAlertDto.toDomain() = StockAlert(
    productUuid = productUuid,
    productName = productName,
    sku = sku,
    alertType = alertType,
    currentStock = stock,
    threshold = threshold
)

fun ExpiringBatchDto.toDomain(): ExpiringBatch {
    val days = expiryDate?.let {
        try {
            val expiry = LocalDate.parse(it)
            val today = Clock.System.now()
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            (expiry.toEpochDays() - today.toEpochDays()).toInt()
        } catch (_: Exception) { 0 }
    } ?: 0
    return ExpiringBatch(
        batchUuid = batchUuid,
        productUuid = productUuid,
        productName = productName,
        batchNumber = batchNumber,
        expiryDate = expiryDate ?: "",
        quantityRemaining = quantityRemaining,
        daysUntilExpiry = days
    )
}

fun DailyCategoryReportDto.toDomain() = DailyCategoryReport(
    categoryName = categoryName,
    totalSales = totalRevenue,
    totalQty = totalQty
)

// ── Receipt Mapper ──

fun ReceiptDto.toDomain() = Receipt(
    invoiceNo = invoiceNo,
    tenantName = tenantName,
    tenantAddress = tenantAddress,
    tenantPhone = tenantPhone,
    customerName = customerName,
    queueNumber = queueNumber,
    orderType = orderType,
    cashierName = cashierName,
    createdAt = createdAt,
    items = items.map { it.toDomain() },
    subtotal = subtotal,
    discount = discount,
    surcharge = surcharge,
    tax = tax,
    deliveryFee = deliveryFee,
    tip = tip,
    adminFee = adminFee,
    total = total,
    paidAmount = paidAmount,
    changeAmount = changeAmount,
    paymentMethod = paymentMethod
)

fun ReceiptItemDto.toDomain() = ReceiptItemDomain(
    productName = name,
    variantName = variantName,
    qty = qty.toIntOrNull() ?: 1,
    price = price,
    subtotal = subtotal,
    note = note
)

// ── Bundle / Modifier Mappers ──

fun BundleDto.toDomain() = Bundle(
    uuid = uuid,
    name = name,
    price = price,
    isActive = isActive,
    items = items.map { BundleItem(it.productUuid, it.productName, it.qty) }
)

fun ModifierDto.toDomain() = Modifier(
    uuid = uuid,
    name = name,
    sortOrder = sortOrder,
    productUuid = productUuid
)
