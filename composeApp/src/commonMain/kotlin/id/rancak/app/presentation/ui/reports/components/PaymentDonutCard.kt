package id.rancak.app.presentation.ui.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.PaymentMethodReport
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.reports.chartColors
import id.rancak.app.presentation.util.formatRupiah

/**
 * Kartu pembayaran: donut chart di kiri + legenda metode pembayaran di kanan,
 * menampilkan total rupiah, jumlah transaksi, dan persentase per metode.
 */
@Composable
internal fun PaymentDonutCard(methods: List<PaymentMethodReport>) {
    val total = methods.sumOf { it.total }.takeIf { it > 0L } ?: return

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(1.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "Metode Pembayaran",
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.outline,
                modifier   = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DonutChart(
                    slices = methods.mapIndexed { i, m ->
                        m.total.toFloat() / total.toFloat() to chartColors[i % chartColors.size]
                    },
                    modifier = Modifier.size(100.dp)
                )

                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    methods.forEachIndexed { i, method ->
                        val pct = (method.total.toFloat() / total.toFloat() * 100).toInt()
                        val dot = chartColors[i % chartColors.size]

                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(9.dp)
                                    .background(dot, CircleShape)
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text       = method.method.replaceFirstChar { it.uppercase() },
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis
                                )
                                Text(
                                    text     = "${formatRupiah(method.total)} · ${method.count}x · $pct%",
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    slices: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.18f
        val stroke      = Stroke(width = strokeWidth)
        val diameter    = size.minDimension - strokeWidth
        val topLeft     = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize     = Size(diameter, diameter)

        var startAngle = -90f
        slices.forEach { (fraction, color) ->
            val sweep = fraction * 360f
            drawArc(
                color      = color,
                startAngle = startAngle,
                sweepAngle = (sweep - 1.5f).coerceAtLeast(0f),
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = stroke
            )
            startAngle += sweep
        }
    }
}

@Preview
@Composable
private fun PaymentDonutCardPreview() {
    RancakTheme {
        Column(Modifier.padding(12.dp)) {
            PaymentDonutCard(
                methods = listOf(
                    PaymentMethodReport("cash", 4_500_000, 32),
                    PaymentMethodReport("qris", 2_800_000, 22),
                    PaymentMethodReport("card", 1_450_000, 10)
                )
            )
        }
    }
}
