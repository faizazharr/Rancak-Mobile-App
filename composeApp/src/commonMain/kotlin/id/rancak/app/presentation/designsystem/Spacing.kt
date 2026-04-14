package id.rancak.app.presentation.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale — digunakan di seluruh layar untuk jarak yang konsisten.
 * Akses via `RancakSpacing.current` di dalam composable yang dibungkus `RancakTheme`.
 *
 * Contoh: `Spacer(Modifier.height(RancakSpacing.current.md))`
 */
@Immutable
data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp  = 4.dp,
    val sm: Dp  = 8.dp,
    val md: Dp  = 16.dp,
    val lg: Dp  = 24.dp,
    val xl: Dp  = 32.dp,
    val xxl: Dp = 48.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

/**
 * Shortcut — `RancakSpacing.current` di mana saja dalam `RancakTheme`.
 */
object RancakSpacing {
    val current: Spacing
        @Composable get() = LocalSpacing.current
}
