package id.rancak.app.presentation.ui.reservations.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.Reservation
import id.rancak.app.presentation.designsystem.Info
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.designsystem.StatusAvailable
import id.rancak.app.presentation.designsystem.StatusMaintenance
import id.rancak.app.presentation.designsystem.Warning

/**
 * Card reservasi dengan aksen strip kiri berwarna status, avatar inisial,
 * dan tombol aksi kontekstual.
 */
@Composable
fun ReservationCard(
    reservation: Reservation,
    onConfirm: () -> Unit,
    onSeat: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onEdit: () -> Unit
) {
    val (statusLabel, statusColor) = statusVisuals(reservation.status)
    val initials = reservation.customerName
        .trim().split(" ")
        .take(2)
        .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }

    val isTerminal = reservation.status in listOf("completed", "cancelled", "no_show")

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTerminal) 0.dp else 1.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isTerminal)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Box {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // ── Accent strip kiri ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── Header: avatar + nama + status ───────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Inisial avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = initials,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = reservation.customerName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        reservation.customerPhone?.let {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Phone, null,
                                    modifier = Modifier.size(10.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Status badge
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text  = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // ── Info utama: waktu, tamu, meja ───────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(
                        icon  = Icons.Default.Schedule,
                        text  = formatTime(reservation.reservedAt),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    InfoChip(
                        icon  = Icons.Default.Group,
                        text  = "${reservation.partySize} orang",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    InfoChip(
                        icon  = Icons.Default.Timer,
                        text  = "${reservation.durationMinutes}m",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                reservation.tableName?.let {
                    InfoChip(
                        icon  = Icons.Default.TableBar,
                        text  = it,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                reservation.note?.takeIf { it.isNotBlank() }?.let {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Notes, null,
                            modifier = Modifier.size(13.dp).padding(top = 1.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 2
                        )
                    }
                }

                // ── Action buttons ───────────────────────────────────────────
                val isActive = reservation.status in listOf("pending", "confirmed", "seated")
                if (isActive) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (reservation.status) {
                            "pending" -> {
                                FilledTonalButton(
                                    onClick = onConfirm,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Konfirmasi", style = MaterialTheme.typography.labelMedium)
                                }
                                OutlinedButton(
                                    onClick = onEdit,
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                                }
                                OutlinedButton(
                                    onClick = onCancel,
                                    modifier = Modifier.wrapContentWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Batal", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            "confirmed" -> {
                                FilledTonalButton(
                                    onClick = onSeat,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.EventSeat, null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Tamu Tiba", style = MaterialTheme.typography.labelMedium)
                                }
                                OutlinedButton(
                                    onClick = onEdit,
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                                }
                                OutlinedButton(
                                    onClick = onCancel,
                                    modifier = Modifier.wrapContentWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Batal", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            "seated" -> {
                                Button(
                                    onClick = onComplete,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Done, null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Selesai", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

            // ── Stamp overlay untuk status terminal ──────────────────────────
            if (isTerminal) {
                val stampIcon = if (reservation.status == "completed") Icons.Default.CheckCircle
                                else Icons.Default.Cancel
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 12.dp)
                ) {
                    Icon(
                        imageVector        = stampIcon,
                        contentDescription = null,
                        modifier           = Modifier.size(20.dp),
                        tint               = statusColor.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String, color: Color) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.10f),
        modifier = Modifier.border(0.5.dp, color.copy(alpha = 0.25f), CircleShape)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(icon, contentDescription = null,
                modifier = Modifier.size(11.dp), tint = color.copy(alpha = 0.85f))
            Text(text,
                style = MaterialTheme.typography.labelSmall,
                color = color)
        }
    }
}

/** Potong ISO-8601 jadi jam:menit + tanggal singkat. */
private fun formatTime(isoString: String): String {
    // "2026-05-01T19:30" → "19:30 · 01 Mei"
    return try {
        val parts = isoString.split("T")
        val date  = parts.getOrElse(0) { "" }
        val time  = parts.getOrElse(1) { "" }.take(5)
        val dateP = date.split("-")
        val monthNames = listOf("", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
        val mo = dateP.getOrElse(1) { "0" }.toIntOrNull() ?: 0
        "${time} · ${dateP.getOrElse(2) { "" }} ${monthNames.getOrElse(mo) { "" }}"
    } catch (_: Exception) {
        isoString
    }
}

private fun statusVisuals(status: String): Pair<String, Color> = when (status) {
    "pending"   -> "Menunggu"      to Warning
    "confirmed" -> "Dikonfirmasi"  to Info
    "seated"    -> "Sedang Hadir"  to StatusAvailable
    "completed" -> "Selesai"       to StatusMaintenance
    "cancelled" -> "Dibatalkan"    to Color(0xFFBA1A1A)
    "no_show"   -> "Tidak Hadir"   to Color(0xFFBA1A1A)
    else        -> status          to Color(0xFF666666)
}

// ── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ReservationCardPreview() {
    RancakTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            ReservationCard(
                reservation = Reservation(
                    uuid = "1", customerName = "Budi Santoso",
                    customerPhone = "08123456789", partySize = 4,
                    reservedAt = "2026-05-01T19:30", durationMinutes = 90,
                    status = "pending", tableName = "Meja 3 · Indoor",
                    note = "Minta tempat sudut, ada anak kecil", createdAt = "2026-04-29T10:00"
                ),
                onConfirm = {}, onSeat = {}, onComplete = {}, onCancel = {}, onEdit = {}
            )
            ReservationCard(
                reservation = Reservation(
                    uuid = "2", customerName = "Siti Rahma",
                    customerPhone = null, partySize = 2,
                    reservedAt = "2026-05-01T20:00", durationMinutes = 60,
                    status = "confirmed", tableName = null, note = null,
                    createdAt = "2026-04-29T11:00"
                ),
                onConfirm = {}, onSeat = {}, onComplete = {}, onCancel = {}, onEdit = {}
            )
            ReservationCard(
                reservation = Reservation(
                    uuid = "3", customerName = "Ahmad Fauzi",
                    customerPhone = "0821111", partySize = 6,
                    reservedAt = "2026-05-01T18:00", durationMinutes = 120,
                    status = "seated", tableName = "Meja VIP",
                    note = null, createdAt = "2026-04-29T09:00"
                ),
                onConfirm = {}, onSeat = {}, onComplete = {}, onCancel = {}, onEdit = {}
            )
        }
    }
}
