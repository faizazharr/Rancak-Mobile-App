package id.rancak.app

import androidx.compose.runtime.Composable
import id.rancak.app.di.appModules
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.navigation.RancakNavHost
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModules)
    }) {
        RancakTheme {
            RancakNavHost()
        }
    }
}