package id.rancak.app.presentation.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.data.local.SettingsStore
import id.rancak.app.data.printing.PrintMode
import id.rancak.app.data.printing.PrinterDevice
import id.rancak.app.presentation.components.rememberRequestBluetoothPermission
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.viewmodel.SettingsUiState
import id.rancak.app.presentation.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Kategori navigasi pengaturan
// ─────────────────────────────────────────────────────────────────────────────

private enum class SettingsNav {
    PRINTER, PRINT_MODE, KITCHEN, STORE, GENERAL
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.checkBluetoothState() }

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
        onPrintMode = viewModel::setPrintMode,
        onKitchenPrinterType = viewModel::setKitchenPrinterType,
        onSelectKitchenPrinter = viewModel::selectKitchenBluetoothPrinter,
        onDisconnectKitchen = viewModel::disconnectKitchenPrinter,
        onKitchenNetworkIp = viewModel::setKitchenNetworkIp,
        onKitchenNetworkPort = viewModel::setKitchenNetworkPort,
        onSaveKitchenNetwork = viewModel::saveKitchenNetworkPrinter,
        onStoreName = viewModel::setStoreName,
        onStoreAddress = viewModel::setStoreAddress,
        onStorePhone = viewModel::setStorePhone,
        onFooterText = viewModel::setFooterText,
        onAutoPrint = viewModel::setAutoPrint,
        onPaperWidth = viewModel::setPaperWidth,
        onClearMessage = viewModel::clearMessage
    )
}

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
    onPrintMode: (PrintMode) -> Unit = {},
    onKitchenPrinterType: (String) -> Unit = {},
    onSelectKitchenPrinter: (PrinterDevice) -> Unit = {},
    onDisconnectKitchen: () -> Unit = {},
    onKitchenNetworkIp: (String) -> Unit = {},
    onKitchenNetworkPort: (String) -> Unit = {},
    onSaveKitchenNetwork: () -> Unit = {},
    onStoreName: (String) -> Unit = {},
    onStoreAddress: (String) -> Unit = {},
    onStorePhone: (String) -> Unit = {},
    onFooterText: (String) -> Unit = {},
    onAutoPrint: (Boolean) -> Unit = {},
    onPaperWidth: (Int) -> Unit = {},
    onClearMessage: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val requestBluetoothPermission = rememberRequestBluetoothPermission { granted ->
        if (granted) onScanBluetooth()
    }
    LaunchedEffect(uiState.printerMessage) {
        uiState.printerMessage?.let { snackbarHostState.showSnackbar(it); onClearMessage() }
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Pengaturan",
                icon     = Icons.Default.Settings,
                subtitle = "Konfigurasi aplikasi",
                onBack   = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val isTablet = maxWidth >= 600.dp

            if (isTablet) {
                // ── Tablet: dua panel (nav kiri + konten kanan) ───────────────
                var selected by remember { mutableStateOf(SettingsNav.PRINTER) }

                Row(Modifier.fillMaxSize()) {
                    // Panel kiri — daftar kategori
                    Column(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "KATEGORI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )

                        SettingsNavItem(
                            icon     = Icons.Default.Print,
                            iconBg   = MaterialTheme.colorScheme.primary,
                            title    = "Printer Kasir",
                            subtitle = if (uiState.hasPrinter)
                                uiState.savedPrinterName.ifBlank { uiState.savedPrinterAddress }
                            else "Belum diatur",
                            badge    = if (uiState.hasPrinter) "Tersimpan" else null,
                            badgeOk  = uiState.hasPrinter,
                            selected = selected == SettingsNav.PRINTER,
                            onClick  = { selected = SettingsNav.PRINTER }
                        )
                        SettingsNavItem(
                            icon     = Icons.Default.Tune,
                            iconBg   = Color(0xFF6750A4),
                            title    = "Mode Cetak",
                            subtitle = when (uiState.printMode) {
                                PrintMode.RECEIPT_ONLY         -> "Struk kasir saja"
                                PrintMode.DUAL_PRINTER         -> "Dua printer"
                                PrintMode.SINGLE_KOT_FIRST     -> "Satu printer, KOT dulu"
                                PrintMode.SINGLE_RECEIPT_FIRST -> "Satu printer, struk dulu"
                            },
                            selected = selected == SettingsNav.PRINT_MODE,
                            onClick  = { selected = SettingsNav.PRINT_MODE }
                        )
                        if (uiState.printMode != PrintMode.RECEIPT_ONLY) {
                            SettingsNavItem(
                                icon     = Icons.Default.Restaurant,
                                iconBg   = Color(0xFFB5340A),
                                title    = "Printer Dapur",
                                subtitle = if (uiState.hasKitchenPrinter)
                                    uiState.kitchenPrinterName.ifBlank { uiState.kitchenPrinterAddress }
                                else "Belum diatur",
                                badge    = if (uiState.hasKitchenPrinter) "Tersimpan" else null,
                                badgeOk  = uiState.hasKitchenPrinter,
                                selected = selected == SettingsNav.KITCHEN,
                                onClick  = { selected = SettingsNav.KITCHEN }
                            )
                        }
                        SettingsNavItem(
                            icon     = Icons.Default.Store,
                            iconBg   = Color(0xFF1A6B3C),
                            title    = "Informasi Toko",
                            subtitle = uiState.storeName.ifBlank { "Belum diisi" },
                            selected = selected == SettingsNav.STORE,
                            onClick  = { selected = SettingsNav.STORE }
                        )
                        SettingsNavItem(
                            icon     = Icons.Default.Settings,
                            iconBg   = Color(0xFF555555),
                            title    = "Umum",
                            subtitle = if (uiState.autoPrint) "Auto print aktif" else "Auto print mati",
                            selected = selected == SettingsNav.GENERAL,
                            onClick  = { selected = SettingsNav.GENERAL }
                        )
                    }

                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Panel kanan — konten kategori
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        when (selected) {
                            SettingsNav.PRINTER -> PrinterContent(
                                uiState = uiState,
                                onPrinterType = onPrinterType,
                                onScan = requestBluetoothPermission,
                                onSelectPrinter = onSelectPrinter,
                                onDisconnect = onDisconnect,
                                onNetworkIp = onNetworkIp,
                                onNetworkPort = onNetworkPort,
                                onSaveNetwork = onSaveNetwork,
                                onTestPrint = onTestPrint
                            )
                            SettingsNav.PRINT_MODE -> PrintModeContent(
                                printMode = uiState.printMode,
                                onPrintMode = onPrintMode
                            )
                            SettingsNav.KITCHEN -> KitchenContent(
                                uiState = uiState,
                                onKitchenPrinterType = onKitchenPrinterType,
                                onScan = requestBluetoothPermission,
                                onSelectKitchenPrinter = onSelectKitchenPrinter,
                                onDisconnectKitchen = onDisconnectKitchen,
                                onKitchenNetworkIp = onKitchenNetworkIp,
                                onKitchenNetworkPort = onKitchenNetworkPort,
                                onSaveKitchenNetwork = onSaveKitchenNetwork,
                                onTestPrint = onTestPrint
                            )
                            SettingsNav.STORE -> StoreContent(
                                storeName    = uiState.storeName,
                                storeAddress = uiState.storeAddress,
                                storePhone   = uiState.storePhone,
                                footerText   = uiState.footerText,
                                onStoreName  = onStoreName,
                                onStoreAddress = onStoreAddress,
                                onStorePhone = onStorePhone,
                                onFooterText = onFooterText
                            )
                            SettingsNav.GENERAL -> GeneralContent(
                                autoPrint  = uiState.autoPrint,
                                paperWidth = uiState.paperWidth,
                                onAutoPrint  = onAutoPrint,
                                onPaperWidth = onPaperWidth
                            )
                        }
                    }
                }

            } else {
                // ── Phone: satu kolom scrollable ──────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ContentSectionTitle(Icons.Default.Print, "Printer Kasir",
                            Color(0xFF1A6B3C))
                        Spacer(Modifier.height(8.dp))
                        PrinterContent(
                            uiState = uiState,
                            onPrinterType = onPrinterType,
                            onScan = requestBluetoothPermission,
                            onSelectPrinter = onSelectPrinter,
                            onDisconnect = onDisconnect,
                            onNetworkIp = onNetworkIp,
                            onNetworkPort = onNetworkPort,
                            onSaveNetwork = onSaveNetwork,
                            onTestPrint = onTestPrint
                        )
                    }
                    item {
                        ContentSectionTitle(Icons.Default.Tune, "Mode Cetak", Color(0xFF6750A4))
                        Spacer(Modifier.height(8.dp))
                        PrintModeContent(uiState.printMode, onPrintMode)
                    }
                    if (uiState.printMode != PrintMode.RECEIPT_ONLY) {
                        item {
                            ContentSectionTitle(Icons.Default.Restaurant, "Printer Dapur",
                                Color(0xFFB5340A))
                            Spacer(Modifier.height(8.dp))
                            KitchenContent(
                                uiState = uiState,
                                onKitchenPrinterType = onKitchenPrinterType,
                                onScan = requestBluetoothPermission,
                                onSelectKitchenPrinter = onSelectKitchenPrinter,
                                onDisconnectKitchen = onDisconnectKitchen,
                                onKitchenNetworkIp = onKitchenNetworkIp,
                                onKitchenNetworkPort = onKitchenNetworkPort,
                                onSaveKitchenNetwork = onSaveKitchenNetwork,
                                onTestPrint = onTestPrint
                            )
                        }
                    }
                    item {
                        ContentSectionTitle(Icons.Default.Store, "Informasi Toko",
                            Color(0xFF1A6B3C))
                        Spacer(Modifier.height(8.dp))
                        StoreContent(
                            storeName    = uiState.storeName,
                            storeAddress = uiState.storeAddress,
                            storePhone   = uiState.storePhone,
                            footerText   = uiState.footerText,
                            onStoreName  = onStoreName,
                            onStoreAddress = onStoreAddress,
                            onStorePhone = onStorePhone,
                            onFooterText = onFooterText
                        )
                    }
                    item {
                        ContentSectionTitle(Icons.Default.Settings, "Umum", Color(0xFF555555))
                        Spacer(Modifier.height(8.dp))
                        GeneralContent(uiState.autoPrint, uiState.paperWidth, onAutoPrint, onPaperWidth)
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Nav item kiri (tablet)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeOk: Boolean = true,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    else Color.Transparent

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = bg,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White,
                    modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                Text(subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1)
            }
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (badgeOk) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(badge,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (badgeOk) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Judul section konten
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ContentSectionTitle(icon: ImageVector, title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(color),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp)) }
        Text(title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Settings Row — baris setting standar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    description: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (description != null) {
                Text(description, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing?.invoke()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Settings Card wrapper — tipis, tanpa padding berlebih
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// FilterChip berwarma primary — gantikan default warna orange Material3
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrimaryFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    FilterChip(
        selected    = selected,
        onClick     = onClick,
        label       = label,
        modifier    = modifier,
        leadingIcon = leadingIcon,
        colors      = FilterChipDefaults.filterChipColors(
            selectedContainerColor    = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            selectedLabelColor        = MaterialTheme.colorScheme.primary,
            selectedLeadingIconColor  = MaterialTheme.colorScheme.primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled              = true,
            selected             = selected,
            selectedBorderColor  = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            selectedBorderWidth  = 1.2.dp,
            borderColor          = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            borderWidth          = 0.8.dp
        )
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            content()
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Printer Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrinterContent(
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
    // Printer tersimpan
    if (uiState.hasPrinter) {
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
                    Text(uiState.savedPrinterName.ifBlank { uiState.savedPrinterAddress },
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(uiState.savedPrinterAddress,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)) {
                    Text("Tersimpan",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onTestPrint, enabled = !uiState.isPrinting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isPrinting) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(if (uiState.isPrinting) "Mencetak…" else "Test Print",
                        style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hapus", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    // Pilih tipe koneksi
    SettingsCard {
        Text("Tipe Koneksi",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryFilterChip(
                selected = uiState.printerType == SettingsStore.TYPE_BLUETOOTH,
                onClick = { onPrinterType(SettingsStore.TYPE_BLUETOOTH) },
                label = { Text("Bluetooth", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = { Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
            PrimaryFilterChip(
                selected = uiState.printerType == SettingsStore.TYPE_NETWORK,
                onClick = { onPrinterType(SettingsStore.TYPE_NETWORK) },
                label = { Text("Wi-Fi / LAN", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Bluetooth
    if (uiState.printerType == SettingsStore.TYPE_BLUETOOTH) {
        if (!uiState.isBluetoothOn) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.BluetoothDisabled, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Text("Bluetooth tidak aktif — aktifkan di pengaturan perangkat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
        SettingsCard {
            Button(
                onClick = onScan,
                enabled = !uiState.isScanning && !uiState.isConnecting && uiState.isBluetoothOn,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(6.dp))
                }
                Icon(Icons.Default.Bluetooth, contentDescription = null,
                    modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (uiState.isScanning) "Mencari…" else "Cari Printer Bluetooth",
                    style = MaterialTheme.typography.labelMedium)
            }

            if (uiState.isConnecting) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Text("Menghubungkan…", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (!uiState.hasScannedOnce && uiState.discoveredPrinters.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Text("Tips:", style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Nyalakan printer → aktifkan Bluetooth → pair printer di Pengaturan perangkat → tekan Cari",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            AnimatedVisibility(visible = uiState.discoveredPrinters.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    Text("Ditemukan ${uiState.discoveredPrinters.size} printer:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    uiState.discoveredPrinters.forEach { device ->
                        val isSaved = device.address == uiState.savedPrinterAddress
                        PrinterDeviceRow(device = device, isSaved = isSaved,
                            isConnecting = uiState.isConnecting,
                            onClick = { onSelectPrinter(device) })
                    }
                }
            }

            if (uiState.hasScannedOnce && uiState.discoveredPrinters.isEmpty() && !uiState.isScanning) {
                Spacer(Modifier.height(8.dp))
                Text("Tidak ada printer ditemukan. Pastikan printer sudah di-pair.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // Network
    if (uiState.printerType == SettingsStore.TYPE_NETWORK) {
        SettingsCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.networkIp,
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
                    value = uiState.networkPort,
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Print Mode Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrintModeContent(printMode: PrintMode, onPrintMode: (PrintMode) -> Unit) {
    val modes = listOf(
        PrintMode.RECEIPT_ONLY         to ("Struk Kasir Saja"          to "Hanya cetak struk kasir, tanpa KOT dapur"),
        PrintMode.DUAL_PRINTER         to ("Dua Printer"               to "Struk kasir ke printer kasir, KOT ke printer dapur secara bersamaan"),
        PrintMode.SINGLE_KOT_FIRST     to ("Satu Printer — KOT Dulu"   to "Cetak KOT dapur dulu, lalu struk kasir"),
        PrintMode.SINGLE_RECEIPT_FIRST to ("Satu Printer — Struk Dulu" to "Cetak struk kasir dulu, lalu KOT dapur")
    )

    SettingsCard {
        Text("Tentukan bagaimana struk dan tiket dapur (KOT) dicetak setelah pembayaran.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        modes.forEachIndexed { idx, (mode, labelDesc) ->
            val (label, desc) = labelDesc
            Surface(
                onClick = { onPrintMode(mode) },
                shape = MaterialTheme.shapes.small,
                color = if (printMode == mode)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = printMode == mode, onClick = { onPrintMode(mode) },
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold)
                        Text(desc, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (idx < modes.lastIndex) Spacer(Modifier.height(4.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Kitchen Printer Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun KitchenContent(
    uiState: SettingsUiState,
    onKitchenPrinterType: (String) -> Unit,
    onScan: () -> Unit,
    onSelectKitchenPrinter: (PrinterDevice) -> Unit,
    onDisconnectKitchen: () -> Unit,
    onKitchenNetworkIp: (String) -> Unit,
    onKitchenNetworkPort: (String) -> Unit,
    onSaveKitchenNetwork: () -> Unit,
    onTestPrint: () -> Unit
) {
    if (uiState.hasKitchenPrinter) {
        SettingsCard {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    if (uiState.kitchenPrinterType == SettingsStore.TYPE_BLUETOOTH) Icons.Default.Bluetooth
                    else Icons.Default.Wifi,
                    contentDescription = null, tint = Color(0xFFB5340A),
                    modifier = Modifier.size(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(uiState.kitchenPrinterName.ifBlank { uiState.kitchenPrinterAddress },
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(uiState.kitchenPrinterAddress,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFB5340A).copy(alpha = 0.12f)) {
                    Text("Printer Dapur",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB5340A))
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onDisconnectKitchen, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Hapus Printer Dapur", style = MaterialTheme.typography.labelMedium)
            }
        }
    }

    SettingsCard {
        Text("Tipe Koneksi Dapur",
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryFilterChip(
                selected = uiState.kitchenPrinterType == SettingsStore.TYPE_BLUETOOTH,
                onClick = { onKitchenPrinterType(SettingsStore.TYPE_BLUETOOTH) },
                label = { Text("Bluetooth", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = { Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
            PrimaryFilterChip(
                selected = uiState.kitchenPrinterType == SettingsStore.TYPE_NETWORK,
                onClick = { onKitchenPrinterType(SettingsStore.TYPE_NETWORK) },
                label = { Text("Wi-Fi / LAN", style = MaterialTheme.typography.labelMedium) },
                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (uiState.kitchenPrinterType == SettingsStore.TYPE_BLUETOOTH) {
        SettingsCard {
            Button(
                onClick = onScan,
                enabled = !uiState.isScanning && !uiState.isConnecting && uiState.isBluetoothOn,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isScanning) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(6.dp))
                }
                Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (uiState.isScanning) "Mencari…" else "Cari Printer Dapur",
                    style = MaterialTheme.typography.labelMedium)
            }
            AnimatedVisibility(visible = uiState.discoveredPrinters.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    uiState.discoveredPrinters.forEach { device ->
                        PrinterDeviceRow(device = device,
                            isSaved = device.address == uiState.kitchenPrinterAddress,
                            isConnecting = uiState.isConnecting,
                            onClick = { onSelectKitchenPrinter(device) })
                    }
                }
            }
        }
    }

    if (uiState.kitchenPrinterType == SettingsStore.TYPE_NETWORK) {
        SettingsCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.kitchenNetworkIp,
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
                    value = uiState.kitchenNetworkPort,
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Store / Receipt Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StoreContent(
    storeName: String,
    storeAddress: String,
    storePhone: String,
    footerText: String,
    onStoreName: (String) -> Unit,
    onStoreAddress: (String) -> Unit,
    onStorePhone: (String) -> Unit,
    onFooterText: (String) -> Unit
) {
    SettingsCard {
        Text("Info ini tampil di header setiap struk yang dicetak.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = storeName, onValueChange = onStoreName,
            label = { Text("Nama Toko") }, placeholder = { Text("Rancak Coffee") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = storeAddress, onValueChange = onStoreAddress,
            label = { Text("Alamat") }, placeholder = { Text("Jl. Sudirman No. 1") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = storePhone, onValueChange = onStorePhone,
                label = { Text("Telepon") }, placeholder = { Text("0812-3456-7890") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f), textStyle = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = footerText, onValueChange = onFooterText,
                label = { Text("Footer Struk") }, placeholder = { Text("Terima kasih!") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f), textStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// General Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GeneralContent(
    autoPrint: Boolean,
    paperWidth: Int,
    onAutoPrint: (Boolean) -> Unit,
    onPaperWidth: (Int) -> Unit
) {
    SettingsCard {
        // Auto print
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Print, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Auto Print Struk", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Text("Cetak otomatis setelah pembayaran berhasil",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = autoPrint, onCheckedChange = onAutoPrint)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Paper width
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Receipt, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Lebar Kertas", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Text(if (paperWidth == 58) "58mm — 32 karakter/baris" else "80mm — 48 karakter/baris",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(58, 80).forEach { w ->
                    PrimaryFilterChip(
                        selected = paperWidth == w,
                        onClick = { onPaperWidth(w) },
                        label = { Text("${w}mm", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Printer Device Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PrinterDeviceRow(
    device: PrinterDevice,
    isSaved: Boolean,
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(enabled = !isSaved && !isConnecting, onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (isSaved) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Bluetooth, contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSaved) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant)
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name.ifBlank { "Unknown Device" },
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                Text(device.address, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSaved) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Terpilih",
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            } else {
                Text("Pilih", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
