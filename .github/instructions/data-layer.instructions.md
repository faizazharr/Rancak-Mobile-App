---
description: "Data layer patterns for Rancak POS: RepositoryImpl, DTO, Mapper, API extension, FlexibleLongSerializer, ApiConstants, RepositoryHelpers."
applyTo: "composeApp/src/**/data/**/*.kt"
---

# Data Layer Patterns — Rancak POS

## Repository Implementation

Every method uses one of three helpers from `data/util/RepositoryHelpers.kt`.
Never inline a `try/catch` — use the helpers:

```kotlin
class XxxRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : XxxRepository {

    // Always computed — reads fresh tenantId on every call, never captured at injection time
    private val tenantId get() = tokenManager.tenantUuid

    // GET list → safeList (each item mapped individually)
    override suspend fun listXxx(query: String?): Resource<List<Xxx>> =
        safeList(
            block    = { api.listXxx(tenantId, query = query) },
            errorMsg = "Gagal memuat daftar"
        ) { it.toDomain() }

    // GET single → safe with map lambda
    override suspend fun getXxx(id: String): Resource<Xxx> =
        safe(
            block    = { api.getXxx(tenantId, id) },
            map      = { it.toDomain() },
            errorMsg = "Gagal memuat data"
        )

    // POST → safe with map lambda
    override suspend fun createXxx(input: XxxInput): Resource<Xxx> =
        safe(
            block    = { api.createXxx(tenantId, input.toRequest()) },
            map      = { it.toDomain() },
            errorMsg = "Gagal membuat data"
        )

    // DELETE → safeUnit (no meaningful response body)
    override suspend fun deleteXxx(id: String): Resource<Unit> =
        safeUnit(
            block    = { api.deleteXxx(tenantId, id) },
            errorMsg = "Gagal menghapus data"
        )
}
```

**Rules:**
- `tenantId` is always `get() = tokenManager.tenantUuid` — never `val tenantId = tokenManager.tenantUuid`
- `errorMsg` is always Bahasa Indonesia
- Implement every method declared in the interface — no `TODO()` stubs
- HTTP 409 is treated as success by the `safe()` helpers — do not add special handling

### Paginated endpoints

When the endpoint returns `ApiResponse<PaginatedData<XxxDto>>`, use `safe()` and unwrap `.items`:

```kotlin
override suspend fun listXxxPaged(page: Int): Resource<List<Xxx>> =
    safe(
        block    = { api.listXxxPaginated(tenantId, page = page) },
        map      = { it.items.map { dto -> dto.toDomain() } },
        errorMsg = "Gagal memuat daftar"
    )
// Do NOT use safeList() — it expects ApiResponse<List<T>>, not ApiResponse<PaginatedData<T>>
```

---

## DTO

```kotlin
package id.rancak.app.data.remote.dto.xxx

import id.rancak.app.data.remote.dto.FlexibleLongSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class XxxResponse(
    val uuid: String,
    @SerialName("field_name") val fieldName: String,  // @SerialName when JSON is snake_case

    // Monetary fields MUST use FlexibleLongSerializer — backend may send "60000.00" or 60000
    @Serializable(with = FlexibleLongSerializer::class) val price: Long = 0,
    @Serializable(with = FlexibleLongSerializer::class) val total: Long = 0,

    // Non-monetary Long fields (IDs, counters, versions) do NOT need it
    val itemCount: Int = 0,

    // Optional fields must have defaults so missing JSON keys don't crash
    val note: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CreateXxxRequest(
    @SerialName("field_name") val fieldName: String,
    val amount: Long   // request DTOs: no FlexibleLongSerializer (you control the format)
)
```

**FlexibleLongSerializer — when to use:**
- price, total, subtotal, amount, balance, discount, tax, fee, surcharge, tip
- Any `Long` from the API that represents a financial figure

**FlexibleLongSerializer — when NOT to use:**
- String fields, Int/Boolean fields, non-financial Longs (`version`, `itemCount`)
- Request DTO fields

---

## Mapper

Extension functions only — no class instantiation, no injected dependencies:

```kotlin
// data/mapper/XxxMappers.kt
fun XxxResponse.toDomain(): Xxx = Xxx(
    uuid      = uuid,
    fieldName = fieldName,
    price     = price,
    createdAt = createdAt
)

fun XxxInput.toRequest(): CreateXxxRequest = CreateXxxRequest(
    fieldName = field,
    amount    = amount
)

// String → Long conversion when the API sends a price as String
val amount: Long = amountString.toLongOrNull() ?: 0L

// Enum mapping — always provide an explicit fallback
val status: XxxStatus = when (statusString) {
    "active"   -> XxxStatus.ACTIVE
    "inactive" -> XxxStatus.INACTIVE
    else       -> XxxStatus.UNKNOWN  // never let unknown values crash
}
```

---

## API Extension

```kotlin
package id.rancak.app.data.remote.api

// GET list with optional query params
suspend fun RancakApiService.listXxx(
    tenantUuid: String,
    query: String? = null,
    page: Int = 1,
    limit: Int = 20
): ApiResponse<List<XxxResponse>> =
    client.get(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX) {
        parameter("page", page)
        parameter("limit", limit)
        query?.let { parameter("q", it) }  // only add when non-null
    }.body()

// POST standard
suspend fun RancakApiService.createXxx(
    tenantUuid: String,
    request: CreateXxxRequest
): ApiResponse<XxxResponse> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

// POST financial transaction — idempotency key required
suspend fun RancakApiService.createXxxTransaction(
    tenantUuid: String,
    request: CreateXxxRequest,
    idempotencyKey: String   // generated by the repository caller: uuid4().toString()
): ApiResponse<XxxResponse> =
    client.post(ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX) {
        contentType(ContentType.Application.Json)
        header("X-Idempotency-Key", idempotencyKey)
        setBody(request)
    }.body()

// PATCH — partial update (only changed fields)
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

// DELETE — returns Unit (no body expected)
suspend fun RancakApiService.deleteXxx(
    tenantUuid: String,
    id: String
): ApiResponse<Unit> =
    client.delete(
        ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "${ApiConstants.XXX}/$id"
    ).body()
```

**Rules — never break these:**

| ❌ Never | ✅ Always |
|---|---|
| `header("X-API-Key", ...)` | Auto-added by `HttpClientFactory` — never add manually |
| `header(HttpHeaders.Authorization, ...)` | Handled by Ktor `Auth` plugin |
| `"$baseUrl/tenants/$id/xxx"` raw string | `ApiConstants.BASE_URL + ApiConstants.tenantPath(uuid) + ApiConstants.XXX` |
| `"?page=$page"` in URL | `parameter("page", page)` inside request builder |
| Returning `XxxResponse` directly | Always wrap: `ApiResponse<XxxResponse>` |

---

## ApiConstants

Every URL path string lives in `ApiConstants.kt`. Never inline path strings:

```kotlin
// Add to ApiConstants.kt before writing any extension function
const val XXX = "/xxx"

// Pattern for sub-resources
fun xxxPath(id: String) = "/xxx/$id"
```
