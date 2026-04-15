package id.rancak.app.presentation.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrinterDevice
import id.rancak.app.presentation.components.rememberRequestBluetoothPermission
import id.rancak.app.presentation.viewmodel.SettingsUiState
import id.rancak.app.presentation.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresh BT state when screen is shown
    LaunchedEffect(Unit) {
        viewModel.checkBluetoothState()
    }

    SettingsScreenContent(
        uiState = uiState,
        onBack = onBack,
        onPrinterType = viewModel::setPrinterType,
        onScanBluetooth = viewModel::scanBluetoothPrinters,
        onSelectPrinter = viewModel::selectBluetoothPrinter,
        onDisconnect = viewModel::disconnectPrinter,
        onNetworkIp = viewModel::setNetworkIp,
        onNetworkPort = viewModel::setNetworkPort,
        onSaveNetwork = viewModel::saveNetworkPrinter,
        onTestPrint = viewModel::testPrint,
        onStoreName = viewModel::setStoreName,
        onStoreAddress = viewModel::setStoreAddress,
        onStorePhone = viewModel::setStorePhone,
        onFooterText = viewModel::setFooterText,
        onAutoPrint = viewModel::setAutoPrint,
        onPaperWidth = viewModel::setPaperWidth,
        onClearMessage = viewModel::clearMessage
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    uiState: SettingsUiState,
    onBack: () -> Unit = {},
    onPrinterType: (String) -> Unit = {},
    onScanBluetooth: () -> Unit = {},
    onSelectPrinter: (PrinterDevice) -> Unit = {},
    onDisconnect: () -> Unit = {},
    onNetworkIp: (String) -> Unit = {},
    onNetworkPort: (String) -> Unit = {},
    onSaveNetwork: () -> Unit = {},
    onTestPrint: () -> Unit = {},
    onStoreName: (String) -> Unit = {},
    onStoreAddress: (String) -> Unit = {},
    onStorePhone: (String) -> Unit = {},
    onFooterText: (String) -> Unit = {},
    onAutoPrint: (Boolean) -> Unit = {},
    onPaperWidth: (Int) -> Unit = {},
    onClearMessage: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Request BT permission before scanning — on Android 12+ this triggers
    // the system permission dialog; on older Android / iOS it's a no-op.
    val requestBluetoothPermission = rememberRequestBluetoothPermission { granted ->
        if (granted) {
            onScanBluetooth()
        }
    }

    LaunchedEffect(uiState.printerMessage) {
        uiState.printerMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Printer Section ──────────────────────────────────────────
            item { SectionHeader(icon = Icons.Default.Print, title = "Printer") }

            // No printer configured — show setup guide
            if (!uiState.hasPrinter) {
                item {
                    NoPrinterCard(
                        printerType = uiState.printerType,
                        onScanBluetooth = requestBluetoothPermission
                    )
                }
            }

            item {
                PrinterTypeSelector(
                    selected = uiState.printerType,
                    onSelect = onPrinterType
                )
            }

            // Saved printer info
            if (uiState.hasPrinter) {
                item {
                    SavedPrinterCard(
                        name = uiState.savedPrinterName,
                        address = uiState.savedPrinterAddress,
                        type = uiState.printerType,
                        isPrinting = uiState.isPrinting,
                        isConnected = uiState.isConnected,
                        onTestPrint = onTestPrint,
                        onDisconnect = onDisconnect
                    )
                }
            }

            // Bluetooth OFF warning
            if (uiState.printerType == SettingsStore.TYPE_BLUETOOTH && !uiState.isBluetoothOn) {
                item {
                    BluetoothOffWarning()
                }
            }

            // Bluetooth section
            if (uiState.printerType == SettingsStore.TYPE_BLUETOOTH) {
                item {
                    BluetoothSection(
                        printers = uiState.discoveredPrinters,
                        isScanning = uiState.isScanning,
                        isConnecting = uiState.isConnecting,
                        savedAddress = uiState.savedPrinterAddress,
                        hasScannedOnce = uiState.hasScannedOnce,
                        isBluetoothOn = uiState.isBluetoothOn,
                        onScan = requestBluetoothPermission,
                        onSelect = onSelectPrinter
                    )
                }
            }

            // Network section
            if (uiState.printerType == SettingsStore.TYPE_NETWORK) {
                item {
                    NetworkSection(
                        ip = uiState.networkIp,
                        port = uiState.networkPort,
                        onIpChange = onNetworkIp,
                        onPortChange = onNetworkPort,
                        onSave = onSaveNetwork
                    )
                }
            }

            // ── Receipt Section ──────────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
            item { SectionHeader(icon = Icons.Default.Receipt, title = "Struk / Receipt") }
            item {
                ReceiptSection(
                    storeName = uiState.storeName,
                    storeAddress = uiState.storeAddress,
                    storePhone = uiState.storePhone,
                    footerText = uiState.footerText,
                    onStoreName = onStoreName,
                    onStoreAddress = onStoreAddress,
                    onStorePhone = onStorePhone,
                    onFooterText = onFooterText
                )
            }

            // ── General Section ──────────────────────────────────────────
            item { Spacer(Modifier.height(8.dp)) }
            item { SectionHeader(icon = Icons.Default.Settings, title = "Umum") }
            item {
                GeneralSection(
                    autoPrint = uiState.autoPrint,
                    paperWidth = uiState.paperWidth,
                    onAutoPrint = onAutoPrint,
                    onPaperWidth = onPaperWidth
                )
            }

            // Bottom spacing
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// No Printer Configured Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NoPrinterCard(
    printerType: String,
    onScanBluetooth: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.PrintDisabled,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Belum ada printer terhubung",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (printerType == SettingsStore.TYPE_BLUETOOTH)
                    "Pilih tipe koneksi lalu cari printer Bluetooth yang sudah di-pair, atau masukkan IP printer jaringan."
                else
                    "Masukkan alamat IP dan port printer jaringan Wi-Fi Anda di bawah.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Struk akan tetap tersimpan meskipun printer belum dipasang.\nAnda bisa mencetak ulang kapan saja setelah printer terhubung.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Printer Type Selector
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrinterTypeSelector(selected: String, onSelect: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selected == SettingsStore.TYPE_BLUETOOTH,
                onClick = { onSelect(SettingsStore.TYPE_BLUETOOTH) },
                label = { Text("Bluetooth") },
                leadingIcon = {
                    Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selected == SettingsStore.TYPE_NETWORK,
                onClick = { onSelect(SettingsStore.TYPE_NETWORK) },
                label = { Text("Jaringan (Wi-Fi)") },
                leadingIcon = {
                    Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Saved Printer Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SavedPrinterCard(
    name: String,
    address: String,
    type: String,
    isPrinting: Boolean,
    isConnected: Boolean,
    onTestPrint: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (type == SettingsStore.TYPE_BLUETOOTH) Icons.Default.Bluetooth else Icons.Default.Wifi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isConnected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        if (isConnected) "Terhubung" else "Tersimpan",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isConnected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onTestPrint,
                    enabled = !isPrinting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isPrinting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isPrinting) "Mencetak..." else "Test Print")
                }
                OutlinedButton(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.LinkOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Putuskan")
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bluetooth Off Warning
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BluetoothOffWarning() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.BluetoothDisabled,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "Bluetooth Tidak Aktif",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Aktifkan Bluetooth di Pengaturan perangkat untuk mencari dan menghubungkan printer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bluetooth Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BluetoothSection(
    printers: List<PrinterDevice>,
    isScanning: Boolean,
    isConnecting: Boolean,
    savedAddress: String,
    hasScannedOnce: Boolean,
    isBluetoothOn: Boolean,
    onScan: () -> Unit,
    onSelect: (PrinterDevice) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = onScan,
                enabled = !isScanning && !isConnecting && isBluetoothOn,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    when {
                        isScanning -> "Mencari..."
                        !isBluetoothOn -> "Bluetooth Mati"
                        else -> "Cari Printer Bluetooth"
                    }
                )
            }

            // Connecting indicator
            if (isConnecting) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Menghubungkan ke printer...",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Tips before first scan
            if (!hasScannedOnce && printers.isEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Tips sebelum mencari printer:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "1. Nyalakan printer Bluetooth\n2. Aktifkan Bluetooth di perangkat\n3. Pair/sambungkan printer di Pengaturan Bluetooth perangkat\n4. Tekan tombol di atas untuk mencari",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // No printers found after scanning
            if (hasScannedOnce && printers.isEmpty() && !isScanning) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.BluetoothDisabled,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Tidak ada printer ditemukan. Pastikan printer sudah di-pair di Pengaturan Bluetooth.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Discovered printers list
            AnimatedVisibility(visible = printers.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        "Printer yang ditemukan:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    printers.forEach { device ->
                        val isSaved = device.address == savedAddress
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = !isSaved && !isConnecting) { onSelect(device) },
                            color = if (isSaved)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSaved)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        device.name.ifBlank { "Unknown Device" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        device.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSaved) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Terpilih",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Network Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NetworkSection(
    ip: String,
    port: String,
    onIpChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = ip,
                onValueChange = onIpChange,
                label = { Text("Alamat IP") },
                placeholder = { Text("192.168.1.100") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = port,
                onValueChange = onPortChange,
                label = { Text("Port") },
                placeholder = { Text("9100") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text("Simpan Printer Jaringan")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Receipt Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReceiptSection(
    storeName: String,
    storeAddress: String,
    storePhone: String,
    footerText: String,
    onStoreName: (String) -> Unit,
    onStoreAddress: (String) -> Unit,
    onStorePhone: (String) -> Unit,
    onFooterText: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Header & footer struk akan tampil di semua receipt.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = storeName,
                onValueChange = onStoreName,
                label = { Text("Nama Toko") },
                placeholder = { Text("Rancak Coffee") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = storeAddress,
                onValueChange = onStoreAddress,
                label = { Text("Alamat Toko") },
                placeholder = { Text("Jl. Sudirman No. 1") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = storePhone,
                onValueChange = onStorePhone,
                label = { Text("No. Telepon") },
                placeholder = { Text("0812-3456-7890") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = footerText,
                onValueChange = onFooterText,
                label = { Text("Footer Struk") },
                placeholder = { Text("Terima kasih!") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// General Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GeneralSection(
    autoPrint: Boolean,
    paperWidth: Int,
    onAutoPrint: (Boolean) -> Unit,
    onPaperWidth: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Auto-print toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Auto Print Struk",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Cetak struk otomatis setelah pembayaran berhasil",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = autoPrint, onCheckedChange = onAutoPrint)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Paper width selector
            Text(
                "Lebar Kertas",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(58, 80).forEach { width ->
                    FilterChip(
                        selected = paperWidth == width,
                        onClick = { onPaperWidth(width) },
                        label = { Text("${width}mm") }
                    )
                }
            }
            Text(
                if (paperWidth == 58) "32 karakter per baris" else "48 karakter per baris",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
