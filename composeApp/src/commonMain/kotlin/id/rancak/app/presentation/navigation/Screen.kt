package id.rancak.app.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable data object Login : Screen()
    @Serializable data object TenantPicker : Screen()
    @Serializable data object Pos : Screen()
    @Serializable data object Cart : Screen()
    @Serializable data object Payment : Screen()
    @Serializable data object Shift : Screen()
    @Serializable data object Tables : Screen()
    @Serializable data object Kds : Screen()
    @Serializable data object SalesHistory : Screen()
}
