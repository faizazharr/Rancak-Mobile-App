package id.rancak.app.presentation.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ukuran umum yang dipakai di seluruh aplikasi.
 * Akses via `RancakDesign.sizes`.
 */
@Immutable
data class Sizes(
    // ── Icons ─────────────────────────────────────────────────────────────
    /** Icon dalam chip / inline label. */
    val iconXs: Dp = 14.dp,
    /** Icon kecil untuk badge atau status. */
    val iconSm: Dp = 16.dp,
    /** Icon standar untuk tombol & list item. */
    val iconMd: Dp = 20.dp,
    /** Icon untuk header / kategori / FAB. */
    val iconLg: Dp = 24.dp,
    /** Icon hero / empty state. */
    val iconXl: Dp = 32.dp,

    // ── Tap targets ───────────────────────────────────────────────────────
    /** Minimum touch target sesuai Material guideline. */
    val minTouchTarget: Dp = 48.dp,
    /** Tinggi tombol standar. */
    val buttonHeight: Dp = 48.dp,
    /** Tinggi text field standar. */
    val inputHeight: Dp = 56.dp,
    /** Tinggi top-app-bar. */
    val topBarHeight: Dp = 64.dp,
    /** Lebar drawer modal. */
    val drawerWidth: Dp = 280.dp,

    // ── Layout constraints ────────────────────────────────────────────────
    /** Lebar maksimum form pada tablet (mencegah field melar). */
    val formMaxWidth: Dp = 560.dp,
    /** Lebar maksimum konten yang dibaca (settings, detail). */
    val readableMaxWidth: Dp = 720.dp,
    /** Breakpoint phone vs tablet. */
    val tabletBreakpoint: Dp = 600.dp,

    // ── Border / divider ──────────────────────────────────────────────────
    val borderThin: Dp = 1.dp,
    val borderEmphasis: Dp = 1.5.dp
)

val LocalSizes = staticCompositionLocalOf { Sizes() }

object RancakSizes {
    val current: Sizes
        @Composable get() = LocalSizes.current
}
