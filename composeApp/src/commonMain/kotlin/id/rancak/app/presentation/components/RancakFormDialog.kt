package id.rancak.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.PrimaryGradientEnd
import id.rancak.app.presentation.designsystem.RancakTheme


/**
 * Komponen dialog form standar Rancak POS.
 *
 * Tampilan:
 * - **Header** bergradien teal dengan ikon, judul, dan subjudul.
 * - **Body** scrollable — isi form melalui slot [content].
 * - **Footer** tombol "Batal" (TextButton) + tombol konfirmasi bergradien.
 *
 * Gunakan komponen ini sebagai wrapper untuk semua dialog yang berisi form input,
 * sehingga tampilan konsisten di seluruh aplikasi.
 *
 * Contoh penggunaan:
 * ```kotlin
 * RancakFormDialog(
 *     icon         = Icons.Default.Category,
 *     title        = "Tambah Kategori",
 *     subtitle     = "Isi detail kategori baru",
 *     onDismissRequest = onDismiss,
 *     confirmLabel = "Simpan",
 *     onConfirm    = { onConfirm(name, desc) },
 *     confirmEnabled  = name.isNotBlank(),
 *     isSubmitting = isSubmitting
 * ) {
 *     OutlinedTextField(value = name, ...)
 *     OutlinedTextField(value = description, ...)
 * }
 * ```
 *
 * @param icon           Ikon yang ditampilkan di header.
 * @param title          Judul dialog (bold, putih).
 * @param subtitle       Teks pendukung di bawah judul (putih samar).
 * @param onDismissRequest Dipanggil saat dialog ditutup (via back atau klik luar).
 * @param confirmLabel   Label tombol konfirmasi.
 * @param onConfirm      Aksi saat tombol konfirmasi ditekan.
 * @param confirmEnabled Apakah tombol konfirmasi aktif (default: true).
 * @param isSubmitting   Saat true: tombol konfirmasi menampilkan spinner.
 * @param dismissLabel   Label tombol batal (default: "Batal").
 * @param maxWidth       Lebar maksimum card dialog (default: 520.dp).
 * @param content        Slot berisi field-field form.
 */
@Composable
fun RancakFormDialog(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onDismissRequest: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    isSubmitting: Boolean = false,
    dismissLabel: String = "Batal",
    maxWidth: Dp = 520.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismissRequest() },
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(
            modifier         = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            // Bound the body height so the footer is always visible.
            // Header ~80dp + footer ~64dp + vertical padding ~40dp ≈ 184dp overhead.
            val bodyMaxHeight = (maxHeight - 184.dp).coerceIn(200.dp, 480.dp)

            Card(
                modifier  = Modifier.widthIn(max = maxWidth).fillMaxWidth(),
                shape     = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // ── Gradient header ───────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(listOf(Primary, FormDialogGradientEnd))
                            )
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
                                    imageVector        = icon,
                                    contentDescription = null,
                                    tint               = Color.White,
                                    modifier           = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text       = title,
                                    style      = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = Color.White
                                )
                                Text(
                                    text  = subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }
                        }
                    }

                    // ── Scrollable body ───────────────────────────────────
                    // heightIn(max = bodyMaxHeight) ensures the body never grows
                    // past the available space, keeping the footer always on screen.
                    Column(
                        modifier = Modifier
                            .heightIn(max = bodyMaxHeight)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        content = content
                    )

                    // ── Footer ────────────────────────────────────────────
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick  = onDismissRequest,
                            enabled  = !isSubmitting,
                            modifier = Modifier.weight(1f)
                        ) { Text(dismissLabel) }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    if (confirmEnabled)
                                        Brush.horizontalGradient(listOf(Primary, FormDialogGradientEnd))
                                    else
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                )
                                .clickable(
                                    enabled           = confirmEnabled,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication        = null,
                                    onClick           = onConfirm
                                ),
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
                                    text       = confirmLabel,
                                    style      = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = if (confirmEnabled) Color.White
                                                 else MaterialTheme.colorScheme.onSurfaceVariant
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

@Preview(showBackground = true)
@Composable
private fun RancakFormDialogPreview() {
    RancakTheme {
        RancakFormDialog(
            icon             = Icons.Default.Edit,
            title            = "Tambah Kategori",
            subtitle         = "Isi detail kategori baru",
            onDismissRequest = {},
            confirmLabel     = "Simpan",
            onConfirm        = {},
            confirmEnabled   = true,
            isSubmitting     = false
        ) {
            OutlinedTextField(
                value         = "Makanan",
                onValueChange = {},
                label         = { Text("Nama Kategori *") },
                modifier      = Modifier.fillMaxWidth(),
                shape         = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value         = "",
                onValueChange = {},
                label         = { Text("Deskripsi") },
                modifier      = Modifier.fillMaxWidth(),
                maxLines      = 3,
                shape         = MaterialTheme.shapes.medium
            )
        }
    }
}
