package id.rancak.app.presentation.components

import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavigateToHome = staticCompositionLocalOf<(() -> Unit)?> { null }
