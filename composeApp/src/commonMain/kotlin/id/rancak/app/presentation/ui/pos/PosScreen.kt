package id.rancak.app.presentation.ui.pos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.repository.AuthRepository
import id.rancak.app.domain.repository.CartItem
import id.rancak.app.presentation.barcode.BarcodeScannerView
import id.rancak.app.presentation.ui.pos.components.CartBar
import id.rancak.app.presentation.ui.pos.components.OrderPanel
import id.rancak.app.presentation.ui.pos.components.PosCategoryRow
import id.rancak.app.presentation.ui.pos.components.PosSearchBar
import id.rancak.app.presentation.ui.pos.components.PosTopBar
import id.rancak.app.presentation.ui.pos.components.ProductGridContent
import id.rancak.app.presentation.viewmodel.CartUiState
import id.rancak.app.presentation.viewmodel.CartViewModel
import id.rancak.app.presentation.viewmodel.PosUiState
import id.rancak.app.presentation.viewmodel.PosViewModel
import id.rancak.app.presentation.viewmodel.ShiftViewModel
import org.koin.compose.koinInject
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
    onSaveClick: () -> Unit = {},
    posViewModel: PosViewModel = koinViewModel(),
    shiftViewModel: ShiftViewModel = koinViewModel(),
    cartViewModel: CartViewModel
) {
    val uiState    by posViewModel.uiState.collectAsStateWithLifecycle()
    val cartState  by cartViewModel.uiState.collectAsStateWithLifecycle()
    val shiftState by shiftViewModel.uiState.collectAsStateWithLifecycle()
    val authRepo: AuthRepository = koinInject()
    val outletName = remember { authRepo.getCurrentTenantName() ?: "" }
    var showScanner by remember { mutableStateOf(false) }

    val hasOpenShift = shiftState.currentShift != null

    LaunchedEffect(Unit) {
        posViewModel.loadProducts()
        posViewModel.loadCategories()
        shiftViewModel.loadCurrentShift()
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

    val cartQtyMap = remember(cartState.items) {
        cartState.items.associate { it.productUuid to it.qty }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp || maxWidth > maxHeight

        if (isWide) {
            SplitLayout(
                uiState          = uiState,
                cartState        = cartState,
                cartQtyMap       = cartQtyMap,
                outletName       = outletName,
                onMenuClick      = onMenuClick,
                onCartClick      = onCartClick,
                onCheckoutClick  = onCheckoutClick,
                onSaveClick      = onSaveClick,
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
                onDiscount       = { cartViewModel.setDiscount(it) },
                onTax            = { cartViewModel.setTax(it) },
                onAdminFee       = { cartViewModel.setAdminFee(it) },
                onDeliveryFee    = { cartViewModel.setDeliveryFee(it) },
                onTip            = { cartViewModel.setTip(it) },
                onVoucherCode    = { cartViewModel.setVoucherCode(it) },
                onScanClick      = { showScanner = true }
            )
        } else {
            PhoneLayout(
                uiState          = uiState,
                cartState        = cartState,
                cartQtyMap       = cartQtyMap,
                outletName       = outletName,
                hasOpenShift     = hasOpenShift,
                onMenuClick      = onMenuClick,
                onCartClick      = onCartClick,
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
    cartQtyMap: Map<String, Int>,
    outletName: String,
    hasOpenShift: Boolean,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (Category?) -> Unit,
    onRefresh: () -> Unit,
    onAdd: (Product) -> Unit,
    onScanClick: () -> Unit
) {
    val hasCart = cartState.itemCount > 0
    val primary = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            PosTopBar(
                outletName   = outletName,
                hasCart      = hasCart,
                itemCount    = cartState.itemCount,
                hasOpenShift = hasOpenShift,
                onMenuClick  = onMenuClick,
                onCartClick  = onCartClick,
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
                PosCategoryRow(uiState.categories, uiState.selectedCategory, onCategorySelect)
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
                CartBar(cartState = cartState, primary = primary, onCartClick = onCartClick)
            }
        }
    }
}

// ─── Tablet / landscape split layout ────────────────────────────────────────

@Composable
private fun SplitLayout(
    uiState: PosUiState,
    cartState: CartUiState,
    cartQtyMap: Map<String, Int>,
    outletName: String,
    hasOpenShift: Boolean,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onSaveClick: () -> Unit,
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
    onDiscount: (Long) -> Unit,
    onTax: (Long) -> Unit,
    onAdminFee: (Long) -> Unit,
    onDeliveryFee: (Long) -> Unit,
    onTip: (Long) -> Unit,
    onVoucherCode: (String) -> Unit,
    onScanClick: () -> Unit
) {
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
            PosCategoryRow(uiState.categories, uiState.selectedCategory, onCategorySelect)
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
            cartState       = cartState,
            onUpdateQty     = onUpdateQty,
            onUpdateNote    = onUpdateNote,
            onClearCart     = onClearCart,
            onOrderType     = onOrderType,
            onCustomerName  = onCustomerName,
            onPax           = onPax,
            onDiscount      = onDiscount,
            onTax           = onTax,
            onAdminFee      = onAdminFee,
            onDeliveryFee   = onDeliveryFee,
            onTip           = onTip,
            onVoucherCode   = onVoucherCode,
            onSaveClick     = onSaveClick,
            onCheckoutClick = { if (hasOpenShift) onCheckoutClick() },
            modifier        = Modifier
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

private val previewProducts = listOf(
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
            uiState          = PosUiState(products = previewProducts, categories = listOf(previewCategory)),
            cartState        = CartUiState(),
            cartQtyMap       = emptyMap(),
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
            uiState          = PosUiState(products = previewProducts, categories = listOf(previewCategory)),
            cartState        = CartUiState(),
            cartQtyMap       = emptyMap(),
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
            onDiscount       = {},
            onTax            = {},
            onAdminFee       = {},
            onDeliveryFee    = {},
            onTip            = {},
            onVoucherCode    = {},
            onScanClick      = {}
        )
    }
}
