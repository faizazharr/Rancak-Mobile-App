---
description: "Visual design rules for Rancak POS: token access, color usage, typography scale, spacing, shapes, elevation, component catalog, card anatomy, adaptive visual targets, and design anti-patterns."
applyTo: "composeApp/src/**/presentation/**/*.kt"
---

# Design System — Rancak POS

## Single Entry Point — `RancakDesign`

**Always** access design tokens through `RancakDesign` or `MaterialTheme`. Never hardcode a raw `Color(0xFF...)`, `dp`, `sp`, or `FontWeight` that duplicates a token.

```kotlin
// ✅ Correct — tokens, auto light/dark aware
RancakDesign.colors.primary           // MaterialTheme.colorScheme.primary
RancakDesign.type.titleMedium         // MaterialTheme.typography.titleMedium
RancakDesign.shapes.medium            // RoundedCornerShape(6.dp)
RancakDesign.spacing.md               // 16.dp
RancakDesign.elevation.card           // 1.dp
RancakDesign.sizes.formMaxWidth       // 560.dp
RancakDesign.semantic.success         // Color(0xFF2E7D32) in light theme

// Shorthand — both are equivalent:
RancakDesign.semantic    ==   RancakColors.semantic
RancakDesign.spacing     ==   RancakSpacing.current
RancakDesign.elevation   ==   RancakElevation.current
RancakDesign.sizes       ==   RancakSizes.current
```

---

## Color Tokens

### Material roles — when to use each

| Token | Usage |
|---|---|
| `primary` `#0D9373` | Teal — CTAs, top bar, selected states, price text, primary buttons |
| `onPrimary` | Text/icons on primary background (top bar icons, button labels) |
| `primaryContainer.copy(alpha=0.25f)` | Selected card background, active shift card |
| `secondary` `#E8772E` | Orange — accent, secondary action, optional highlights |
| `background` `#F2F3F5` | Screen background — enterprise gray, NOT white |
| `surface` | Card/dialog container background — white |
| `onSurface` | Primary text on cards |
| `onSurfaceVariant` | Secondary/supporting text, body in empty state |
| `outline` | Muted metadata, dividers, timestamps |
| `outlineVariant` | Subtle dividers, dashed lines |
| `error` | Destructive confirm button text, error state icon/text |
| `errorContainer` | Error chip background |

### Semantic colors — always via `RancakColors.semantic`

```kotlin
val sem = RancakColors.semantic

// Status
sem.success            // #2E7D32 — PAID, active, available
sem.warning            // #F9A825 — HELD, pending, occupied
sem.info               // #1976D2 — REFUNDED, info, reserved
// error = MaterialTheme.colorScheme.error — VOID, CANCELLED

// Table status
sem.statusAvailable    // #4CAF50
sem.statusOccupied     // #FF9800
sem.statusReserved     // #2196F3
sem.statusMaintenance  // #9E9E9E

// Payment method
sem.paymentCash        // #4CAF50
sem.paymentCard        // #2196F3
sem.paymentQris        // #9C27B0
sem.paymentTransfer    // #FF9800
```

**Rule:** Never write `Color(0xFF4CAF50)` — write `RancakColors.semantic.statusAvailable`. The dark theme uses different values; hardcoded hex breaks dark mode.

---

## Typography Scale

Always use `MaterialTheme.typography.XxxYyy`. Do **not** set `fontSize` directly on a `Text`.

| Style | Size | Weight | Use for |
|---|---|---|---|
| `headlineLarge` | 26sp SemiBold | 32sp lh | Major screen headlines (rarely used) |
| `headlineMedium` | 22sp SemiBold | – | Dashboard section headers |
| `headlineSmall` | 20sp SemiBold | – | Empty-state headline, form section title |
| `titleLarge` | 18sp SemiBold | – | Dialog titles, large card labels |
| `titleMedium` | 15sp Medium | – | Top bar title (+Bold), card primary line, KPI values (+Bold) |
| `titleSmall` | 13sp Medium | – | Section subheader (+SemiBold), card secondary line |
| `bodyLarge` | 14sp Normal | – | Standard body, error/empty messages |
| `bodyMedium` | 13sp Normal | – | Supporting description, dialog body |
| `bodySmall` | 11sp Normal | – | Field error messages (`errorMessage`) |
| `labelLarge` | 13sp Medium | – | Button text, filter chip labels |
| `labelMedium` | 11sp Medium | – | Badge text, category chip |
| `labelSmall` | 10sp Medium | – | Timestamps, metadata, status chip text (+SemiBold), inline tag |

**Typography rules:**
- Monetary values: `titleMedium` + `FontWeight.Bold` + `color = primary`
- Timestamps / metadata: `labelSmall` + `color = MaterialTheme.colorScheme.outline`
- Supporting description below a headline: `bodyMedium` + `color = onSurfaceVariant`
- Status chips: `labelSmall` + `FontWeight.SemiBold` + `color = statusColor`

---

## Spacing Scale

```kotlin
val sp = RancakDesign.spacing
// xxs=2  xs=4  sm=8  md=16  lg=24  xl=32  xxl=48
```

**Spacing rules — memorize these:**

| Context | Value |
|---|---|
| Inside a compact card | `padding(12.dp)` — use `sp.md` minus 4 |
| Inside a standard card | `padding(16.dp)` = `sp.md` |
| Inside a large/summary card | `padding(20.dp)` |
| Between cards in a LazyColumn | `verticalArrangement = Arrangement.spacedBy(sp.sm)` (8dp) |
| Between form fields | `verticalArrangement = Arrangement.spacedBy(12.dp)` |
| Between sections in a screen | `Spacer(Modifier.height(sp.xl))` (32dp) |
| Screen horizontal padding — phone | `padding(horizontal = sp.md)` (16dp) |
| Screen horizontal padding — tablet | `padding(horizontal = sp.lg)` (24dp) |

---

## Shape Scale

```kotlin
MaterialTheme.shapes.extraSmall  // 2dp — inline badge, tiny tag
MaterialTheme.shapes.small       // 4dp — row item, printer device row
MaterialTheme.shapes.medium      // 6dp — MOST cards, buttons, text fields, nav items ← default
MaterialTheme.shapes.large       // 8dp — summary card, highlighted container
MaterialTheme.shapes.extraLarge  // 10dp — bottom sheet handle, large hero card
```

`StatusChip` is the only component that deliberately uses `RoundedCornerShape(20.dp)` (pill shape) — that is defined in `StatusChip.kt` and must not be reproduced elsewhere.

---

## Elevation Scale

```kotlin
val el = RancakDesign.elevation
// card=1dp  cardSelected=2dp  raised=4dp  modal=8dp

// Standard card
Card(elevation = CardDefaults.cardElevation(defaultElevation = el.card)) { ... }

// Selected / highlighted card
Surface(tonalElevation = el.cardSelected, shadowElevation = 0.dp) { ... }

// FAB, top bar drop shadow
elevation = el.raised   // 4dp

// Dialog, bottom sheet
elevation = el.modal    // 8dp
```

---

## Component Catalog

### `RancakTopBar` — mandatory on every screen

```kotlin
RancakTopBar(
    title    = "Judul Layar",          // screen name, Bahasa Indonesia
    icon     = Icons.Default.Xxx,      // REQUIRED — represents the screen
    subtitle = "Sub-info opsional",    // optional — e.g. count or context
    onBack   = onNavigateBack,         // use onBack OR onMenu, not both
    actions  = { /* right-side icons */ }
)
```

- Background is always `primary` (teal) — do NOT override it
- Icons on the top bar use `onPrimary` tint (white)
- There is a decorative ghost icon at the right (10% opacity) — do NOT fight it with your own right slot

### `RancakButton` / `RancakOutlinedButton`

```kotlin
// Primary action (filled)
RancakButton(
    text      = "Simpan",
    onClick   = onSave,
    isLoading = uiState.isSubmitting,       // shows spinner, disables button
    enabled   = uiState.formIsValid,
    modifier  = Modifier.fillMaxWidth()     // always full width at bottom of form
)

// Secondary / cancel action (outlined)
RancakOutlinedButton(text = "Batal", onClick = onDismiss)
```

- Height is fixed at `40.dp` — do NOT set a taller modifier on buttons
- Shape is `shapes.medium` (6dp) — matches text fields
- In dialogs use `TextButton` for confirm/dismiss; `RancakButton` is for screen-level CTAs

### `RancakTextField`

```kotlin
RancakTextField(
    value         = uiState.fieldName,
    onValueChange = viewModel::onFieldChange,
    label         = "Nama Field",
    isError       = uiState.fieldError != null,
    errorMessage  = uiState.fieldError
)

// Currency field — use raw OutlinedTextField with prefix
OutlinedTextField(
    value    = uiState.amount,
    onValueChange = { v -> viewModel.onAmountChange(v.filter { it.isDigit() }) },
    label    = { Text("Jumlah") },
    prefix   = { Text("Rp ") },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    shape    = MaterialTheme.shapes.medium,
    modifier = Modifier.fillMaxWidth()
)
```

### `StatusChip`

```kotlin
StatusChip(text = "LUNAS",  color = RancakColors.semantic.success)
StatusChip(text = "VOID",   color = MaterialTheme.colorScheme.error)
StatusChip(text = "BELUM BAYAR", color = RancakColors.semantic.warning)
StatusChip(text = "REFUND", color = RancakColors.semantic.info)
```

### `RoleGate` / `RoleGatedScreen`

```kotlin
// Gate a single element
RoleGate(minRole = UserRole.ADMIN) {
    AdminOnlyButton()
}

// Gate an entire screen
RoleGatedScreen(minRole = UserRole.ADMIN) {
    AdminOnlyContent()
}
```

---

## Card Anatomy — Standard Patterns

### Content card (list item)

```kotlin
Card(
    modifier  = Modifier.fillMaxWidth(),
    shape     = MaterialTheme.shapes.medium,        // 6dp
    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // Primary line: titleMedium + Bold
        // Secondary line: labelSmall + onSurfaceVariant
        // Metadata: labelSmall + outline
    }
}
```

### Selected / highlighted card

```kotlin
Surface(
    shape           = MaterialTheme.shapes.medium,
    color           = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
    tonalElevation  = 2.dp,
    shadowElevation = 0.dp
) { ... }
```

### Summary / section card (e.g. ShiftScreen active shift)

```kotlin
Card(
    shape  = MaterialTheme.shapes.large,    // 8dp
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    )
) {
    Column(Modifier.padding(20.dp)) { ... }
}
```

### Left-edge status strip (e.g. SaleCard)

```kotlin
Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
    Box(
        modifier = Modifier.width(4.dp).fillMaxHeight()
            .background(statusColor)                     // 4dp wide colored stripe
    )
    Column(
        modifier = Modifier.weight(1f)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // content
    }
}
```

### Icon-in-colored-box (KPI card, settings nav item)

```kotlin
Box(
    modifier = Modifier
        .size(32.dp)                                        // or 28/36dp
        .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
    contentAlignment = Alignment.Center
) {
    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(17.dp))
}
```

---

## Adaptive Visual Targets

Same breakpoint everywhere: `maxWidth >= 600.dp` = tablet.

### Phone visual target
- Single column, full width
- FAB in bottom-right for create actions
- Detail opens as `ModalBottomSheet` (not a separate screen on phone)
- Form fields span full width
- Card list with `8.dp` vertical gap
- Horizontal padding: `16.dp`

### Tablet visual target
- Master-detail side by side (list 38%, detail 62%)
- NO FAB — add button appears inline in the list header or detail panel
- Detail opens in the right panel (never bottom sheet on tablet)
- Form fields constrained to `widthIn(max = 560.dp)`, centered
- 2-column grids for KPI cards, product grids
- Horizontal padding: `24.dp`

### Tablet "add" button — replace FAB

```kotlin
// Phone → FAB; Tablet → inline button in header
Scaffold(
    floatingActionButton = {
        if (!isTablet) {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    }
) { ... }

// In tablet header row:
if (isTablet) {
    RancakButton(text = "Tambah", onClick = onAdd, modifier = Modifier.width(120.dp))
}
```

---

## Design Anti-Patterns

```kotlin
// ❌ Hardcoded color — breaks dark mode, mismatches semantic meaning
color = Color(0xFF4CAF50)
// ✅ use token
color = RancakColors.semantic.success

// ❌ Raw dp for spacing — misaligned with scale
Modifier.padding(20.dp)   // on a compact card
// ✅ match the spacing system
Modifier.padding(12.dp)   // compact  |  16dp standard  |  20dp large

// ❌ Wrong shape for a card
shape = RoundedCornerShape(16.dp)   // too rounded — consumer app look
// ✅ shapes.medium is 6dp — ERP feels professional and sharp
shape = MaterialTheme.shapes.medium

// ❌ Card on top of card — nested elevation creates visual noise
Card { Card { ... } }
// ✅ use Surface for inner grouping, not another Card

// ❌ Full-width form on tablet — fields stretch to 800+ dp
Modifier.fillMaxWidth()    // on tablet
// ✅ constrain forms
Modifier.widthIn(max = RancakDesign.sizes.formMaxWidth)  // 560dp

// ❌ Custom top bar — breaks brand consistency
TopAppBar(title = { Text("...") })
// ✅ always RancakTopBar with the required icon param

// ❌ Hardcoded font size or weight
Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
// ✅ use typography style
Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)

// ❌ Touch target below 48dp
Button(modifier = Modifier.height(32.dp))
// ✅ minimum 48dp height for any tappable element (RancakButton is already 40dp — acceptable
// for inline actions; screen-level CTAs use fillMaxWidth which ensures enough area)

// ❌ Bottom sheet on tablet
if (!isTablet) ModalBottomSheet(...)   // missing else clause → nothing on tablet
// ✅ side panel on tablet, bottom sheet on phone:
if (isTablet) SideDetailPanel(item) else ModalBottomSheet { DetailContent(item) }

// ❌ Showing FAB on tablet
FloatingActionButton(onClick = onAdd)  // unconditional
// ✅ hide on tablet, show inline button instead
if (!isTablet) FloatingActionButton(onClick = onAdd)
```

---

## Quick Reference — Status → Color Mapping

| Status | Color token |
|---|---|
| PAID / Active / Success | `RancakColors.semantic.success` |
| HELD / Pending / Warning | `RancakColors.semantic.warning` |
| VOID / Error / Cancelled | `MaterialTheme.colorScheme.error` |
| REFUNDED / Info | `RancakColors.semantic.info` |
| Table: Available | `RancakColors.semantic.statusAvailable` |
| Table: Occupied | `RancakColors.semantic.statusOccupied` |
| Table: Reserved | `RancakColors.semantic.statusReserved` |
| Table: Maintenance | `RancakColors.semantic.statusMaintenance` |
| Payment: Cash | `RancakColors.semantic.paymentCash` |
| Payment: Card | `RancakColors.semantic.paymentCard` |
| Payment: QRIS | `RancakColors.semantic.paymentQris` |
| Payment: Transfer | `RancakColors.semantic.paymentTransfer` |
