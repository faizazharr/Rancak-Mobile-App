package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.presentation.designsystem.RancakTheme

/**
 * Compact search bar with inline scan shortcut for the POS screen.
 */
@Composable
internal fun PosSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary          = MaterialTheme.colorScheme.primary

    BasicTextField(
        value         = query,
        onValueChange = onQueryChange,
        singleLine    = true,
        textStyle     = MaterialTheme.typography.bodySmall.copy(
            color    = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        ),
        cursorBrush = SolidColor(primary),
        modifier    = modifier,
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(0.5f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Search, null,
                    Modifier.size(18.dp),
                    tint = onSurfaceVariant
                )
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            "Cari produk atau scan barcode...",
                            style    = MaterialTheme.typography.bodySmall,
                            fontSize = 14.sp,
                            color    = onSurfaceVariant.copy(0.55f)
                        )
                    }
                    inner()
                }
                if (query.isNotEmpty()) {
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(0.35f))
                            .clickable { onQueryChange("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(11.dp), tint = onSurfaceVariant)
                    }
                } else {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(primary.copy(0.1f))
                            .clickable(onClick = onScanClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner, null,
                            Modifier.size(16.dp),
                            tint = primary
                        )
                    }
                }
            }
        }
    )
}

@Preview
@Composable
private fun PosSearchBarPreview_Empty() {
    RancakTheme {
        PosSearchBar(query = "", onQueryChange = {}, onScanClick = {})
    }
}

@Preview
@Composable
private fun PosSearchBarPreview_Filled() {
    RancakTheme {
        PosSearchBar(query = "kopi", onQueryChange = {}, onScanClick = {})
    }
}
