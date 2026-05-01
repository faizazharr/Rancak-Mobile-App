# Adaptive Layout — Rancak POS

Use this prompt when you need to implement or fix a screen's layout for multiple
device sizes: small phones, large phones, and tablets — in both portrait and landscape.

---

## Context This Prompt Depends On

`copilot-instructions.md` is **always pre-loaded** by Copilot — you already have access to:
- Design system spacing tokens (`xs`=4dp, `sm`=8dp, `md`=16dp, `lg`=24dp, `xl`=32dp)
- Screen/Content composable split pattern (`XxxScreen` owns ViewModel, `XxxContent` is pure UI)
- Canonical reference files for layout (PosScreen, LoginScreen, SalesHistoryScreen, KdsScreen, ShiftScreen)
- Role-gating pattern (`currentRole.atLeast(UserRole.XXX)`)

**Do not re-read `copilot-instructions.md`.**

This prompt is self-contained for layout work. Read additional files only in these cases:

| Situation | Also read |
|---|---|
| Implementing a brand-new screen (ViewModel + data layer needed too) | `.github/prompts/generate-feature.prompt.md` |
| The screen has data issues or you're reviewing before merging | `.github/prompts/code-review.prompt.md` — use its Adaptive Layout checklist |
| Unclear which canonical file to reference for a layout pattern | `.github/CONTEXT_INDEX.md` |

> **Key rule:** Always read the **existing screen file** before implementing layout changes.
> The screen may already have `BoxWithConstraints` — adding a second one is wrong.
> Phase 1 of this prompt mandates reading the file first; do not skip it.

---

## How to Use

Describe what you need, for example:

> "Make the Inventory screen work on tablet — right now it's a single column on all devices."

> "The Report screen has too much empty space on tablet. Fix the layout."

> "Build a new Supplier list screen that looks good on both phone and 10-inch tablet."

> "The form on the Pricing screen feels cramped on phone and too wide on tablet."

---

## Phase 1 — Understand the Screen First

Before writing any layout code, answer these questions by reading the screen file:

1. **What is the primary content type?**
   - List of items → likely needs `GridCells.Adaptive` or master-detail
   - Form / input → likely needs constrained single-column
   - Dashboard / summary → likely needs tile grid
   - Detail view → likely needs constrained single-column with wider content

2. **What does the user DO on this screen?**
   - Rapid selection (cashier choosing products) → dense, touch-friendly grid
   - Reading detail (viewing a sale receipt) → comfortable, scannable layout
   - Data entry (filling a form) → single focused column, clear field separation
   - Monitoring (KDS, order board) → information density matters, auto-refresh

3. **Is there a natural master-detail relationship?**
   - If the user selects an item from a list to see its detail → use master-detail on tablet
   - If items are independent actions → grid or list with navigation is fine

4. **Does the current screen already have `BoxWithConstraints`?**
   - If yes, read the existing breakpoint logic before adding more
   - If no, you will add it

---

## Phase 2 — Choose the Right Pattern

### Pattern 1: Conditional branch — separate phone and tablet layouts

**When to use**: phone and tablet need fundamentally different structure.
Examples: POS screen, Login, Sales History, Product Management.

```kotlin
@Composable
fun XxxContent(uiState: XxxUiState, ...) {
    Scaffold(topBar = { RancakTopBar(...) }) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isTablet = maxWidth >= 600.dp
            if (isTablet) TabletXxxLayout(uiState, ...)
            else          PhoneXxxLayout(uiState, ...)
        }
    }
}

@Composable
private fun TabletXxxLayout(uiState: XxxUiState, ...) {
    // Tablet-specific UI — side-by-side, grid, master-detail
}

@Composable
private fun PhoneXxxLayout(uiState: XxxUiState, ...) {
    // Phone-specific UI — single column, sequential
}
```

**Also handle landscape on phones** for screens where width matters:
```kotlin
val isWide = maxWidth >= 600.dp || maxWidth > maxHeight
```
Reference: `PosScreen.kt` — uses `isWide` because a phone in landscape behaves like a narrow tablet.

---

### Pattern 2: Constrained single-column — same structure, capped width

**When to use**: forms, settings, detail views, confirmation screens.
The layout is the same for phone and tablet — but on tablet, it must not
stretch to full width (a 900 dp form is visually broken and hard to read).

```kotlin
@Composable
fun XxxContent(uiState: XxxUiState, ...) {
    Scaffold(topBar = { RancakTopBar(...) }) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isTablet = maxWidth >= 600.dp

            val contentModifier = if (isTablet)
                Modifier
                    .widthIn(max = 560.dp)
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
            else
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())

            Column(
                modifier = contentModifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // form fields, content sections
            }
        }
    }
}
```
Reference: `ShiftScreen.kt`

Max widths by content type:
| Content | Max width on tablet |
|---|---|
| Forms, settings | `560.dp` |
| Detail card / receipt | `640.dp` |
| Confirmation dialog content | `480.dp` |
| Full dashboard | `fillMaxWidth` (no cap — use internal column weights instead) |

---

### Pattern 3: Adaptive grid — auto-fill columns

**When to use**: product grids, card collections, tile dashboards.
The grid fills columns automatically based on available width.

```kotlin
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp),
    contentPadding = PaddingValues(12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement   = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxSize()
) {
    items(items, key = { it.uuid }) { item ->
        XxxCard(item = item, onClick = { onSelect(item) })
    }
}
```

Recommended `minSize` values by card type:
| Card type | minSize | Result on phone | Result on 10" tablet |
|---|---|---|---|
| Product card (compact) | `140.dp` | 2 columns | 5–6 columns |
| Product card (with image) | `160.dp` | 2 columns | 4–5 columns |
| Order / KDS card | `260.dp` | 1 column | 2–3 columns |
| Table chip | `108.dp` | 3 columns | 6–7 columns |

Never use `GridCells.Fixed(n)` — it ignores screen width and produces a bad result on
either phone (too many columns, too cramped) or tablet (too few columns, too sparse).

---

### Pattern 4: Master-detail (tablet only)

**When to use**: any list where tapping an item shows its detail — inventory,
sales history, product management, reservations.

On **tablet**: list panel on the left (38%), detail panel on the right (62%).
On **phone**: list as full screen; detail navigates to a new screen or opens a bottom sheet.

```kotlin
// Tablet layout
@Composable
private fun TabletXxxLayout(uiState: XxxUiState, ...) {
    Row(Modifier.fillMaxSize()) {

        // Left: list panel
        Column(
            modifier = Modifier
                .weight(0.38f)
                .fillMaxHeight()
        ) {
            SearchBar(...)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.items) { item ->
                    XxxListItem(
                        item       = item,
                        isSelected = item.uuid == uiState.selectedItem?.uuid,
                        onClick    = { onSelect(item) }
                    )
                }
            }
        }

        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Right: detail panel
        Box(
            modifier = Modifier
                .weight(0.62f)
                .fillMaxHeight()
        ) {
            if (uiState.selectedItem != null) {
                XxxDetailPanel(item = uiState.selectedItem, ...)
            } else {
                EmptyDetailPlaceholder()  // hint: "Pilih item untuk melihat detail"
            }
        }
    }
}

// Phone layout — list + bottom sheet or navigation
@Composable
private fun PhoneXxxLayout(uiState: XxxUiState, ...) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(uiState.items) { item ->
            XxxListItem(item = item, onClick = { onSelect(item) })
        }
    }
    // Selected item → show as ModalBottomSheet or navigate to detail screen
    uiState.selectedItem?.let { item ->
        ModalBottomSheet(onDismissRequest = { onSelect(null) }) {
            XxxDetailPanel(item = item, ...)
        }
    }
}
```
Reference: `SalesHistoryScreen.kt`

---

## Spacing and Density

Rancak POS is used by cashiers under time pressure. The goal is **comfortable density**:
content is easy to tap and readable at a glance, with no large empty voids and no cramped
elements that are hard to distinguish.

### Spacing scale

| Token | dp | Use for |
|---|---|---|
| `xs` | `4.dp` | Icon-to-label gap, badge padding |
| `sm` | `8.dp` | Between cards in a grid, internal card elements |
| `md` | `16.dp` | Screen horizontal margin, between form sections |
| `lg` | `24.dp` | Between major content groups, section headers |
| `xl` | `32.dp` | Top/bottom of hero sections, empty state padding |

### Padding rules by context

```kotlin
// Screen edge — horizontal margin
Modifier.padding(horizontal = 16.dp)          // phone
Modifier.padding(horizontal = 24.dp)          // tablet (inside the constrained column)

// Card internal padding
Modifier.padding(horizontal = 12.dp, vertical = 8.dp)   // compact card (product grid)
Modifier.padding(horizontal = 16.dp, vertical = 12.dp)  // standard card (list item)
Modifier.padding(20.dp)                                  // large detail card

// Between list items
verticalArrangement = Arrangement.spacedBy(8.dp)   // standard
verticalArrangement = Arrangement.spacedBy(4.dp)   // dense (KDS, order board)

// Between form fields
verticalArrangement = Arrangement.spacedBy(12.dp)

// Section spacing
Spacer(Modifier.height(24.dp))   // between sections
Spacer(Modifier.height(32.dp))   // before the primary action button
```

### What "too much whitespace" looks like — and how to fix it

**Symptom**: tablet shows a single narrow column centered in an 800 dp screen with
400+ dp of grey on each side.
**Fix**: `widthIn(max = 560.dp)` only constrains the **content column** — the
`BoxWithConstraints` with `.fillMaxSize()` fills the screen, and `Alignment.Center`
places the column in the middle. The screen background color fills naturally.

**Symptom**: large empty area above content because of too much `Spacer` before the first item.
**Fix**: Use `contentPadding` on `LazyColumn`/`LazyVerticalGrid` instead of manual Spacers at the top.

**Symptom**: `LazyColumn` with huge `verticalArrangement = Arrangement.spacedBy(24.dp)` between cards.
**Fix**: Reduce to `8.dp` for card lists; `24.dp` is for major section separations.

### What "too cramped" looks like — and how to fix it

**Symptom**: touch targets smaller than 48 dp — user taps adjacent item accidentally.
**Fix**: ensure all tappable composables have `Modifier.heightIn(min = 48.dp)` or sufficient natural padding.

**Symptom**: text is truncated or overflowing because the card is too small.
**Fix**: increase the `minSize` in `GridCells.Adaptive`, or reduce the number of fields shown per card.

**Symptom**: insufficient contrast between card boundary and background.
**Fix**: increase card elevation slightly (`2.dp`), or add a border with `OutlineVariant` color.

---

## UX Decision Guide

### Dialog vs Bottom Sheet vs Navigate

| Situation | Use |
|---|---|
| Destructive action needing confirmation ("Void sale?") | `AlertDialog` — 2 buttons, short message |
| Simple input needed before proceeding (name, quantity) | `AlertDialog` with a `TextField` inside |
| Showing detail of a selected item on phone | `ModalBottomSheet` — dismissable, partial screen |
| Full editing form for a selected item | Navigate to a new route (full screen) |
| Quick filter or sort options | `ModalBottomSheet` |
| Error that blocks all action | Full-screen `ErrorScreen` or top `ErrorBanner` |
| Transient success/info feedback | `Snackbar` — non-blocking, auto-dismisses |

### When to use FAB vs toolbar action vs inline button

| Situation | Use |
|---|---|
| Primary action on the screen ("Create", "Add") | FAB in `Scaffold.floatingActionButton` slot |
| Destructive or secondary action | Trailing icon in top bar, or inline text button |
| Action on a specific list item | Swipe-to-reveal or contextual button inside the item card |
| Action only available after selection | Show in bottom bar or replace FAB when item is selected |

### When to use Card vs Surface vs just background color

| Use | When |
|---|---|
| `Card(elevation = 1.dp)` | Grouping related content, list items that need visual boundary |
| `Card(elevation = 0.dp)` | Sections inside a scrollable column that don't need depth |
| `Surface` | Full-page background, dialog contents |
| Raw `Box` with `background(color)` | Status chips, badges, section headers |

---

## Checklist — Adaptive Layout

After implementing, verify:

- [ ] `BoxWithConstraints` is used to detect available width — not `LocalConfiguration`
- [ ] Tablet threshold is `maxWidth >= 600.dp` — consistent with the codebase
- [ ] Phone and tablet use separate named composables when structures diverge significantly
- [ ] Single-column forms are constrained with `widthIn(max = 560.dp)` on tablet
- [ ] Grids use `GridCells.Adaptive` with appropriate `minSize` — not `GridCells.Fixed`
- [ ] Master-detail implemented on tablet for any list-with-detail screen
- [ ] Touch targets are at least `48.dp` in height
- [ ] Screen horizontal margin is `16.dp` on phone, `24.dp` inside constrained columns on tablet
- [ ] Card internal padding: `12/8.dp` (compact) or `16/12.dp` (standard) or `20.dp` (detail)
- [ ] No `Arrangement.spacedBy(24.dp)` between regular list items — that is section spacing
- [ ] Landscape on wide phones handled if the screen has a split-panel layout
- [ ] EmptyDetailPlaceholder shown on tablet master-detail when nothing is selected
- [ ] Bottom sheet used for item detail on phone when tablet uses a side panel
