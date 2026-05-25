package id.rancak.app.presentation.ui.pos.components

import androidx.compose.ui.graphics.Color
import id.rancak.app.presentation.designsystem.accentPalette
import kotlin.math.absoluteValue

/** Pick a stable accent colour for a given key (product, category, etc.). */
internal fun accentFor(key: String): Color =
    accentPalette[key.hashCode().absoluteValue % accentPalette.size]
