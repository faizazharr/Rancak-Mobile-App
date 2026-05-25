package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.viewmodel.CartUiState

@Composable
internal fun OrderCustomerRow(
    cartState:      CartUiState,
    surface:        Color,
    primary:        Color,
    onSurface:      Color,
    onSurfaceVariant: Color,
    onCustomerName: (String) -> Unit,
    onPax:          (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // ── Customer name field ───────────────────────────────────────────
        BasicTextField(
            value         = cartState.customerName,
            onValueChange = onCustomerName,
            singleLine    = true,
            textStyle     = MaterialTheme.typography.bodyMedium.copy(
                color    = onSurface
            ),
            cursorBrush = SolidColor(primary),
            modifier    = Modifier.weight(1f),
            decorationBox = { inner ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                            MaterialTheme.shapes.large
                        )
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = onSurfaceVariant)
                    Box(Modifier.weight(1f)) {
                        if (cartState.customerName.isEmpty()) {
                            Text(
                                "Nama customer",
                                style    = MaterialTheme.typography.bodyMedium,
                                color    = onSurfaceVariant.copy(0.5f)
                            )
                        }
                        inner()
                    }
                }
            }
        )

        // ── Pax stepper ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                    MaterialTheme.shapes.large
                )
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (cartState.pax > 1) MaterialTheme.colorScheme.outlineVariant.copy(0.35f)
                        else Color.Transparent
                    )
                    .clickable(enabled = cartState.pax > 1) { onPax(cartState.pax - 1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Remove, null,
                    Modifier.size(12.dp),
                    tint = if (cartState.pax > 1) onSurface else onSurfaceVariant.copy(0.3f)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${cartState.pax}",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = onSurface,
                    modifier   = Modifier.widthIn(min = 18.dp),
                    textAlign  = TextAlign.Center
                )
                Text(
                    "tamu",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant.copy(0.6f)
                )
            }

            Box(
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(primary)
                    .clickable { onPax(cartState.pax + 1) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun OrderCustomerRowPreview() {
    RancakTheme {
        OrderCustomerRow(
            cartState        = CartUiState(customerName = "Budi", pax = 3),
            surface          = MaterialTheme.colorScheme.surface,
            primary          = MaterialTheme.colorScheme.primary,
            onSurface        = MaterialTheme.colorScheme.onSurface,
            onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
            onCustomerName   = {},
            onPax            = {}
        )
    }
}
