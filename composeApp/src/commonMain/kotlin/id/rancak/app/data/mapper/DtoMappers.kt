package id.rancak.app.data.mapper

import id.rancak.app.data.remote.api.KdsItemDto
import id.rancak.app.data.remote.api.KdsOrderDto
import id.rancak.app.data.remote.dto.auth.LoginResponse
import id.rancak.app.data.remote.dto.auth.TenantDto
import id.rancak.app.data.remote.dto.auth.UserDto
import id.rancak.app.data.remote.dto.product.CategoryDto
import id.rancak.app.data.remote.dto.product.ProductDto
import id.rancak.app.data.remote.dto.sale.SaleDto
import id.rancak.app.data.remote.dto.sale.SaleItemDto
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

fun TenantDto.toDomain() = Tenant(uuid = uuid, name = name)

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
    updatedAt = updatedAt
)

fun CategoryDto.toDomain() = Category(
    uuid = uuid,
    name = name,
    description = description
)

// ── Sale Mappers ──

fun SaleDto.toDomain() = Sale(
    uuid = uuid,
    invoiceNo = invoiceNo,
    orderType = OrderType.from(orderType),
    queueNumber = queueNumber,
    status = SaleStatus.from(status),
    subtotal = subtotal,
    discount = discount,
    surcharge = surcharge,
    tax = tax,
    total = total,
    paymentMethod = PaymentMethod.from(paymentMethod),
    paidAmount = paidAmount,
    changeAmount = changeAmount,
    items = items.map { it.toDomain() },
    createdAt = createdAt
)

fun SaleItemDto.toDomain() = SaleItem(
    uuid = uuid,
    productName = productName,
    qty = qty,
    price = price,
    subtotal = subtotal,
    variantName = variantName,
    note = note
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
    totalSales = totalSales,
    totalExpenses = totalExpenses
)

fun KdsOrderDto.toDomain() = KdsOrder(
    uuid = uuid,
    invoiceNo = invoiceNo,
    orderType = OrderType.from(orderType),
    tableName = tableName,
    queueNumber = queueNumber,
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
