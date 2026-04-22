package id.rancak.app.presentation.ui.pos.components

import androidx.compose.ui.graphics.Color
import kotlin.math.absoluteValue

/**
 * Shared accent palette for POS cards, avatars, and category chips.
 * Deterministic per-key so that reopening the screen yields stable colours.
 */
private val ACCENT_PALETTE = listOf(
    Color(0xFFFF6B35), Color(0xFF06D6A0), Color(0xFF118AB2),
    Color(0xFFEF476F), Color(0xFF7B5EA7), Color(0xFFF4A261),
    Color(0xFF2EC4B6), Color(0xFFE76F51)
)

/** Pick a stable accent colour for a given key (product, category, etc.). */
internal fun accentFor(key: String): Color =
    ACCENT_PALETTE[key.hashCode().absoluteValue % ACCENT_PALETTE.size]
