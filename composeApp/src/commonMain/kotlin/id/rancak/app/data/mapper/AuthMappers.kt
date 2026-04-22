package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.auth.LoginResponse
import id.rancak.app.data.remote.dto.auth.MyTenantDto
import id.rancak.app.data.remote.dto.auth.ReceiptSettingsDto
import id.rancak.app.data.remote.dto.auth.SessionDto
import id.rancak.app.data.remote.dto.auth.TenantMembershipDto
import id.rancak.app.data.remote.dto.auth.TenantSettingsDto
import id.rancak.app.data.remote.dto.auth.UserDto
import id.rancak.app.domain.model.AuthTokens
import id.rancak.app.domain.model.LoginResult
import id.rancak.app.domain.model.ReceiptSettings
import id.rancak.app.domain.model.Session
import id.rancak.app.domain.model.Tenant
import id.rancak.app.domain.model.TenantSettings
import id.rancak.app.domain.model.User

/**
 * DTO → domain mappers for Auth, User, Tenant, Session, and Receipt-settings.
 */

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
