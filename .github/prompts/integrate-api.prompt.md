# Integrate API Endpoint — Rancak POS

Use this prompt when you need to wire a new backend endpoint into the app —
from the OpenAPI spec all the way through to a usable repository method.

This prompt is focused purely on the **data layer**: DTOs, API extension, mapper,
and repository method. Use `generate-feature.prompt.md` if you also need
ViewModel and Screen on top.

---

## How to Use

Describe the endpoint(s) you want to integrate:

> "Integrate `GET /tenants/:id/loyalty/customers/:customerId/points`
> and `POST /tenants/:id/loyalty/customers/:customerId/points/redeem`.
> The GET returns `{ points_balance: 12500, tier: "gold" }`.
> The POST body is `{ amount: Long, reason: String? }` and returns the updated balance."

> "Wire up the entire `/tenants/:id/inventory/suppliers` domain —
> CRUD endpoints from the openapi.yaml."

> "Add `PATCH /tenants/:id/products/:id` to the existing ProductRepository."

---

## Phase 1 — Read Before Writing

Before creating any file:

1. **Read `openapi.yaml`** for the target endpoint(s):
   - Exact path, HTTP method
   - Request body schema (field names, types, required vs optional)
   - Response body schema (field names, types — note which are numeric)
   - Query parameters and their types
   - Error codes specific to this endpoint (beyond 401/409/422)

2. **Read `data/remote/api/ApiConstants.kt`**:
   - Does a constant already exist for this resource path?
   - If not, where in the file should the new constant be added?

3. **Read the relevant existing API extension file** (e.g., `ProductApi.kt` for product endpoints):
   - Does the function already exist under a different name?
   - What import style does the file use?

4. **Read the relevant existing DTO folder** (e.g., `dto/product/`):
   - Are any DTOs already defined that cover this response?
   - Is there already a `FlexibleLongSerializer` import in the file?

5. **Read the relevant `*RepositoryImpl.kt`**:
   - Does the method already exist?
   - What Koin registration style does it use?

**Do not create any file until Phase 1 is complete and you have stated your plan.**

---

## Phase 2 — State the Plan

Write a short plan before touching any code:

```
## Integration Plan: [endpoint name]

Endpoint(s):  METHOD /path
New files:    [list, or "none" if extending existing files]
Modified files:
  - ApiConstants.kt — add const val XXX = "/xxx"
  - XxxApi.kt       — add getXxx(), createXxx()
  - XxxDtos.kt      — add XxxResponse, CreateXxxRequest
  - XxxMappers.kt   — add XxxResponse.toDomain()
  - XxxRepositoryImpl.kt — add override fun getXxx()
  - XxxRepository.kt     — add suspend fun getXxx(): Resource<Xxx>
  - AppModule.kt    — no changes needed / update binding

FlexibleLongSerializer needed on: [list fields, or "none"]
PaginatedData needed: yes / no
Idempotency key needed: yes / no
Binary (ByteArray) response: yes / no
```

---

## Phase 3 — Execute in This Order

### 3-1. Register path constant (`data/remote/api/ApiConstants.kt`)

Add to the tenant-scoped constants block. Group by domain:

```kotlin
object ApiConstants {
    // ...existing constants...

    // Loyalty (new domain)
    const val LOYALTY_POINTS = "/loyalty/points"
    const val LOYALTY_REDEEM = "/loyalty/redeem"

    // Or as sub-path of existing resource:
    fun loyaltyPoints(customerId: String) = "/loyalty/customers/$customerId/points"
}
```

**Rule**: every literal path string must live here — never inline in extension functions.

---

### 3-2. Create DTOs (`data/remote/dto/<domain>/XxxDtos.kt`)

```kotlin
package id.rancak.app.data.remote.dto.xxx

import id.rancak.app.data.remote.dto.FlexibleLongSerializer
import id.rancak.app.data.remote.dto.PaginatedData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Response DTO ──────────────────────────────────────────────────────────────

@Serializable
data class XxxResponse(
    val uuid: String,
    @SerialName("field_name") val fieldName: String,

    // Monetary fields — MUST use FlexibleLongSerializer
    @Serializable(with = FlexibleLongSerializer::class)
    val amount: Long = 0,

    // Optional fields — always provide a default
    val note: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// ── Request DTO ───────────────────────────────────────────────────────────────

@Serializable
data class CreateXxxRequest(
    @SerialName("field_name") val fieldName: String,
    val amount: Long,
    val note: String? = null   // nullable optional fields are fine in requests too
)
```

#### When to use `FlexibleLongSerializer`

Use it on every `Long` field in a **response** DTO that represents:
- price, total, subtotal, amount, balance
- discount, tax, tip, fee, surcharge
- any field that could plausibly be sent as `"60000.00"` by the backend

Do **not** use it on:
- String fields
- Int/Boolean fields
- Non-financial Long fields (e.g., `version: Long`)
- Request DTO fields (you control the format you send)

#### When to use `PaginatedData<T>`

When the endpoint returns a paginated list, wrap accordingly:

```kotlin
// API extension returns:
ApiResponse<PaginatedData<XxxResponse>>

// Repository maps items only:
safeList(
    block    = { api.listXxx(tenantId) },
    errorMsg = "Gagal memuat daftar"
) { it.toDomain() }
// ↑ won't compile — safeList expects ApiResponse<List<T>>
// For paginated endpoints, use safe() and map .items:
safe(
    block    = { api.listXxx(tenantId) },
    map      = { paginatedData -> paginatedData.items.map { it.toDomain() } },
    errorMsg = "Gagal memuat daftar"
)
```

---

### 3-3. Create API extension (`data/remote/api/XxxApi.kt`)

```kotlin
package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.PaginatedData
import id.rancak.app.data.remote.dto.xxx.CreateXxxRequest
import id.rancak.app.data.remote.dto.xxx.XxxResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

// ── GET single ────────────────────────────────────────────────────────────────

suspend fun RancakApiService.getXxx(
    tenantUuid: String,
    id: String
): ApiResponse<XxxResponse> =
    client.get(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.XXX}/$id"
    ).body()

// ── GET list with optional filters ───────────────────────────────────────────

suspend fun RancakApiService.listXxx(
    tenantUuid: String,
    query: String? = null,
    status: String? = null,
    page: Int = 1,
    limit: Int = 20
): ApiResponse<List<XxxResponse>> =
    client.get(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX
    ) {
        parameter("page", page)
        parameter("limit", limit)
        query?.let  { parameter("q", it) }
        status?.let { parameter("status", it) }
    }.body()

// ── GET paginated (if endpoint uses PaginatedData) ────────────────────────────

suspend fun RancakApiService.listXxxPaginated(
    tenantUuid: String,
    page: Int = 1,
    limit: Int = 20
): ApiResponse<PaginatedData<XxxResponse>> =
    client.get(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX
    ) {
        parameter("page", page)
        parameter("limit", limit)
    }.body()

// ── POST standard ─────────────────────────────────────────────────────────────

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

// ── POST with idempotency key (financial transactions only) ───────────────────

suspend fun RancakApiService.createXxxTransaction(
    tenantUuid: String,
    request: CreateXxxRequest,
    idempotencyKey: String      // caller must supply: uuid4().toString()
): ApiResponse<XxxResponse> =
    client.post(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX
    ) {
        contentType(ContentType.Application.Json)
        header("X-Idempotency-Key", idempotencyKey)
        setBody(request)
    }.body()

// ── PATCH partial update ──────────────────────────────────────────────────────

suspend fun RancakApiService.updateXxx(
    tenantUuid: String,
    id: String,
    request: UpdateXxxRequest
): ApiResponse<XxxResponse> =
    client.patch(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.XXX}/$id"
    ) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

// ── DELETE ────────────────────────────────────────────────────────────────────

suspend fun RancakApiService.deleteXxx(
    tenantUuid: String,
    id: String
): ApiResponse<Unit> =
    client.delete(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.XXX}/$id"
    ).body()

// ── Binary response (ESC/POS, receipts, file downloads) ──────────────────────

suspend fun RancakApiService.getXxxBytes(
    tenantUuid: String,
    id: String
): ByteArray =
    client.get(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.XXX}/$id/bytes"
    ) {
        accept(ContentType.Application.OctetStream)
    }.body()
```

#### Rules — never break these

| ❌ Never do | ✅ Always do instead |
|---|---|
| `header("X-API-Key", ...)` | — it's auto-added by `HttpClientFactory` |
| `header(Authorization, ...)` | — handled by Ktor `Auth` plugin |
| `"$baseUrl/tenants/$tenantId/xxx"` | `ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX` |
| `"?page=$page&limit=$limit"` in URL | `parameter("page", page)` inside the builder |
| Returning `XxxResponse` directly | Always wrap in `ApiResponse<XxxResponse>` |
| Returning `List<XxxResponse>` directly | `ApiResponse<List<XxxResponse>>` |

---

### 3-4. Create mapper (`data/mapper/XxxMappers.kt`)

```kotlin
package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.xxx.XxxResponse
import id.rancak.app.domain.model.Xxx

fun XxxResponse.toDomain(): Xxx = Xxx(
    uuid      = uuid,
    fieldName = fieldName,
    amount    = amount,
    note      = note,
    createdAt = createdAt
)
```

Special cases in mappers:
```kotlin
// String from API that should be Long in domain (e.g. VariantDto.priceAdjustment)
val amount: Long = amountString.toLongOrNull() ?: 0L

// Enum mapping — use when() with an explicit fallback
val status: XxxStatus = when (statusString) {
    "active"   -> XxxStatus.ACTIVE
    "inactive" -> XxxStatus.INACTIVE
    else       -> XxxStatus.UNKNOWN   // always provide a fallback
}

// Nested DTO → nested domain model
val category: Category? = categoryDto?.toDomain()
```

---

### 3-5. Add to repository interface (`domain/repository/XxxRepository.kt`)

```kotlin
interface XxxRepository {
    suspend fun getXxx(id: String): Resource<Xxx>
    suspend fun listXxx(query: String? = null, page: Int = 1): Resource<List<Xxx>>
    suspend fun createXxx(input: XxxInput): Resource<Xxx>
    suspend fun deleteXxx(id: String): Resource<Unit>
}
```

---

### 3-6. Implement in repository (`data/repository/XxxRepositoryImpl.kt`)

```kotlin
class XxxRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : XxxRepository {

    // Always a computed property — never stored at construction time
    private val tenantId get() = tokenManager.tenantUuid

    // GET single
    override suspend fun getXxx(id: String): Resource<Xxx> =
        safe(
            block    = { api.getXxx(tenantId, id) },
            map      = { it.toDomain() },
            errorMsg = "Gagal memuat data"
        )

    // GET list
    override suspend fun listXxx(query: String?, page: Int): Resource<List<Xxx>> =
        safeList(
            block    = { api.listXxx(tenantId, query = query, page = page) },
            errorMsg = "Gagal memuat daftar"
        ) { it.toDomain() }

    // GET paginated — unwrap .items
    override suspend fun listXxxPaged(page: Int): Resource<List<Xxx>> =
        safe(
            block    = { api.listXxxPaginated(tenantId, page = page) },
            map      = { it.items.map { dto -> dto.toDomain() } },
            errorMsg = "Gagal memuat daftar"
        )

    // POST
    override suspend fun createXxx(input: XxxInput): Resource<Xxx> =
        safe(
            block    = { api.createXxx(tenantId, input.toRequest()) },
            map      = { it.toDomain() },
            errorMsg = "Gagal membuat data"
        )

    // DELETE
    override suspend fun deleteXxx(id: String): Resource<Unit> =
        safeUnit(
            block    = { api.deleteXxx(tenantId, id) },
            errorMsg = "Gagal menghapus data"
        )
}
```

---

### 3-7. Register in Koin (`di/AppModule.kt`)

```kotlin
// In repositoryModule
singleOf(::XxxRepositoryImpl) bind XxxRepository::class

// If constructor needs explicit wiring:
single<XxxRepository> { XxxRepositoryImpl(get(), get()) }
```

---

## Phase 4 — Verify

After completing all steps, verify:

- [ ] `ApiConstants.kt` has the new path constant
- [ ] DTO file is in the correct `dto/<domain>/` subfolder
- [ ] Every monetary `Long` response field has `@Serializable(with = FlexibleLongSerializer::class)`
- [ ] Every camelCase field with a snake_case API name has `@SerialName(...)`
- [ ] API extension functions use `ApiConstants.BASE_URL + ApiConstants.tenantPath()` — no raw strings
- [ ] No `X-API-Key` or `Authorization` header added manually
- [ ] Query params use `parameter()` — not string-interpolated in the URL
- [ ] `idempotencyKey` parameter present only on financial transaction endpoints
- [ ] Mapper covers all domain model fields — no silent omissions
- [ ] Repository `errorMsg` strings are in Bahasa Indonesia
- [ ] Repository registered in `AppModule.kt` with shorthand DSL
- [ ] Repository interface updated in `domain/repository/XxxRepository.kt`
- [ ] No `android.*` imports anywhere in `commonMain`

---

## Common Mistakes and How to Avoid Them

### Mistake 1 — Forgetting FlexibleLongSerializer on monetary fields
Backend sends `"total": "150000.00"` → deserialization crashes with `NumberFormatException`.
**Fix**: annotate every money-related `Long` in response DTOs.

### Mistake 2 — Using raw string URLs in extension functions
`client.get("https://be-rancak.up.railway.app/tenants/$tenantId/xxx")` — breaks when BASE_URL changes.
**Fix**: always use `ApiConstants.BASE_URL + ApiConstants.tenantPath(...) + ApiConstants.XXX`.

### Mistake 3 — Adding X-API-Key manually
The key is already injected by `HttpClientFactory` `DefaultRequest` — adding it again sends the header twice.
**Fix**: remove the manual header call entirely.

### Mistake 4 — Returning `List<XxxResponse>` instead of `ApiResponse<List<XxxResponse>>`
The API extension must always return the envelope type — `RepositoryHelpers.kt` unwraps it.
**Fix**: return `ApiResponse<List<XxxResponse>>` from the extension function.

### Mistake 5 — Querying tenantId at construction time
```kotlin
class XxxRepositoryImpl(api: RancakApiService, tokenManager: TokenManager) {
    private val tenantId = tokenManager.tenantUuid  // ← captures value at injection time
}
```
If the user switches tenant, this will be stale.
**Fix**: always use `private val tenantId get() = tokenManager.tenantUuid` (computed property).

### Mistake 6 — String-interpolating query params into the URL
`client.get("$base/products?q=$query&page=$page")` — breaks with special characters.
**Fix**: use `parameter("q", query)` inside the Ktor request builder.
