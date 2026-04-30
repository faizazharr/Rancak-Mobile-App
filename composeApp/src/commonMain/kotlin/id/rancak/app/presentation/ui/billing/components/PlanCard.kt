package id.rancak.app.presentation.ui.billing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.rancak.app.domain.model.Plan
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.designsystem.Secondary
import id.rancak.app.presentation.ui.billing.formatPlanPrice
import id.rancak.app.presentation.ui.billing.linearGradientBrush

@Composable
fun PlanCard(
    plan: Plan,
    isCurrentPlan: Boolean,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    if (isCurrentPlan) {
        Card(
            modifier = modifier,
            shape = shape,
            border = androidx.compose.foundation.BorderStroke(2.dp, Primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradientBrush(listOf(Primary, Color(0xFF1DB88A))),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            plan.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (plan.isTrial) {
                                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.22f)) {
                                    Text("TRIAL", style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold, color = Color.White,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                                }
                            }
                            Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.22f)) {
                                Text("AKTIF", style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold, color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (plan.description != null) {
                        Text(plan.description, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
                            overflow = TextOverflow.Ellipsis)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(formatPlanPrice(plan.totalPrice), style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold, color = Primary)
                            Text("${plan.durationDays} hari" + if (plan.maxUsers != null) " · ${plan.maxUsers} user" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.CheckCircle, null, tint = Primary, modifier = Modifier.size(18.dp))
                            Text("Aktif", style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold, color = Primary)
                        }
                    }
                }
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Text(plan.name, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (plan.isTrial) SmallBadge("TRIAL", Secondary)
                }
                if (plan.description != null) {
                    Text(plan.description, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
                        overflow = TextOverflow.Ellipsis)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(formatPlanPrice(plan.totalPrice), style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${plan.durationDays} hari" + if (plan.maxUsers != null) " · ${plan.maxUsers} user" else "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = onSubscribe, shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                        Text(if (plan.isTrial) "Coba" else "Langganan",
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

private val samplePlan = Plan("1", "pro", "Pro Plan", "Paket terbaik untuk bisnis", 100000.0, 0.11, 30, 5, false, 111000.0)

@Preview
@Composable
private fun PlanCardInactivePreview() {
    RancakTheme {
        PlanCard(plan = samplePlan, isCurrentPlan = false, onSubscribe = {})
    }
}

@Preview
@Composable
private fun PlanCardActivePreview() {
    RancakTheme {
        PlanCard(plan = samplePlan, isCurrentPlan = true, onSubscribe = {})
    }
}
