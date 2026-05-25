package id.rancak.app.presentation.designsystem

import androidx.compose.ui.graphics.Color

// ── Primary – Teal / Emerald ──
val Primary = Color(0xFF0D9373)
val PrimaryGradientEnd = Color(0xFF0B7A60)  // gradient end for primary CTA buttons
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFB4F1E0)
val OnPrimaryContainer = Color(0xFF00201A)

// ── Secondary – Warm Orange ──
val Secondary = Color(0xFFE8772E)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFFFDCC8)
val OnSecondaryContainer = Color(0xFF2E1500)

// ── Tertiary – Deep Blue ──
val Tertiary = Color(0xFF3366CC)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFD6E2FF)
val OnTertiaryContainer = Color(0xFF001A41)

// ── Error ──
val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF410002)

// ── Background & Surface — ERP neutral palette ──
val Background = Color(0xFFF2F3F5)   // abu-abu enterprise, lebih padat dari putih
val OnBackground = Color(0xFF1A1C1E)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1A1C1E)
val SurfaceVariant = Color(0xFFE8EAED)
val OnSurfaceVariant = Color(0xFF3D4043)
val SurfaceTint = Primary
val Outline = Color(0xFFB0B7B3)       // lebih halus agar border card tidak terlalu tebal
val OutlineVariant = Color(0xFFD5D9D6)

// ── Dark Theme ──
val DarkPrimary = Color(0xFF5FDBB8)
val DarkOnPrimary = Color(0xFF003829)
val DarkPrimaryContainer = Color(0xFF005139)
val DarkOnPrimaryContainer = Color(0xFFB4F1E0)

val DarkSecondary = Color(0xFFFFB68A)
val DarkOnSecondary = Color(0xFF4E2600)
val DarkSecondaryContainer = Color(0xFF6F3800)
val DarkOnSecondaryContainer = Color(0xFFFFDCC8)

val DarkTertiary = Color(0xFFAAC7FF)
val DarkOnTertiary = Color(0xFF002F68)
val DarkTertiaryContainer = Color(0xFF15448F)
val DarkOnTertiaryContainer = Color(0xFFD6E2FF)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkBackground = Color(0xFF121412)
val DarkOnBackground = Color(0xFFE1E3E0)
val DarkSurface = Color(0xFF1A1C1A)
val DarkOnSurface = Color(0xFFE1E3E0)
val DarkSurfaceVariant = Color(0xFF2A2D2B)
val DarkOnSurfaceVariant = Color(0xFFBFC9C3)
val DarkOutline = Color(0xFF89938E)
val DarkOutlineVariant = Color(0xFF404944)

// ── Semantic Colors (Light) ──
val Success = Color(0xFF2E7D32)
val Warning = Color(0xFFF9A825)
val Info = Color(0xFF1976D2)

val StatusAvailable = Color(0xFF4CAF50)
val StatusOccupied = Color(0xFFFF9800)
val StatusReserved = Color(0xFF2196F3)
val StatusMaintenance = Color(0xFF9E9E9E)

val PaymentCash = Color(0xFF4CAF50)
val PaymentCard = Color(0xFF2196F3)
val PaymentQris = Color(0xFF9C27B0)
val PaymentTransfer = Color(0xFFFF9800)

// ── Shift / Connectivity Status ──
val StatusOnline  = Color(0xFF4ADE80)  // shift buka / online indicator
val StatusOffline = Color(0xFFFF4444)  // shift tutup / offline indicator

// ── Chart Extended Palette (beyond core semantic colors) ──
val ChartCyan  = Color(0xFF00BCD4)
val ChartPink  = Color(0xFFE91E63)
val ChartBrown = Color(0xFF795548)

/**
 * Urutan warna untuk donut chart / legenda pembayaran di ReportScreen.
 * Referensi ini bisa dipakai di luar composable (tidak perlu LocalSemanticColors).
 */
val chartPalette: List<Color> = listOf(
    StatusAvailable,
    PaymentCard,
    PaymentTransfer,
    PaymentQris,
    ChartCyan,
    ChartPink,
    ChartBrown
)

// ── Semantic Colors (Dark) ──
val DarkSuccess = Color(0xFF66BB6A)
val DarkWarning = Color(0xFFFFD54F)
val DarkInfo = Color(0xFF42A5F5)

val DarkStatusAvailable = Color(0xFF81C784)
val DarkStatusOccupied = Color(0xFFFFB74D)
val DarkStatusReserved = Color(0xFF64B5F6)
val DarkStatusMaintenance = Color(0xFFBDBDBD)

val DarkStatusOnline  = Color(0xFF86EFAC)
val DarkStatusOffline = Color(0xFFFC7171)

val DarkPaymentCash = Color(0xFF81C784)
val DarkPaymentCard = Color(0xFF64B5F6)
val DarkPaymentQris = Color(0xFFCE93D8)
val DarkPaymentTransfer = Color(0xFFFFB74D)

val DarkChartCyan  = Color(0xFF4DD0E1)
val DarkChartPink  = Color(0xFFF48FB1)
val DarkChartBrown = Color(0xFFA1887F)

// ── Settings Section Icon Backgrounds ──────────────────────────────────────
/** Latar belakang ikon "Mode Cetak" di Settings. */
val SettingsAccentTune    = Color(0xFF6750A4)
/** Latar belakang ikon "Printer Dapur" / Kitchen di Settings. */
val SettingsAccentKitchen = Color(0xFFB5340A)
/** Latar belakang ikon "Printer Kasir" dan "Informasi Toko" di Settings. */
val SettingsAccentStore   = Color(0xFF1A6B3C)
/** Latar belakang ikon "Umum" di Settings. */
val SettingsAccentNeutral = Color(0xFF555555)

// ── KDS Order Status Colors ─────────────────────────────────────────────────
/** Header KDS — pesanan baru. */
val KdsColorNew     = Color(0xFF1565C0)
/** Header KDS — sedang dimasak. */
val KdsColorCooking = Color(0xFFE65100)
/** Header KDS — siap saji / sudah tersinkron ke dapur. */
val KdsColorReady   = Color(0xFF2E7D32)
// KdsColorDone  → gunakan StatusMaintenance (Color 0xFF9E9E9E)

// ── OrderBoard Sale Status Colors ───────────────────────────────────────────
/** Header OrderBoard — order ditahan/HELD. */
val SaleColorHeld = Color(0xFFF57C00)
// SaleColorPaid → gunakan Success

// ── Accent Palette — deterministik untuk avatar produk & kategori ────────────
/**
 * Palet 8 warna cerah untuk generate latar belakang avatar produk/kategori secara deterministik.
 * Dipakai di luar composable — akses langsung tanpa perlu LocalComposition.
 */
val accentPalette: List<Color> = listOf(
    Color(0xFFFF6B35), Color(0xFF06D6A0), Color(0xFF118AB2),
    Color(0xFFEF476F), Color(0xFF7B5EA7), Color(0xFFF4A261),
    Color(0xFF2EC4B6), Color(0xFFE76F51)
)

// ── Warning Gradient ─────────────────────────────────────────────────────────
/** Akhir gradient untuk banner/latar bertema warning (amber terang). */
val WarningGradientEnd = Color(0xFFFBBF24)
