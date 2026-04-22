package id.rancak.app.presentation.ui.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterConnectionType
import id.rancak.app.data.printing.PrinterDevice
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.SettingsUiState

/**
 * Panel pengaturan printer kasir: tampilkan printer tersimpan, pemilihan tipe
 * koneksi (Bluetooth / Wi-Fi), scanning, input jaringan, dan aksi test print.
 */
@Composable
internal fun PrinterContent(
    uiState: SettingsUiState,
    onPrinterType: (String) -> Unit,
    onScan: () -> Unit,
    onSelectPrinter: (PrinterDevice) -> Unit,
    onDisconnect: () -> Unit,
    onNetworkIp: (String) -> Unit,
    onNetworkPort: (String) -> Unit,
    onSaveNetwork: () -> Unit,
    onTestPrint: () -> Unit
) {
    if (uiState.hasPrinter) {
        SavedPrinterCard(
            uiState     = uiState,
            onTestPrint = onTestPrint,
            onDisconnect = onDisconnect
        )
    }

    ConnectionTypePicker(
        currentType   = uiState.printerType,
        onPrinterType = onPrinterType
    )

    if (uiState.printerType == SettingsStore.TYPE_BLUETOOTH) {
        BluetoothPrinterSection(
            uiState         = uiState,
            onScan          = onScan,
            onSelectPrinter = onSelectPrinter
        )
    }

    if (uiState.printerType == SettingsStore.TYPE_NETWORK) {
        NetworkPrinterSection(
            networkIp     = uiState.networkIp,
            networkPort   = uiState.networkPort,
            onNetworkIp   = onNetworkIp,
            onNetworkPort = onNetworkPort,
            onSaveNetwork = onSaveNetwork
        )
    }
}

// ── Sub-sections ────────────────────────────────────────────────────────────

@Composable
private fun SavedPrinterCard(
    uiState: SettingsUiState,
    onTestPrint: () -> Unit,
    onDisconnect: () -> Unit
) {
    SettingsCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                if (uiState.printerType == SettingsStore.TYPE_BLUETOOTH) Icons.Default.Bluetooth
                else Icons.Default.Wifi,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    uiState.savedPrinterName.ifBlank { uiState.savedPrinterAddress },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    uiState.savedPrinterAddress,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Text(
                    "Tersimpan",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick  = onTestPrint,
                enabled  = !uiState.isPrinting,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isPrinting) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    if (uiState.isPrinting) "Mencetak…" else "Test Print",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            OutlinedButton(
                onClick = onDisconnect,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Hapus", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ConnectionTypePicker(
    currentType: String,
    onPrinterType: (String) -> Unit
) {
    SettingsCard {
        Text(
            "Tipe Koneksi",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryFilterChip(
                selected    = currentType == SettingsStore.TYPE_BLUETOOTH,
                onClick     = { onPrinterType(SettingsStore.TYPE_BLUETOOTH) },
                label       = { Text("Bluetooth", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = { Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier    = Modifier.weight(1f)
            )
            PrimaryFilterChip(
                selected    = currentType == SettingsStore.TYPE_NETWORK,
                onClick     = { onPrinterType(SettingsStore.TYPE_NETWORK) },
                label       = { Text("Wi-Fi / LAN", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier    = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BluetoothPrinterSection(
    uiState: SettingsUiState,
    onScan: () -> Unit,
    onSelectPrinter: (PrinterDevice) -> Unit
) {
    if (!uiState.isBluetoothOn) BluetoothOffBanner()

    SettingsCard {
        Button(
            onClick = onScan,
            enabled = !uiState.isScanning && !uiState.isConnecting && uiState.isBluetoothOn,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(6.dp))
            }
            Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                if (uiState.isScanning) "Mencari…" else "Cari Printer Bluetooth",
                style = MaterialTheme.typography.labelMedium
            )
        }

        if (uiState.isConnecting) {
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                Text("Menghubungkan…", style = MaterialTheme.typography.bodySmall)
            }
        }

        if (!uiState.hasScannedOnce && uiState.discoveredPrinters.isEmpty()) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text(
                "Tips:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Nyalakan printer → aktifkan Bluetooth → pair printer di Pengaturan perangkat → tekan Cari",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(visible = uiState.discoveredPrinters.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                Text(
                    "Ditemukan ${uiState.discoveredPrinters.size} printer:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                uiState.discoveredPrinters.forEach { device ->
                    PrinterDeviceRow(
                        device       = device,
                        isSaved      = device.address == uiState.savedPrinterAddress,
                        isConnecting = uiState.isConnecting,
                        onClick      = { onSelectPrinter(device) }
                    )
                }
            }
        }

        if (uiState.hasScannedOnce && uiState.discoveredPrinters.isEmpty() && !uiState.isScanning) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Tidak ada printer ditemukan. Pastikan printer sudah di-pair.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun BluetoothOffBanner() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.BluetoothDisabled, contentDescription = null,
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)
            )
            Text(
                "Bluetooth tidak aktif — aktifkan di pengaturan perangkat",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun NetworkPrinterSection(
    networkIp: String,
    networkPort: String,
    onNetworkIp: (String) -> Unit,
    onNetworkPort: (String) -> Unit,
    onSaveNetwork: () -> Unit
) {
    SettingsCard {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = networkIp,
                onValueChange = onNetworkIp,
                label = { Text("Alamat IP") },
                placeholder = { Text("192.168.1.100") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = networkPort,
                onValueChange = onNetworkPort,
                label = { Text("Port") },
                placeholder = { Text("9100") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(90.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = onSaveNetwork, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Simpan Printer Jaringan", style = MaterialTheme.typography.labelMedium)
        }
    }
}

// ── Preview ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun PrinterContentPreview_Bluetooth() {
    RancakTheme {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrinterContent(
                uiState = SettingsUiState(
                    savedPrinterName    = "EPSON TM-T82",
                    savedPrinterAddress = "AA:BB:CC:DD:EE:01",
                    printerType         = SettingsStore.TYPE_BLUETOOTH,
                    isBluetoothOn       = true,
                    hasScannedOnce      = true,
                    discoveredPrinters  = listOf(
                        PrinterDevice("EPSON TM-T82",      "AA:BB:CC:DD:EE:01", PrinterConnectionType.BLUETOOTH),
                        PrinterDevice("Bluetooth Printer", "AA:BB:CC:DD:EE:02", PrinterConnectionType.BLUETOOTH)
                    )
                ),
                onPrinterType   = {},
                onScan          = {},
                onSelectPrinter = {},
                onDisconnect    = {},
                onNetworkIp     = {},
                onNetworkPort   = {},
                onSaveNetwork   = {},
                onTestPrint     = {}
            )
        }
    }
}

@Preview
@Composable
private fun PrinterContentPreview_Network() {
    RancakTheme {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrinterContent(
                uiState = SettingsUiState(
                    printerType = SettingsStore.TYPE_NETWORK,
                    networkIp   = "192.168.1.100",
                    networkPort = "9100"
                ),
                onPrinterType   = {},
                onScan          = {},
                onSelectPrinter = {},
                onDisconnect    = {},
                onNetworkIp     = {},
                onNetworkPort   = {},
                onSaveNetwork   = {},
                onTestPrint     = {}
            )
        }
    }
}
