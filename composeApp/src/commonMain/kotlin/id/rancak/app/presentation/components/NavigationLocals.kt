package id.rancak.app.presentation.components

import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavigateToHome = staticCompositionLocalOf<(() -> Unit)?> { null }

/** True jika langganan tenant aktif sudah kedaluwarsa — semua tombol transaksi harus dinonaktifkan. */
val LocalSubscriptionExpired = staticCompositionLocalOf { false }
