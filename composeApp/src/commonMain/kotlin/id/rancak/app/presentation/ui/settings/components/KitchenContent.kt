package id.rancak.app.presentation.ui.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterConnectionType
import id.rancak.app.data.printing.PrinterDevice
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.SettingsUiState

private val KitchenAccent = Color(0xFFB5340A)

/**
 * Panel pengaturan printer dapur (KOT). Mirip [PrinterContent] namun dengan
 * aksen warna oranye dan hanya muncul saat [PrintMode] bukan `RECEIPT_ONLY`.
 */
@Composable
internal fun KitchenContent(
    uiState: SettingsUiState,
    onKitchenPrinterType: (String) -> Unit,
    onScan: () -> Unit,
    onSelectKitchenPrinter: (PrinterDevice) -> Unit,
    onDisconnectKitchen: () -> Unit,
    onKitchenNetworkIp: (String) -> Unit,
    onKitchenNetworkPort: (String) -> Unit,
    onSaveKitchenNetwork: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onTestPrint: () -> Unit
) {
    if (uiState.hasKitchenPrinter) {
        SavedKitchenPrinterCard(
            uiState = uiState,
            onDisconnect = onDisconnectKitchen
        )
    }

    KitchenConnectionTypePicker(
        currentType          = uiState.kitchenPrinterType,
        onKitchenPrinterType = onKitchenPrinterType
    )

    if (uiState.kitchenPrinterType == SettingsStore.TYPE_BLUETOOTH) {
        KitchenBluetoothSection(
            uiState         = uiState,
            onScan          = onScan,
            onSelectPrinter = onSelectKitchenPrinter
        )
    }

    if (uiState.kitchenPrinterType == SettingsStore.TYPE_NETWORK) {
        KitchenNetworkSection(
            networkIp            = uiState.kitchenNetworkIp,
            networkPort          = uiState.kitchenNetworkPort,
            onKitchenNetworkIp   = onKitchenNetworkIp,
            onKitchenNetworkPort = onKitchenNetworkPort,
            onSaveKitchenNetwork = onSaveKitchenNetwork
        )
    }
}

@Composable
private fun SavedKitchenPrinterCard(
    uiState: SettingsUiState,
    onDisconnect: () -> Unit
) {
    SettingsCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                if (uiState.kitchenPrinterType == SettingsStore.TYPE_BLUETOOTH) Icons.Default.Bluetooth
                else Icons.Default.Wifi,
                contentDescription = null,
                tint     = KitchenAccent,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    uiState.kitchenPrinterName.ifBlank { uiState.kitchenPrinterAddress },
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    uiState.kitchenPrinterAddress,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = KitchenAccent.copy(alpha = 0.12f)
            ) {
                Text(
                    "Printer Dapur",
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = KitchenAccent
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick  = onDisconnect,
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Hapus Printer Dapur", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun KitchenConnectionTypePicker(
    currentType: String,
    onKitchenPrinterType: (String) -> Unit
) {
    SettingsCard {
        Text(
            "Tipe Koneksi Dapur",
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryFilterChip(
                selected    = currentType == SettingsStore.TYPE_BLUETOOTH,
                onClick     = { onKitchenPrinterType(SettingsStore.TYPE_BLUETOOTH) },
                label       = { Text("Bluetooth", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = { Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier    = Modifier.weight(1f)
            )
            PrimaryFilterChip(
                selected    = currentType == SettingsStore.TYPE_NETWORK,
                onClick     = { onKitchenPrinterType(SettingsStore.TYPE_NETWORK) },
                label       = { Text("Wi-Fi / LAN", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier    = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KitchenBluetoothSection(
    uiState: SettingsUiState,
    onScan: () -> Unit,
    onSelectPrinter: (PrinterDevice) -> Unit
) {
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
                if (uiState.isScanning) "Mencari…" else "Cari Printer Dapur",
                style = MaterialTheme.typography.labelMedium
            )
        }
        AnimatedVisibility(visible = uiState.discoveredPrinters.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                uiState.discoveredPrinters.forEach { device ->
                    PrinterDeviceRow(
                        device       = device,
                        isSaved      = device.address == uiState.kitchenPrinterAddress,
                        isConnecting = uiState.isConnecting,
                        onClick      = { onSelectPrinter(device) }
                    )
                }
            }
        }
    }
}

@Composable
private fun KitchenNetworkSection(
    networkIp: String,
    networkPort: String,
    onKitchenNetworkIp: (String) -> Unit,
    onKitchenNetworkPort: (String) -> Unit,
    onSaveKitchenNetwork: () -> Unit
) {
    SettingsCard {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = networkIp,
                onValueChange = onKitchenNetworkIp,
                label = { Text("IP Printer Dapur") },
                placeholder = { Text("192.168.1.101") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = networkPort,
                onValueChange = onKitchenNetworkPort,
                label = { Text("Port") },
                placeholder = { Text("9100") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(90.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = onSaveKitchenNetwork, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Simpan Printer Dapur", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview
@Composable
private fun KitchenContentPreview() {
    RancakTheme {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KitchenContent(
                uiState = SettingsUiState(
                    kitchenPrinterName    = "Kitchen XP-80",
                    kitchenPrinterAddress = "AA:BB:CC:DD:EE:10",
                    kitchenPrinterType    = SettingsStore.TYPE_BLUETOOTH,
                    isBluetoothOn         = true,
                    discoveredPrinters = listOf(
                        PrinterDevice("Kitchen XP-80", "AA:BB:CC:DD:EE:10", PrinterConnectionType.BLUETOOTH)
                    )
                ),
                onKitchenPrinterType   = {},
                onScan                 = {},
                onSelectKitchenPrinter = {},
                onDisconnectKitchen    = {},
                onKitchenNetworkIp     = {},
                onKitchenNetworkPort   = {},
                onSaveKitchenNetwork   = {},
                onTestPrint            = {}
            )
        }
    }
}
