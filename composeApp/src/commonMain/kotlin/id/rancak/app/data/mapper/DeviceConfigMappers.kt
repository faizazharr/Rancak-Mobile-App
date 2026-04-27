package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.deviceconfig.AppConfigDto
import id.rancak.app.data.remote.dto.deviceconfig.PrinterConfigDto
import id.rancak.app.domain.model.AppConfig
import id.rancak.app.domain.model.Printer

fun PrinterConfigDto.toDomain(): Printer = Printer(
    uuid = uuid,
    deviceId = deviceId,
    printerName = printerName,
    printerType = printerType,
    connectionType = connectionType,
    address = address,
    paperWidthMm = paperWidthMm,
    isDefault = isDefault,
    createdAt = createdAt
)

fun AppConfigDto.toDomain(): AppConfig = AppConfig(
    key = key,
    value = value,
    updatedAt = updatedAt
)
