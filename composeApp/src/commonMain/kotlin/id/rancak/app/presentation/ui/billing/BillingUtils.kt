package id.rancak.app.presentation.ui.billing

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun formatPlanPrice(amount: Double): String {
    val s = amount.toLong().toString().reversed()
    val parts = s.chunked(3).joinToString(".")
    return "Rp ${parts.reversed()}"
}

fun Brush.Companion.linearGradientBrush(colors: List<Color>) =
    linearGradient(colors)

/** Helper to avoid destructuring ambiguity in when-expressions. */
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
