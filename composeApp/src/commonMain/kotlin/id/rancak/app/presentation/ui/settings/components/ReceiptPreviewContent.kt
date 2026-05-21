package id.rancak.app.presentation.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.data.printing.ReceiptTextRenderer
import id.rancak.app.domain.model.ReceiptSettingsConfig
import id.rancak.app.presentation.designsystem.RancakDesign
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Live preview struk thermal — render plain-text persis seperti yang akan
 * dicetak ESC/POS, dibungkus card "kertas" putih dengan font monospace.
 *
 * Lebar card disesuaikan dengan lebar kertas yang dipilih: 58 mm, 70 mm,
 * atau 80 mm. Pengguna bisa beralih ukuran langsung di sini untuk melihat
 * perbandingan. Logo placeholder tampil di atas header bila [showLogo] aktif.
 */
@Composable
internal fun ReceiptPreviewContent(
    storeName: String,
    storeAddress: String,
    storePhone: String,
    footerText: String,
    paperWidthMm: Int,
    showLogo: Boolean = false,
    receiptSettings: ReceiptSettingsConfig? = null
) {
    // Local state: lebar preview yang sedang ditampilkan — terpisah dari setting
    // aktual agar user bisa bandingkan tanpa mengubah konfigurasi printer.
    var previewWidth by remember(paperWidthMm) { mutableIntStateOf(paperWidthMm) }

    val data = ReceiptTextRenderer.sample(
        storeName    = storeName,
        storeAddress = storeAddress,
        storePhone   = storePhone,
        footerText   = footerText
    )
    val text = ReceiptTextRenderer.render(data, previewWidth, receiptSettings)

    // Lebar card kertas: 58 mm → 220 dp, 70 mm → 260 dp, 80 mm → 300 dp
    val paperWidthDp = when {
        previewWidth >= 80 -> 300.dp
        previewWidth >= 70 -> 260.dp
        else               -> 220.dp
    }

    // Font: makin lebar kertas makin besar sedikit agar teks terisi natural
    val fontSize   = when { previewWidth >= 80 -> 10.sp; previewWidth >= 70 -> 9.5.sp; else -> 9.sp }
    val lineHeight = when { previewWidth >= 80 -> 13.sp; previewWidth >= 70 -> 12.5.sp; else -> 12.sp }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Header row ─────────────────────────────────────────────────────
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
        }

        Text(
            "Tampilan akan tercetak seperti ini saat struk dikirim ke printer thermal.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ── Pemilih lebar kertas ────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Lebar kertas:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            listOf(58, 70, 80).forEach { widthOption ->
                FilterChip(
                    selected = previewWidth == widthOption,
                    onClick  = { previewWidth = widthOption },
                    label    = { Text("${widthOption} mm") },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

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
                shape    = RoundedCornerShape(
                    topStart    = 4.dp, topEnd    = 4.dp,
                    bottomStart = 12.dp, bottomEnd = 12.dp
                ),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = RancakDesign.elevation.raised)
            ) {
                Column {
                    // Decorative torn-paper notch on top
                    PaperNotchEdge()

                    // Logo placeholder (hanya tampil bila showLogo aktif)
                    if (showLogo) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFAAAAAA),
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "[ LOGO TOKO ]",
                                fontFamily = FontFamily.Monospace,
                                fontSize   = 8.sp,
                                color      = Color(0xFF888888),
                                textAlign  = TextAlign.Center
                            )
                        }
                    }

                    Text(
                        text       = text,
                        modifier   = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize   = fontSize,
                        lineHeight = lineHeight,
                        color      = Color(0xFF222222)
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
                paperWidthMm = 58,
                showLogo     = true
            )
        }
    }
}

@Preview(name = "Receipt Preview – 70mm", widthDp = 420)
@Composable
private fun ReceiptPreviewContent70Preview() {
    RancakTheme {
        Box(Modifier.padding(16.dp)) {
            ReceiptPreviewContent(
                storeName    = "Warung Kopi Senja",
                storeAddress = "Jl. Mawar No. 12, Bandung",
                storePhone   = "0812-3456-7890",
                footerText   = "Terima kasih, hati-hati di jalan!",
                paperWidthMm = 70
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
