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
import id.rancak.app.data.remote.dto.product.CategoryDto
import id.rancak.app.data.remote.dto.product.ProductDto
import id.rancak.app.data.remote.dto.product.VariantGroupDto
import id.rancak.app.data.remote.dto.product.VariantDto
import id.rancak.app.data.remote.dto.product.ProductBatchDto
import id.rancak.app.data.remote.dto.sale.QrPaymentDto
import id.rancak.app.data.remote.dto.sale.SaleDto
import id.rancak.app.data.remote.dto.sale.SaleItemDto
import id.rancak.app.data.remote.dto.sale.SaleItemAddonDto
import id.rancak.app.data.remote.dto.sale.SalePaymentDto
import id.rancak.app.data.remote.dto.sale.DeliveryResponseDto
import id.rancak.app.data.remote.dto.sync.ShiftDto
import id.rancak.app.data.remote.dto.sync.TableDto
import id.rancak.app.domain.model.*

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
