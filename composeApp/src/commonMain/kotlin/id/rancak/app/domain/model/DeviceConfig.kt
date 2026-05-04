package id.rancak.app.domain.model

import androidx.compose.runtime.Immutable

/** Konfigurasi printer untuk perangkat tertentu (atau global tenant). */
@Immutable
data class Printer(
    val uuid: String,
    val deviceId: String?,
    val printerName: String,
    /** "receipt" | "kitchen" | "queue" | "label" */
    val printerType: String,
    /** "bluetooth" | "usb" | "network" */
    val connectionType: String,
    /** MAC address / IP / device path tergantung connectionType. */
    val address: String,
    val paperWidthMm: Int = 58,
    val isDefault: Boolean = false,
    val createdAt: String? = null
)

/** Key-value config tenant (mis. invoice prefix, tax rate cache, dll). */
@Immutable
data class AppConfig(
    val key: String,
    val value: String,
    val updatedAt: String? = null
)
