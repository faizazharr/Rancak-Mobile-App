package id.rancak.app.presentation.ui.pos

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.Category
import id.rancak.app.domain.model.OrderType
import id.rancak.app.domain.model.Product
import id.rancak.app.domain.repository.AuthRepository
import id.rancak.app.domain.repository.CartItem
import id.rancak.app.presentation.barcode.BarcodeScannerView
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartUiState
import id.rancak.app.presentation.viewmodel.CartViewModel
import id.rancak.app.presentation.viewmodel.PosUiState
import id.rancak.app.presentation.viewmodel.PosViewModel
import kotlin.math.absoluteValue
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Accent palette (untuk avatar & category chip)
// ─────────────────────────────────────────────────────────────────────────────
private val ACCENT_PALETTE = listOf(
    Color(0xFFFF6B35), Color(0xFF06D6A0), Color(0xFF118AB2),
    Color(0xFFEF476F), Color(0xFF7B5EA7), Color(0xFFF4A261),
    Color(0xFF2EC4B6), Color(0xFFE76F51)
)

private fun accentFor(key: String): Color =
    ACCENT_PALETTE[key.hashCode().absoluteValue % ACCENT_PALETTE.size]

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PosScreen(
    onCartClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onMenuClick: () -> Unit,
    onSaveClick: () -> Unit = {},
    posViewModel: PosViewModel = koinViewModel(),
    cartViewModel: CartViewModel
) {
    val uiState   by posViewModel.uiState.collectAsState()
    val cartState by cartViewModel.uiState.collectAsState()
    val authRepo: AuthRepository = koinInject()
    val outletName = remember { authRepo.getCurrentTenantName() ?: "" }
    var showScanner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        posViewModel.loadProducts()
        posViewModel.loadCategories()
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

// ─────────────────────────────────────────────────────────────────────────────
// PHONE / PORTRAIT  ─  AppBar + product grid + floating cart bar
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PhoneLayout(
    uiState: PosUiState,
    cartState: CartUiState,
    cartQtyMap: Map<String, Int>,
    outletName: String,
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
                outletName = outletName,
                hasCart    = hasCart,
                itemCount  = cartState.itemCount,
                onMenuClick = onMenuClick,
                onCartClick = onCartClick,
                showCart    = true
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

            // Floating cart bar
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

// ─────────────────────────────────────────────────────────────────────────────
// TABLET / LANDSCAPE  ─  left: product grid | right: order panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SplitLayout(
    uiState: PosUiState,
    cartState: CartUiState,
    cartQtyMap: Map<String, Int>,
    outletName: String,
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

        // ── Kiri: panel produk ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.58f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            PosTopBar(
                outletName  = outletName,
                hasCart     = false,
                itemCount   = 0,
                onMenuClick = onMenuClick,
                onCartClick = {},
                showCart    = false
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

        // ── Kanan: panel order ───────────────────────────────────────────────
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
            onCheckoutClick = onCheckoutClick,
            modifier        = Modifier
                .fillMaxHeight()
                .weight(0.42f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top bar  ─  solid primary, clock, shift status
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PosTopBar(
    outletName: String,
    hasCart: Boolean,
    itemCount: Int,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    showCart: Boolean = true
) {
    val primary = MaterialTheme.colorScheme.primary

    // Real-time clock  HH:MM
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val h = now.hour.toString().padStart(2, '0')
            val m = now.minute.toString().padStart(2, '0')
            currentTime = "$h:$m"
            delay(30_000L)
        }
    }

    TopAppBar(
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Kasir",
                        style     = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color     = Color.White
                    )
                    if (outletName.isNotBlank()) {
                        Box(
                            Modifier
                                .size(3.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.45f))
                        )
                        Text(
                            outletName,
                            style    = MaterialTheme.typography.bodySmall,
                            color    = Color.White.copy(0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
            }
        },
        actions = {
            // Shift status chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(0.16f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4ADE80))
                    )
                    Text(
                        "Shift Buka",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Clock
            if (currentTime.isNotEmpty()) {
                Text(
                    currentTime,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }

            // Cart icon (phone only)
            if (showCart) {
                BadgedBox(
                    badge = {
                        if (hasCart) Badge(
                            containerColor = Color.White,
                            contentColor   = primary
                        ) {
                            Text(
                                "$itemCount",
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                ) {
                    IconButton(onClick = onCartClick) {
                        Icon(Icons.Default.ShoppingCart, null, tint = Color.White)
                    }
                }
            } else {
                Spacer(Modifier.width(4.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = primary
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Search bar  ─  clean, light
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PosSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary          = MaterialTheme.colorScheme.primary

    BasicTextField(
        value         = query,
        onValueChange = onQueryChange,
        singleLine    = true,
        textStyle     = MaterialTheme.typography.bodySmall.copy(
            color    = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        ),
        cursorBrush = SolidColor(primary),
        modifier    = modifier,
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Search, null,
                    Modifier.size(18.dp),
                    tint = onSurfaceVariant
                )
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            "Cari produk atau scan barcode...",
                            style    = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp,
                            color    = onSurfaceVariant.copy(0.55f)
                        )
                    }
                    inner()
                }
                if (query.isNotEmpty()) {
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(0.35f))
                            .clickable { onQueryChange("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(11.dp), tint = onSurfaceVariant)
                    }
                } else {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(primary.copy(0.1f))
                            .clickable(onClick = onScanClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner, null,
                            Modifier.size(16.dp),
                            tint = primary
                        )
                    }
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Category row + chip  ─  clean Material3 style
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PosCategoryRow(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category?) -> Unit
) {
    if (categories.isEmpty()) return
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier              = Modifier.padding(bottom = 8.dp)
    ) {
        item { PosChip("Semua", selected == null) { onSelect(null) } }
        items(categories) { cat ->
            PosChip(cat.name, selected == cat) { onSelect(cat) }
        }
    }
}

@Composable
private fun PosChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (isSelected) primary
                else MaterialTheme.colorScheme.surface
            )
            .border(
                1.dp,
                if (isSelected) Color.Transparent
                else MaterialTheme.colorScheme.outlineVariant,
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color      = if (isSelected) Color.White
                         else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Product grid content  ─  shared phone & tablet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductGridContent(
    uiState: PosUiState,
    cartQtyMap: Map<String, Int>,
    bottomPad: Dp,
    onRefresh: () -> Unit,
    onAdd: (Product) -> Unit,
    minCellDp: Int = 110
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    when {
        uiState.isLoading -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(strokeWidth = 2.dp)
        }

        uiState.error != null -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Default.WifiOff, null,
                    Modifier.size(44.dp),
                    tint = onSurfaceVariant.copy(0.4f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    uiState.error,
                    color     = onSurfaceVariant.copy(0.65f),
                    style     = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onRefresh) { Text("Coba Lagi") }
            }
        }

        uiState.filteredProducts.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.SearchOff, null,
                    Modifier.size(44.dp),
                    tint = onSurfaceVariant.copy(0.3f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Produk tidak ditemukan",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant.copy(0.5f)
                )
            }
        }

        else -> LazyVerticalGrid(
            columns               = GridCells.Adaptive(minSize = minCellDp.dp),
            contentPadding        = PaddingValues(
                start  = 8.dp, end = 8.dp,
                top    = 4.dp, bottom = bottomPad
            ),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement   = Arrangement.spacedBy(7.dp)
        ) {
            items(uiState.filteredProducts, key = { it.uuid }) { product ->
                val qty = cartQtyMap[product.uuid] ?: 0
                PosProductCard(
                    product = product,
                    qty     = qty,
                    onAdd   = { onAdd(product) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Product card  ─  clean white, no glassmorphism
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PosProductCard(
    product: Product,
    qty: Int,
    onAdd: () -> Unit
) {
    val inCart           = qty > 0
    val accent           = accentFor(product.category?.name ?: product.name)
    val primary          = MaterialTheme.colorScheme.primary
    val onSurface        = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant   = MaterialTheme.colorScheme.surfaceVariant

    val cardBg = when {
        !product.isActive -> surfaceVariant.copy(0.35f)
        inCart            -> accent.copy(0.05f)
        else              -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        !product.isActive -> MaterialTheme.colorScheme.outlineVariant.copy(0.3f)
        inCart            -> accent.copy(0.65f)
        else              -> MaterialTheme.colorScheme.outlineVariant.copy(0.55f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(
                width  = if (inCart) 2.dp else 1.dp,
                color  = borderColor,
                shape  = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = product.isActive, onClick = onAdd)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {

            // ── Nama menu (dominan) + qty badge ─────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Text(
                    product.name,
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 13.sp,
                    color      = if (product.isActive) onSurface else onSurfaceVariant.copy(0.4f),
                    maxLines   = 3,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 17.sp,
                    modifier   = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                )

                Spacer(Modifier.width(4.dp))

                AnimatedVisibility(
                    visible = inCart,
                    enter   = scaleIn(tween(150)) + fadeIn(tween(150)),
                    exit    = scaleOut(tween(110)) + fadeOut(tween(110))
                ) {
                    Box(
                        Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(accent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$qty",
                            style      = MaterialTheme.typography.labelSmall,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Color.White
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Harga (kecil) + kontrol qty ─────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    formatRupiah(product.price),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 11.sp,
                    color      = if (product.isActive) accent.copy(0.85f)
                                 else onSurfaceVariant.copy(0.3f),
                    maxLines   = 1
                )

                // Produk tidak aktif → badge "Habis"
                if (!product.isActive) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "Habis",
                            style    = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color    = onSurfaceVariant.copy(0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardQtyButton(
    icon: ImageVector,
    bg: Color,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.4f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, Modifier.size(12.dp), tint = iconTint)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Floating cart bar (phone layout)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CartBar(
    cartState: CartUiState,
    primary: Color,
    onCartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(primary)
            .clickable(onClick = onCartClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${cartState.itemCount}",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                }
                Column {
                    Text(
                        "${cartState.itemCount} item",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = Color.White.copy(0.72f),
                        lineHeight = 14.sp
                    )
                    Text(
                        formatRupiah(cartState.subtotal),
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                }
            }
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    "Keranjang",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward, null,
                    Modifier.size(16.dp),
                    tint = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Order panel (tablet / landscape, kanan)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OrderPanel(
    cartState: CartUiState,
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
    onSaveClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary          = MaterialTheme.colorScheme.primary
    val surface          = MaterialTheme.colorScheme.surface
    val onSurface        = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val hasItems         = cartState.items.isNotEmpty()

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.25f))
            .systemBarsPadding()
    ) {

        // ── Header ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surface)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Receipt, null, Modifier.size(18.dp), tint = primary)
                Text(
                    "Pesanan",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = onSurface
                )
                if (hasItems) {
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .background(primary)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${cartState.itemCount}",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                    }
                }
            }
            if (hasItems) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable(onClick = onClearCart)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Hapus Semua",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))

        // ── Customer name + Pax ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            // Customer name field
            BasicTextField(
                value         = cartState.customerName,
                onValueChange = onCustomerName,
                singleLine    = true,
                textStyle     = MaterialTheme.typography.bodySmall.copy(
                    color    = onSurface,
                    fontSize = 13.sp
                ),
                cursorBrush = SolidColor(primary),
                modifier    = Modifier.weight(1f),
                decorationBox = { inner ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Person, null,
                            Modifier.size(14.dp),
                            tint = onSurfaceVariant
                        )
                        Box(Modifier.weight(1f)) {
                            if (cartState.customerName.isEmpty()) {
                                Text(
                                    "Nama customer",
                                    style    = MaterialTheme.typography.bodySmall,
                                    fontSize = 13.sp,
                                    color    = onSurfaceVariant.copy(0.5f)
                                )
                            }
                            inner()
                        }
                    }
                }
            )

            // Pax counter
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(
                            if (cartState.pax > 1)
                                MaterialTheme.colorScheme.outlineVariant.copy(0.35f)
                            else Color.Transparent
                        )
                        .clickable(enabled = cartState.pax > 1) { onPax(cartState.pax - 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Remove, null,
                        Modifier.size(12.dp),
                        tint = if (cartState.pax > 1) onSurface else onSurfaceVariant.copy(0.3f)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${cartState.pax}",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = onSurface,
                        modifier   = Modifier.widthIn(min = 18.dp),
                        textAlign  = TextAlign.Center
                    )
                    Text(
                        "tamu",
                        style    = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color    = onSurfaceVariant.copy(0.6f)
                    )
                }
                Box(
                    Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(primary)
                        .clickable { onPax(cartState.pax + 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add, null,
                        Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))

        // ── Order type selector ─────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surface)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OrderType.entries.forEach { type ->
                val isSelected = cartState.orderType == type
                val (icon, label) = when (type) {
                    OrderType.DINE_IN  -> Icons.Default.Restaurant to "Dine In"
                    OrderType.TAKEAWAY -> Icons.Default.ShoppingBag to "Takeaway"
                    OrderType.DELIVERY -> Icons.Default.DeliveryDining to "Delivery"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) primary else surface)
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent
                            else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onOrderType(type) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            icon, null,
                            Modifier.size(16.dp),
                            tint = if (isSelected) Color.White else onSurfaceVariant
                        )
                        Text(
                            label,
                            style      = MaterialTheme.typography.labelSmall,
                            fontSize   = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color      = if (isSelected) Color.White else onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))

        // ── Cart items list ─────────────────────────────────────────────────
        if (!hasItems) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCartCheckout, null,
                        Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        "Belum ada pesanan",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                    Text(
                        "Tap produk untuk menambahkan",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant.copy(0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier       = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(
                    cartState.items,
                    key = { "${it.productUuid}_${it.variantUuid}" }
                ) { item ->
                    OrderItemRow(
                        item       = item,
                        primary    = primary,
                        onIncrease = { onUpdateQty(item, item.qty + 1) },
                        onDecrease = { onUpdateQty(item, item.qty - 1) },
                        onSetQty   = { onUpdateQty(item, it) },
                        onSetNote  = { onUpdateNote(item, it) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color    = MaterialTheme.colorScheme.outlineVariant.copy(0.3f)
                    )
                }
            }
        }

        // ── Summary + action buttons ────────────────────────────────────────
        Surface(shadowElevation = 8.dp, color = surface) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Subtotal
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Subtotal",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                    Text(
                        formatRupiah(cartState.subtotal),
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = onSurface
                    )
                }

                Spacer(Modifier.height(6.dp))

                // ── Komponen biaya ───────────────────────────────────────
                FeeInputRow(
                    label      = "Diskon",
                    icon       = Icons.Default.Discount,
                    value      = cartState.discount,
                    onValue    = onDiscount,
                    isNegative = true
                )
                FeeInputRow(
                    label   = "Pajak",
                    icon    = Icons.Default.AccountBalance,
                    value   = cartState.tax,
                    onValue = onTax
                )
                FeeInputRow(
                    label   = "Biaya Admin",
                    icon    = Icons.Default.Receipt,
                    value   = cartState.adminFee,
                    onValue = onAdminFee
                )
                if (cartState.orderType == OrderType.DELIVERY) {
                    FeeInputRow(
                        label   = "Ongkir",
                        icon    = Icons.Default.DeliveryDining,
                        value   = cartState.deliveryFee,
                        onValue = onDeliveryFee
                    )
                }
                FeeInputRow(
                    label   = "Tip",
                    icon    = Icons.Default.Favorite,
                    value   = cartState.tip,
                    onValue = onTip
                )

                Spacer(Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
                Spacer(Modifier.height(8.dp))

                // Total
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "TOTAL",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = onSurface
                    )
                    Text(
                        formatRupiah(cartState.total),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = primary
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Simpan + Bayar
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Simpan (outline)
                    Box(
                        modifier = Modifier
                            .weight(0.38f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(surface)
                            .border(
                                1.5.dp,
                                if (hasItems) primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = hasItems, onClick = onSaveClick)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Default.BookmarkBorder, null,
                                Modifier.size(15.dp),
                                tint = if (hasItems) primary else onSurfaceVariant.copy(0.4f)
                            )
                            Text(
                                "Simpan",
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color      = if (hasItems) primary else onSurfaceVariant.copy(0.4f)
                            )
                        }
                    }

                    // Bayar (filled)
                    Box(
                        modifier = Modifier
                            .weight(0.62f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (hasItems) primary
                                else MaterialTheme.colorScheme.outlineVariant.copy(0.4f)
                            )
                            .clickable(enabled = hasItems, onClick = onCheckoutClick)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                Icons.Default.Payment, null,
                                Modifier.size(16.dp),
                                tint = if (hasItems) Color.White else onSurfaceVariant
                            )
                            Text(
                                if (hasItems) "Bayar ${formatRupiah(cartState.total)}"
                                else "Pilih Produk",
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color      = if (hasItems) Color.White else onSurfaceVariant,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fee input row  ─  compact editable baris biaya
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun FeeInputRow(
    label:      String,
    icon:       ImageVector,
    value:      Long,
    onValue:    (Long) -> Unit,
    isNegative: Boolean = false
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary          = MaterialTheme.colorScheme.primary
    val error            = MaterialTheme.colorScheme.error

    var showDialog by remember { mutableStateOf(false) }

    // Buka dialog numpad saat baris ditekan
    if (showDialog) {
        FeeInputDialog(
            title        = label,
            icon         = icon,
            initialValue = value,
            isNegative   = isNegative,
            onDismiss    = { showDialog = false },
            onConfirm    = { amount ->
                onValue(amount)
                showDialog = false
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
            .padding(vertical = 5.dp, horizontal = 2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Label + ikon kiri
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier              = Modifier.weight(1f)
        ) {
            Icon(icon, null, Modifier.size(12.dp), tint = onSurfaceVariant.copy(0.6f))
            Text(label, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
        }

        // Nilai kanan — tap hint jika kosong
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (value > 0L) {
                if (isNegative) {
                    Text("−", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold, color = error)
                }
                Text(
                    "Rp ${feeFormatNumber(value)}",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = if (isNegative) error else primary
                )
            } else {
                Text(
                    "Ketuk untuk isi",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant.copy(0.35f)
                )
            }
            Icon(
                Icons.Default.ChevronRight, null,
                Modifier.size(12.dp),
                tint = onSurfaceVariant.copy(0.30f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Order item row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun OrderItemRow(
    item: CartItem,
    primary: Color,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onSetQty: (Int) -> Unit,
    onSetNote: (String) -> Unit
) {
    val accent           = accentFor(item.productName)
    val onSurface        = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Dialog: qty numpad
    var showQtyDialog  by remember { mutableStateOf(false) }
    // Dialog: catatan item
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteText       by remember(item.note) { mutableStateOf(item.note ?: "") }

    if (showQtyDialog) {
        FeeInputDialog(
            title        = item.productName,
            icon         = Icons.Default.ShoppingCart,
            initialValue = item.qty.toLong(),
            prefix       = "",          // qty, bukan nominal rupiah
            onDismiss    = { showQtyDialog = false },
            onConfirm    = { qty ->
                onSetQty(qty.toInt().coerceAtLeast(0))
                showQtyDialog = false
            }
        )
    }

    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = {
                Text(
                    "Catatan — ${item.productName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value         = noteText,
                    onValueChange = { noteText = it },
                    label         = { Text("Contoh: gula sedikit, tambah es") },
                    shape         = RoundedCornerShape(12.dp),
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 3
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onSetNote(noteText.trim())
                    showNoteDialog = false
                }) { Text("Simpan", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) { Text("Batal") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(0.14f))
                    .border(1.dp, accent.copy(0.32f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.productName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = accent
                )
            }

            // Nama + harga
            Column(Modifier.weight(1f)) {
                Text(
                    item.productName + (item.variantName?.let { " · $it" } ?: ""),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = onSurface,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    formatRupiah(item.price),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant
                )
            }

            // Qty controls: − | angka (tap → numpad) | +
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmallQtyButton(Icons.Default.Remove, onClick = onDecrease)

                // Tap angka → buka numpad qty
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { showQtyDialog = true }
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${item.qty}",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = primary,
                        textAlign  = TextAlign.Center
                    )
                }

                SmallQtyButton(Icons.Default.Add, tint = primary, onClick = onIncrease)
            }

            // Subtotal + ikon catatan
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    formatRupiah(item.subtotal),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = primary
                )
                // Ikon catatan — tap untuk tambah/edit catatan
                Icon(
                    if (item.note.isNullOrBlank()) Icons.AutoMirrored.Filled.NoteAdd
                    else Icons.Default.Edit,
                    contentDescription = "Catatan",
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { showNoteDialog = true },
                    tint = if (item.note.isNullOrBlank())
                        onSurfaceVariant.copy(0.35f)
                    else primary.copy(0.75f)
                )
            }
        }

        // Catatan — tampil di bawah jika ada
        if (!item.note.isNullOrBlank()) {
            Row(
                modifier              = Modifier.padding(start = 42.dp, top = 3.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Notes, null,
                    Modifier.size(11.dp),
                    tint = primary.copy(0.55f)
                )
                Text(
                    item.note,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = onSurfaceVariant.copy(0.75f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis,
                    fontSize  = 10.sp
                )
            }
        }
    }
}

@Composable
private fun SmallQtyButton(
    icon: ImageVector,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, Modifier.size(13.dp), tint = tint)
    }
}
