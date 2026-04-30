# Rancak POS — GitHub Copilot Agent Instructions

You are a senior Kotlin Multiplatform engineer and architect working on **Rancak POS** —
a multi-tenant Point of Sale app for Android & iOS built with Compose Multiplatform.
All business logic and UI live in `commonMain` and are shared across both platforms.

Your role is not just to generate code. You are expected to:
- **Understand the full codebase** before making changes — read before writing
- **Reason about structural quality** — identify tech debt, inconsistencies, and violations
- **Propose and execute refactors** at any scale, from a single function to an entire layer
- **Push back** when a request would violate architecture rules, and explain why
- **Use the actual patterns that exist in the codebase**, not invented alternatives

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| UI | Compose Multiplatform | 1.10.3 |
| Language | Kotlin | 2.3.20 |
| Async | Kotlinx Coroutines | 1.10.2 |
| Serialization | Kotlinx Serialization | 1.11.0 |
| Networking | Ktor Client | 3.4.2 |
| DI | Koin | 4.2.1 |
| Navigation | Compose Navigation | 2.9.2 |
| Storage | multiplatform-settings | 1.3.0 |

---

## Architecture

**Clean Architecture + MVVM.** Dependency direction: `Presentation → Domain ← Data`.
The domain layer must have zero dependencies on data or presentation.

```
commonMain/kotlin/id/rancak/app/
├── data/
│   ├── local/         # TokenManager, OfflineSaleQueue, SettingsStore, SecureSettings
│   ├── mapper/        # *Mappers.kt  (DTO → domain model)
│   ├── remote/api/    # RancakApiService + per-domain extension files
│   ├── remote/dto/    # @Serializable request/response DTOs
│   ├── repository/    # *RepositoryImpl
│   └── util/          # RepositoryHelpers (safe/safeList/safeUnit), DateTimeUtils
├── di/                # AppModule.kt, PlatformModule.kt (expect)
├── domain/
│   ├── model/         # Pure Kotlin data classes — no platform imports
│   └── repository/    # Repository interfaces, one per domain
└── presentation/
    ├── components/    # Shared composables
    ├── designsystem/  # Color.kt, Theme.kt, Typography.kt
    ├── navigation/    # Screen.kt (type-safe routes), RancakNavHost.kt
    ├── ui/            # Screens by feature — always split Screen + Content
    ├── util/          # CurrencyFormatter.kt
    └── viewmodel/     # One ViewModel per feature, all in commonMain
```

---

## Canonical Reference Files

When in doubt about a pattern, read these files — they are the gold standard for this codebase:

| What | File | Why |
|---|---|---|
| Simple ViewModel | `presentation/viewmodel/ShiftViewModel.kt` | Clean single-repository, `.update {}` pattern |
| Complex ViewModel (filter state) | `presentation/viewmodel/SalesHistoryViewModel.kt` | `combine() + stateIn()` for derived state |
| Complex ViewModel (derived fields) | `presentation/viewmodel/PosViewModel.kt` | `recompute()` pattern for pre-computed derived fields |
| Screen split | `presentation/ui/shift/ShiftScreen.kt` | Gold-standard Screen + Content split |
| Repository impl | `data/repository/InventoryRepositoryImpl.kt` | Correct use of `safe/safeList/safeUnit` + Koin `singleOf...bind` |
| Koin registration | `di/AppModule.kt` | The only source of truth for DI registration style |
| API extension (simple) | `data/remote/api/AuthApi.kt` | Clean GET/POST extension functions, non-tenant-scoped |
| API extension (complex) | `data/remote/api/SaleApi.kt` | Idempotency key, query params, ByteArray responses, tenant-scoped |
| URL constants | `data/remote/api/ApiConstants.kt` | Single source of truth for all endpoint paths |
| DTO with FlexibleLong | `data/remote/dto/sale/SaleDtos.kt` | Monetary fields annotated with `FlexibleLongSerializer` |
| HTTP client config | `data/remote/HttpClientFactory.kt` | Auth plugin, DefaultRequest (X-API-Key), timeout, JSON config |

---

## Mandatory Code Patterns

### 1. Resource — async result wrapper

```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
```

Every repository method must return `Resource<T>`. Never throw exceptions across layer boundaries.

### 2. Repository helpers — always use, never inline the try/catch

File: `data/util/RepositoryHelpers.kt`

```kotlin
// GET single object
safe(
    block    = { api.getProduct(tenantId, id) },
    map      = { it.toDomain() },
    errorMsg = "Gagal memuat produk"        // ← always Bahasa Indonesia
)

// GET list
safeList(
    block    = { api.listProducts(tenantId) },
    errorMsg = "Gagal memuat daftar produk" // ← always Bahasa Indonesia
) { it.toDomain() }

// POST / PUT / DELETE with no meaningful return body
safeUnit(
    block    = { api.deleteProduct(tenantId, id) },
    errorMsg = "Gagal menghapus produk"     // ← always Bahasa Indonesia
)
```

### 3. ViewModel — two patterns, choose based on complexity

**Pattern A — Simple state** (single data source, no derived state across multiple flows):
Use `MutableStateFlow(UiState()) + .update { it.copy(...) }`.
Reference: `ShiftViewModel.kt`

```kotlin
class ShiftViewModel(private val repo: OperationsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ShiftUiState())
    val uiState: StateFlow<ShiftUiState> = _uiState.asStateFlow()

    fun loadCurrentShift() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repo.getCurrentShift()) {
                is Resource.Success -> _uiState.update { it.copy(currentShift = result.data, isLoading = false) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }
}
```

**Pattern B — Complex derived state** (multiple independent flows that produce a combined UiState):
Use `combine() + stateIn()`. Each concern gets its own private `MutableStateFlow`.
Reference: `SalesHistoryViewModel.kt`

```kotlin
class SalesHistoryViewModel(private val repo: SaleRepository) : ViewModel() {

    // Each independent concern has its own private flow
    private val _allSales     = MutableStateFlow<List<Sale>>(emptyList())
    private val _isLoading    = MutableStateFlow(false)
    private val _error        = MutableStateFlow<String?>(null)
    private val _searchQuery  = MutableStateFlow("")
    private val _dateFilter   = MutableStateFlow(DateFilter.ALL)

    // UiState is derived, never set directly
    val uiState: StateFlow<SalesHistoryUiState> = combine(
        _allSales, _isLoading, _error, _searchQuery, _dateFilter
    ) { allSales, loading, error, query, filter ->
        SalesHistoryUiState(
            sales     = applyFilters(allSales, query, filter),
            isLoading = loading,
            error     = error,
            // ...
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SalesHistoryUiState())
}
```

**Pattern C — Pre-computed derived fields** (expensive computations that should not run on every recomposition):
Use `recompute()` extension on the UiState itself.
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

**How to choose:**
- One data source, no filtering → Pattern A
- Multiple independent data sources that combine into one state → Pattern B
- Single state with expensive derived fields (filtering, aggregation) → Pattern C

### 4. Screen split — mandatory for every feature

```kotlin
// XxxScreen.kt — owns the ViewModel, collects state, calls Content
@Composable
fun XxxScreen(
    onNavigateBack: () -> Unit,
    viewModel: XxxViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadData() }
    XxxContent(
        uiState        = uiState,
        onNavigateBack = onNavigateBack,
        onAction       = viewModel::doAction
    )
}

// XxxContent — pure UI, receives only plain data + lambdas, no ViewModel reference
@Composable
fun XxxContent(
    uiState: XxxUiState,
    onNavigateBack: () -> Unit = {},
    onAction: () -> Unit = {}
) {
    Scaffold(/* ... */) { padding ->
        when {
            uiState.isLoading       -> LoadingScreen()
            uiState.error != null   -> ErrorScreen(uiState.error)
            uiState.data == null    -> EmptyScreen("Belum ada data")
            else                    -> { /* main content */ }
        }
    }
}

// Previews always use XxxContent (not XxxScreen) with a mock UiState
// @Preview — uncomment only temporarily during development
@Composable
private fun XxxPreview() {
    RancakTheme { XxxContent(uiState = XxxUiState(/* mock data */)) }
}
```

### 5. Koin registration — use the DSL shorthand, not the lambda form

The codebase uses Koin's shorthand DSL. Match this style exactly:

```kotlin
// ✅ Correct — shorthand DSL (matches AppModule.kt)
singleOf(::XxxRepositoryImpl) bind XxxRepository::class
viewModelOf(::XxxViewModel)

// ✅ Also correct when the constructor needs explicit wiring
single<XxxRepository> { XxxRepositoryImpl(get(), get(), get()) }

// ❌ Wrong — verbose form that the codebase does not use
single<XxxRepository> { XxxRepositoryImpl(get()) }  // only if truly needed
```

Registration location: repositories go in `repositoryModule`, ViewModels go in `viewModelModule` in `AppModule.kt`.

### 6. Tenant-scoped API

```kotlin
// Always read from TokenManager — never hardcode or pass tenantId as a parameter
private val tenantId get() = tokenManager.tenantUuid

// Endpoint pattern — /tenants/{tenantId}/resource
```

### 7. Navigation

```kotlin
// Screen.kt — type-safe route
@Serializable
data class XxxRoute(val id: String? = null) : Screen()

// RancakNavHost.kt
composable<XxxRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<XxxRoute>()
    XxxScreen(onNavigateBack = { navController.popBackStack() })
}
```

---

## Anti-Patterns — What NOT to Do

These are the most common mistakes. If you see any of these in existing code, flag them.

### ❌ Exposing MutableStateFlow from a ViewModel
```kotlin
// WRONG — consumers can modify state directly
val uiState = MutableStateFlow(XxxUiState())

// CORRECT
private val _uiState = MutableStateFlow(XxxUiState())
val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()
```

### ❌ Making API calls inside a ViewModel
```kotlin
// WRONG — data layer concern in presentation layer
class XxxViewModel : ViewModel() {
    fun load() {
        viewModelScope.launch {
            val response = httpClient.get(
                ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantId) + "/xxx"
            ).body<XxxResponse>()
        }
    }
}

// CORRECT — delegate to repository
class XxxViewModel(private val repo: XxxRepository) : ViewModel() {
    fun load() {
        viewModelScope.launch {
            when (val result = repo.getXxx()) { /* handle Resource */ }
        }
    }
}
```

### ❌ Putting business rules in a Screen composable
```kotlin
// WRONG — business rule in UI layer
@Composable
fun CartContent(uiState: CartUiState) {
    val canCheckout = uiState.items.isNotEmpty() && uiState.items.sumOf { it.price } >= 1000
    // ...
}

// CORRECT — business rule belongs in the domain model or ViewModel
data class CartUiState(
    val items: List<CartItem> = emptyList()
) {
    val canCheckout: Boolean get() = items.isNotEmpty() && totalAmount >= 1000
    val totalAmount: Long get() = items.sumOf { it.price * it.quantity }
}
```

### ❌ Inlining try/catch in a repository instead of using RepositoryHelpers
```kotlin
// WRONG — duplicates error handling logic
override suspend fun getShift(): Resource<Shift> {
    return try {
        val response = api.getShift(tenantId)
        if (response.isSuccess) Resource.Success(response.data!!.toDomain())
        else Resource.Error(response.message ?: "Error")
    } catch (e: Exception) { Resource.Error(e.message ?: "Error") }
}

// CORRECT — use the helper
override suspend fun getShift(): Resource<Shift> =
    safe(block = { api.getShift(tenantId) }, map = { it.toDomain() }, errorMsg = "Gagal memuat shift")
```

### ❌ Importing android.* or Apple frameworks in commonMain
```kotlin
// WRONG — breaks iOS compilation
import android.util.Log  // in commonMain

// CORRECT — use expect/actual or Kotlin's built-in alternatives
// In commonMain: expect fun log(tag: String, msg: String)
// In androidMain: actual fun log(tag: String, msg: String) { android.util.Log.d(tag, msg) }
```

### ❌ Using the wrong Koin registration for a ViewModel
```kotlin
// WRONG — ViewModel should never be a singleton
single { XxxViewModel(get()) }

// CORRECT
viewModelOf(::XxxViewModel)
```

### ❌ Hardcoding colors or spacing
```kotlin
// WRONG
Text(text = "Hello", color = Color(0xFF0D9373))
Spacer(Modifier.height(16.dp)) // only wrong if embedded in shared components without semantic meaning

// CORRECT
Text(text = "Hello", color = MaterialTheme.colorScheme.primary)
Spacer(Modifier.height(Spacing.md)) // or use the spacing scale consistently
```

---

## Non-Negotiable Rules

| # | Rule |
|---|---|
| 1 | No `android.*` or Apple imports in `commonMain` — use `expect`/`actual` |
| 2 | `MutableStateFlow` must never be exposed from a ViewModel |
| 3 | All prices are `Long` (integer Rupiah) — always display via `CurrencyFormatter.formatRupiah()` |
| 4 | All user-facing strings and error messages must be in **Bahasa Indonesia** |
| 5 | `POST /sales` requires `X-Idempotency-Key: <UUIDv4>` header |
| 6 | HTTP 409 → treat as success (idempotency duplicate) |
| 7 | HTTP 401 → refresh token → retry, never force logout |
| 8 | Role gating via `currentRole.atLeast(UserRole.ADMIN)` — Staff cannot see Admin/Owner features |
| 9 | Cashier must have an open shift before any sale can be created |
| 10 | QRIS payment is online-only — always check connectivity first |
| 11 | `errorMsg` in all `safe/safeList/safeUnit` calls must be in Bahasa Indonesia |

---

## Design System

Never hardcode colors, spacing, or typography. Always use tokens from `designsystem/`:

```kotlin
MaterialTheme.colorScheme.primary    // Teal #0D9373
MaterialTheme.colorScheme.secondary  // Orange #E8772E

// Table status chips
RancakColors.StatusAvailable / StatusOccupied / StatusReserved / StatusMaintenance

// Payment method badges
RancakColors.PaymentCash / PaymentCard / PaymentQris / PaymentTransfer

// Spacing scale — use these exact values
4.dp / 8.dp / 16.dp / 24.dp / 32.dp  // xs / sm / md / lg / xl
```

---

## Responsive & Adaptive Layout

Rancak POS runs on phones (360–430 dp wide) and tablets (600 dp+), often in landscape on tablets.
Every screen must look intentional on both — not just "zoomed in phone" on tablet,
and not cramped on small phones.

### Breakpoints

```kotlin
// The single breakpoint used throughout the entire codebase:
val isTablet = maxWidth >= 600.dp

// POS screen also checks landscape (wide phone in landscape = tablet-like):
val isWide = maxWidth >= 600.dp || maxWidth > maxHeight
```

Always derive from `BoxWithConstraints` — never from window metrics or device API.

### When to use which layout pattern

**Pattern 1 — Conditional branch (different structure per form factor)**
Use when phone and tablet need fundamentally different UI: phone = single-column sequential,
tablet = side-by-side panels, grid, or master-detail.

```kotlin
BoxWithConstraints(Modifier.fillMaxSize()) {
    val isTablet = maxWidth >= 600.dp
    if (isTablet) TabletLayout(uiState, actions)
    else          PhoneLayout(uiState, actions)
}
```
References: `LoginScreen`, `SalesHistoryScreen`, `TableMapScreen`

**Pattern 2 — Constrained single-column (same structure, capped width)**
Use for forms, detail views, and settings — where a single column is correct on both,
but should not stretch to full tablet width (would look empty and hard to read).

```kotlin
BoxWithConstraints(Modifier.fillMaxSize()) {
    val contentModifier = if (maxWidth >= 600.dp)
        Modifier.widthIn(max = 560.dp).align(Alignment.Center).verticalScroll(rememberScrollState())
    else
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())

    Column(modifier = contentModifier.padding(16.dp)) { /* form fields */ }
}
```
Reference: `ShiftScreen`

**Pattern 3 — Adaptive grid (auto-fill columns)**
Use for product grids, card collections, and any content that flows naturally into multiple columns.

```kotlin
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp),  // auto-fills columns ≥ 160 dp
    contentPadding = PaddingValues(12.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement   = Arrangement.spacedBy(8.dp)
)
```
References: `ProductGridContent`, `KdsScreen`, `AddItemsToHeldOrderScreen`

**Pattern 4 — Master-detail (tablet: list + detail side by side)**
Use for any list where selecting an item shows a detail view — history, inventory, reports.

```kotlin
Row(Modifier.fillMaxSize()) {
    // List panel — narrower
    Column(Modifier.weight(0.38f).fillMaxHeight()) {
        /* list */
    }
    VerticalDivider()
    // Detail panel — wider
    Box(Modifier.weight(0.62f).fillMaxHeight()) {
        if (selectedItem != null) DetailPanel(selectedItem)
        else EmptyDetailPlaceholder()
    }
}
```
Reference: `SalesHistoryScreen → TabletLayout`

### Spacing density philosophy

Rancak POS is a **transaction-focused app used under time pressure**. The goal is
**comfortable density** — content is easy to tap, readable at a glance, and uses
screen space efficiently without large empty voids.

| Context | Phone padding | Tablet padding | Rationale |
|---|---|---|---|
| Screen edge margin | `16.dp` | `24.dp` | Tablets have more real estate, slightly more breathing room |
| Between cards in a list | `8.dp` | `8.dp` | Consistent tap-target separation |
| Inside a card | `12–16.dp` | `14–18.dp` | Enough internal padding without waste |
| Between form fields | `12.dp` | `12.dp` | Consistent vertical rhythm |
| Section spacing | `24.dp` | `32.dp` | Clear visual grouping on larger screens |
| Constrained form max-width on tablet | `560.dp` | `560.dp` | Prevents form from spanning full 800+ dp |

**Do not** add extra vertical padding just because there is space on tablet.
**Do not** reduce padding on phone to fit more content — respect touch targets.
Minimum touch target: `48.dp` height for any tappable element.

### Key dos and don'ts

```kotlin
// ✅ Grid that adapts column count automatically
GridCells.Adaptive(minSize = 160.dp)

// ❌ Fixed column count that ignores screen width
GridCells.Fixed(2)  // always 2 columns, even on a 10-inch tablet

// ✅ Form capped at readable width on tablet
Modifier.widthIn(max = 560.dp).align(Alignment.Center)

// ❌ Form that stretches to full tablet width
Modifier.fillMaxWidth()  // on a 900 dp tablet, this is unreadable

// ✅ Structural split for tablet, sequential for phone
if (isTablet) Row { ListPanel(); DetailPanel() }
else          Column { List(); if (selected != null) Detail() }

// ❌ Ignoring tablet entirely
Column { /* same narrow phone layout on all devices */ }
```

### UX vocabulary — what terms mean in this codebase

When a user or designer uses these terms, map them to these patterns:

| Term | Meaning | Implementation |
|---|---|---|
| **Master-detail** | List on left, detail on right (tablet) | Pattern 4 above |
| **Bottom sheet** | Panel that slides up from bottom | `ModalBottomSheet` from Material3 |
| **Dialog** | Blocking modal for confirmations | `AlertDialog` — use for destructive actions |
| **Full-screen takeover** | Replaces current screen entirely | Navigate to a new route via `NavController` |
| **Contextual action** | Action that appears when item is selected | Toolbar state change or FAB replacement |
| **FAB** | Floating action button | `FloatingActionButton` in Scaffold `floatingActionButton` slot |
| **Snackbar** | Non-blocking transient feedback | `SnackbarHost` — use for success/info messages |
| **Chip** | Compact filter or status pill | `FilterChip` / `AssistChip` from Material3 |
| **Top app bar** | Always `RancakTopBar` — never build a custom one from scratch | See `components/RancakTopBar.kt` |
| **Dense layout** | More items per screen, smaller padding | Reduce to `xs/sm` spacing, smaller card padding |
| **Comfortable layout** | Standard padding, clear hierarchy | Use `sm/md` spacing as baseline |
| **Card** | Elevated surface grouping related content | `Card` with `elevation = 1.dp` (subtle) |
| **Divider** | Thin line between sections | `HorizontalDivider` or `VerticalDivider` |
| **Empty state** | Screen with no content | `EmptyScreen()` from `components/StateScreens.kt` |
| **Loading state** | Data being fetched | `LoadingScreen()` from `components/StateScreens.kt` |
| **Error state** | Failed to load | `ErrorScreen()` from `components/StateScreens.kt` |
| **Pill / badge** | Small status indicator | `StatusChip` from `components/StatusChip.kt` |

### Separate layout composables for clean code

When phone and tablet layouts diverge significantly, extract them as separate private composables
inside the same file — do not use deeply nested `if (isTablet)` inside a single large composable:

```kotlin
// ✅ Clean — separate named composables
BoxWithConstraints(Modifier.fillMaxSize()) {
    if (maxWidth >= 600.dp) TabletInventoryLayout(uiState, actions)
    else                    PhoneInventoryLayout(uiState, actions)
}

@Composable private fun TabletInventoryLayout(...) { /* tablet-specific UI */ }
@Composable private fun PhoneInventoryLayout(...)  { /* phone-specific UI */  }

// ❌ Messy — tablet check buried deep inside a large composable
Column {
    Header()
    if (isTablet) {
        Row { /* ... */ }
    } else {
        /* ... */
    }
    if (isTablet) { /* ... */ } else { /* ... */ }
    // 200 more lines...
}
```

---

## API Contract

**Base URL**: defined in `ApiConstants.BASE_URL` — never hardcode it in extension functions.
**Tenant scope**: every protected endpoint prefixed with `ApiConstants.tenantPath(tenantUuid)`.
**Auth**: `Authorization: Bearer <token>` — handled automatically by Ktor `Auth` plugin.
**API Key**: `X-API-Key` is auto-added by `HttpClientFactory` via `DefaultRequest` — never add it manually in extension functions.

### Response envelope — actual Kotlin type

```kotlin
// data/remote/dto/ApiResponse.kt
@Serializable
data class ApiResponse<T>(
    @SerialName("status_code") val statusCode: Int = 200,
    val message: String? = null,
    val data: T? = null
) {
    val isSuccess: Boolean get() = statusCode in 200..299
}

// For paginated endpoints
@Serializable
data class PaginatedData<T>(
    val items: List<T>,
    val total: Int? = null,
    val page: Int? = null,
    val limit: Int? = null
)
```

Use `ApiResponse<XxxDto>` for single/list responses, `ApiResponse<PaginatedData<XxxDto>>` for paginated ones.

### HTTP status handling

| Status | Handling |
|---|---|
| `401` | Ktor `Auth` plugin auto-refreshes token → retries — never force logout |
| `409` | Treat as success (idempotency duplicate) — the `safe()` helper handles this |
| `422` | Surface `message` to user in Bahasa Indonesia |
| `402` | Subscription expired — navigate user to billing screen |

### URL construction — canonical pattern

Always use `ApiConstants` — never concatenate raw strings:

```kotlin
// Tenant-scoped endpoint
ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.PRODUCTS
// → https://be-rancak.up.railway.app/tenants/{uuid}/products

// Sub-resource
ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.PRODUCTS}/$productUuid"

// Non-tenant-scoped (auth endpoints)
ApiConstants.BASE_URL + ApiConstants.LOGIN
```

When adding a new domain, register its path constant in `ApiConstants.kt`:
```kotlin
const val LOYALTY = "/loyalty"   // ← add here, then reference everywhere
```

### FlexibleLongSerializer — required for monetary fields

The backend sometimes sends monetary values as decimal strings (`"60000.00"`) instead of integers.
Use `FlexibleLongSerializer` on every `Long` field that represents money or quantity in a response DTO:

```kotlin
import id.rancak.app.data.remote.dto.FlexibleLongSerializer

@Serializable
data class XxxDto(
    val uuid: String,
    @Serializable(with = FlexibleLongSerializer::class) val price: Long = 0,
    @Serializable(with = FlexibleLongSerializer::class) val total: Long = 0,
    val name: String   // ← non-monetary String fields do NOT need this
)
```

**Rule**: annotate with `FlexibleLongSerializer` whenever the field is a `Long` that comes from the API and represents a price, amount, fee, tax, discount, or any financial figure.

### Query parameters

Use `parameter()` inside the request builder for GET query params — never string-interpolate them:

```kotlin
suspend fun RancakApiService.getXxx(
    tenantUuid: String,
    query: String? = null,
    page: Int = 1,
    limit: Int = 20
): ApiResponse<List<XxxDto>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX) {
        parameter("page", page)
        parameter("limit", limit)
        query?.let { parameter("q", it) }   // ← only add when non-null
    }.body()
```

### Binary responses (ESC/POS, receipts)

Some endpoints return raw bytes, not JSON. Return type is `ByteArray`, not `ApiResponse<T>`:

```kotlin
suspend fun RancakApiService.getReceiptBytes(tenantUuid: String, saleUuid: String): ByteArray =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.receiptEscpos(saleUuid)) {
        accept(ContentType.Application.OctetStream)
    }.body()
```

**Date-time**: always send as `"YYYY-MM-DDT00:00:00Z"` via `String.toDateTimeString()` from `DateTimeUtils.kt`

---

## Platform Code (expect / actual)

```
commonMain  → expect declaration only (no implementation)
androidMain → Android actual implementation
iosMain     → iOS actual implementation
```

Existing pairs: `PrinterManager`, `SyncManager`, `BarcodeScanner`, `GoogleSignInButton`,
`SecureSettings`, `PlatformModule`

Never add platform imports to `commonMain`. If platform-specific behaviour is needed,
add a new `expect`/`actual` pair following the existing ones.

---

## Architectural Thinking

When improving, refactoring, or evaluating code, follow this sequence:

1. **Read first** — open the relevant files, understand the current state, don't assume
2. **Find the root cause** — a naming problem is often a symptom of a missing abstraction
3. **Classify the problem** — layer violation / missing abstraction / inconsistency / coupling / incomplete pattern
4. **State the plan** — name every file that changes, the before/after, ripple effects, and risk level
5. **Execute in dependency order** — `domain → data → di → viewmodel → ui → navigation`
6. **Verify** — read the changed files back, check Koin graph, check navigation registration

When you touch a file and notice a nearby violation, fix it unless the fix is large enough to deserve its own task.

---

## Pre-Submission Checklist

- [ ] No `android.*` / Apple imports in `commonMain`
- [ ] All repository methods return `Resource<T>`
- [ ] ViewModels expose `StateFlow`, not `MutableStateFlow`
- [ ] All `Resource` variants handled in every `when` block
- [ ] Screen split: `XxxScreen` (has VM) + `XxxContent` (pure UI, no VM reference)
- [ ] Correct ViewModel state pattern chosen (A / B / C)
- [ ] New classes registered in correct Koin module in `AppModule.kt` using shorthand DSL
- [ ] New routes registered in `Screen.kt` and `RancakNavHost.kt`
- [ ] Prices as `Long`, displayed via `formatRupiah()`
- [ ] All user-facing strings in Bahasa Indonesia (including `errorMsg` in repository helpers)
- [ ] Role-gating applied where required
- [ ] `POST /sales` sends `X-Idempotency-Key`
- [ ] No hardcoded colors, spacing, or typography
- [ ] Screen handles both phone and tablet (≥ 600 dp) — uses correct adaptive pattern
- [ ] Forms use `widthIn(max = 560.dp)` constraint on tablet — not `fillMaxWidth`
- [ ] Grids use `GridCells.Adaptive` — not `GridCells.Fixed`
- [ ] Tablet layout uses master-detail or side-by-side where appropriate
- [ ] Touch targets ≥ 48 dp for all interactive elements
