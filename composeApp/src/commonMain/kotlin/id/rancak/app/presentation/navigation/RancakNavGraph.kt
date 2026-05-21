package id.rancak.app.presentation.navigation

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import id.rancak.app.data.local.LocalOpenBill
import id.rancak.app.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import id.rancak.app.presentation.ui.auth.ForgotPasswordScreen
import id.rancak.app.presentation.ui.auth.LoginScreen
import id.rancak.app.presentation.ui.auth.ResetPasswordScreen
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
import id.rancak.app.presentation.ui.pricing.BundleManagementScreen
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
            onLoginSuccess   = {
                // Direct A→C: Login → TenantPicker, Login dihapus dari back stack.
                // User tidak bisa kembali ke Login dengan tombol Back.
                navController.navigate(Screen.TenantPicker()) {
                    popUpTo(Screen.Login) { inclusive = true }
                }
            },
            onForgotPassword = {
                navController.navigate(Screen.ForgotPassword)
            }
        )
    }

    composable<Screen.ForgotPassword> {
        ForgotPasswordScreen(
            onBack = { navController.popBackStack() },
            onNavigateToResetPassword = {
                navController.navigate(Screen.ResetPassword)
            }
        )
    }

    composable<Screen.ResetPassword> {
        ResetPasswordScreen(
            onBack    = { navController.popBackStack() },
            onSuccess = {
                navController.navigate(Screen.Login) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }

    composable<Screen.TenantPicker> { entry ->
        val route = entry.toRoute<Screen.TenantPicker>()
        // scope terikat pada composable ini, bukan ViewModel — sehingga
        // authRepository.logout() tetap selesai meski ViewModel di-clear
        // akibat popUpTo(0) saat navigasi terjadi.
        val scope = rememberCoroutineScope()
        val authRepository: AuthRepository = koinInject()
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
                // Logout dijalankan di composable scope (bukan viewModelScope) agar
                // clearSessionData() selesai sebelum navigasi terjadi, dan tidak
                // ikut di-cancel ketika back stack di-pop.
                scope.launch {
                    authRepository.logout()
                    navController.navigate(Screen.Login) {
                        popUpTo(0) { inclusive = true }
                    }
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
            onOpenBillClick = { navController.navigate(Screen.OpenBillList()) }
        )
    }

    composable<Screen.Cart> {
        CartScreen(
            onBack     = { navController.popBackStack() },
            // launchSingleTop mencegah duplicate Payment entry jika user tap
            // checkout di Cart dan Pos secara berurutan tanpa sempat melihat Payment.
            onCheckout = { navController.navigate(Screen.Payment) { launchSingleTop = true } }
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
            }
        )
    }

    composable<Screen.OpenBillList> {
        val cartViewModel = LocalCartViewModel.current
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
            // Layar ini bisa dibuka dari dua konteks:
            //   a) SalesHistory → PayHeldOrder  (stack: [..., SalesHistory, PayHeldOrder])
            //   b) OpenBillList → PayHeldOrder  (stack: [..., Pos, OpenBillList, PayHeldOrder])
            // Coba pop ke SalesHistory dulu; jika gagal (tidak ada di stack),
            // pop ke Pos — menutup sekaligus OpenBillList dan PayHeldOrder.
            onPaymentComplete = {
                if (!navController.popBackStack(Screen.SalesHistory, inclusive = false)) {
                    navController.popBackStack(Screen.Pos, inclusive = false)
                }
            }
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
            // Prioritas pop: OpenBillList (dari kasir) → SalesHistory → Pos (fallback).
            // Setelah item berhasil ditambahkan, kembali ke OpenBillList agar jumlah
            // item terbaru langsung terlihat di daftar.
            onSuccess = {
                if (!navController.popBackStack(Screen.OpenBillList(), inclusive = false)) {
                    if (!navController.popBackStack(Screen.SalesHistory, inclusive = false)) {
                        navController.popBackStack(Screen.Pos, inclusive = false)
                    }
                }
            }
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
    composable<Screen.PricingManagement>  {
        PricingManagementScreen(
            onBack             = onMenuClick,
            onBundleManagement = { navController.navigate(Screen.BundleManagement) }
        )
    }
    composable<Screen.BundleManagement>   { BundleManagementScreen(onBack = { navController.popBackStack() }) }
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
