package id.rancak.app.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import id.rancak.app.domain.repository.AuthRepository
import id.rancak.app.presentation.ui.auth.LoginScreen
import id.rancak.app.presentation.ui.splash.SplashScreen
import org.koin.compose.koinInject
import id.rancak.app.presentation.ui.auth.TenantPickerScreen
import id.rancak.app.presentation.ui.cart.CartScreen
import id.rancak.app.presentation.ui.finance.CashExpenseScreen
import id.rancak.app.presentation.ui.kds.KdsScreen
import id.rancak.app.presentation.ui.orderboard.OrderBoardScreen
import id.rancak.app.presentation.ui.payment.PayHeldOrderScreen
import id.rancak.app.presentation.ui.payment.PaymentScreen
import id.rancak.app.presentation.ui.pos.PosScreen
import id.rancak.app.presentation.ui.reports.ReportScreen
import id.rancak.app.presentation.ui.sales.SalesHistoryScreen
import id.rancak.app.presentation.ui.sales.AddItemsToHeldOrderScreen
import id.rancak.app.presentation.ui.settings.SettingsScreen
import id.rancak.app.presentation.ui.shift.ShiftScreen
import id.rancak.app.presentation.ui.splitbill.SplitBillScreen
import id.rancak.app.presentation.ui.tables.TableMapScreen
import id.rancak.app.presentation.viewmodel.CartViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private data class DrawerItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RancakNavHost() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()
    val authRepository: AuthRepository = koinInject()

    // TODO(role-gating): setelah backend mengembalikan field `role` pada respons
    // tenant/login, ganti semua UserRole.STAFF di bawah dengan peran yang sesuai
    // dan uncomment baris filter `visibleDrawerItems`.
    val drawerItems = remember {
        listOf(
            DrawerItem("Kasir",       Icons.Default.PointOfSale,    Screen.Pos),
            DrawerItem("Shift",       Icons.Default.AccessTime,     Screen.Shift),
            DrawerItem("Meja",        Icons.Default.TableBar,       Screen.Tables),
            DrawerItem("Dapur (KDS)", Icons.Default.Restaurant,     Screen.Kds),
            DrawerItem("Order Board", Icons.Default.Dashboard,      Screen.OrderBoard),
            DrawerItem("Riwayat",     Icons.Default.Receipt,        Screen.SalesHistory),
            DrawerItem("Kas & Biaya", Icons.Default.AccountBalance, Screen.CashExpense),
            DrawerItem("Laporan",     Icons.Default.BarChart,       Screen.Reports),
            DrawerItem("Pengaturan",  Icons.Default.Settings,       Screen.Settings),
        )
    }
    // Role filtering dinonaktifkan sementara — semua item ditampilkan.
    val visibleDrawerItems = drawerItems

    val showDrawer = remember(navBackStackEntry) {
        val route = navBackStackEntry?.destination?.route
        route != null &&
            !route.contains("Splash") &&
            !route.contains("Login") &&
            !route.contains("TenantPicker")
    }

    // Pastikan drawer selalu tertutup saat baru masuk ke layar utama (misal setelah pilih outlet)
    LaunchedEffect(showDrawer) {
        if (showDrawer) drawerState.snapTo(DrawerValue.Closed)
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

                    visibleDrawerItems.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                            selected = false,
                            onClick = {
                                navController.navigate(item.screen) {
                                    launchSingleTop = true
                                }
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(4.dp))

                    // ── Ganti Outlet ───────────────────────────────────────
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Store, contentDescription = null) },
                        label = { Text("Ganti Outlet") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.TenantPicker) {
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

    NavHost(
        navController = navController,
        startDestination = Screen.Splash
    ) {
        composable<Screen.Splash> {
            SplashScreen(
                onNavigate = { destination ->
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.TenantPicker) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.TenantPicker> {
            TenantPickerScreen(
                onTenantSelected = {
                    navController.navigate(Screen.Pos) {
                        popUpTo(Screen.TenantPicker) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Pos> {
            PosScreen(
                onCartClick = { navController.navigate(Screen.Cart) },
                onCheckoutClick = {
                    navController.navigate(Screen.Payment) {
                        launchSingleTop = true
                    }
                },
                onMenuClick = onMenuClick,
                onSaveClick = {
                    // TODO: implement hold/save order when backend ready
                },
                cartViewModel = cartViewModel
            )
        }

        composable<Screen.Cart> {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckout = { navController.navigate(Screen.Payment) },
                cartViewModel = cartViewModel
            )
        }

        composable<Screen.Payment> {
            PaymentScreen(
                onBack = { navController.popBackStack() },
                onPaymentComplete = {
                    navController.navigate(Screen.Pos) {
                        popUpTo(Screen.Pos) { inclusive = true }
                    }
                },
                cartViewModel = cartViewModel
            )
        }

        composable<Screen.Shift> {
            ShiftScreen(onBack = onMenuClick)
        }

        composable<Screen.Tables> {
            TableMapScreen(onBack = onMenuClick)
        }

        composable<Screen.Kds> {
            KdsScreen(onBack = onMenuClick)
        }

        composable<Screen.OrderBoard> {
            OrderBoardScreen(onBack = onMenuClick)
        }

        composable<Screen.SalesHistory> {
            SalesHistoryScreen(
                onBack = onMenuClick,
                onPayHeldOrder = { saleUuid -> navController.navigate(Screen.PayHeldOrder(saleUuid)) },
                onSplitBill    = { saleUuid -> navController.navigate(Screen.SplitBill(saleUuid)) },
                onAddItems     = { saleUuid -> navController.navigate(Screen.AddItemsToHeldOrder(saleUuid)) }
            )
        }

        composable<Screen.PayHeldOrder> { backStackEntry ->
            val route: Screen.PayHeldOrder = backStackEntry.toRoute()
            PayHeldOrderScreen(
                saleUuid = route.saleUuid,
                onBack   = { navController.popBackStack() },
                onPaymentComplete = {
                    navController.popBackStack(Screen.SalesHistory, inclusive = false)
                }
            )
        }

        composable<Screen.SplitBill> { backStackEntry ->
            val route: Screen.SplitBill = backStackEntry.toRoute()
            SplitBillScreen(
                saleUuid = route.saleUuid,
                onBack   = { navController.popBackStack() },
                onSplitComplete = { _, _ ->
                    navController.popBackStack(Screen.SalesHistory, inclusive = false)
                }
            )
        }

        composable<Screen.AddItemsToHeldOrder> { backStackEntry ->
            val route: Screen.AddItemsToHeldOrder = backStackEntry.toRoute()
            AddItemsToHeldOrderScreen(
                saleUuid  = route.saleUuid,
                onBack    = { navController.popBackStack() },
                onSuccess = {
                    navController.popBackStack(Screen.SalesHistory, inclusive = false)
                }
            )
        }

        // TODO(role-gating): wrap dengan RoleGatedScreen(UserRole.OWNER) setelah
        // backend menyediakan field `role` di respons tenant/login.
        composable<Screen.Reports> {
            ReportScreen(onBack = onMenuClick)
        }

        composable<Screen.CashExpense> {
            CashExpenseScreen(onBack = onMenuClick)
        }

        composable<Screen.Settings> {
            SettingsScreen(onBack = onMenuClick)
        }
    }
}
