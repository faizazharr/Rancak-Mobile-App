package id.rancak.app.presentation.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Elevation scale — semua bayangan di app harus pakai token ini, bukan angka mentah.
 * Akses via `RancakDesign.elevation` atau `LocalElevation.current`.
 *
 * Contoh:
 * ```
 * Card(elevation = CardDefaults.cardElevation(RancakDesign.elevation.card)) { ... }
 * ```
 */
@Immutable
data class Elevation(
    /** Tanpa bayangan — flat surface, dipakai untuk container netral. */
    val none: Dp = 0.dp,
    /** Bayangan halus untuk card konten utama (product card, list item). */
    val card: Dp = 1.dp,
    /** Card aktif/terpilih, summary card highlighted. */
    val cardSelected: Dp = 2.dp,
    /** Surface yang melayang (FAB resting, top-app-bar shadow). */
    val raised: Dp = 4.dp,
    /** Modal / dialog / bottom-sheet. */
    val modal: Dp = 8.dp,
    /** FAB pressed state. */
    val pressed: Dp = 12.dp
)

val LocalElevation = staticCompositionLocalOf { Elevation() }

object RancakElevation {
    val current: Elevation
        @Composable get() = LocalElevation.current
}
