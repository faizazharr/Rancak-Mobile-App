package id.rancak.app.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable data object Splash : Screen()
    @Serializable data object Login : Screen()
    @Serializable data object TenantPicker : Screen()
    @Serializable data object Pos : Screen()
    @Serializable data object Cart : Screen()
    @Serializable data object Payment : Screen()
    @Serializable data object Shift : Screen()
    @Serializable data object Tables : Screen()
    @Serializable data object Kds : Screen()
    @Serializable data object SalesHistory : Screen()
    @Serializable data class  PayHeldOrder(val saleUuid: String) : Screen()
    @Serializable data class  SplitBill(val saleUuid: String) : Screen()
    @Serializable data class  AddItemsToHeldOrder(val saleUuid: String) : Screen()
    @Serializable data object OrderBoard : Screen()
    @Serializable data object Reports : Screen()
    @Serializable data object CashExpense : Screen()
    @Serializable data object Settings : Screen()
}
