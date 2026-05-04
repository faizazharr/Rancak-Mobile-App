package id.rancak.app.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.AuthRepository
import id.rancak.app.presentation.viewmodel.CartViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

private data class DrawerGroup(
    val label: String,
    val icon: ImageVector,
    val items: List<DrawerItem>,
    val expandedByDefault: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RancakNavHost() {
    val navController = rememberNavController()
    // Pakai `remember` (bukan rememberDrawerState yang saveable) supaya drawer
    // SELALU mulai dalam state Closed setelah proses dibuat ulang. Tanpa ini,
    // drawer bisa muncul terbuka sesaat setelah splash screen karena state
    // dipulihkan dari sesi sebelumnya.
    val drawerState = remember { DrawerState(DrawerValue.Closed) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()
    val authRepository: AuthRepository = koinInject()

    // TODO(role-gating): setelah backend mengembalikan field `role` pada respons
    // tenant/login, ganti semua UserRole.STAFF di bawah dengan peran yang sesuai
    // dan uncomment baris filter `visibleDrawerItems`.
    val drawerGroups = remember {
        listOf(
            DrawerGroup(
                label = "Kasir",
                icon = Icons.Default.PointOfSale,
                expandedByDefault = true,
                items = listOf(
                    DrawerItem("Kasir",  Icons.Default.PointOfSale, Screen.Pos),
                    DrawerItem("Shift",  Icons.Default.AccessTime,  Screen.Shift),
                )
            ),
            DrawerGroup(
                label = "Operasional",
                icon = Icons.Default.TableBar,
                expandedByDefault = true,
                items = listOf(
                    DrawerItem("Meja",        Icons.Default.TableBar,   Screen.Tables),
                    DrawerItem("Reservasi",   Icons.Default.EventSeat,  Screen.Reservations),
                    DrawerItem("Dapur (KDS)", Icons.Default.Restaurant, Screen.Kds),
                    DrawerItem("Order Board", Icons.Default.Dashboard,  Screen.OrderBoard),
                )
            ),
            DrawerGroup(
                label = "Keuangan",
                icon = Icons.Default.AccountBalance,
                expandedByDefault = false,
                items = listOf(
                    DrawerItem("Riwayat",     Icons.Default.Receipt,        Screen.SalesHistory),
                    DrawerItem("Kas & Biaya", Icons.Default.AccountBalance, Screen.CashExpense),
                    DrawerItem("Laporan",     Icons.Default.BarChart,       Screen.Reports),
                )
            ),
            DrawerGroup(
                label = "Manajemen",
                icon = Icons.Default.ManageAccounts,
                expandedByDefault = false,
                items = listOf(
                    DrawerItem("Produk",        Icons.Default.Inventory2,    Screen.ProductManagement),
                    DrawerItem("Stok Opname",   Icons.Default.Inventory,     Screen.StockOpname),
                    DrawerItem("Voucher",        Icons.Default.LocalOffer,    Screen.VoucherManagement),
                    DrawerItem("Harga & Diskon", Icons.Default.Percent,       Screen.PricingManagement),
                    DrawerItem("Modifier",        Icons.Default.Tune,          Screen.ModifierManagement),
                    DrawerItem("Supplier",        Icons.Default.LocalShipping, Screen.SupplierManagement),
                    DrawerItem("Purchase Order",  Icons.Default.ShoppingCart,  Screen.PurchaseOrders),
                    DrawerItem("Billing",        Icons.Default.CreditCard,    Screen.Billing()),  // fromSetup = false (drawer)
                    DrawerItem("Pengaturan",     Icons.Default.Settings,      Screen.Settings),
                )
            ),
        )
    }
    val expandedGroups = remember {
        mutableStateMapOf<String, Boolean>().apply {
            drawerGroups.forEach { put(it.label, it.expandedByDefault) }
        }
    }

    val currentDestination = navBackStackEntry?.destination
    // showDrawer: gunakan hasRoute bertipe aman — tidak bergantung pada string route yang rapuh.
    // Screen.Billing dikecualikan meskipun bukan layar auth, karena:
    //   - fromSetup=true: dibuka dari TenantPicker (flow auth), tidak butuh drawer sama sekali.
    //   - fromSetup=false: dibuka dari drawer; drawer di layar ini redundan dan menyebabkan
    //     white-flash saat transisi dari TenantPicker → Billing.
    val showDrawer = remember(currentDestination) {
        currentDestination != null &&
            !currentDestination.hasRoute(Screen.Splash::class) &&
            !currentDestination.hasRoute(Screen.Login::class) &&
            !currentDestination.hasRoute(Screen.TenantPicker::class) &&
            !currentDestination.hasRoute(Screen.Billing::class)
    }

    // Pastikan drawer selalu tertutup setiap kali pindah destinasi.
    // Hanya jalankan animasi close() bila drawer benar-benar terbuka agar
    // tidak memicu kerja yang tidak perlu di setiap navigasi.
    LaunchedEffect(currentDestination) {
        if (drawerState.isOpen) drawerState.close()
    }

    // ── Always keep NavigationContent in the same composition node ───────────
    // Wrapping in ModalNavigationDrawer unconditionally prevents the NavHost
    // from being destroyed and recreated when showDrawer changes (login → main),
    // which was the root cause of white-screen flashes.
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = showDrawer,
        drawerContent = {
            if (showDrawer) {
                ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                    Spacer(Modifier.height(24.dp))

                    // ── Header: brand + outlet aktif ──────────────────────
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                            Text(
                                "Rancak",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            val activeTenantName = authRepository.getCurrentTenantName()
                            if (activeTenantName != null) {
                                Spacer(Modifier.height(6.dp))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Store,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        activeTenantName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Text(
                                    "Point of Sale",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        drawerGroups.forEach { group ->
                            DrawerAccordionGroup(
                                group = group,
                                isExpanded = expandedGroups[group.label] ?: group.expandedByDefault,
                                onToggle = {
                                    expandedGroups[group.label] =
                                        !(expandedGroups[group.label] ?: group.expandedByDefault)
                                },
                                onItemClick = { item ->
                                    // Navigasi top-level drawer:
                                    //  - popUpTo(Screen.Pos, inclusive=false, saveState=true):
                                    //    Pos adalah root sejati back stack setelah auth selesai.
                                    //    Screen.Splash sudah dihapus dari stack saat auth flow,
                                    //    jadi popUpTo(Splash) sebelumnya adalah no-op.
                                    //    inclusive=false: Pos TETAP ada di stack → Back dari
                                    //    drawer destination selalu kembali ke Pos lalu keluar app.
                                    //  - launchSingleTop: cegah duplikasi destinasi yang aktif.
                                    //  - restoreState: pulihkan state destinasi tujuan jika pernah
                                    //    dikunjungi (scroll position, ViewModel state).
                                    navController.navigate(item.screen) {
                                        popUpTo(Screen.Pos) {
                                            saveState = true
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                        restoreState    = true
                                    }
                                    scope.launch { drawerState.close() }
                                },
                                // Sorot item yang route-nya cocok dengan destinasi aktif saat ini.
                                // hasRoute(KClass<*>) aman untuk data object maupun data class.
                                isSelected = { item ->
                                    currentDestination?.hasRoute(item.screen::class) == true
                                }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(4.dp))

                    // ── Ganti Outlet ───────────────────────────────────────
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Store, contentDescription = null) },
                        label = { Text("Ganti Outlet") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.TenantPicker(switchMode = true)) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    // ── Keluar ─────────────────────────────────────────────
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                        label = { Text("Keluar") },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                authRepository.logout()
                                navController.navigate(Screen.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    ) {
        NavigationContent(navController, onMenuClick = {
            if (showDrawer) scope.launch { drawerState.open() }
        })
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Accordion group composable for the nav drawer
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DrawerAccordionGroup(
    group: DrawerGroup,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onItemClick: (DrawerItem) -> Unit,
    /** Kembalikan true jika item adalah destinasi yang sedang aktif. */
    isSelected: (DrawerItem) -> Boolean = { false }
) {
    // ── Section header ────────────────────────────────────────────────────────
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 20.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            group.icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            group.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Icon(
            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "Tutup" else "Buka",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // ── Items (animated) ──────────────────────────────────────────────────────
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column {
            group.items.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = null) },
                    label = { Text(item.label) },
                    selected = isSelected(item),
                    onClick = { onItemClick(item) },
                    modifier = Modifier.padding(
                        start = 24.dp, end = 12.dp, bottom = 2.dp
                    )
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun NavigationContent(
    navController: NavHostController,
    onMenuClick: () -> Unit
) {
    // CartViewModel is created here (Activity scope) so PosScreen, CartScreen,
    // and PaymentScreen all share the SAME instance — items added in PosScreen
    // are visible in CartScreen and PaymentScreen.
    val cartViewModel: CartViewModel = koinViewModel()
    val authRepository: AuthRepository = koinInject()
    val scope = rememberCoroutineScope()

    // ── Billing guard real-time ───────────────────────────────────────────────
    // Setiap kali Activity di-resume (termasuk saat kembali dari background),
    // periksa status billing tenant aktif. Jika kedaluwarsa / belum aktif,
    // paksa kembali ke TenantPicker — user tidak bisa mengakses layar lain.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    // Gunakan hasRoute (type-safe) — lebih aman dari string contains
                    val currentDest =
                        navController.currentBackStackEntry?.destination ?: return@launch
                    val isExempt = currentDest.hasRoute(Screen.Splash::class) ||
                                   currentDest.hasRoute(Screen.Login::class) ||
                                   currentDest.hasRoute(Screen.TenantPicker::class) ||
                                   currentDest.hasRoute(Screen.Billing::class)
                    if (isExempt) return@launch

                    val storedUuid = authRepository.getCurrentTenantUuid() ?: return@launch
                    val result = authRepository.getMyTenants()
                    if (result is Resource.Success) {
                        val tenant = result.data.find { it.uuid == storedUuid }
                        val status = tenant?.subscriptionStatus?.lowercase()
                        val hasIssue = status == "expired" ||
                                       status == "past_due" ||
                                       status == "inactive"
                        if (hasIssue) {
                            navController.navigate(Screen.TenantPicker()) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Surface dengan warna background tema mencegah “white flash” saat
    // Compose Navigation berpindah destinasi (frame kosong di antara dispose
    // composable lama dan compose pertama composable baru).
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController    = navController,
            startDestination = Screen.Splash,
            modifier         = Modifier.fillMaxSize(),
            // Transisi default seragam: cross-fade halus mencegah “white flash”
            // dan memberi efek mulus saat berpindah destinasi.
            enterTransition    = { fadeIn(animationSpec  = tween(durationMillis = 180)) },
            exitTransition     = { fadeOut(animationSpec = tween(durationMillis = 140)) },
            popEnterTransition = { fadeIn(animationSpec  = tween(durationMillis = 180)) },
            popExitTransition  = { fadeOut(animationSpec = tween(durationMillis = 140)) }
        ) {
            authGraph(navController)
            kasirGraph(navController, cartViewModel, onMenuClick)
            operationsGraph(navController, onMenuClick)
            salesGraph(navController, onMenuClick)
            financeGraph(onMenuClick)
            managementGraph(navController, onMenuClick)
        }
    }
}
