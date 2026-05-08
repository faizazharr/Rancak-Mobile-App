package id.rancak.app.presentation.ui.pos.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.OrderType
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakTheme
import id.rancak.app.presentation.ui.pos.FeeInputDialog
import id.rancak.app.presentation.ui.pos.feeFormatNumber
import id.rancak.app.presentation.util.formatRupiah
import id.rancak.app.presentation.viewmodel.CartUiState

private val GradientEnd = Color(0xFF0B7A60)

// ── SummaryAndActions ─────────────────────────────────────────────────────────

/**
 * Bagian bawah panel pesanan: subtotal, grid fee (2×2), auto-fees, voucher,
 * total bayar, dan tombol Open Bill + Bayar.
 */
@Composable
internal fun OrderSummaryActions(
    cartState:         CartUiState,
    surface:           Color,
    primary:           Color,
    onSurface:         Color,
    onSurfaceVariant:  Color,
    hasItems:          Boolean,
    hasOpenShift:      Boolean,
    isHolding:         Boolean = false,
    holdError:         String? = null,
    onHoldErrorDismiss: () -> Unit = {},
    onDiscount:        (Long, Boolean) -> Unit,
    onTax:             (Long, Boolean) -> Unit,
    onAdminFee:        (Long, Boolean) -> Unit,
    onDeliveryFee:     (Long) -> Unit,
    onTip:             (Long) -> Unit,
    onVoucherCode:     (String) -> Unit,
    onSaveClick:       () -> Unit,
    onCheckoutClick:   () -> Unit
) {
    Surface(shadowElevation = 8.dp, color = surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // ── Subtotal row ─────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.45f))
                    )
                    Text("Subtotal", style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
                }
                Text(
                    formatRupiah(cartState.subtotal),
                    style      = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = onSurface
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Fee grid 2×2 ─────────────────────────────────────────────────
            val gridBorder = MaterialTheme.colorScheme.outlineVariant.copy(0.45f)
            val gridShape  = RoundedCornerShape(10.dp)
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(gridShape)
                    .border(1.dp, gridBorder, gridShape)
            ) {
                Column {
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        FeeCellItem(
                            label             = "Diskon",
                            icon              = Icons.Default.Discount,
                            value             = cartState.discountInput,
                            onValue           = onDiscount,
                            isNegative        = true,
                            showPercentToggle = true,
                            valueIsPercent    = cartState.discountIsPercent,
                            computedAmount    = cartState.discount,
                            modifier          = Modifier.weight(1f)
                        )
                        Box(Modifier.fillMaxHeight().width(1.dp).background(gridBorder))
                        FeeCellItem(
                            label             = "Pajak",
                            icon              = Icons.Default.AccountBalance,
                            value             = cartState.taxInput,
                            onValue           = onTax,
                            showPercentToggle = true,
                            valueIsPercent    = cartState.taxIsPercent,
                            computedAmount    = cartState.tax,
                            modifier          = Modifier.weight(1f)
                        )
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(gridBorder))
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        FeeCellItem(
                            label             = "Biaya Admin",
                            icon              = Icons.Default.Receipt,
                            value             = cartState.adminFeeInput,
                            onValue           = onAdminFee,
                            showPercentToggle = true,
                            valueIsPercent    = cartState.adminFeeIsPercent,
                            computedAmount    = cartState.adminFee,
                            modifier          = Modifier.weight(1f)
                        )
                        Box(Modifier.fillMaxHeight().width(1.dp).background(gridBorder))
                        if (cartState.orderType == OrderType.DELIVERY) {
                            FeeCellItem(
                                label    = "Ongkir",
                                icon     = Icons.Default.DeliveryDining,
                                value    = cartState.deliveryFee,
                                onValue  = { v, _ -> onDeliveryFee(v) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            FeeCellItem(
                                label    = "Tip",
                                icon     = Icons.Default.Favorite,
                                value    = cartState.tip,
                                onValue  = { v, _ -> onTip(v) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Auto-fee rows dari konfigurasi Pricing
            cartState.activeTaxConfigs.forEach { cfg ->
                AutoFeeRow(
                    label  = "${cfg.name} (${cfg.rate}%)",
                    amount = run {
                        val basis = if (cfg.applyTo == "subtotal") cartState.subtotal
                                    else (cartState.subtotal - cartState.discount).coerceAtLeast(0L)
                        ((basis * (cfg.rate * 100).toLong()) / 10_000L).coerceAtLeast(0L)
                    },
                    onSurfaceVariant = onSurfaceVariant,
                    onSurface        = onSurface
                )
            }
            cartState.activeSurcharges.forEach { sc ->
                val raw = if (sc.isPercentage) {
                    val basis = (cartState.subtotal - cartState.discount).coerceAtLeast(0L)
                    (basis * sc.amount / 100L).coerceAtLeast(0L)
                } else sc.amount
                val amt = sc.maxAmount?.let { cap -> raw.coerceAtMost(cap) } ?: raw
                AutoFeeRow(
                    label  = sc.name + if (sc.isPercentage) " (${sc.amount}%)" else "",
                    amount = amt,
                    onSurfaceVariant = onSurfaceVariant,
                    onSurface        = onSurface
                )
            }



            VoucherInputRow(
                value   = cartState.voucherCode,
                primary = primary,
                onApply = onVoucherCode
            )

            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
            Spacer(Modifier.height(10.dp))

            // Hold error chip
            AnimatedVisibility(
                visible = holdError != null,
                enter   = expandVertically(tween(220)) + fadeIn(tween(180)),
                exit    = shrinkVertically(tween(200)) + fadeOut(tween(150))
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .clickable(onClick = onHoldErrorDismiss)
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                holdError ?: "",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Text("✕", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Bottom bar: Total (kiri) + CTA (kanan) ────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Bayar
                Column {
                    Text(
                        "TOTAL BAYAR",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = onSurfaceVariant
                    )
                    val totalColor by animateColorAsState(
                        if (hasItems && hasOpenShift) primary else onSurface,
                        tween(300), label = "TotalColor"
                    )
                    Text(
                        formatRupiah(cartState.total),
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = totalColor
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ── Open Bill button ──────────────────────────────────────
                    val isUpdate       = cartState.activeOpenBillId != null
                    val billActive     = hasItems && !isHolding
                    val billBorderColor by animateColorAsState(
                        if (billActive) primary.copy(0.28f) else Color.Transparent,
                        tween(220), label = "BillBorder"
                    )
                    val billContentColor by animateColorAsState(
                        if (billActive) onSurface else onSurfaceVariant.copy(0.35f),
                        tween(220), label = "BillContent"
                    )

                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, billBorderColor, MaterialTheme.shapes.medium)
                            .clickable(enabled = billActive, onClick = onSaveClick)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isHolding) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(16.dp),
                                color       = primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(Icons.Default.BookmarkBorder, null, Modifier.size(16.dp), tint = billContentColor)
                                Text(
                                    if (isUpdate) "Perbarui" else "Open Bill",
                                    style      = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color      = billContentColor
                                )
                            }
                        }
                    }

                    // ── Bayar button (gradient when active) ───────────────────
                    val canPay      = hasItems && hasOpenShift
                    val gradStart   by animateColorAsState(
                        when { !hasItems -> MaterialTheme.colorScheme.outlineVariant.copy(0.4f); !hasOpenShift -> MaterialTheme.colorScheme.errorContainer; else -> Primary },
                        tween(250), label = "PayGradStart"
                    )
                    val gradEnd2    by animateColorAsState(
                        when { !hasItems -> MaterialTheme.colorScheme.outlineVariant.copy(0.4f); !hasOpenShift -> MaterialTheme.colorScheme.errorContainer; else -> GradientEnd },
                        tween(250), label = "PayGradEnd"
                    )
                    val payIconTint by animateColorAsState(
                        when { !hasItems -> onSurfaceVariant; !hasOpenShift -> MaterialTheme.colorScheme.onErrorContainer; else -> Color.White },
                        tween(200), label = "PayIconTint"
                    )
                    val payTextColor by animateColorAsState(
                        when { !hasItems -> onSurfaceVariant; !hasOpenShift -> MaterialTheme.colorScheme.onErrorContainer; else -> Color.White },
                        tween(200), label = "PayTextColor"
                    )

                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .background(Brush.horizontalGradient(listOf(gradStart, gradEnd2)))
                            .clickable(enabled = canPay, onClick = onCheckoutClick)
                            .padding(horizontal = 22.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Payment, null, Modifier.size(16.dp), tint = payIconTint)
                            AnimatedContent(
                                targetState  = when { !hasItems -> "Pilih Produk"; !hasOpenShift -> "Buka Shift"; else -> "Bayar" },
                                transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                                label        = "PayLabel"
                            ) { label ->
                                Text(
                                    label,
                                    style      = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = payTextColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── FeeCellItem ───────────────────────────────────────────────────────────────

@Composable
private fun FeeCellItem(
    label:             String,
    icon:              ImageVector,
    value:             Long,
    onValue:           (Long, Boolean) -> Unit,
    isNegative:        Boolean = false,
    showPercentToggle: Boolean = false,
    valueIsPercent:    Boolean = false,
    computedAmount:    Long    = 0L,
    modifier:          Modifier = Modifier
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary          = MaterialTheme.colorScheme.primary
    val error            = MaterialTheme.colorScheme.error
    var showDialog by remember { mutableStateOf(false) }

    val filled     = value > 0L
    val valueColor = if (isNegative) error else primary
    val cellBg     by animateColorAsState(
        if (filled) (if (isNegative) error else primary).copy(alpha = 0.06f)
        else Color.Transparent,
        tween(220), label = "FeeCellBg"
    )
    val iconTint   by animateColorAsState(
        if (filled) (if (isNegative) error else primary).copy(0.75f) else onSurfaceVariant.copy(0.5f),
        tween(220), label = "FeeCellIcon"
    )
    val labelColor by animateColorAsState(
        if (filled) (if (isNegative) error else primary).copy(0.8f) else onSurfaceVariant,
        tween(220), label = "FeeCellLabel"
    )

    if (showDialog) {
        FeeInputDialog(
            title             = label,
            icon              = icon,
            initialValue      = value,
            isNegative        = isNegative,
            showPercentToggle = showPercentToggle,
            initialIsPercent  = valueIsPercent,
            onDismiss         = { showDialog = false },
            onConfirm         = { amount, isPercent ->
                onValue(amount, isPercent)
                showDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .background(cellBg)
            .clickable { showDialog = true }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, Modifier.size(11.dp), tint = iconTint)
            Text(
                label.uppercase(),
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = labelColor,
                fontSize   = 10.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        AnimatedContent(
            targetState  = filled,
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
            label        = "FeeCellValue_$label"
        ) { hasFilled ->
            if (hasFilled) {
                if (valueIsPercent) {
                    Text(
                        "${value}%  ·  Rp ${feeFormatNumber(computedAmount)}",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = valueColor
                    )
                } else {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (isNegative) Text(
                            "−",
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = error
                        )
                        Text(
                            formatRupiah(value),
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color      = valueColor
                        )
                    }
                }
            } else {
                Text(
                    if (isNegative) "— Tambahkan" else "+ Tambahkan",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color      = if (isNegative) error.copy(0.55f) else primary.copy(0.55f)
                )
            }
        }
    }
}

// ── FeeInputRow ───────────────────────────────────────────────────────────────

@Composable
private fun FeeInputRow(
    label:             String,
    icon:              ImageVector,
    value:             Long,
    onValue:           (Long, Boolean) -> Unit,
    isNegative:        Boolean = false,
    showPercentToggle: Boolean = false,
    valueIsPercent:    Boolean = false,
    computedAmount:    Long    = 0L
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary          = MaterialTheme.colorScheme.primary
    val error            = MaterialTheme.colorScheme.error
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        FeeInputDialog(
            title             = label,
            icon              = icon,
            initialValue      = value,
            isNegative        = isNegative,
            showPercentToggle = showPercentToggle,
            initialIsPercent  = valueIsPercent,
            onDismiss         = { showDialog = false },
            onConfirm         = { amount, isPercent ->
                onValue(amount, isPercent)
                showDialog = false
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { showDialog = true }
            .padding(vertical = 5.dp, horizontal = 2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier              = Modifier.weight(1f)
        ) {
            Icon(icon, null, Modifier.size(12.dp), tint = onSurfaceVariant.copy(0.6f))
            Text(label, style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant)
        }
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (value > 0L) {
                if (isNegative) Text("−", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = error)
                val valueColor = if (isNegative) error else primary
                if (valueIsPercent) {
                    Text("${value}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = valueColor)
                    Text("·", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant.copy(0.4f))
                    Text("Rp ${feeFormatNumber(computedAmount)}", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant.copy(0.75f))
                } else {
                    Text("Rp ${feeFormatNumber(value)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = valueColor)
                }
            } else {
                Text("Ketuk untuk isi", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant.copy(0.35f))
            }
            Icon(Icons.Default.ChevronRight, null, Modifier.size(12.dp), tint = onSurfaceVariant.copy(0.30f))
        }
    }
}

// ── AutoFeeRow ────────────────────────────────────────────────────────────────

@Composable
private fun AutoFeeRow(
    label:           String,
    amount:          Long,
    onSurfaceVariant: Color,
    onSurface:       Color
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = onSurfaceVariant)
        Text(
            formatRupiah(amount),
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color      = onSurface
        )
    }
}

// ── VoucherInputRow ───────────────────────────────────────────────────────────

@Composable
private fun VoucherInputRow(
    value:   String,
    primary: Color,
    onApply: (String) -> Unit
) {
    var text    by remember(value) { mutableStateOf(value) }
    val keyboard = LocalSoftwareKeyboardController.current
    val applied  = value.isNotBlank() && value == text

    val fieldBorderColor by animateColorAsState(
        if (applied) primary.copy(0.7f) else MaterialTheme.colorScheme.outlineVariant.copy(0.7f),
        tween(220), label = "VoucherBorder"
    )
    val fieldBg by animateColorAsState(
        if (applied) primary.copy(0.06f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.6f),
        tween(220), label = "VoucherBg"
    )
    val iconTint by animateColorAsState(
        if (applied) primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
        tween(220), label = "VoucherIcon"
    )

    Spacer(Modifier.height(4.dp))
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Default.LocalOffer, null, Modifier.size(15.dp), tint = iconTint)
        // Input field
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(fieldBg)
                .border(1.dp, fieldBorderColor, MaterialTheme.shapes.medium)
                .padding(horizontal = 10.dp, vertical = 9.dp)
        ) {
            if (text.isEmpty()) {
                Text(
                    "Kode voucher",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f)
                )
            }
            BasicTextField(
                value         = text,
                onValueChange = {
                    text = it.uppercase().trim()
                    if (it.isBlank()) onApply("")
                },
                singleLine    = true,
                textStyle     = MaterialTheme.typography.bodySmall.copy(
                    color      = if (applied) primary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (applied) FontWeight.Bold else FontWeight.Normal
                ),
                cursorBrush   = SolidColor(primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.isNotBlank()) onApply(text)
                    keyboard?.hide()
                })
            )
        }
        // Apply / Clear button
        val btnBg by animateColorAsState(
            when {
                applied          -> MaterialTheme.colorScheme.errorContainer
                text.isNotBlank() -> primary
                else              -> MaterialTheme.colorScheme.outlineVariant.copy(0.4f)
            },
            tween(220), label = "VoucherBtnBg"
        )
        val btnText by animateColorAsState(
            when {
                applied          -> MaterialTheme.colorScheme.onErrorContainer
                text.isNotBlank() -> Color.White
                else              -> MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
            },
            tween(220), label = "VoucherBtnText"
        )
        Box(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .background(btnBg)
                .clickable(enabled = text.isNotBlank()) {
                    if (applied) { text = ""; onApply("") }
                    else { onApply(text); keyboard?.hide() }
                }
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            AnimatedContent(
                targetState  = if (applied) "Hapus" else "Pakai",
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(120)) },
                label        = "VoucherBtnLabel"
            ) { label ->
                Text(
                    label,
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = btnText
                )
            }
        }
    }
    AnimatedVisibility(
        visible = applied,
        enter   = expandVertically(tween(200)) + fadeIn(tween(160)),
        exit    = shrinkVertically(tween(180)) + fadeOut(tween(140))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier.padding(start = 21.dp, top = 3.dp)
        ) {
            Icon(Icons.Default.CheckCircle, null, Modifier.size(10.dp), tint = primary.copy(0.75f))
            Text(
                "Voucher \"$value\" diterapkan",
                style = MaterialTheme.typography.labelSmall,
                color = primary.copy(0.85f)
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, heightDp = 380)
@Composable
private fun OrderSummaryActionsPreview_WithItems() {
    RancakTheme {
        OrderSummaryActions(
            cartState          = CartUiState(
                items         = emptyList(),
                discountInput = 5_000L,
                taxInput      = 6_000L
            ),
            surface            = MaterialTheme.colorScheme.surface,
            primary            = MaterialTheme.colorScheme.primary,
            onSurface          = MaterialTheme.colorScheme.onSurface,
            onSurfaceVariant   = MaterialTheme.colorScheme.onSurfaceVariant,
            hasItems           = false,
            hasOpenShift       = true,
            onDiscount         = { _, _ -> },
            onTax              = { _, _ -> },
            onAdminFee         = { _, _ -> },
            onDeliveryFee      = {},
            onTip              = {},
            onVoucherCode      = {},
            onSaveClick        = {},
            onCheckoutClick    = {}
        )
    }
}
