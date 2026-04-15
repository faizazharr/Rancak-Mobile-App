package id.rancak.app

import androidx.compose.runtime.Composable

/**
 * On Android, Koin is already started by [RancakApplication.onCreate].
 * KoinContext is no longer needed — Compose Koin context is set up via StartKoin().
 */
@Composable
actual fun KoinAppWrapper(content: @Composable () -> Unit) {
    content()
}
