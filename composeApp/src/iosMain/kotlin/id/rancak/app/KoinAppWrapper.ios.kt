package id.rancak.app

import androidx.compose.runtime.Composable
import org.koin.compose.KoinContext

/**
 * iOS Koin wrapper.
 *
 * Koin sudah diinisialisasi secara global di [MainViewController] via startKoin { }.
 * Di sini kita hanya menghubungkan global Koin instance ke Compose tree —
 * sama dengan [KoinAppWrapper.android.kt] yang juga pakai KoinContext.
 *
 * JANGAN ganti ke KoinApplication { } — itu akan crash karena mencoba
 * start instance baru padahal sudah ada yang berjalan dari MainViewController.
 */
@Composable
actual fun KoinAppWrapper(content: @Composable () -> Unit) {
    KoinContext {
        content()
    }
}
