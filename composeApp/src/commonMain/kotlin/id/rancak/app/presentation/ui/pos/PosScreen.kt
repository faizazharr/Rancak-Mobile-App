package id.rancak.app.presentation.ui.pos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.model.CartItem
import id.rancak.app.presentation.barcode.BarcodeScannerView
import id.rancak.app.presentation.ui.pos.components.CartBar
import id.rancak.app.presentation.ui.pos.components.OpenBillNameDialog
import id.rancak.app.presentation.ui.pos.components.OrderPanel
import id.rancak.app.presentation.ui.pos.components.PosCategoryRow
import id.rancak.app.presentation.ui.pos.components.PosSearchBar
import id.rancak.app.presentation.ui.pos.components.PosTopBar
import id.rancak.app.presentation.ui.pos.components.ProductGridContent
import id.rancak.app.presentation.navigation.LocalCartViewModel
import id.rancak.app.presentation.viewmodel.CartUiState
import id.rancak.app.presentation.viewmodel.OpenBillViewModel
import id.rancak.app.presentation.viewmodel.PosUiState
import id.rancak.app.presentation.viewmodel.PosViewModel
import id.rancak.app.presentation.viewmodel.ShiftViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main Point-Of-Sale screen. Responsible only for:
 *  - wiring the POS, cart and shift view-models,
 *  - deciding phone vs tablet/landscape layout,
 *  - toggling the full-screen barcode scanner.
 *
 * All visual components live in [id.rancak.app.presentation.ui.pos.components].
 */
@Composable
fun PosScreen(
    onCartClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onMenuClick: () -> Unit,
    /** Dipanggil setelah open bill berhasil dibuat — gunakan untuk navigasi ke daftar open bill. */
    onHoldSuccess: () -> Unit = {},
    onOpenBillClick: () -> Unit = {}
) {
    val posViewModel: PosViewModel       = koinViewModel()
    val cartViewModel                    = LocalCartViewModel.current
    val shiftViewModel: ShiftViewModel   = koinViewModel()
    val openBillViewModel: OpenBillViewModel = koinViewModel()
    val uiState       by posViewModel.uiState.collectAsStateWithLifecycle()
    val cartState     by cartViewModel.uiState.collectAsStateWithLifecycle()
    val shiftState    by shiftViewModel.uiState.collectAsStateWithLifecycle()
    val openBillState by openBillViewModel.uiState.collectAsStateWithLifecycle()
    var showScanner   by remember { mutableStateOf(false) }

    val hasOpenShift = shiftState.currentShift != null

    // Dialog nama open bill — tampilkan saat openBillState.showNameDialog = true
    if (openBillState.showNameDialog) {
        OpenBillNameDialog(
            initialName = openBillState.dialogInitialName,
            isUpdate    = openBillState.editingBillId != null,
            onConfirm   = { name ->
                openBillViewModel.saveCart(
                    name              = name,
                    items             = cartState.items,
                    orderType         = cartState.orderType,
                    tableUuid         = cartState.tableUuid,
                    customerName      = cartState.customerName,
                    note              = cartState.note,
                    pax               = cartState.pax,
                    discountInput     = cartState.discountInput,
                    discountIsPercent = cartState.discountIsPercent,
                    taxInput          = cartState.taxInput,
                    taxIsPercent      = cartState.taxIsPercent,
                    adminFeeInput     = cartState.adminFeeInput,
                    adminFeeIsPercent = cartState.adminFeeIsPercent,
                    deliveryFee       = cartState.deliveryFee,
                    tip               = cartState.tip,
                    voucherCode       = cartState.voucherCode,
                    editingBillId          = cartState.activeOpenBillId,
                    existingRemoteSaleUuid = cartState.activeOpenBillSaleUuid
                )
                cartViewModel.clearCart()
                openBillViewModel.hideDialog()
            },
            onDismiss = openBillViewModel::hideDialog
        )
    }

    // Success dialog — tampil setelah open bill berhasil disimpan
    if (openBillState.showSuccessDialog) {
        AlertDialog(
            onDismissRequest = openBillViewModel::dismissSuccessDialog,
            icon = {
                Icon(
                    imageVector        = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint               = Color(0xFFF59E0B)
                )
            },
            title = {
                Text(
                    "Open Bill Disimpan",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Tagihan berhasil disimpan. Lanjutkan proses dari halaman Open Bill kapan saja.")
            },
            confirmButton = {
                TextButton(onClick = openBillViewModel::dismissSuccessDialog) {
                    Text("Oke")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    LaunchedEffect(Unit) {
        posViewModel.loadProducts()
        posViewModel.loadCategories()
        posViewModel.load86Products()
        shiftViewModel.loadCurrentShift()
    }

    // Reload 86 list every time the POS screen resumes (e.g., after returning from
    // Product Management where an admin may have unmarked a product as 86).
    LifecycleResumeEffect(Unit) {
        posViewModel.load86Products()
        onPauseOrDispose { }
    }

    if (showScanner) {
        BarcodeScannerView(
            onBarcodeDetected = { barcode ->
                posViewModel.onSearchQueryChange(barcode)
                showScanner = false
            },
            onClose = { showScanner = false }
        )
        return
    }

    val cartQtyMap by remember {
        derivedStateOf {
            cartState.items.associate { it.productUuid to it.qty }.toImmutableMap()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp || maxWidth > maxHeight

        if (isWide) {
            SplitLayout(
                uiState          = uiState,
                cartState        = cartState,
                cartQtyMap       = cartQtyMap,
                outletName       = uiState.outletName,
                onMenuClick      = onMenuClick,
                onCartClick      = onCartClick,
                onCheckoutClick  = onCheckoutClick,
                onSaveClick      = {
                    openBillViewModel.showDialog(
                        initialName   = cartState.activeOpenBillName,
                        editingBillId = cartState.activeOpenBillId
                    )
                },
                onOpenBillClick  = onOpenBillClick,
                hasOpenShift     = hasOpenShift,
                onSearchChange   = posViewModel::onSearchQueryChange,
                onCategorySelect = posViewModel::onCategorySelected,
                onRefresh        = posViewModel::refresh,
                onAdd            = { cartViewModel.addProduct(it) },
                onUpdateQty      = { item, qty ->
                    cartViewModel.updateQuantity(item.productUuid, item.variantUuid, qty)
                },
                onUpdateNote     = { item, note ->
                    cartViewModel.updateItemNote(item.productUuid, item.variantUuid, note)
                },
                onClearCart      = { cartViewModel.clearCart() },
                onOrderType      = { cartViewModel.setOrderType(it) },
                onCustomerName   = { cartViewModel.setCustomerName(it) },
                onPax            = { cartViewModel.setPax(it) },
                onDiscount       = { v, isPercent -> cartViewModel.setDiscount(v, isPercent) },
                onTax            = { v, isPercent -> cartViewModel.setTax(v, isPercent) },
                onAdminFee       = { v, isPercent -> cartViewModel.setAdminFee(v, isPercent) },
                onDeliveryFee    = { cartViewModel.setDeliveryFee(it) },
                onTip            = { cartViewModel.setTip(it) },
                onVoucherCode    = { cartViewModel.setVoucherCode(it) },
                onScanClick      = { showScanner = true },
                modifierCache    = uiState.modifierCache,
                onLoadModifiers  = posViewModel::loadModifiersForProduct
            )
        } else {
            PhoneLayout(
                uiState          = uiState,
                cartState        = cartState,
                cartQtyMap       = cartQtyMap,
                outletName       = uiState.outletName,
                hasOpenShift     = hasOpenShift,
                onMenuClick      = onMenuClick,
                onCartClick      = onCartClick,
                onOpenBillClick  = onOpenBillClick,
                onSearchChange   = posViewModel::onSearchQueryChange,
                onCategorySelect = posViewModel::onCategorySelected,
                onRefresh        = posViewModel::refresh,
                onAdd            = { cartViewModel.addProduct(it) },
                onScanClick      = { showScanner = true }
            )
        }
    }
}

// ─── Phone / portrait layout ────────────────────────────────────────────────

@Composable
private fun PhoneLayout(
    uiState: PosUiState,
    cartState: CartUiState,
    cartQtyMap: ImmutableMap<String, Int>,
    outletName: String,
    hasOpenShift: Boolean,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    onOpenBillClick: () -> Unit = {},
    onSearchChange: (String) -> Unit,
    onCategorySelect: (Category?) -> Unit,
    onRefresh: () -> Unit,
    onAdd: (Product) -> Unit,
    onScanClick: () -> Unit
) {
    val hasCart = cartState.itemCount > 0
    val primary = MaterialTheme.colorScheme.primary
    val immutableCategories = uiState.categories

    Scaffold(
        topBar = {
            PosTopBar(
                outletName   = outletName,
                hasCart      = hasCart,
                itemCount    = cartState.itemCount,
                hasOpenShift = hasOpenShift,
                onMenuClick  = onMenuClick,
                onCartClick  = onCartClick,
                onOpenBillClick = onOpenBillClick,
                showCart     = true
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(Modifier.fillMaxSize()) {
                PosSearchBar(
                    query         = uiState.searchQuery,
                    onQueryChange = onSearchChange,
                    onScanClick   = onScanClick,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
                PosCategoryRow(immutableCategories, uiState.selectedCategory, onCategorySelect)
                ProductGridContent(
                    uiState    = uiState,
                    cartQtyMap = cartQtyMap,
                    bottomPad  = if (hasCart) 80.dp else 16.dp,
                    onRefresh  = onRefresh,
                    onAdd      = onAdd
                )
            }

            AnimatedVisibility(
                visible  = hasCart,
                enter    = slideInVertically(tween(220)) { it } + fadeIn(tween(220)),
                exit     = slideOutVertically(tween(180)) { it } + fadeOut(tween(180)),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                CartBar(
                    cartState       = cartState,
                    primary         = primary,
                    onCartClick     = onCartClick,
                    onOpenBillClick = onOpenBillClick
                )
            }
        }
    }
}

// ─── Tablet / landscape split layout ────────────────────────────────────────

@Composable
private fun SplitLayout(
    uiState: PosUiState,
    cartState: CartUiState,
    cartQtyMap: ImmutableMap<String, Int>,
    outletName: String,
    hasOpenShift: Boolean,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onSaveClick: () -> Unit,
    onOpenBillClick: () -> Unit = {},
    onSearchChange: (String) -> Unit,
    onCategorySelect: (Category?) -> Unit,
    onRefresh: () -> Unit,
    onAdd: (Product) -> Unit,
    onUpdateQty: (CartItem, Int) -> Unit,
    onUpdateNote: (CartItem, String) -> Unit,
    onClearCart: () -> Unit,
    onOrderType: (OrderType) -> Unit,
    onCustomerName: (String) -> Unit,
    onPax: (Int) -> Unit,
    onDiscount: (Long, Boolean) -> Unit,
    onTax: (Long, Boolean) -> Unit,
    onAdminFee: (Long, Boolean) -> Unit,
    onDeliveryFee: (Long) -> Unit,
    onTip: (Long) -> Unit,
    onVoucherCode: (String) -> Unit,
    onScanClick: () -> Unit,
    modifierCache: ImmutableMap<String, ImmutableList<id.rancak.app.domain.model.Modifier>> = persistentMapOf(),
    onLoadModifiers: (String) -> Unit = {}
) {
    val immutableCategories = uiState.categories

    Row(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.58f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            PosTopBar(
                outletName   = outletName,
                hasCart      = false,
                itemCount    = 0,
                hasOpenShift = hasOpenShift,
                onMenuClick  = onMenuClick,
                onCartClick  = onCartClick,
                onOpenBillClick = onOpenBillClick,
                showCart     = false
            )
            PosSearchBar(
                query         = uiState.searchQuery,
                onQueryChange = onSearchChange,
                onScanClick   = onScanClick,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
            PosCategoryRow(immutableCategories, uiState.selectedCategory, onCategorySelect)
            ProductGridContent(
                uiState    = uiState,
                cartQtyMap = cartQtyMap,
                bottomPad  = 8.dp,
                onRefresh  = onRefresh,
                onAdd      = onAdd,
                minCellDp  = 120
            )
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))

        OrderPanel(
            cartState         = cartState,
            hasOpenShift      = hasOpenShift,
            onUpdateQty       = onUpdateQty,
            onUpdateNote      = onUpdateNote,
            modifierCache     = modifierCache,
            onLoadModifiers   = onLoadModifiers,
            onClearCart       = onClearCart,
            onOrderType       = onOrderType,
            onCustomerName    = onCustomerName,
            onPax             = onPax,
            onDiscount        = onDiscount,
            onTax             = onTax,
            onAdminFee        = onAdminFee,
            onDeliveryFee     = onDeliveryFee,
            onTip             = onTip,
            onVoucherCode     = onVoucherCode,
            onSaveClick       = onSaveClick,
            onCheckoutClick   = onCheckoutClick,
            modifier          = Modifier
                .fillMaxHeight()
                .weight(0.42f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews — memanggil PhoneLayout / SplitLayout apa adanya
// ─────────────────────────────────────────────────────────────────────────────

private val previewCategory = Category(
    uuid = "c1", name = "Makanan", description = null
)
private val previewCategoryList = persistentListOf(previewCategory)

private val previewProducts = persistentListOf(
    Product(
        uuid = "p1", sku = "SKU-1", barcode = null, name = "Nasi Goreng",
        description = null, category = previewCategory, price = 25_000,
        stock = 12.0, unit = "porsi", imageUrl = null, isActive = true,
        updatedAt = null
    ),
    Product(
        uuid = "p2", sku = "SKU-2", barcode = null, name = "Mie Goreng",
        description = null, category = previewCategory, price = 22_000,
        stock = 8.0, unit = "porsi", imageUrl = null, isActive = true,
        updatedAt = null
    ),
    Product(
        uuid = "p3", sku = "SKU-3", barcode = null, name = "Es Teh",
        description = null, category = previewCategory, price = 5_000,
        stock = 50.0, unit = "gelas", imageUrl = null, isActive = true,
        updatedAt = null
    )
)

@androidx.compose.ui.tooling.preview.Preview(name = "POS – Phone", widthDp = 390, heightDp = 844)
@Composable
private fun PosScreenPhonePreview() {
    id.rancak.app.presentation.designsystem.RancakTheme {
        PhoneLayout(
            uiState          = PosUiState(
                products = previewProducts,
                categories = previewCategoryList
            ),
            cartState        = CartUiState(),
            cartQtyMap       = persistentMapOf(),
            outletName       = "Warung Rancak",
            hasOpenShift     = true,
            onMenuClick      = {},
            onCartClick      = {},
            onSearchChange   = {},
            onCategorySelect = {},
            onRefresh        = {},
            onAdd            = {},
            onScanClick      = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "POS – Tablet", widthDp = 1024, heightDp = 768)
@Composable
private fun PosScreenTabletPreview() {
    id.rancak.app.presentation.designsystem.RancakTheme {
        SplitLayout(
            uiState          = PosUiState(
                products = previewProducts,
                categories = previewCategoryList
            ),
            cartState        = CartUiState(),
            cartQtyMap       = persistentMapOf(),
            outletName       = "Warung Rancak",
            hasOpenShift     = true,
            onMenuClick      = {},
            onCartClick      = {},
            onCheckoutClick  = {},
            onSaveClick      = {},
            onSearchChange   = {},
            onCategorySelect = {},
            onRefresh        = {},
            onAdd            = {},
            onUpdateQty      = { _, _ -> },
            onUpdateNote     = { _, _ -> },
            onClearCart      = {},
            onOrderType      = {},
            onCustomerName   = {},
            onPax            = {},
            onDiscount       = { _, _ -> },
            onTax            = { _, _ -> },
            onAdminFee       = { _, _ -> },
            onDeliveryFee    = {},
            onTip            = {},
            onVoucherCode    = {},
            onScanClick      = {}
        )
    }
}
