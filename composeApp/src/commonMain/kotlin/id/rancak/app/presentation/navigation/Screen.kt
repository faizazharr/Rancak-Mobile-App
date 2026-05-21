package id.rancak.app.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable data object Splash : Screen()
    @Serializable data object Login : Screen()
    @Serializable data class  TenantPicker(val switchMode: Boolean = false) : Screen()
    @Serializable data object Pos : Screen()
    @Serializable data object Cart : Screen()
    @Serializable data object Payment : Screen()
    @Serializable data object Shift : Screen()
    @Serializable data object Tables : Screen()
    @Serializable data object Reservations : Screen()
    @Serializable data object Kds : Screen()
    @Serializable data object SalesHistory : Screen()
    @Serializable data class  OpenBillList(val dummy: Int = 0) : Screen()
    @Serializable data class  PayHeldOrder(val saleUuid: String) : Screen()
    @Serializable data class  SplitBill(val saleUuid: String) : Screen()
    @Serializable data class  AddItemsToHeldOrder(val saleUuid: String) : Screen()
    @Serializable data object OrderBoard : Screen()
    @Serializable data object Reports : Screen()
    @Serializable data object CashExpense : Screen()
    @Serializable data object ProductManagement : Screen()
    @Serializable data class  Billing(val fromSetup: Boolean = false) : Screen()
    @Serializable data object StockOpname : Screen()
    @Serializable data object VoucherManagement : Screen()
    @Serializable data object PricingManagement : Screen()
    @Serializable data object Settings : Screen()
    @Serializable data object ModifierManagement : Screen()
    @Serializable data object SupplierManagement : Screen()
    @Serializable data object PurchaseOrders : Screen()
    @Serializable data object ForgotPassword : Screen()
}
