package id.rancak.app.presentation.ui.reports.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.DailyCategoryReport
import id.rancak.app.presentation.util.formatRupiah
import kotlinx.collections.immutable.ImmutableList

/**
 * Kartu bar chart horizontal yang memvisualisasikan pendapatan per kategori.
 * Menggantikan tabel teks murni dengan representasi grafis yang lebih intuitif.
 * Data: [DailyCategoryReport.totalSales] dinormalisasi terhadap nilai tertinggi.
 */
@Composable
internal fun RevenueCategoryChartCard(
    categories: ImmutableList<DailyCategoryReport>,
    modifier: Modifier = Modifier,
) {
    if (categories.isEmpty()) return

    val maxSales = categories.maxOf { it.totalSales }.takeIf { it > 0 } ?: return

    // Sorted descending by totalSales
    val sorted =
        remember(categories) {
            categories.sortedByDescending { it.totalSales }
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Grafik Penjualan per Kategori",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 10.dp),
            )
            sorted.forEachIndexed { index, cat ->
                CategoryBarRow(
                    category = cat,
                    maxSales = maxSales,
                    barColor = barColor(index),
                    isLast = index == sorted.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun CategoryBarRow(
    category: DailyCategoryReport,
    maxSales: Long,
    barColor: androidx.compose.ui.graphics.Color,
    isLast: Boolean,
) {
    val fraction = (category.totalSales.toFloat() / maxSales.toFloat()).coerceIn(0f, 1f)

    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }
    val animatedFraction by animateFloatAsState(
        targetValue = if (triggered) fraction else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "bar_${category.categoryName}",
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (!isLast) Modifier.padding(bottom = 10.dp) else Modifier),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatRupiah(category.totalSales),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(4.dp))
        // Track (background)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            // Filled bar
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(barColor),
            )
        }
        Text(
            text = "${category.totalQty.toInt()} item terjual",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun barColor(index: Int): androidx.compose.ui.graphics.Color {
    val colors =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.primaryContainer,
        )
    return colors[index % colors.size]
}
