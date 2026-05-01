---
description: "Presentation layer patterns for Rancak POS: three ViewModel patterns, Screen/Content split, error handling, adaptive layout, role gating."
applyTo: "composeApp/src/**/presentation/**/*.kt"
---

# Presentation Layer Patterns — Rancak POS

## ViewModel — Three Patterns

Choose the **simplest** pattern that fits. Do not use `combine` when `.update` is sufficient.

### Pattern A — Simple state (one data source, no derived filtering)
Reference: `ShiftViewModel.kt`

```kotlin
data class XxxUiState(
    val isLoading: Boolean = true,   // start true → LoadingScreen on first render immediately
    val error: String? = null,
    val items: List<Xxx> = emptyList(),
    val selectedItem: Xxx? = null
)

class XxxViewModel(private val repo: XxxRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(XxxUiState())
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

    fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.listXxx()) {
                is Resource.Success -> _uiState.update { it.copy(items = result.data, isLoading = false) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    fun selectItem(item: Xxx) = _uiState.update { it.copy(selectedItem = item) }

    // clearError() is mandatory — Screen calls it after the error is shown
    fun clearError() = _uiState.update { it.copy(error = null) }
}
```

### Pattern B — Multiple independent flows → single combined UiState
Use when: search query + date filter + data from multiple repos must all combine.
Reference: `SalesHistoryViewModel.kt`

```kotlin
class SalesHistoryViewModel(private val repo: SaleRepository) : ViewModel() {

    private val _allSales    = MutableStateFlow<List<Sale>>(emptyList())
    private val _isLoading   = MutableStateFlow(true)
    private val _error       = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _dateFilter  = MutableStateFlow(DateFilter.ALL)

    val uiState: StateFlow<SalesHistoryUiState> = combine(
        _allSales, _isLoading, _error, _searchQuery, _dateFilter
    ) { allSales, loading, error, query, filter ->
        SalesHistoryUiState(
            sales     = allSales.filter { /* apply query + filter */ },
            isLoading = loading,
            error     = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        // WhileSubscribed(5_000): keeps the flow active for 5 seconds after the last
        // subscriber leaves (e.g. screen goes to background). This survives config changes
        // (rotation) without re-fetching, but cancels the flow if the user navigates away.
        // Do not change this value without understanding the lifecycle implications.
        initialValue = SalesHistoryUiState()
    )
}
```

### Pattern C — Pre-computed derived fields (heavy filtering / aggregation)
Use when: filtering or aggregating on state updates would run on every recomposition.
Reference: `PosViewModel.kt`

```kotlin
private fun PosUiState.recompute(): PosUiState {
    val filtered = products
        .filter { it.isActive }
        .filter { selectedCategory == null || it.category?.uuid == selectedCategory.uuid }
        .filter { searchQuery.isBlank() || it.name.lowercase().contains(searchQuery.lowercase()) }
    return copy(filteredProducts = filtered)
}

// Call recompute() whenever source data changes:
_uiState.update { it.copy(searchQuery = query).recompute() }
```

### When to use role gating in a ViewModel

If the feature has Admin/Owner-only actions, inject `tokenManager`:

```kotlin
class XxxViewModel(
    private val repo: XxxRepository,
    private val tokenManager: TokenManager  // inject when role gating is needed
) : ViewModel() {
    val currentRole: UserRole = tokenManager.userRole  // plain val, not StateFlow
}
// Koin resolves tokenManager automatically via viewModelOf(::XxxViewModel)
```

---

## Screen / Content Split — Mandatory

Every feature must have **two composables**: `XxxScreen` owns the ViewModel; `XxxContent` takes plain data only.

```kotlin
// XxxScreen — ViewModel owner, state collector, error handler
@Composable
fun XxxScreen(
    onNavigateBack: () -> Unit,
    viewModel: XxxViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadItems() }

    // Show error in snackbar, then clear so it doesn't reappear
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    XxxContent(
        uiState           = uiState,
        snackbarHostState = snackbarHostState,
        onNavigateBack    = onNavigateBack,
        onSelectItem      = viewModel::selectItem
    )
}

// XxxContent — pure UI, NO ViewModel, NO Flow
@Composable
fun XxxContent(
    uiState: XxxUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},      // default no-ops enable Previews without crashes
    onSelectItem: (Xxx) -> Unit = {}
) {
    Scaffold(
        topBar       = { RancakTopBar(title = "Judul Fitur", icon = Icons.Default.Xxx, onMenu = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading        -> LoadingScreen()
                uiState.error != null    -> ErrorScreen(uiState.error!!)  // non-null asserted — branch only reached when error != null
                uiState.items.isEmpty()  -> EmptyScreen("Belum ada data")
                else                     -> ItemList(uiState.items, onSelectItem)
            }
        }
    }
}

// Previews always target XxxContent, never XxxScreen
@Composable
private fun XxxPreview() {
    RancakTheme { XxxContent(uiState = XxxUiState(isLoading = false)) }
}
```

**Rules:**
- `XxxContent` parameters: plain data types and lambdas only — no ViewModel, no Flow, no coroutine scope
- All lambdas have default no-op values (`= {}` or `= null`) so `XxxContent` can be Previewed standalone
- Handle all four states: loading, error, empty, data-present — never skip any

---

## Adaptive Layout

Rancak POS runs on phones (360–430 dp) and tablets (600 dp+). Every Screen must handle both.

**Always use `BoxWithConstraints`** — never `LocalConfiguration` or display metrics.
**Tablet threshold: `maxWidth >= 600.dp`** — consistent throughout the entire codebase.

### Pattern 1 — Different structure per form factor (most features)

Extract phone and tablet layouts as separate private composables — they receive the same
parameters as `XxxContent` (forwarded verbatim). Never nest deeply-branched `if (isTablet)`
inside a single large composable.

```kotlin
// Inside XxxContent:
BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
    val isTablet = maxWidth >= 600.dp
    if (isTablet) TabletXxxLayout(uiState = uiState, onSelectItem = onSelectItem)
    else          PhoneXxxLayout(uiState = uiState, onSelectItem = onSelectItem)
}

// Declare as private composables in the same file:
@Composable
private fun TabletXxxLayout(
    uiState: XxxUiState,
    onSelectItem: (Xxx) -> Unit
) { /* side-by-side panels, grid, or master-detail */ }

@Composable
private fun PhoneXxxLayout(
    uiState: XxxUiState,
    onSelectItem: (Xxx) -> Unit
) { /* single column, sequential */ }
```

### Pattern 2 — Same structure, capped width (forms, detail views, settings)

`BoxWithConstraints` goes **inside** the `Scaffold` content lambda, not outside it.
The `Scaffold` handles the outer layout (top bar, snackbar, padding) — `BoxWithConstraints`
constrains only the content area:

```kotlin
Scaffold(topBar = { ... }, snackbarHost = { ... }) { padding ->
    // BoxWithConstraints goes here — inside the Scaffold content
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentAlignment = Alignment.TopCenter
    ) {
        val contentModifier = if (maxWidth >= 600.dp)
            Modifier
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        else
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)

        Column(modifier = contentModifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            /* form fields */
        }
    }
}
```

### Pattern 3 — Adaptive grid (product grids, card collections)
```kotlin
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp),  // auto-fills columns
    contentPadding = PaddingValues(12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement   = Arrangement.spacedBy(8.dp)
)
// Never use GridCells.Fixed(n) — it ignores screen width
```

### Pattern 4 — Master-detail (tablet: list + detail side by side)
```kotlin
Row(Modifier.fillMaxSize()) {
    Column(Modifier.weight(0.38f).fillMaxHeight()) { /* list panel */ }
    VerticalDivider()
    Box(Modifier.weight(0.62f).fillMaxHeight()) {
        if (selectedItem != null) DetailPanel(selectedItem)
        else EmptyDetailPlaceholder()  // never leave the panel blank
    }
}
```

**Wide/landscape phone** — same as tablet for split-panel screens:
```kotlin
val isWide = maxWidth >= 600.dp || maxWidth > maxHeight
```

### Spacing rules
| Context | Value |
|---|---|
| Screen edge margin | `16.dp` phone / `24.dp` tablet |
| Between cards in a list | `8.dp` (NOT 24 dp — that is section spacing) |
| Card internal padding | `12/8.dp` compact · `16/12.dp` standard · `20.dp` large |
| Between form fields | `12.dp` |
| Touch target minimum | `48.dp` height |

---

## Role Gating

**Role hierarchy** (lowest → highest): `STAFF < ADMIN < OWNER`
- `STAFF` — cashier, can create sales and view their own shift
- `ADMIN` — manager, can manage products, inventory, and view all reports
- `OWNER` — full access including billing, user management, and settings

`atLeast(role)` returns true if the current role is equal to or higher than the given role.
Always use `atLeast()` — never compare enum values directly (fragile if hierarchy changes).

```kotlin
// Gate UI in XxxContent — pass currentRole as a plain parameter
@Composable
fun XxxContent(
    uiState: XxxUiState,
    currentRole: UserRole = UserRole.STAFF,  // STAFF default: most restricted, safe for Preview
    onAdminAction: () -> Unit = {}
) {
    // Visible to ADMIN and OWNER, hidden from STAFF
    if (currentRole.atLeast(UserRole.ADMIN)) {
        AdminActionButton(onClick = onAdminAction)
    }
}

// ✅ Correct — uses role hierarchy
currentRole.atLeast(UserRole.ADMIN)

// ❌ Wrong — breaks if a new role is added between ADMIN and OWNER
currentRole == UserRole.OWNER || currentRole == UserRole.ADMIN
```

---

## UX Vocabulary

| Term | Implementation |
|---|---|
| Bottom sheet | `ModalBottomSheet` (for item detail on phone; tablet uses side panel) |
| Dialog | `AlertDialog` (destructive confirmations only) |
| Empty state | `EmptyScreen()` from `components/StateScreens.kt` |
| Loading state | `LoadingScreen()` from `components/StateScreens.kt` |
| Error state | `ErrorScreen()` from `components/StateScreens.kt` |
| Snackbar | `SnackbarHost` + `showSnackbar()` — non-blocking, auto-dismisses |
| FAB | `FloatingActionButton` in `Scaffold.floatingActionButton` slot |
| Top bar | Always `RancakTopBar` — never build a custom one |
| Status chip | `StatusChip` from `components/StatusChip.kt` |
