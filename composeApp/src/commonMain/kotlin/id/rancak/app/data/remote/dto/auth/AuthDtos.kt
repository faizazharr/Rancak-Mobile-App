package id.rancak.app.data.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Request DTOs ──

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class GoogleLoginRequest(
    @SerialName("id_token") val idToken: String
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class LogoutRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class ChangePasswordRequest(
    @SerialName("current_password") val currentPassword: String,
    @SerialName("new_password") val newPassword: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    @SerialName("new_password") val newPassword: String
)

// ── Response DTOs ──

@Serializable
data class LoginResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("refresh_expires_at") val refreshExpiresAt: String,
    val user: UserDto
)

@Serializable
data class UserDto(
    val uuid: String,
    val name: String,
    val email: String,
    val tenants: List<TenantMembershipDto> = emptyList()
)

/** Minimal tenant ref returned inside UserDto/AuthResponse. */
@Serializable
data class TenantMembershipDto(
    val uuid: String,
    val name: String
)

/**
 * Full tenant info returned by GET /tenants — includes subscription status,
 * role, and contact info.
 */
@Serializable
data class MyTenantDto(
    val uuid: String,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    val role: String? = null,
    @SerialName("subscription_status") val subscriptionStatus: String? = null,
    @SerialName("subscription_expires_at") val subscriptionExpiresAt: String? = null
)

/** Tenant settings returned by GET /tenants/{id}/settings. */
@Serializable
data class TenantSettingsDto(
    val uuid: String,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("subscription_status") val subscriptionStatus: String? = null,
    @SerialName("subscription_plan") val subscriptionPlan: String? = null,
    @SerialName("subscription_started_at") val subscriptionStartedAt: String? = null,
    @SerialName("subscription_expires_at") val subscriptionExpiresAt: String? = null,
    @SerialName("max_users") val maxUsers: Int? = null,
    @SerialName("current_users") val currentUsers: Int? = null
)

/** Receipt settings returned by GET /tenants/{id}/receipt-settings. */
@Serializable
data class ReceiptSettingsDto(
    @SerialName("logo_url") val logoUrl: String? = null,
    val email: String? = null,
    val website: String? = null,
    val npwp: String? = null,
    @SerialName("receipt_header") val receiptHeader: String? = null,
    @SerialName("receipt_footer") val receiptFooter: String? = null,
    @SerialName("receipt_footer2") val receiptFooter2: String? = null,
    @SerialName("logo_position") val logoPosition: String? = "center",
    @SerialName("logo_size_pct") val logoSizePct: Int? = 80,
    @SerialName("receipt_name_size") val receiptNameSize: String? = "large",
    @SerialName("separator_style") val separatorStyle: String? = "dashed",
    @SerialName("separator_count") val separatorCount: Int? = 1,
    @SerialName("footer_position") val footerPosition: String? = "center",
    @SerialName("receipt_instagram") val receiptInstagram: String? = null,
    @SerialName("receipt_facebook") val receiptFacebook: String? = null,
    @SerialName("receipt_wifi_ssid") val receiptWifiSsid: String? = null,
    @SerialName("receipt_wifi_password") val receiptWifiPassword: String? = null
)

/** Session info returned by GET /auth/sessions. */
@Serializable
data class SessionDto(
    @SerialName("session_id") val sessionId: String,
    @SerialName("user_agent") val userAgent: String? = null,
    @SerialName("issued_at") val issuedAt: String? = null,
    @SerialName("last_used_at") val lastUsedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    val current: Boolean = false
)
