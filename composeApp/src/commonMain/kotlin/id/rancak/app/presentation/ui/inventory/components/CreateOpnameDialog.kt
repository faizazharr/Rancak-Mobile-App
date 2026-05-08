package id.rancak.app.presentation.ui.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakTheme

@Composable
fun CreateOpnameDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var note by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier  = Modifier.width(460.dp),
            shape     = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                // ── Gradient header ───────────────────────────────────────
                val gradientStart = Primary
                val gradientEnd   = Color(0xFF0B7A60)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(gradientStart, gradientEnd)))
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(42.dp)
                                .background(Color.White.copy(alpha = 0.18f), MaterialTheme.shapes.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Inventory, null,
                                tint     = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                "Buat Sesi Opname Baru",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = Color.White
                            )
                            Text(
                                "Mulai sesi penghitungan stok fisik",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }
                }

                // ── Body ──────────────────────────────────────────────────
                Column(
                    modifier            = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value         = note,
                        onValueChange = { note = it },
                        label         = { Text("Catatan (opsional)") },
                        placeholder   = { Text("Contoh: Opname bulanan Mei 2026") },
                        modifier      = Modifier.fillMaxWidth(),
                        maxLines      = 3,
                        shape         = MaterialTheme.shapes.medium
                    )

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick  = onDismiss,
                            enabled  = !isSubmitting,
                            modifier = Modifier.weight(1f)
                        ) { Text("Batal") }

                        // Gradient confirm button
                        Box(
                            modifier         = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    if (!isSubmitting)
                                        Brush.horizontalGradient(listOf(gradientStart, gradientEnd))
                                    else
                                        Brush.horizontalGradient(
                                            listOf(gradientStart.copy(alpha = 0.45f), gradientEnd.copy(alpha = 0.45f))
                                        )
                                )
                                .clickable(
                                    enabled             = !isSubmitting,
                                    interactionSource   = remember { MutableInteractionSource() },
                                    indication          = null
                                ) { onConfirm(note.ifBlank { null }) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color       = Color.White
                                )
                            } else {
                                Text(
                                    "Buat Sesi",
                                    style      = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun CreateOpnameDialogPreview() {
    RancakTheme {
        CreateOpnameDialog(isSubmitting = false, onDismiss = {}, onConfirm = {})
    }
}

