package id.rancak.app.presentation.ui.pos

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.rancak.app.presentation.designsystem.RancakTheme

// ─────────────────────────────────────────────────────────────────────────────
// Dialog numpad untuk input nominal biaya
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FeeInputDialog(
    title:        String,
    icon:         ImageVector,
    initialValue: Long = 0L,
    isNegative:   Boolean = false,
    prefix:       String = "Rp ",   // kosong ("") untuk input qty/jumlah
    onDismiss:    () -> Unit,
    onConfirm:    (Long) -> Unit
) {
    var raw by remember {
        mutableStateOf(if (initialValue > 0L) initialValue.toString() else "")
    }

    val amount    = raw.toLongOrNull() ?: 0L
    val hasValue  = amount > 0L
    val primary   = MaterialTheme.colorScheme.primary
    val error     = MaterialTheme.colorScheme.error
    val valueColor = when {
        isNegative && hasValue -> error
        hasValue               -> primary
        else                   -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier      = Modifier.width(300.dp),
            shape         = RoundedCornerShape(24.dp),
            color         = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Header ──────────────────────────────────────────────────
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, null, Modifier.size(17.dp), tint = primary)
                    }
                    Text(
                        title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(14.dp))

                // ── Display nominal ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        if (isNegative && hasValue) {
                            Text(
                                "− (Pengurangan)",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = error.copy(alpha = 0.65f),
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            "$prefix${feeFormatNumber(amount)}",
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color      = valueColor,
                            fontSize   = 26.sp
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // ── Numpad ──────────────────────────────────────────────────
                val rows = listOf(
                    listOf("7", "8", "9"),
                    listOf("4", "5", "6"),
                    listOf("1", "2", "3"),
                    listOf("000", "0", "⌫")
                )

                rows.forEach { keyRow ->
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        keyRow.forEach { key ->
                            NumpadKey(
                                label      = key,
                                modifier   = Modifier.weight(1f),
                                isBackspace = key == "⌫",
                                onClick    = {
                                    raw = when (key) {
                                        "⌫"   -> raw.dropLast(1)
                                        "000" -> (raw + "000").trimStart('0').ifEmpty { "" }
                                            .let { if (it.length > 12) raw else raw + "000" }
                                        else  -> {
                                            val next = if (raw == "0") key else raw + key
                                            if (next.length > 12) raw else next
                                        }
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(4.dp))

                // ── Tombol aksi ─────────────────────────────────────────────
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick  = { onConfirm(raw.toLongOrNull() ?: 0L) },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = if (isNegative && hasValue) error else primary
                        )
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tombol numpad
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NumpadKey(
    label:       String,
    modifier:    Modifier = Modifier,
    isBackspace: Boolean  = false,
    onClick:     () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor by animateColorAsState(
        targetValue = when {
            isBackspace && isPressed -> MaterialTheme.colorScheme.errorContainer
            isBackspace              -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
            isPressed                -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else                     -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(80),
        label         = "numpad_bg"
    )

    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isBackspace) {
            Icon(
                Icons.Default.Backspace,
                contentDescription = "Hapus",
                tint   = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text       = label,
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign  = TextAlign.Center,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Format angka dengan pemisah ribuan (titik)
// ─────────────────────────────────────────────────────────────────────────────

// ── Previews ──

@Preview
@Composable
private fun FeeInputDialogPreview() {
    RancakTheme {
        FeeInputDialog(
            title = "Diskon",
            icon = Icons.Default.Check,
            initialValue = 15000L,
            isNegative = true,
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview
@Composable
private fun FeeInputDialogEmptyPreview() {
    RancakTheme {
        FeeInputDialog(
            title = "Biaya Admin",
            icon = Icons.Default.Check,
            initialValue = 0L,
            isNegative = false,
            onDismiss = {},
            onConfirm = {}
        )
    }
}

internal fun feeFormatNumber(value: Long): String {
    if (value == 0L) return "0"
    val str = value.toString()
    val sb  = StringBuilder()
    var count = 0
    for (i in str.indices.reversed()) {
        if (count > 0 && count % 3 == 0) sb.insert(0, '.')
        sb.insert(0, str[i])
        count++
    }
    return sb.toString()
}
