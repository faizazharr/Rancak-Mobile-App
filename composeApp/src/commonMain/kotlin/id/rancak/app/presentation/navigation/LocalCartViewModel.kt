package id.rancak.app.presentation.navigation

import androidx.compose.runtime.compositionLocalOf
import id.rancak.app.presentation.viewmodel.CartViewModel

/**
 * CompositionLocal yang menyediakan [CartViewModel] secara implisit kepada
 * semua composable dalam hierarki NavigationContent — PosScreen, CartScreen,
 * dan PaymentScreen — tanpa perlu meneruskannya sebagai parameter.
 *
 * Instance tunggal dibuat di [NavigationContent] (Activity scope) sehingga
 * ketiga layar tersebut berbagi state keranjang yang sama.
 */
val LocalCartViewModel = compositionLocalOf<CartViewModel> {
    error("LocalCartViewModel belum disediakan. Pastikan NavigationContent membungkus NavHost.")
}
