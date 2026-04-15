package id.rancak.app.presentation.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    outline = Outline,
    outlineVariant = OutlineVariant
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

// ERP kasir: sudut tajam → kesan profesional, bukan consumer-app
val RancakShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small      = RoundedCornerShape(4.dp),
    medium     = RoundedCornerShape(6.dp),
    large      = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(10.dp)
)

// ── Semantic / Extended Colors ──────────────────────────────────────────────

/**
 * Warna semantik bisnis yang otomatis menyesuaikan light / dark theme.
 * Akses via [RancakColors.semantic] di dalam composable.
 */
@Immutable
data class SemanticColors(
    val success: Color,
    val warning: Color,
    val info: Color,
    val statusAvailable: Color,
    val statusOccupied: Color,
    val statusReserved: Color,
    val statusMaintenance: Color,
    val paymentCash: Color,
    val paymentCard: Color,
    val paymentQris: Color,
    val paymentTransfer: Color
)

private val LightSemanticColors = SemanticColors(
    success           = Success,
    warning           = Warning,
    info              = Info,
    statusAvailable   = StatusAvailable,
    statusOccupied    = StatusOccupied,
    statusReserved    = StatusReserved,
    statusMaintenance = StatusMaintenance,
    paymentCash       = PaymentCash,
    paymentCard       = PaymentCard,
    paymentQris       = PaymentQris,
    paymentTransfer   = PaymentTransfer
)

private val DarkSemanticColors = SemanticColors(
    success           = DarkSuccess,
    warning           = DarkWarning,
    info              = DarkInfo,
    statusAvailable   = DarkStatusAvailable,
    statusOccupied    = DarkStatusOccupied,
    statusReserved    = DarkStatusReserved,
    statusMaintenance = DarkStatusMaintenance,
    paymentCash       = DarkPaymentCash,
    paymentCard       = DarkPaymentCard,
    paymentQris       = DarkPaymentQris,
    paymentTransfer   = DarkPaymentTransfer
)

val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }

// ── Theme ───────────────────────────────────────────────────────────────────

@Composable
fun RancakTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSemanticColors provides LightSemanticColors
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography  = RancakTypography,
            shapes      = RancakShapes,
            content     = content
        )
    }
}

/**
 * Akses cepat ke warna semantik dari mana saja dalam `RancakTheme`.
 *
 * ```
 * val color = RancakColors.semantic.success
 * ```
 */
object RancakColors {
    val semantic: SemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSemanticColors.current
}
