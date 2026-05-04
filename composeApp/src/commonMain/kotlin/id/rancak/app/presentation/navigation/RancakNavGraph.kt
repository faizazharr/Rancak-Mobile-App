package id.rancak.app.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import id.rancak.app.data.local.LocalOpenBill
import id.rancak.app.presentation.ui.auth.LoginScreen
import id.rancak.app.presentation.ui.auth.TenantPickerScreen
import id.rancak.app.presentation.ui.billing.BillingScreen
import id.rancak.app.presentation.ui.cart.CartScreen
import id.rancak.app.presentation.ui.finance.CashExpenseScreen
import id.rancak.app.presentation.ui.kds.KdsScreen
import id.rancak.app.presentation.ui.modifiers.ModifierManagementScreen
import id.rancak.app.presentation.ui.openbill.OpenBillListScreen
import id.rancak.app.presentation.ui.orderboard.OrderBoardScreen
import id.rancak.app.presentation.ui.payment.PayHeldOrderScreen
import id.rancak.app.presentation.ui.payment.PaymentScreen
import id.rancak.app.presentation.ui.pos.PosScreen
import id.rancak.app.presentation.ui.pricing.PricingManagementScreen
import id.rancak.app.presentation.ui.inventory.PurchaseOrderScreen
import id.rancak.app.presentation.ui.reports.ReportScreen
import id.rancak.app.presentation.ui.reservations.ReservationScreen
import id.rancak.app.presentation.ui.sales.AddItemsToHeldOrderScreen
import id.rancak.app.presentation.ui.sales.SalesHistoryScreen
import id.rancak.app.presentation.ui.settings.SettingsScreen
import id.rancak.app.presentation.ui.shift.ShiftScreen
import id.rancak.app.presentation.ui.splitbill.SplitBillScreen
import id.rancak.app.presentation.ui.inventory.StockOpnameScreen
import id.rancak.app.presentation.ui.inventory.SupplierScreen
import id.rancak.app.presentation.ui.tables.TableMapScreen
import id.rancak.app.presentation.ui.pricing.VoucherManagementScreen
import id.rancak.app.presentation.ui.products.ProductManagementScreen
import id.rancak.app.presentation.ui.splash.SplashScreen
import id.rancak.app.presentation.viewmodel.CartViewModel

// ─────────────────────────────────────────────────────────────────────────────
// Auth graph — Splash, Login, TenantPicker
// ─────────────────────────────────────────────────────────────────────────────

internal fun NavGraphBuilder.authGraph(navController: NavHostController) {

    composable<Screen.Splash> {
        SplashScreen(
            onNavigate = { destination ->
                // Direct routing: Splash → destination (Login, TenantPicker, atau Pos).
                // Splash langsung dihapus dari back stack — tombol Back keluar dari app.
                navController.navigate(destination) {
                    popUpTo(Screen.Splash) { inclusive = true }
                }
            }
        )
    }

    composable<Screen.Login> {
        LoginScreen(
            onLoginSuccess = {
                // Direct A→C: Login → TenantPicker, Login dihapus dari back stack.
                // User tidak bisa kembali ke Login dengan tombol Back.
                navController.navigate(Screen.TenantPicker()) {
                    popUpTo(Screen.Login) { inclusive = true }
                }
            }
        )
    }

    composable<Screen.TenantPicker> { entry ->
        val route = entry.toRoute<Screen.TenantPicker>()
        TenantPickerScreen(
            switchMode       = route.switchMode,
            onTenantSelected = {
                // Direct routing: seluruh back stack dibersihkan → langsung ke Pos.
                // Back stack setelah navigasi: [Pos] — tidak ada intermediate screen.
                navController.navigate(Screen.Pos) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onLoggedOut = {
                navController.navigate(Screen.Login) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToBilling = {
                // "Detail push": back arrow kembali ke TenantPicker untuk retry.
                // Stack: [TenantPicker, Billing] — intentional agar user bisa ulang pilih.
                navController.navigate(Screen.Billing(fromSetup = true))
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Kasir graph — POS, Cart, Payment, Open Bills
// ─────────────────────────────────────────────────────────────────────────────

internal fun NavGraphBuilder.kasirGraph(
    navController: NavHostController,
    cartViewModel: CartViewModel,
    onMenuClick: () -> Unit
) {
    composable<Screen.Pos> {
        PosScreen(
            onCartClick     = { navController.navigate(Screen.Cart) },
            onCheckoutClick = {
                navController.navigate(Screen.Payment) { launchSingleTop = true }
            },
            onMenuClick     = onMenuClick,
            onHoldSuccess   = { navController.navigate(Screen.OpenBillList()) },
            onOpenBillClick = { navController.navigate(Screen.OpenBillList()) },
            cartViewModel   = cartViewModel
        )
    }

    composable<Screen.Cart> {
        CartScreen(
            onBack        = { navController.popBackStack() },
            onCheckout    = { navController.navigate(Screen.Payment) },
            cartViewModel = cartViewModel
        )
    }

    composable<Screen.Payment> {
        PaymentScreen(
            onBack = { navController.popBackStack() },
            onPaymentComplete = {
                // Direct routing: pembayaran selesai → Pos, menghapus Cart + Payment.
                // Back stack setelah navigasi: [Pos] — tidak ada looping.
                navController.navigate(Screen.Pos) {
                    popUpTo(Screen.Pos) { inclusive = true }
                }
            },
            cartViewModel = cartViewModel
        )
    }

    composable<Screen.OpenBillList> {
        OpenBillListScreen(
            onBack   = { navController.popBackStack() },
            onResume = { bill: LocalOpenBill ->
                cartViewModel.loadOpenBill(bill)
                // Pop OpenBillList, pulihkan Pos di bawahnya tanpa menduplikasi entry-nya.
                navController.navigate(Screen.Pos) {
                    popUpTo(Screen.Pos) { inclusive = false }
                    launchSingleTop = true
                }
            },
            onPayHeldOrder = { saleUuid ->
                navController.navigate(Screen.PayHeldOrder(saleUuid))
            },
            onAddItems = { saleUuid ->
                navController.navigate(Screen.AddItemsToHeldOrder(saleUuid))
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Operations graph — Shift, Tables, Reservations, KDS, Order Board
// ─────────────────────────────────────────────────────────────────────────────

internal fun NavGraphBuilder.operationsGraph(
    navController: NavHostController,
    onMenuClick: () -> Unit
) {
    composable<Screen.Shift>        { ShiftScreen(onBack = onMenuClick) }
    composable<Screen.Tables>       { TableMapScreen(onBack = onMenuClick) }
    composable<Screen.Reservations> { ReservationScreen(onBack = onMenuClick) }
    composable<Screen.Kds>          { KdsScreen(onBack = onMenuClick) }
    composable<Screen.OrderBoard>   { OrderBoardScreen(onBack = onMenuClick) }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sales graph — History, PayHeldOrder, SplitBill, AddItems
// ─────────────────────────────────────────────────────────────────────────────

internal fun NavGraphBuilder.salesGraph(
    navController: NavHostController,
    onMenuClick: () -> Unit
) {
    composable<Screen.SalesHistory> {
        SalesHistoryScreen(
            onBack         = onMenuClick,
            onPayHeldOrder = { saleUuid -> navController.navigate(Screen.PayHeldOrder(saleUuid)) },
            onSplitBill    = { saleUuid -> navController.navigate(Screen.SplitBill(saleUuid)) },
            onAddItems     = { saleUuid -> navController.navigate(Screen.AddItemsToHeldOrder(saleUuid)) }
        )
    }

    composable<Screen.PayHeldOrder> { entry ->
        val route = entry.toRoute<Screen.PayHeldOrder>()
        PayHeldOrderScreen(
            saleUuid          = route.saleUuid,
            onBack            = { navController.popBackStack() },
            // Direct routing: pembayaran selesai → pop semua detail, kembali ke SalesHistory.
            onPaymentComplete = { navController.popBackStack(Screen.SalesHistory, inclusive = false) }
        )
    }

    composable<Screen.SplitBill> { entry ->
        val route = entry.toRoute<Screen.SplitBill>()
        SplitBillScreen(
            saleUuid        = route.saleUuid,
            onBack          = { navController.popBackStack() },
            onSplitComplete = { _, _ ->
                navController.popBackStack(Screen.SalesHistory, inclusive = false)
            }
        )
    }

    composable<Screen.AddItemsToHeldOrder> { entry ->
        val route = entry.toRoute<Screen.AddItemsToHeldOrder>()
        AddItemsToHeldOrderScreen(
            saleUuid  = route.saleUuid,
            onBack    = { navController.popBackStack() },
            onSuccess = { navController.popBackStack(Screen.SalesHistory, inclusive = false) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Finance graph — Cash Expense, Reports
// ─────────────────────────────────────────────────────────────────────────────

internal fun NavGraphBuilder.financeGraph(onMenuClick: () -> Unit) {
    composable<Screen.CashExpense> { CashExpenseScreen(onBack = onMenuClick) }
    composable<Screen.Reports>     { ReportScreen(onBack = onMenuClick) }
}

// ─────────────────────────────────────────────────────────────────────────────
// Management graph — Products, Stock, Vouchers, Pricing, Modifiers,
//                    Suppliers, Purchase Orders, Billing, Settings
// ─────────────────────────────────────────────────────────────────────────────

internal fun NavGraphBuilder.managementGraph(
    navController: NavHostController,
    onMenuClick: () -> Unit
) {
    composable<Screen.ProductManagement>  { ProductManagementScreen(onBack = onMenuClick) }
    composable<Screen.StockOpname>        { StockOpnameScreen(onBack = onMenuClick) }
    composable<Screen.VoucherManagement>  { VoucherManagementScreen(onBack = onMenuClick) }
    composable<Screen.PricingManagement>  { PricingManagementScreen(onBack = onMenuClick) }
    composable<Screen.ModifierManagement> { ModifierManagementScreen(onBack = onMenuClick) }
    composable<Screen.SupplierManagement> { SupplierScreen(onBack = onMenuClick) }
    composable<Screen.PurchaseOrders>     { PurchaseOrderScreen(onBack = onMenuClick) }
    composable<Screen.Settings>           { SettingsScreen(onBack = onMenuClick) }

    // TODO(role-gating): wrap dengan RoleGatedScreen(UserRole.OWNER) setelah
    // backend menyediakan field `role` di respons tenant/login.
    composable<Screen.Billing> { entry ->
        val route = entry.toRoute<Screen.Billing>()
        if (route.fromSetup) {
            // Dibuka dari TenantPicker (fromSetup=true):
            // Back stack: [TenantPicker, Billing].
            // Back arrow → popBackStack() → kembali ke BillingIssueContent di TenantPicker.
            // Pembayaran sukses → bersihkan seluruh stack → POS.
            BillingScreen(
                onNavigateUp      = { navController.popBackStack() },
                onPaymentComplete = {
                    navController.navigate(Screen.Pos) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        } else {
            // Dibuka dari drawer (fromSetup=false):
            // Back stack setelah drawer nav: [Pos, Billing].
            // Screen.Billing dikecualikan dari showDrawer → onMenuClick tidak membuka drawer.
            // Back arrow → popBackStack() → kembali ke Pos.
            // Pembayaran sukses → kembali ke Pos (sudah di stack), tidak perlu navigate baru.
            BillingScreen(
                onBack            = { navController.popBackStack() },
                onPaymentComplete = {
                    navController.popBackStack(Screen.Pos, inclusive = false)
                }
            )
        }
    }
}
