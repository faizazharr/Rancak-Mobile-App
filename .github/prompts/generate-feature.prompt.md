# Generate Feature — Rancak POS

Use this prompt in GitHub Copilot Chat by attaching this file and describing the feature you need.

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
```kotlin
class XxxRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : XxxRepository {

    private val tenantId get() = tokenManager.tenantUuid

    override suspend fun getXxx(id: String): Resource<Xxx> =
        safe(
            block    = { api.getXxx(tenantId, id) },
            map      = { it.toDomain() },
            errorMsg = "Gagal memuat data"
        )

    override suspend fun createXxx(input: XxxInput): Resource<Xxx> =
        safe(
            block    = { api.createXxx(tenantId, input.toRequest()) },
            map      = { it.toDomain() },
            errorMsg = "Gagal membuat data"
        )

    override suspend fun deleteXxx(id: String): Resource<Unit> =
        safeUnit(
            block    = { api.deleteXxx(tenantId, id) },
            errorMsg = "Gagal menghapus data"
        )
}
```

### Step 7 — ViewModel (`presentation/viewmodel/XxxViewModel.kt`)
```kotlin
data class XxxUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<Xxx> = emptyList(),
    val selectedItem: Xxx? = null,
    // input fields if a form is needed
)

class XxxViewModel(
    private val xxxRepository: XxxRepository
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

    LaunchedEffect(Unit) { viewModel.loadItems() }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // show snackbar or handle error navigation
            viewModel.clearError()
        }
    }

    XxxContent(
        uiState        = uiState,
        onNavigateBack = onNavigateBack,
        onAction       = viewModel::doAction
    )
}

@Composable
fun XxxContent(
    uiState: XxxUiState,
    onNavigateBack: () -> Unit = {},
    onAction: (Xxx) -> Unit = {}
) {
    Scaffold(
        topBar = { RancakTopBar(title = "Feature Title", onMenu = onNavigateBack) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isLoading        -> LoadingScreen()
                uiState.error != null    -> ErrorScreen(uiState.error)
                uiState.items.isEmpty()  -> EmptyScreen("No items yet")
                else                     -> ItemList(uiState.items, onAction)
            }
        }
    }
}

// Private composable helpers stay in the same file
@Composable
private fun ItemList(items: List<Xxx>, onAction: (Xxx) -> Unit) { /* ... */ }
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

If the feature is restricted to certain roles, gate the UI — not just the repository:

```kotlin
// In the ViewModel, expose the current role
val currentRole: UserRole = tokenManager.userRole

// In XxxContent, check before rendering admin actions
if (currentRole.atLeast(UserRole.ADMIN)) {
    AdminActionButton(onClick = onAdminAction)
}
```

---

## Idempotency for Transactions

If the feature creates a sale, payment, or any financial transaction:

```kotlin
client.post("$baseUrl/tenants/$tenantId/sales") {
    header("X-Idempotency-Key", uuid4().toString())
    contentType(ContentType.Application.Json)
    setBody(request)
}
```

---

## Post-Generation Checklist

After generating all files, verify:

- [ ] No `android.*` or Apple imports in `commonMain`
- [ ] All repository methods return `Resource<T>`
- [ ] ViewModel exposes `StateFlow`, not `MutableStateFlow`
- [ ] All four `Resource` variants handled (`Success`, `Error`, `Loading`, empty/idle)
- [ ] Screen is split: `XxxScreen` (has VM) + `XxxContent` (pure UI, no VM reference)
- [ ] New repository and ViewModel registered in `AppModule.kt`
- [ ] New route registered in `Screen.kt` and `RancakNavHost.kt`
- [ ] All prices use `Long` and are displayed via `formatRupiah()`
- [ ] All user-facing error messages are in Bahasa Indonesia
- [ ] Role gating applied where the feature description requires it
- [ ] `X-Idempotency-Key` added if the feature touches financial transactions
- [ ] No hardcoded colors, spacing, or typography — use design system tokens
