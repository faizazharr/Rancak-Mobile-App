package id.rancak.app.presentation.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.data.printing.PrintMode
import id.rancak.app.data.printing.PrinterDevice
import id.rancak.app.presentation.components.RancakTopBar
import id.rancak.app.presentation.components.rememberRequestBluetoothPermission
import id.rancak.app.presentation.ui.settings.components.ContentSectionTitle
import id.rancak.app.presentation.ui.settings.components.GeneralContent
import id.rancak.app.presentation.ui.settings.components.KitchenContent
import id.rancak.app.presentation.ui.settings.components.PrintModeContent
import id.rancak.app.presentation.ui.settings.components.PrinterContent
import id.rancak.app.presentation.ui.settings.components.SettingsNavItem
import id.rancak.app.presentation.ui.settings.components.StoreContent
import id.rancak.app.presentation.viewmodel.SettingsUiState
import id.rancak.app.presentation.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Kategori pengaturan yang dipilih di panel kiri (tablet) atau diurut ke bawah
 * (phone).
 */
private enum class SettingsNav { PRINTER, PRINT_MODE, KITCHEN, STORE, GENERAL }

// ─────────────────────────────────────────────────────────────────────────────
// Entry point — menghubungkan [SettingsViewModel] ke [SettingsScreenContent].
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.checkBluetoothState() }

    SettingsScreenContent(
        uiState                = uiState,
        onBack                 = onBack,
        onPrinterType          = viewModel::setPrinterType,
        onScanBluetooth        = viewModel::scanBluetoothPrinters,
        onSelectPrinter        = viewModel::selectBluetoothPrinter,
        onDisconnect           = viewModel::disconnectPrinter,
        onNetworkIp            = viewModel::setNetworkIp,
        onNetworkPort          = viewModel::setNetworkPort,
        onSaveNetwork          = viewModel::saveNetworkPrinter,
        onTestPrint            = viewModel::testPrint,
        onPrintMode            = viewModel::setPrintMode,
        onKitchenPrinterType   = viewModel::setKitchenPrinterType,
        onSelectKitchenPrinter = viewModel::selectKitchenBluetoothPrinter,
        onDisconnectKitchen    = viewModel::disconnectKitchenPrinter,
        onKitchenNetworkIp     = viewModel::setKitchenNetworkIp,
        onKitchenNetworkPort   = viewModel::setKitchenNetworkPort,
        onSaveKitchenNetwork   = viewModel::saveKitchenNetworkPrinter,
        onStoreName            = viewModel::setStoreName,
        onStoreAddress         = viewModel::setStoreAddress,
        onStorePhone           = viewModel::setStorePhone,
        onFooterText           = viewModel::setFooterText,
        onAutoPrint            = viewModel::setAutoPrint,
        onPaperWidth           = viewModel::setPaperWidth,
        onClearMessage         = viewModel::clearMessage
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Pure UI body — Scaffold + responsive tablet/phone layout.
// ─────────────────────────────────────────────────────────────────────────────

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
        uiState.printerMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessage()
        }
    }

    Scaffold(
        topBar = {
            RancakTopBar(
                title    = "Pengaturan",
                icon     = Icons.Default.Settings,
                subtitle = "Konfigurasi aplikasi",
                onMenu   = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (maxWidth >= 600.dp) {
                TabletLayout(
                    uiState                = uiState,
                    onScan                 = requestBluetoothPermission,
                    onPrinterType          = onPrinterType,
                    onSelectPrinter        = onSelectPrinter,
                    onDisconnect           = onDisconnect,
                    onNetworkIp            = onNetworkIp,
                    onNetworkPort          = onNetworkPort,
                    onSaveNetwork          = onSaveNetwork,
                    onTestPrint            = onTestPrint,
                    onPrintMode            = onPrintMode,
                    onKitchenPrinterType   = onKitchenPrinterType,
                    onSelectKitchenPrinter = onSelectKitchenPrinter,
                    onDisconnectKitchen    = onDisconnectKitchen,
                    onKitchenNetworkIp     = onKitchenNetworkIp,
                    onKitchenNetworkPort   = onKitchenNetworkPort,
                    onSaveKitchenNetwork   = onSaveKitchenNetwork,
                    onStoreName            = onStoreName,
                    onStoreAddress         = onStoreAddress,
                    onStorePhone           = onStorePhone,
                    onFooterText           = onFooterText,
                    onAutoPrint            = onAutoPrint,
                    onPaperWidth           = onPaperWidth
                )
            } else {
                PhoneLayout(
                    uiState                = uiState,
                    onScan                 = requestBluetoothPermission,
                    onPrinterType          = onPrinterType,
                    onSelectPrinter        = onSelectPrinter,
                    onDisconnect           = onDisconnect,
                    onNetworkIp            = onNetworkIp,
                    onNetworkPort          = onNetworkPort,
                    onSaveNetwork          = onSaveNetwork,
                    onTestPrint            = onTestPrint,
                    onPrintMode            = onPrintMode,
                    onKitchenPrinterType   = onKitchenPrinterType,
                    onSelectKitchenPrinter = onSelectKitchenPrinter,
                    onDisconnectKitchen    = onDisconnectKitchen,
                    onKitchenNetworkIp     = onKitchenNetworkIp,
                    onKitchenNetworkPort   = onKitchenNetworkPort,
                    onSaveKitchenNetwork   = onSaveKitchenNetwork,
                    onStoreName            = onStoreName,
                    onStoreAddress         = onStoreAddress,
                    onStorePhone           = onStorePhone,
                    onFooterText           = onFooterText,
                    onAutoPrint            = onAutoPrint,
                    onPaperWidth           = onPaperWidth
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tablet — panel nav kiri + konten kanan.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TabletLayout(
    uiState: SettingsUiState,
    onScan: () -> Unit,
    onPrinterType: (String) -> Unit,
    onSelectPrinter: (PrinterDevice) -> Unit,
    onDisconnect: () -> Unit,
    onNetworkIp: (String) -> Unit,
    onNetworkPort: (String) -> Unit,
    onSaveNetwork: () -> Unit,
    onTestPrint: () -> Unit,
    onPrintMode: (PrintMode) -> Unit,
    onKitchenPrinterType: (String) -> Unit,
    onSelectKitchenPrinter: (PrinterDevice) -> Unit,
    onDisconnectKitchen: () -> Unit,
    onKitchenNetworkIp: (String) -> Unit,
    onKitchenNetworkPort: (String) -> Unit,
    onSaveKitchenNetwork: () -> Unit,
    onStoreName: (String) -> Unit,
    onStoreAddress: (String) -> Unit,
    onStorePhone: (String) -> Unit,
    onFooterText: (String) -> Unit,
    onAutoPrint: (Boolean) -> Unit,
    onPaperWidth: (Int) -> Unit
) {
    var selected by remember { mutableStateOf(SettingsNav.PRINTER) }

    Row(Modifier.fillMaxSize()) {
        // Panel kiri — kategori
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
                style         = MaterialTheme.typography.labelSmall,
                fontWeight    = FontWeight.Bold,
                color         = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier      = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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

        // Panel kanan — konten
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
                    uiState         = uiState,
                    onPrinterType   = onPrinterType,
                    onScan          = onScan,
                    onSelectPrinter = onSelectPrinter,
                    onDisconnect    = onDisconnect,
                    onNetworkIp     = onNetworkIp,
                    onNetworkPort   = onNetworkPort,
                    onSaveNetwork   = onSaveNetwork,
                    onTestPrint     = onTestPrint
                )
                SettingsNav.PRINT_MODE -> PrintModeContent(
                    printMode   = uiState.printMode,
                    onPrintMode = onPrintMode
                )
                SettingsNav.KITCHEN -> KitchenContent(
                    uiState                = uiState,
                    onKitchenPrinterType   = onKitchenPrinterType,
                    onScan                 = onScan,
                    onSelectKitchenPrinter = onSelectKitchenPrinter,
                    onDisconnectKitchen    = onDisconnectKitchen,
                    onKitchenNetworkIp     = onKitchenNetworkIp,
                    onKitchenNetworkPort   = onKitchenNetworkPort,
                    onSaveKitchenNetwork   = onSaveKitchenNetwork,
                    onTestPrint            = onTestPrint
                )
                SettingsNav.STORE -> StoreContent(
                    storeName      = uiState.storeName,
                    storeAddress   = uiState.storeAddress,
                    storePhone     = uiState.storePhone,
                    footerText     = uiState.footerText,
                    onStoreName    = onStoreName,
                    onStoreAddress = onStoreAddress,
                    onStorePhone   = onStorePhone,
                    onFooterText   = onFooterText
                )
                SettingsNav.GENERAL -> GeneralContent(
                    autoPrint    = uiState.autoPrint,
                    paperWidth   = uiState.paperWidth,
                    onAutoPrint  = onAutoPrint,
                    onPaperWidth = onPaperWidth
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Phone — semua kategori dirender berurut dalam LazyColumn.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PhoneLayout(
    uiState: SettingsUiState,
    onScan: () -> Unit,
    onPrinterType: (String) -> Unit,
    onSelectPrinter: (PrinterDevice) -> Unit,
    onDisconnect: () -> Unit,
    onNetworkIp: (String) -> Unit,
    onNetworkPort: (String) -> Unit,
    onSaveNetwork: () -> Unit,
    onTestPrint: () -> Unit,
    onPrintMode: (PrintMode) -> Unit,
    onKitchenPrinterType: (String) -> Unit,
    onSelectKitchenPrinter: (PrinterDevice) -> Unit,
    onDisconnectKitchen: () -> Unit,
    onKitchenNetworkIp: (String) -> Unit,
    onKitchenNetworkPort: (String) -> Unit,
    onSaveKitchenNetwork: () -> Unit,
    onStoreName: (String) -> Unit,
    onStoreAddress: (String) -> Unit,
    onStorePhone: (String) -> Unit,
    onFooterText: (String) -> Unit,
    onAutoPrint: (Boolean) -> Unit,
    onPaperWidth: (Int) -> Unit
) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        contentPadding      = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ContentSectionTitle(Icons.Default.Print, "Printer Kasir", Color(0xFF1A6B3C))
            Spacer(Modifier.height(8.dp))
            PrinterContent(
                uiState         = uiState,
                onPrinterType   = onPrinterType,
                onScan          = onScan,
                onSelectPrinter = onSelectPrinter,
                onDisconnect    = onDisconnect,
                onNetworkIp     = onNetworkIp,
                onNetworkPort   = onNetworkPort,
                onSaveNetwork   = onSaveNetwork,
                onTestPrint     = onTestPrint
            )
        }
        item {
            ContentSectionTitle(Icons.Default.Tune, "Mode Cetak", Color(0xFF6750A4))
            Spacer(Modifier.height(8.dp))
            PrintModeContent(uiState.printMode, onPrintMode)
        }
        if (uiState.printMode != PrintMode.RECEIPT_ONLY) {
            item {
                ContentSectionTitle(Icons.Default.Restaurant, "Printer Dapur", Color(0xFFB5340A))
                Spacer(Modifier.height(8.dp))
                KitchenContent(
                    uiState                = uiState,
                    onKitchenPrinterType   = onKitchenPrinterType,
                    onScan                 = onScan,
                    onSelectKitchenPrinter = onSelectKitchenPrinter,
                    onDisconnectKitchen    = onDisconnectKitchen,
                    onKitchenNetworkIp     = onKitchenNetworkIp,
                    onKitchenNetworkPort   = onKitchenNetworkPort,
                    onSaveKitchenNetwork   = onSaveKitchenNetwork,
                    onTestPrint            = onTestPrint
                )
            }
        }
        item {
            ContentSectionTitle(Icons.Default.Store, "Informasi Toko", Color(0xFF1A6B3C))
            Spacer(Modifier.height(8.dp))
            StoreContent(
                storeName      = uiState.storeName,
                storeAddress   = uiState.storeAddress,
                storePhone     = uiState.storePhone,
                footerText     = uiState.footerText,
                onStoreName    = onStoreName,
                onStoreAddress = onStoreAddress,
                onStorePhone   = onStorePhone,
                onFooterText   = onFooterText
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
