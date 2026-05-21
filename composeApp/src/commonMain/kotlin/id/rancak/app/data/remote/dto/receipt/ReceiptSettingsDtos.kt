package id.rancak.app.data.remote.dto.receipt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response DTO untuk `GET /tenants/{id}/receipt-settings`.
 *
 * Field nullable — Android skip / tidak render bagian yang null.
 * Field NOT NULL (logo_position, logo_size_pct, dll.) selalu memiliki nilai
 * default dari server.
 */
@Serializable
data class ReceiptSettingsDto(
    @SerialName("logo_url")            val logoUrl: String? = null,
    @SerialName("email")               val email: String? = null,
    @SerialName("website")             val website: String? = null,
    @SerialName("npwp")                val npwp: String? = null,
    @SerialName("receipt_header")      val receiptHeader: String? = null,
    @SerialName("receipt_footer")      val receiptFooter: String? = null,
    @SerialName("receipt_footer2")     val receiptFooter2: String? = null,
    @SerialName("logo_position")       val logoPosition: String? = "center",
    @SerialName("logo_size_pct")       val logoSizePct: Int? = 80,
    @SerialName("receipt_name_size")   val receiptNameSize: String? = "large",
    @SerialName("separator_style")     val separatorStyle: String? = "dashed",
    @SerialName("separator_count")     val separatorCount: Int? = 1,
    @SerialName("footer_position")     val footerPosition: String? = "center",
    @SerialName("receipt_instagram")   val receiptInstagram: String? = null,
    @SerialName("receipt_facebook")    val receiptFacebook: String? = null,
    @SerialName("receipt_wifi_ssid")   val receiptWifiSsid: String? = null,
    @SerialName("receipt_wifi_password") val receiptWifiPassword: String? = null
)

/**
 * Request body untuk `PATCH /tenants/{id}/receipt-settings`.
 *
 * PATCH semantics — hanya field yang dikirim yang di-update.
 * Kirim `null` / string kosong untuk menghapus nilai nullable.
 * Field NOT NULL (logo_position, dll.): string kosong / null diabaikan.
 */
@Serializable
data class UpdateReceiptSettingsDto(
    @SerialName("logo_url")            val logoUrl: String? = null,
    @SerialName("email")               val email: String? = null,
    @SerialName("website")             val website: String? = null,
    @SerialName("npwp")                val npwp: String? = null,
    @SerialName("receipt_header")      val receiptHeader: String? = null,
    @SerialName("receipt_footer")      val receiptFooter: String? = null,
    @SerialName("receipt_footer2")     val receiptFooter2: String? = null,
    @SerialName("logo_position")       val logoPosition: String? = null,
    @SerialName("logo_size_pct")       val logoSizePct: Int? = null,
    @SerialName("receipt_name_size")   val receiptNameSize: String? = null,
    @SerialName("separator_style")     val separatorStyle: String? = null,
    @SerialName("separator_count")     val separatorCount: Int? = null,
    @SerialName("footer_position")     val footerPosition: String? = null,
    @SerialName("receipt_instagram")   val receiptInstagram: String? = null,
    @SerialName("receipt_facebook")    val receiptFacebook: String? = null,
    @SerialName("receipt_wifi_ssid")   val receiptWifiSsid: String? = null,
    @SerialName("receipt_wifi_password") val receiptWifiPassword: String? = null
)
