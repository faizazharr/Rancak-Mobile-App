package id.rancak.app.data.remote.dto.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body request `POST /applications` — submit pengajuan outlet.
 * Field `googleMapsUrl` opsional; sisanya wajib.
 */
@Serializable
data class SubmitApplicationRequest(
    @SerialName("outlet_name")     val outletName: String,
    @SerialName("phone")           val phone: String,
    @SerialName("address")         val address: String,
    @SerialName("nib")             val nib: String,
    @SerialName("business_type")   val businessType: String,
    @SerialName("google_maps_url") val googleMapsUrl: String? = null
)

/**
 * Response `POST /applications` dan item `GET /applications/me`.
 * Sejak revisi auto-approve, status saat submit selalu `approved` dan
 * `approvedTenantUuid` selalu populated.
 */
@Serializable
data class TenantApplicationDto(
    val uuid: String,
    @SerialName("outlet_name")          val outletName: String,
    val phone: String,
    val address: String,
    @SerialName("google_maps_url")      val googleMapsUrl: String? = null,
    val nib: String,
    @SerialName("business_type")        val businessType: String,
    val status: String,
    @SerialName("rejection_reason")     val rejectionReason: String? = null,
    @SerialName("reviewed_at")          val reviewedAt: String? = null,
    @SerialName("approved_tenant_uuid") val approvedTenantUuid: String? = null,
    @SerialName("created_at")           val createdAt: String,
    @SerialName("updated_at")           val updatedAt: String
)
