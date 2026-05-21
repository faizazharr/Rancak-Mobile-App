package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.receipt.ReceiptSettingsDto
import id.rancak.app.data.remote.dto.receipt.UpdateReceiptSettingsDto
import id.rancak.app.domain.model.ReceiptSettingsConfig

fun ReceiptSettingsDto.toDomain() = ReceiptSettingsConfig(
    logoUrl           = logoUrl,
    email             = email,
    website           = website,
    npwp              = npwp,
    receiptHeader     = receiptHeader,
    receiptFooter     = receiptFooter,
    receiptFooter2    = receiptFooter2,
    logoPosition      = logoPosition ?: "center",
    logoSizePct       = logoSizePct ?: 80,
    receiptNameSize   = receiptNameSize ?: "large",
    separatorStyle    = separatorStyle ?: "dashed",
    separatorCount    = separatorCount ?: 1,
    footerPosition    = footerPosition ?: "center",
    receiptInstagram  = receiptInstagram,
    receiptFacebook   = receiptFacebook,
    receiptWifiSsid   = receiptWifiSsid,
    receiptWifiPassword = receiptWifiPassword
)

fun ReceiptSettingsConfig.toUpdateDto() = UpdateReceiptSettingsDto(
    logoUrl           = logoUrl,
    email             = email,
    website           = website,
    npwp              = npwp,
    receiptHeader     = receiptHeader,
    receiptFooter     = receiptFooter,
    receiptFooter2    = receiptFooter2,
    logoPosition      = logoPosition,
    logoSizePct       = logoSizePct,
    receiptNameSize   = receiptNameSize,
    separatorStyle    = separatorStyle,
    separatorCount    = separatorCount,
    footerPosition    = footerPosition,
    receiptInstagram  = receiptInstagram,
    receiptFacebook   = receiptFacebook,
    receiptWifiSsid   = receiptWifiSsid,
    receiptWifiPassword = receiptWifiPassword
)
