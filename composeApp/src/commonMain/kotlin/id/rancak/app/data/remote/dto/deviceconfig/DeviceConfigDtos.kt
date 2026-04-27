package id.rancak.app.data.remote.dto.deviceconfig

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrinterConfigDto(
    val uuid: String,
    @SerialName("device_id")        val deviceId: String,
    @SerialName("printer_name")     val printerName: String,
    @SerialName("printer_type")     val printerType: String,   // receipt|kitchen|label
    @SerialName("connection_type")  val connectionType: String, // network|bluetooth|usb
    val address: String,
    @SerialName("paper_width_mm")   val paperWidthMm: Int = 80,
    @SerialName("is_default")       val isDefault: Boolean = false,
    @SerialName("created_at")       val createdAt: String? = null
)

@Serializable
data class CreatePrinterConfigRequest(
    @SerialName("printer_name")     val printerName: String,
    @SerialName("printer_type")     val printerType: String,
    @SerialName("connection_type")  val connectionType: String,
    val address: String,
    @SerialName("paper_width_mm")   val paperWidthMm: Int = 80,
    @SerialName("is_default")       val isDefault: Boolean = false
)

@Serializable
data class UpdatePrinterConfigRequest(
    @SerialName("printer_name")     val printerName: String? = null,
    @SerialName("connection_type")  val connectionType: String? = null,
    val address: String? = null,
    @SerialName("paper_width_mm")   val paperWidthMm: Int? = null,
    @SerialName("is_default")       val isDefault: Boolean? = null
)

@Serializable
data class AppConfigDto(
    val key: String,
    val value: String,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class UpsertAppConfigRequest(val value: String)
