# Generate Feature — Rancak POS

Use this prompt in GitHub Copilot Chat by attaching this file and describing the feature you need.

---

## Context This Prompt Depends On

`copilot-instructions.md` is **always pre-loaded** by Copilot — you already have access to:
- Full architecture overview (layers, package structure)
- All three ViewModel patterns (simple `.update`, `combine + stateIn`, `recompute()`)
- Koin DSL shorthand (`singleOf`, `viewModelOf`)
- Non-negotiable rules (no `android.*` in commonMain, all prices as `Long`, Bahasa Indonesia errors)
- Design system tokens, `Resource<T>` sealed class, `UiState` pattern
- `ApiResponse<T>` format, `ApiConstants`, `HttpClientFactory` behaviour

**Do not re-read `copilot-instructions.md`.**

Read these additional files **only if your feature involves these domains**:

| Feature involves… | Also read |
|---|---|
| A new API endpoint (DTO, mapper, repository) | `.github/prompts/integrate-api.prompt.md` |
| A screen that must work on tablet + phone | `.github/prompts/adaptive-layout.prompt.md` |
| Restructuring existing code | `.github/prompts/refactor-structure.prompt.md` |
| Unsure what a topic like FlexibleLong or BoxWithConstraints is for | `.github/CONTEXT_INDEX.md` |

A full feature almost always touches both API integration and adaptive layout.
**Reading all three prompt files for a full feature is normal — do it in a single pass before writing any code.**

---

## How to Use

Describe the feature clearly, including:
- What it does (user-facing behaviour)
- Which API endpoints it uses
- Which user roles can access it
- Any offline behaviour needed

**Example prompt:**
> "Generate a **Loyalty Points** feature. Users can view their point balance and redeem points
> at checkout. Endpoints: `GET /tenants/:id/customers/:customerId/points` and
> `POST /tenants/:id/customers/:customerId/points/redeem`.
> STAFF and above can view; ADMIN and above can redeem."

---

## Before You Write Any Code

1. **Read the relevant existing files** to understand what already exists. Check:
   - `domain/model/` for existing models that might be extended
   - `domain/repository/` for interfaces that might already cover this
   - `di/AppModule.kt` to see what's already registered
   - `navigation/Screen.kt` for existing routes

2. **State your plan first** — list every file you will create or modify, in order.

3. **Flag any conflicts** — if the feature touches an existing ViewModel or repository, name the risk.

---

## Generation Order (always follow this sequence — do not skip or reorder)

### Step 1 — Domain Model (`domain/model/XxxModels.kt`)
Pure Kotlin only. No platform imports. No framework annotations.

```kotlin
data class Xxx(
    val uuid: String,
    val tenantUuid: String,
    // fields that reflect actual business concepts, not API shape
)

// Input model for write operations (separate from response model)
data class XxxInput(
    val field: String
)
```

### Step 2 — Repository Interface (`domain/repository/XxxRepository.kt`)
```kotlin
interface XxxRepository {
    suspend fun getXxx(id: String): Resource<Xxx>
    suspend fun listXxx(): Resource<List<Xxx>>
    suspend fun createXxx(input: XxxInput): Resource<Xxx>
    suspend fun deleteXxx(id: String): Resource<Unit>
}
```
Methods return `Resource<T>`. No implementation details leak through the interface.

### Step 3 — DTOs (`data/remote/dto/xxx/XxxDto.kt`)

```kotlin
import id.rancak.app.data.remote.dto.FlexibleLongSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XxxResponse(
    val uuid: String,
    @SerialName("field_name") val fieldName: String,
    // Monetary / price fields MUST use FlexibleLongSerializer —
    // the backend can send them as integers OR decimal strings ("60000.00")
    @Serializable(with = FlexibleLongSerializer::class) val amount: Long = 0,
    // Non-monetary Long fields (IDs, counters) do NOT need FlexibleLongSerializer
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CreateXxxRequest(
    @SerialName("field_name") val fieldName: String,
    val amount: Long
)
```

Rules:
- `@SerialName` is required whenever JSON key uses `snake_case` and the Kotlin field uses `camelCase`
- `@Serializable(with = FlexibleLongSerializer::class)` is required on every `Long` field that represents money, price, fee, discount, tax, or any financial figure
- Add `= defaultValue` for all optional fields so missing JSON keys don't crash deserialization
- `ignoreUnknownKeys = true` is set globally in `HttpClientFactory` — you don't need to map every API field, only the ones your feature actually uses

### Step 3b — Register path constant in `ApiConstants.kt`

Before writing the API extension, add the resource path constant:

```kotlin
// data/remote/api/ApiConstants.kt
const val XXX = "/xxx"    // ← add to the tenant-scoped constants block
```

Reference it everywhere instead of writing the string literal.

### Step 4 — Mapper (`data/mapper/XxxMappers.kt`)

```kotlin
fun XxxResponse.toDomain(): Xxx = Xxx(
    uuid    = uuid,
    field   = fieldName,
    amount  = amount,
)

fun XxxInput.toRequest(): CreateXxxRequest = CreateXxxRequest(
    fieldName = field,
    amount    = amount,
)
```

Rules:
- Extension functions only — no class instantiation, no injected dependencies
- If a field comes as `String` from the API but should be `Long` in domain (like `VariantDto.priceAdjustment`), convert in the mapper: `priceAdjustment.toLongOrNull() ?: 0L`

### Step 5 — API Service Extension (`data/remote/api/XxxApi.kt`)

```kotlin
import id.rancak.app.data.remote.api.ApiConstants
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.dto.ApiResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

// GET single — no query params
suspend fun RancakApiService.getXxx(
    tenantUuid: String,
    id: String
): ApiResponse<XxxResponse> =
    client.get(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.XXX}/$id"
    ).body()

// GET list — with optional query parameters
suspend fun RancakApiService.listXxx(
    tenantUuid: String,
    query: String? = null,
    page: Int = 1,
    limit: Int = 20
): ApiResponse<List<XxxResponse>> =
    client.get(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX
    ) {
        parameter("page", page)
        parameter("limit", limit)
        query?.let { parameter("q", it) }   // only add when non-null
    }.body()

// POST — standard write
suspend fun RancakApiService.createXxx(
    tenantUuid: String,
    request: CreateXxxRequest
): ApiResponse<XxxResponse> =
    client.post(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX
    ) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

// POST — financial transaction (requires idempotency key)
suspend fun RancakApiService.createXxxTransaction(
    tenantUuid: String,
    request: CreateXxxRequest,
    idempotencyKey: String          // caller generates: uuid4().toString()
): ApiResponse<XxxResponse> =
    client.post(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX
    ) {
        contentType(ContentType.Application.Json)
        header("X-Idempotency-Key", idempotencyKey)
        setBody(request)
    }.body()
```

Important rules:
- **Never** add `header("X-API-Key", ...)` — it is already injected globally by `HttpClientFactory`
- **Never** add `header(HttpHeaders.Authorization, ...)` — handled by the Ktor `Auth` plugin
- Use `ApiConstants.XXX` (the constant you registered in Step 3b), not a raw string literal
- Only add `header("X-Idempotency-Key", ...)` on endpoints that create financial transactions
- Use `parameter()` for query params — never string-interpolate them into the URL

### Step 6 — Repository Implementation (`data/repository/XxxRepositoryImpl.kt`)

Implement **every method declared in the interface** from Step 2.

```kotlin
class XxxRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : XxxRepository {

    // Always a computed property — reads tenantId fresh on every call, never at injection time
    private val tenantId get() = tokenManager.tenantUuid

    // GET list — use safeList when the endpoint returns ApiResponse<List<T>>
    override suspend fun listXxx(query: String?): Resource<List<Xxx>> =
        safeList(
            block    = { api.listXxx(tenantId, query = query) },
            errorMsg = "Gagal memuat daftar"
        ) { it.toDomain() }

    // GET single — use safe() with a map lambda
    override suspend fun getXxx(id: String): Resource<Xxx> =
        safe(
            block    = { api.getXxx(tenantId, id) },
            map      = { it.toDomain() },
            errorMsg = "Gagal memuat data"
        )

    // POST — use safe() with a map lambda
    override suspend fun createXxx(input: XxxInput): Resource<Xxx> =
        safe(
            block    = { api.createXxx(tenantId, input.toRequest()) },
            map      = { it.toDomain() },
            errorMsg = "Gagal membuat data"
        )

    // DELETE — use safeUnit() when there is no meaningful response body
    override suspend fun deleteXxx(id: String): Resource<Unit> =
        safeUnit(
            block    = { api.deleteXxx(tenantId, id) },
            errorMsg = "Gagal menghapus data"
        )
}
```

### Step 7 — ViewModel (`presentation/viewmodel/XxxViewModel.kt`)

> **UiState rule:** start `isLoading = true` so the Screen shows `LoadingScreen()` immediately
> on first render — before `LaunchedEffect` has fired. Starting with `false` causes a
> brief empty-state flash on every navigation to this screen.

```kotlin
data class XxxUiState(
    val isLoading: Boolean = true,   // ← true: shows LoadingScreen immediately on first render
    val error: String? = null,
    val items: List<Xxx> = emptyList(),
    val selectedItem: Xxx? = null,
    // add input fields here if the feature has a form
)

class XxxViewModel(
    private val xxxRepository: XxxRepository
    // Add tokenManager: TokenManager if this feature needs role gating
    // (see Role Gating Pattern section below)
) : ViewModel() {

    private val _uiState = MutableStateFlow(XxxUiState())
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

    fun loadItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = xxxRepository.listXxx()) {
                is Resource.Success -> _uiState.update { it.copy(items = result.data, isLoading = false) }
                is Resource.Error   -> _uiState.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> {}
            }
        }
    }

    // Name your action functions after what they do, not generic names
    fun selectItem(item: Xxx) = _uiState.update { it.copy(selectedItem = item) }

    // clearError() is mandatory — Screen calls it after showing the error
    fun clearError() = _uiState.update { it.copy(error = null) }
}
```

### Step 8 — Screen (`presentation/ui/xxx/XxxScreen.kt`)
```kotlin
@Composable
fun XxxScreen(
    onNavigateBack: () -> Unit,
    viewModel: XxxViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadItems() }

    // Show error in snackbar, then clear it so it doesn't reappear
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
        onSelectItem      = viewModel::selectItem   // ← matches function in ViewModel template
    )
}

@Composable
fun XxxContent(
    uiState: XxxUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateBack: () -> Unit = {},
    onSelectItem: (Xxx) -> Unit = {}
) {
    Scaffold(
        topBar = { RancakTopBar(title = "Judul Fitur", onMenu = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading        -> LoadingScreen()
                uiState.error != null    -> ErrorScreen(uiState.error)
                uiState.items.isEmpty()  -> EmptyScreen("Belum ada data")  // ← Bahasa Indonesia
                else                     -> ItemList(uiState.items, onSelectItem)
            }
        }
    }
}

// Private composable helpers stay in the same file
@Composable
private fun ItemList(items: List<Xxx>, onSelect: (Xxx) -> Unit) { /* ... */ }
```

### Step 9 — Koin Registration (`di/AppModule.kt`)

Use the shorthand DSL that the codebase already uses — check `AppModule.kt` for the exact style:

```kotlin
// In repositoryModule — prefer singleOf + bind
singleOf(::XxxRepositoryImpl) bind XxxRepository::class

// Only use the explicit form when constructor args need manual wiring:
single<XxxRepository> { XxxRepositoryImpl(get(), get(), get()) }

// In viewModelModule — always use viewModelOf
viewModelOf(::XxxViewModel)
```

Never register a ViewModel as `single` — that would make it a shared singleton across screens.

### Step 10 — Navigation
```kotlin
// navigation/Screen.kt
@Serializable
data class XxxRoute(val id: String? = null) : Screen()

// navigation/RancakNavHost.kt
composable<XxxRoute> {
    XxxScreen(onNavigateBack = { navController.popBackStack() })
}
```

---

## Role Gating Pattern

If the feature is restricted to certain roles, gate the UI — not just the repository.
`tokenManager` must be **injected into the ViewModel constructor** — it is not a global.

```kotlin
// Step 1 — add tokenManager to the ViewModel constructor
class XxxViewModel(
    private val xxxRepository: XxxRepository,
    private val tokenManager: TokenManager   // ← inject when role gating is needed
) : ViewModel() {

    // Expose the current role as a plain val — not a StateFlow, roles don't change mid-session
    val currentRole: UserRole = tokenManager.userRole

    // ...
}

// Step 2 — Koin registration must include tokenManager (get() resolves it automatically)
// In AppModule.kt — viewModelOf handles this automatically IF the constructor lists it:
viewModelOf(::XxxViewModel)   // Koin injects all constructor params via get()

// Step 3 — Pass currentRole to XxxContent as a plain parameter
@Composable
fun XxxScreen(..., viewModel: XxxViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    XxxContent(
        uiState      = uiState,
        currentRole  = viewModel.currentRole,
        onAdminAction = viewModel::doAdminAction
    )
}

// Step 4 — Gate UI inside XxxContent using the passed role
@Composable
fun XxxContent(
    uiState: XxxUiState,
    currentRole: UserRole = UserRole.STAFF,
    onAdminAction: () -> Unit = {}
) {
    // Gate based on role — Staff cannot see Admin/Owner-only actions
    if (currentRole.atLeast(UserRole.ADMIN)) {
        AdminActionButton(onClick = onAdminAction)
    }
}
```

---

## Idempotency for Transactions

Any POST that creates a financial transaction (sale, payment, refund, redemption) **must**
include `X-Idempotency-Key`. This prevents duplicate charges if the request is retried.

The key must be generated by the caller using `uuid4().toString()` and passed to the API
extension function as a parameter — never generated inside the API extension itself
(the VM or repository that initiates the request must own the key).

```kotlin
// In the API extension (data/remote/api/SaleApi.kt)
suspend fun RancakApiService.createSale(
    tenantUuid: String,
    request: CreateSaleRequest,
    idempotencyKey: String   // ← caller generates and owns this
): ApiResponse<SaleResponse> =
    client.post(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.SALES
    ) {
        contentType(ContentType.Application.Json)
        header("X-Idempotency-Key", idempotencyKey)
        setBody(request)
    }.body()

// In the RepositoryImpl — generate the key here, pass it down
override suspend fun createSale(input: SaleInput): Resource<Sale> =
    safe(
        block    = { api.createSale(tenantId, input.toRequest(), uuid4().toString()) },
        map      = { it.toDomain() },
        errorMsg = "Gagal membuat transaksi"
    )
```

---

## Post-Generation Checklist

After generating all files, verify:

- [ ] No `android.*` or Apple imports in `commonMain`
- [ ] All repository methods return `Resource<T>`
- [ ] ViewModel exposes `StateFlow`, not `MutableStateFlow`
- [ ] All three `Resource` variants handled in every `when` block (`Success`, `Error`, `Loading -> {}`)
- [ ] `UiState` initialises with `isLoading = true` (prevents empty-state flash on first render)
- [ ] `clearError()` function exists in ViewModel; Screen calls it after displaying the error
- [ ] Errors are shown via `SnackbarHost` in `XxxContent` — not silently swallowed
- [ ] Screen is split: `XxxScreen` (has VM) + `XxxContent` (pure UI, no VM reference)
- [ ] `XxxContent` parameters are plain data + lambdas only — no ViewModel, no Flow
- [ ] `XxxContent` has default no-op values for all parameters (enables Preview without crashes)
- [ ] New repository and ViewModel registered in `AppModule.kt`
- [ ] New route registered in `Screen.kt` and `RancakNavHost.kt`
- [ ] All prices use `Long` and are displayed via `formatRupiah()`
- [ ] All user-facing strings are in Bahasa Indonesia (labels, empty states, `errorMsg`, snackbar text)
- [ ] Role gating applied where the feature description requires it
- [ ] If role gating: `tokenManager` injected into ViewModel constructor and passed as plain param to `XxxContent`
- [ ] `X-Idempotency-Key` added if the feature touches any financial transaction (sale, payment, refund)
- [ ] No hardcoded colors, spacing, or typography — use design system tokens
- [ ] Screen handles both phone and tablet using the correct adaptive layout pattern
