package id.rancak.app

import androidx.compose.runtime.Composable
import org.koin.compose.KoinContext

/**
 * On Android, Koin is already started by [RancakApplication.onCreate].
 * We only need to provide the Koin context to the Compose tree.
 */
@Composable
actual fun KoinAppWrapper(content: @Composable () -> Unit) {
    KoinContext {
        content()
    }
}
