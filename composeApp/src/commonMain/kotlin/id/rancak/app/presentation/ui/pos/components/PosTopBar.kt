package id.rancak.app.presentation.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.presentation.designsystem.RancakTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * POS screen top bar. Shows outlet name, shift-open/closed chip, live clock,
 * and (on phone layout) a cart icon with an item count badge.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PosTopBar(
    outletName: String,
    hasCart: Boolean,
    itemCount: Int,
    hasOpenShift: Boolean,
    onMenuClick: () -> Unit,
    onCartClick: () -> Unit,
    showCart: Boolean = true
) {
    val primary = MaterialTheme.colorScheme.primary

    // Real-time clock HH:MM
    var currentTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val h = now.hour.toString().padStart(2, '0')
            val m = now.minute.toString().padStart(2, '0')
            currentTime = "$h:$m"
            delay(30_000L)
        }
    }

    TopAppBar(
        title = { PosTopBarTitle(outletName) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
            }
        },
        actions = {
            ShiftStatusChip(hasOpenShift)
            Spacer(Modifier.width(8.dp))
            if (currentTime.isNotEmpty()) {
                Text(
                    currentTime,
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }
            if (showCart) {
                CartActionButton(hasCart, itemCount, primary, onCartClick)
            } else {
                Spacer(Modifier.width(4.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
    )
}

@Composable
private fun PosTopBarTitle(outletName: String) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Kasir",
                style     = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color     = Color.White
            )
            if (outletName.isNotBlank()) {
                Box(
                    Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.45f))
                )
                Text(
                    outletName,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = Color.White.copy(0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ShiftStatusChip(hasOpenShift: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(if (hasOpenShift) 0.16f else 0.28f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (hasOpenShift) Color(0xFF4ADE80) else Color(0xFFFF4444))
            )
            Text(
                if (hasOpenShift) "Shift Buka" else "Shift Tutup",
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White
            )
        }
    }
}

@Composable
private fun CartActionButton(
    hasCart: Boolean,
    itemCount: Int,
    primary: Color,
    onCartClick: () -> Unit
) {
    BadgedBox(
        badge = {
            if (hasCart) Badge(
                containerColor = Color.White,
                contentColor   = primary
            ) {
                Text(
                    "$itemCount",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) {
        IconButton(onClick = onCartClick) {
            Icon(Icons.Default.ShoppingCart, null, tint = Color.White)
        }
    }
}

@Preview
@Composable
private fun PosTopBarPreview_OpenShift() {
    RancakTheme {
        PosTopBar(
            outletName   = "Warung Kopi Sinar",
            hasCart      = true,
            itemCount    = 3,
            hasOpenShift = true,
            onMenuClick  = {},
            onCartClick  = {},
            showCart     = true
        )
    }
}

@Preview
@Composable
private fun PosTopBarPreview_ClosedShift() {
    RancakTheme {
        PosTopBar(
            outletName   = "Warung Kopi Sinar",
            hasCart      = false,
            itemCount    = 0,
            hasOpenShift = false,
            onMenuClick  = {},
            onCartClick  = {},
            showCart     = false
        )
    }
}
