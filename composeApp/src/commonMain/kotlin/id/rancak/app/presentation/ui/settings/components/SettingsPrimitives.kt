package id.rancak.app.presentation.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
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
import id.rancak.app.data.printing.PrinterConnectionType
import id.rancak.app.data.printing.PrinterDevice
import id.rancak.app.presentation.designsystem.RancakTheme

// ─────────────────────────────────────────────────────────────────────────────
// Small building blocks shared by all Settings content panels.
// ─────────────────────────────────────────────────────────────────────────────

/** Compact card wrapper used by every settings section. */
@Composable
internal fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = MaterialTheme.shapes.medium,
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            content             = content
        )
    }
}

/** Primary-tinted filter chip — overrides Material3's default orange. */
@Composable
internal fun PrimaryFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    FilterChip(
        selected    = selected,
        onClick     = onClick,
        label       = label,
        modifier    = modifier,
        leadingIcon = leadingIcon,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            selectedLabelColor       = MaterialTheme.colorScheme.primary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled             = true,
            selected            = selected,
            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            selectedBorderWidth = 1.2.dp,
            borderColor         = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            borderWidth         = 0.8.dp
        )
    )
}

/** Icon-tinted section title used on the phone layout. */
@Composable
internal fun ContentSectionTitle(icon: ImageVector, title: String, color: Color) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(6.dp)).background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Text(
            title,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = color
        )
    }
}

/** Left navigation item (tablet layout only) for picking a settings category. */
@Composable
internal fun SettingsNavItem(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
    badgeOk: Boolean = true
) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
             else Color.Transparent

    Surface(
        shape    = MaterialTheme.shapes.medium,
        color    = bg,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    subtitle,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (badgeOk) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        badge,
                        modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (badgeOk) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** Row rendering a discovered bluetooth printer; shows save state and click affordance. */
@Composable
internal fun PrinterDeviceRow(
    device: PrinterDevice,
    isSaved: Boolean,
    isConnecting: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(enabled = !isSaved && !isConnecting, onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (isSaved) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.Bluetooth, contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSaved) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    device.name.ifBlank { "Unknown Device" },
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    device.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSaved) {
                Icon(
                    Icons.Default.CheckCircle, contentDescription = "Terpilih",
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    "Pilih",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun SettingsNavItemPreview() {
    RancakTheme {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SettingsNavItem(
                icon     = Icons.Default.Print,
                iconBg   = MaterialTheme.colorScheme.primary,
                title    = "Printer Kasir",
                subtitle = "Rancak-BT01",
                badge    = "Tersimpan",
                badgeOk  = true,
                selected = true,
                onClick  = {}
            )
            SettingsNavItem(
                icon     = Icons.Default.Print,
                iconBg   = Color(0xFF555555),
                title    = "Umum",
                subtitle = "Auto print mati",
                selected = false,
                onClick  = {}
            )
        }
    }
}

@Preview
@Composable
private fun PrinterDeviceRowPreview() {
    RancakTheme {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PrinterDeviceRow(
                device       = PrinterDevice("EPSON TM-T82", "AA:BB:CC:DD:EE:01", PrinterConnectionType.BLUETOOTH),
                isSaved      = true,
                isConnecting = false,
                onClick      = {}
            )
            PrinterDeviceRow(
                device       = PrinterDevice("Bluetooth Printer", "AA:BB:CC:DD:EE:02", PrinterConnectionType.BLUETOOTH),
                isSaved      = false,
                isConnecting = false,
                onClick      = {}
            )
        }
    }
}

@Preview
@Composable
private fun ContentSectionTitlePreview() {
    RancakTheme {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ContentSectionTitle(Icons.Default.Print, "Printer Kasir", Color(0xFF1A6B3C))
        }
    }
}
