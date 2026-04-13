package id.rancak.app

import androidx.compose.runtime.Composable
import id.rancak.app.di.appModules
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.navigation.RancakNavHost

/**
 * Root composable.
 *
 * On Android: Koin is already started by [RancakApplication].
 *             [KoinAppWrapper] uses [org.koin.compose.KoinContext] to consume it.
 * On iOS:     Koin is started here via [org.koin.compose.KoinApplication].
 */
@Composable
fun App() {
    KoinAppWrapper {
        RancakTheme {
            RancakNavHost()
        }
    }
}

/**
 * Platform-specific Koin wrapper.
 * Android = KoinContext (consume existing global Koin).
 * iOS     = KoinApplication (start Koin with modules).
 */
@Composable
expect fun KoinAppWrapper(content: @Composable () -> Unit)
