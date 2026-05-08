package id.rancak.app.presentation.ui.pricing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.rancak.app.domain.model.Voucher
import id.rancak.app.presentation.components.DatePickerField
import id.rancak.app.presentation.designsystem.Primary
import id.rancak.app.presentation.designsystem.RancakColors
import id.rancak.app.presentation.designsystem.RancakTheme
import androidx.compose.animation.*
import androidx.compose.animation.core.*

private val GradientEnd = Color(0xFF0B7A60)

// ── Shared type selector button ───────────────────────────────────────────────

@Composable
private fun DiscountTypeButton(
    label: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor     by animateColorAsState(if (selected) color.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface, tween(220), label = "DiscTypeBg")
    val borderColor by animateColorAsState(if (selected) color else MaterialTheme.colorScheme.outlineVariant, tween(220), label = "DiscTypeBorder")
    val borderWidth by animateDpAsState(if (selected) 1.5.dp else 1.dp, tween(220), label = "DiscTypeBorderW")
    val textColor   by animateColorAsState(if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant, tween(220), label = "DiscTypeText")
    Surface(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication        = null,
            onClick           = onClick
        ),
        shape  = MaterialTheme.shapes.medium,
        color  = bgColor,
        border = androidx.compose.foundation.BorderStroke(width = borderWidth, color = borderColor)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color      = textColor
            )
        }
    }
}

// ── Gradient save button (reused in both content + panel) ─────────────────────

@Composable
private fun GradientSaveButton(
    canConfirm: Boolean,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val gradStart by animateColorAsState(if (canConfirm) Primary      else MaterialTheme.colorScheme.surfaceVariant, tween(250), label = "GradSaveStart")
    val gradEnd   by animateColorAsState(if (canConfirm) GradientEnd  else MaterialTheme.colorScheme.surfaceVariant, tween(250), label = "GradSaveEnd")
    val textColor by animateColorAsState(if (canConfirm) Color.White  else MaterialTheme.colorScheme.onSurfaceVariant, tween(250), label = "GradSaveText")

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Brush.horizontalGradient(listOf(gradStart, gradEnd)))
            .clickable(
                enabled           = canConfirm,
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSubmitting)
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
        else
            Text(
                "Simpan",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color      = textColor
            )
    }
}

// ── Section title helper ──────────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(
        text          = text,
        style         = MaterialTheme.typography.labelSmall,
        fontWeight    = FontWeight.Bold,
        color         = Primary,
        letterSpacing = 0.6.sp
    )
}

// ── Shared form body (LazyListScope) — single source of truth ─────────────────
private fun LazyListScope.voucherFormItems(
    code: String, onCode: (String) -> Unit,
    name: String, onName: (String) -> Unit,
    description: String, onDescription: (String) -> Unit,
    discountType: String, onDiscountType: (String) -> Unit,
    discountValue: String, onDiscountValue: (String) -> Unit, discountValueError: String?,
    maxDiscount: String, onMaxDiscount: (String) -> Unit,
    minPurchase: String, onMinPurchase: (String) -> Unit,
    validFrom: String, onValidFrom: (String) -> Unit,
    validUntil: String, onValidUntil: (String) -> Unit, validUntilError: String?,
    usageLimitText: String, onUsageLimit: (String) -> Unit,
    isActive: Boolean, onIsActive: (Boolean) -> Unit
) {
    val isPct = discountType == "pct"

    // ── Informasi Dasar ──────────────────────────────────────────────────────
    item { SectionTitle("INFORMASI DASAR") }
    item {
        OutlinedTextField(
            value         = code,
            onValueChange = { onCode(it.uppercase().filter { c -> c.isLetterOrDigit() }) },
            label         = { Text("Kode Voucher *") },
            supportingText = { Text("Unik, huruf kapital/angka") },
            leadingIcon   = { Icon(Icons.Default.Tag, null, Modifier.size(18.dp)) },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            shape         = MaterialTheme.shapes.medium
        )
    }
    item {
        OutlinedTextField(
            value         = name,
            onValueChange = onName,
            label         = { Text("Nama Voucher *") },
            leadingIcon   = { Icon(Icons.Default.Label, null, Modifier.size(18.dp)) },
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            shape         = MaterialTheme.shapes.medium
        )
    }
    item {
        OutlinedTextField(
            value         = description,
            onValueChange = onDescription,
            label         = { Text("Deskripsi (opsional)") },
            leadingIcon   = { Icon(Icons.Default.Notes, null, Modifier.size(18.dp)) },
            modifier      = Modifier.fillMaxWidth(),
            maxLines      = 3,
            shape         = MaterialTheme.shapes.medium
        )
    }

    // ── Konfigurasi Diskon ───────────────────────────────────────────────────
    item { HorizontalDivider() }
    item { SectionTitle("KONFIGURASI DISKON") }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Tipe Diskon", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DiscountTypeButton(
                    label    = "Persen (%)",
                    selected = isPct,
                    color    = Primary,
                    modifier = Modifier.weight(1f),
                    onClick  = { onDiscountType("pct") }
                )
                DiscountTypeButton(
                    label    = "Nominal (Rp)",
                    selected = !isPct,
                    color    = RancakColors.semantic.warning,
                    modifier = Modifier.weight(1f),
                    onClick  = { onDiscountType("nominal") }
                )
            }
        }
    }
    item {
        OutlinedTextField(
            value           = discountValue,
            onValueChange   = { onDiscountValue(it.filter { c -> c.isDigit() || c == '.' }) },
            label           = { Text(if (isPct) "Nilai Diskon (%) *" else "Nilai Diskon (Rp) *") },
            leadingIcon     = {
                AnimatedContent(
                    targetState  = isPct,
                    transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
                    label        = "DiscountLeadingIcon"
                ) { pct ->
                    Icon(if (pct) Icons.Default.Percent else Icons.Default.AttachMoney, null, Modifier.size(18.dp))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError         = discountValueError != null,
            supportingText  = discountValueError?.let { { Text(it) } },
            modifier        = Modifier.fillMaxWidth(),
            singleLine      = true,
            shape           = MaterialTheme.shapes.medium
        )
    }
    item {
        AnimatedVisibility(
            visible = isPct,
            enter   = expandVertically(tween(220)) + fadeIn(tween(200)),
            exit    = shrinkVertically(tween(200)) + fadeOut(tween(150))
        ) {
            OutlinedTextField(
                value           = maxDiscount,
                onValueChange   = { onMaxDiscount(it.filter { c -> c.isDigit() }) },
                label           = { Text("Maks. Nominal Diskon (Rp)") },
                leadingIcon     = { Icon(Icons.Default.MoneyOff, null, Modifier.size(18.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText  = { Text("Batasi nilai Rp meski persennya besar") },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                shape           = MaterialTheme.shapes.medium
            )
        }
    }
    item {
        OutlinedTextField(
            value           = minPurchase,
            onValueChange   = { onMinPurchase(it.filter { c -> c.isDigit() }) },
            label           = { Text("Min. Pembelian (Rp)") },
            leadingIcon     = { Icon(Icons.Default.ShoppingCart, null, Modifier.size(18.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            supportingText  = { Text("0 = tidak ada minimum") },
            modifier        = Modifier.fillMaxWidth(),
            singleLine      = true,
            shape           = MaterialTheme.shapes.medium
        )
    }

    // ── Periode Berlaku ──────────────────────────────────────────────────────
    item { HorizontalDivider() }
    item { SectionTitle("PERIODE BERLAKU") }
    item {
        DatePickerField(
            label          = "Berlaku Dari *",
            value          = validFrom,
            onDateSelected = onValidFrom,
            modifier       = Modifier.fillMaxWidth()
        )
    }
    item {
        DatePickerField(
            label          = "Berlaku Sampai (opsional)",
            value          = validUntil,
            onDateSelected = onValidUntil,
            isError        = validUntilError != null,
            supportingText = validUntilError?.let { { Text(it) } },
            modifier       = Modifier.fillMaxWidth()
        )
    }

    // ── Penggunaan & Status ──────────────────────────────────────────────────
    item { HorizontalDivider() }
    item { SectionTitle("PENGGUNAAN & STATUS") }
    item {
        OutlinedTextField(
            value           = usageLimitText,
            onValueChange   = { onUsageLimit(it.filter { c -> c.isDigit() }) },
            label           = { Text("Batas Penggunaan") },
            leadingIcon     = { Icon(Icons.Default.Repeat, null, Modifier.size(18.dp)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            supportingText  = { Text("Kosongkan = tidak terbatas") },
            modifier        = Modifier.fillMaxWidth(),
            singleLine      = true,
            shape           = MaterialTheme.shapes.medium
        )
    }
    item {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    val iconBg   by animateColorAsState(if (isActive) Primary.copy(0.12f) else MaterialTheme.colorScheme.outlineVariant.copy(0.3f), tween(220), label = "ActiveBg")
                    val iconTint by animateColorAsState(if (isActive) Primary else MaterialTheme.colorScheme.onSurfaceVariant, tween(220), label = "ActiveTint")
                    Box(
                        modifier         = Modifier
                            .size(32.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState  = isActive,
                            transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(100)) },
                            label        = "ActiveStatusIcon"
                        ) { active ->
                            Icon(
                                if (active) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                null,
                                tint     = iconTint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column {
                        Text("Voucher Aktif", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(
                            "Voucher bisa digunakan kasir",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(checked = isActive, onCheckedChange = onIsActive)
            }
        }
    }
}

// ── VoucherFormContent — full-screen scaffold (phone) ─────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherFormContent(
    editing: Voucher?,
    isSubmitting: Boolean,
    onBack: () -> Unit,
    onConfirm: (
        code: String, name: String, discountType: String, discountValue: String,
        validFrom: String, description: String?, maxDiscount: String?,
        minPurchase: String, usageLimit: Int?, validUntil: String?, isActive: Boolean
    ) -> Unit
) {
    var code           by remember(editing) { mutableStateOf(editing?.code ?: "") }
    var name           by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var discountType   by remember(editing) { mutableStateOf(editing?.discountType ?: "pct") }
    var discountValue  by remember(editing) { mutableStateOf(editing?.discountValue?.toString() ?: "") }
    var validFrom      by remember(editing) { mutableStateOf(editing?.validFrom?.take(10) ?: "") }
    var validUntil     by remember(editing) { mutableStateOf(editing?.validUntil?.take(10) ?: "") }
    var description    by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var maxDiscount    by remember(editing) { mutableStateOf(editing?.maxDiscount?.toString() ?: "") }
    var minPurchase    by remember(editing) { mutableStateOf(editing?.minPurchase?.toString() ?: "0") }
    var usageLimitText by remember(editing) { mutableStateOf(editing?.usageLimit?.toString() ?: "") }
    var isActive       by remember(editing) { mutableStateOf(editing?.isActive ?: true) }

    val isPct              = discountType == "pct"
    val discountValueNum   = discountValue.toDoubleOrNull()
    val discountValueError = when {
        discountValue.isBlank()             -> null
        discountValueNum == null            -> "Nilai tidak valid"
        isPct && discountValueNum > 100     -> "Persen tidak boleh melebihi 100"
        discountValueNum <= 0               -> "Nilai harus lebih dari 0"
        else                                -> null
    }
    val validUntilError = when {
        validUntil.isBlank() || validFrom.isBlank() -> null
        validUntil <= validFrom                      -> "Harus setelah tanggal berlaku"
        else                                         -> null
    }
    val canConfirm = !isSubmitting &&
        code.isNotBlank() && name.isNotBlank() &&
        discountValue.isNotBlank() && discountValueError == null &&
        validFrom.isNotBlank() && validUntilError == null

    fun doConfirm() = onConfirm(
        code.trim(), name.trim(), discountType, discountValue, validFrom,
        description.ifBlank { null },
        if (isPct) maxDiscount.ifBlank { null } else null,
        minPurchase.ifBlank { "0" }, usageLimitText.toIntOrNull(),
        validUntil.ifBlank { null }, isActive
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editing == null) "Tambah Voucher" else "Edit Voucher",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSubmitting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(onClick = onBack, enabled = !isSubmitting, modifier = Modifier.weight(0.35f)) {
                        Text("Batal")
                    }
                    GradientSaveButton(
                        canConfirm   = canConfirm,
                        isSubmitting = isSubmitting,
                        modifier     = Modifier.weight(0.65f),
                        onClick      = ::doConfirm
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(padding),
            contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            voucherFormItems(
                code = code, onCode = { code = it },
                name = name, onName = { name = it },
                description = description, onDescription = { description = it },
                discountType = discountType, onDiscountType = { discountType = it },
                discountValue = discountValue, onDiscountValue = { discountValue = it },
                discountValueError = discountValueError,
                maxDiscount = maxDiscount, onMaxDiscount = { maxDiscount = it },
                minPurchase = minPurchase, onMinPurchase = { minPurchase = it },
                validFrom = validFrom, onValidFrom = { validFrom = it },
                validUntil = validUntil, onValidUntil = { validUntil = it },
                validUntilError = validUntilError,
                usageLimitText = usageLimitText, onUsageLimit = { usageLimitText = it },
                isActive = isActive, onIsActive = { isActive = it }
            )
        }
    }
}

// ── VoucherFormPanel — embedded panel for tablet split layout ─────────────────

@Composable
fun VoucherFormPanel(
    editing: Voucher?,
    isSubmitting: Boolean,
    onClose: () -> Unit,
    onConfirm: (
        code: String, name: String, discountType: String, discountValue: String,
        validFrom: String, description: String?, maxDiscount: String?,
        minPurchase: String, usageLimit: Int?, validUntil: String?, isActive: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var code           by remember(editing) { mutableStateOf(editing?.code ?: "") }
    var name           by remember(editing) { mutableStateOf(editing?.name ?: "") }
    var discountType   by remember(editing) { mutableStateOf(editing?.discountType ?: "pct") }
    var discountValue  by remember(editing) { mutableStateOf(editing?.discountValue?.toString() ?: "") }
    var validFrom      by remember(editing) { mutableStateOf(editing?.validFrom?.take(10) ?: "") }
    var validUntil     by remember(editing) { mutableStateOf(editing?.validUntil?.take(10) ?: "") }
    var description    by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var maxDiscount    by remember(editing) { mutableStateOf(editing?.maxDiscount?.toString() ?: "") }
    var minPurchase    by remember(editing) { mutableStateOf(editing?.minPurchase?.toString() ?: "0") }
    var usageLimitText by remember(editing) { mutableStateOf(editing?.usageLimit?.toString() ?: "") }
    var isActive       by remember(editing) { mutableStateOf(editing?.isActive ?: true) }

    val isPct              = discountType == "pct"
    val discountValueNum   = discountValue.toDoubleOrNull()
    val discountValueError = when {
        discountValue.isBlank()             -> null
        discountValueNum == null            -> "Nilai tidak valid"
        isPct && discountValueNum > 100     -> "Persen tidak boleh melebihi 100"
        discountValueNum <= 0               -> "Nilai harus lebih dari 0"
        else                                -> null
    }
    val validUntilError = when {
        validUntil.isBlank() || validFrom.isBlank() -> null
        validUntil <= validFrom                      -> "Harus setelah tanggal berlaku"
        else                                         -> null
    }
    val canConfirm = !isSubmitting &&
        code.isNotBlank() && name.isNotBlank() &&
        discountValue.isNotBlank() && discountValueError == null &&
        validFrom.isNotBlank() && validUntilError == null

    fun doConfirm() = onConfirm(
        code.trim(), name.trim(), discountType, discountValue, validFrom,
        description.ifBlank { null },
        if (isPct) maxDiscount.ifBlank { null } else null,
        minPurchase.ifBlank { "0" }, usageLimitText.toIntOrNull(),
        validUntil.ifBlank { null }, isActive
    )

    Column(modifier = modifier) {
        // ── Gradient header ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Primary, GradientEnd)))
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { if (!isSubmitting) onClose() }) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        if (editing == null) "Tambah Voucher" else "Edit: ${editing.code}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                    Text(
                        if (editing == null) "Isi detail voucher di bawah" else editing.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
            }
        }

        // ── Scrollable form fields ────────────────────────────────────────────
        LazyColumn(
            modifier            = Modifier
                .weight(1f)
                .widthIn(max = 560.dp)
                .align(Alignment.CenterHorizontally),
            contentPadding      = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            voucherFormItems(
                code = code, onCode = { code = it },
                name = name, onName = { name = it },
                description = description, onDescription = { description = it },
                discountType = discountType, onDiscountType = { discountType = it },
                discountValue = discountValue, onDiscountValue = { discountValue = it },
                discountValueError = discountValueError,
                maxDiscount = maxDiscount, onMaxDiscount = { maxDiscount = it },
                minPurchase = minPurchase, onMinPurchase = { minPurchase = it },
                validFrom = validFrom, onValidFrom = { validFrom = it },
                validUntil = validUntil, onValidUntil = { validUntil = it },
                validUntilError = validUntilError,
                usageLimitText = usageLimitText, onUsageLimit = { usageLimitText = it },
                isActive = isActive, onIsActive = { isActive = it }
            )
        }

        // ── Action row ────────────────────────────────────────────────────────
        HorizontalDivider()
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            TextButton(onClick = { if (!isSubmitting) onClose() }, enabled = !isSubmitting) { Text("Batal") }
            Spacer(Modifier.width(10.dp))
            GradientSaveButton(
                canConfirm   = canConfirm,
                isSubmitting = isSubmitting,
                modifier     = Modifier.width(140.dp),
                onClick      = ::doConfirm
            )
        }
    }
}
