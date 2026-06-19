package id.rancak.app

import androidx.compose.runtime.Composable

/**
 * iOS Koin wrapper.
 *
 * Koin sudah diinisialisasi secara global di [MainViewController] via startKoin { }.
 * KoinContext is no longer needed — Compose Koin context is set up via StartKoin().
 */
@Composable
actual fun KoinAppWrapper(content: @Composable () -> Unit) {
    content()
}
