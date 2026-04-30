package id.rancak.app.presentation.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.data.printing.ReceiptTextRenderer
import id.rancak.app.presentation.designsystem.RancakDesign
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Live preview struk thermal — render plain-text persis seperti yang akan
 * dicetak ESC/POS, dibungkus card "kertas" putih dengan font monospace.
 *
 * Lebar card disesuaikan dengan [paperWidthMm] supaya user dapat gambaran
 * proporsi kertas yang sesungguhnya (58 mm vs 80 mm).
 */
@Composable
internal fun ReceiptPreviewContent(
    storeName: String,
    storeAddress: String,
    storePhone: String,
    footerText: String,
    paperWidthMm: Int
) {
    val data = ReceiptTextRenderer.sample(
        storeName    = storeName,
        storeAddress = storeAddress,
        storePhone   = storePhone,
        footerText   = footerText
    )
    val text = ReceiptTextRenderer.render(data, paperWidthMm)

    // 58mm ≈ 220 dp paper, 80mm ≈ 300 dp paper (visual scale)
    val paperWidthDp = if (paperWidthMm >= 80) 300.dp else 220.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Preview Struk",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text("${paperWidthMm} mm") },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        Text(
            "Tampilan akan tercetak seperti ini saat struk dikirim ke printer thermal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ── Paper card ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RancakDesign.shapes.medium
                )
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.width(paperWidthDp),
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 12.dp, bottomEnd = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = RancakDesign.elevation.raised)
            ) {
                Column {
                    // Decorative torn-paper notch on top
                    PaperNotchEdge()
                    Text(
                        text = text,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize   = if (paperWidthMm >= 80) 10.sp else 9.sp,
                        lineHeight = if (paperWidthMm >= 80) 13.sp else 12.sp,
                        color = Color(0xFF222222)
                    )
                    PaperNotchEdge()
                }
            }
        }
    }
}

/** Zigzag edge yang meniru kertas struk yang baru dipotong/sobek. */
@Composable
private fun PaperNotchEdge() {
    Row(modifier = Modifier.fillMaxWidth().height(6.dp)) {
        repeat(40) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (it % 2 == 0) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)
                    )
            )
        }
    }
}

@Preview(name = "Receipt Preview – 58mm", widthDp = 360)
@Composable
private fun ReceiptPreviewContent58Preview() {
    RancakTheme {
        Box(Modifier.padding(16.dp)) {
            ReceiptPreviewContent(
                storeName    = "Warung Kopi Senja",
                storeAddress = "Jl. Mawar No. 12, Bandung",
                storePhone   = "0812-3456-7890",
                footerText   = "Terima kasih, hati-hati di jalan!",
                paperWidthMm = 58
            )
        }
    }
}

@Preview(name = "Receipt Preview – 80mm", widthDp = 480)
@Composable
private fun ReceiptPreviewContent80Preview() {
    RancakTheme {
        Box(Modifier.padding(16.dp)) {
            ReceiptPreviewContent(
                storeName    = "Warung Kopi Senja",
                storeAddress = "Jl. Mawar No. 12, Bandung",
                storePhone   = "0812-3456-7890",
                footerText   = "Terima kasih, hati-hati di jalan!",
                paperWidthMm = 80
            )
        }
    }
}
