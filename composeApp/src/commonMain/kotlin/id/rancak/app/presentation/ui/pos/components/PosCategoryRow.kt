package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Category
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Horizontal scrollable category chip row. Emits `null` for the "Semua"
 * (all-categories) selection and the picked [Category] otherwise.
 */
@Composable
internal fun PosCategoryRow(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category?) -> Unit
) {
    if (categories.isEmpty()) return
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier              = Modifier.padding(bottom = 8.dp)
    ) {
        item { PosChip("Semua", selected == null) { onSelect(null) } }
        items(categories) { cat ->
            PosChip(cat.name, selected == cat) { onSelect(cat) }
        }
    }
}

@Composable
internal fun PosChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (isSelected) primary
                else MaterialTheme.colorScheme.surface
            )
            .border(
                1.dp,
                if (isSelected) Color.Transparent
                else MaterialTheme.colorScheme.outlineVariant,
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color      = if (isSelected) Color.White
                         else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun PosCategoryRowPreview() {
    val categories = listOf(
        Category(uuid = "1", name = "Makanan", description = null),
        Category(uuid = "2", name = "Minuman", description = null),
        Category(uuid = "3", name = "Snack",   description = null)
    )
    RancakTheme {
        PosCategoryRow(
            categories = categories,
            selected   = categories[1],
            onSelect   = {}
        )
    }
}
