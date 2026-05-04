package id.rancak.app.presentation.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * **Single source of truth** untuk semua desain token aplikasi Rancak.
 *
 * Pakai ini sebagai satu-satunya entry point dari composable:
 * ```
 * Card(
 *     shape    = RancakDesign.shapes.medium,
 *     elevation = CardDefaults.cardElevation(RancakDesign.elevation.card),
 *     colors   = CardDefaults.cardColors(containerColor = RancakDesign.colors.surface)
 * ) {
 *     Text(
 *         "Hello",
 *         style = RancakDesign.type.titleMedium,
 *         color = RancakDesign.colors.onSurface,
 *         modifier = Modifier.padding(RancakDesign.spacing.md)
 *     )
 * }
 * ```
 *
 * Mengubah satu nilai di file design system → seluruh aplikasi otomatis ikut berubah.
 *
 * Token yang tersedia:
 * - [colors] — palet warna (Material 3 + semantic Rancak)
 * - [type] — typography
 * - [shapes] — corner radius
 * - [spacing] — jarak (xs, sm, md, lg, xl, xxl)
 * - [elevation] — bayangan
 * - [sizes] — ukuran umum (icon, tombol, breakpoint)
 * - [semantic] — warna semantic bisnis (status meja, payment, success/warning/info)
 */
object RancakDesign {

    val colors: ColorScheme
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val type: Typography
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val spacing: Spacing
        @Composable @ReadOnlyComposable
        get() = LocalSpacing.current

    val elevation: Elevation
        @Composable @ReadOnlyComposable
        get() = LocalElevation.current

    val sizes: Sizes
        @Composable @ReadOnlyComposable
        get() = LocalSizes.current

    val semantic: SemanticColors
        @Composable @ReadOnlyComposable
        get() = LocalSemanticColors.current
}
