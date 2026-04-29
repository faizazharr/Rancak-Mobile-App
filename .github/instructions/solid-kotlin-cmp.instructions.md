---
description: "Use when writing or reviewing Kotlin code in this Compose Multiplatform project. Enforces SOLID OOP principles, Clean Architecture layering, ViewModel/Repository/Domain patterns, and Rancak-specific conventions."
applyTo: "composeApp/src/**/*.kt"
---

# SOLID & Clean Architecture — Rancak POS

## S — Single Responsibility Principle

> Every class has exactly one reason to change.

| Class type | Allowed responsibility | Not allowed |
|---|---|---|
| `ViewModel` | Holds `UiState`, calls repository, emits to `StateFlow` | Direct API/DB calls, UI logic |
| `*RepositoryImpl` | Translates API/local calls to `Resource<T>` | Business rules, UI state |
| Repository **interface** | Declares the contract | Implementation details |
| `Screen` composable | Obtain ViewModel, collect state, delegate to Content | Business logic, data fetching |
| `Content` composable | Render UI from plain data + lambdas | ViewModel access, coroutines |
| Domain `model` | Pure data structure | Serialization annotations, platform types |
| DTO | Wire representation for serialization | Domain logic |
| `Mapper` extension | Map one type to another (DTO → domain) | Any other concern |

```kotlin
// ✅ GOOD — ViewModel has one job: manage screen state
class CartViewModel(private val saleRepository: SaleRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun addItem(product: Product) { /* update state only */ }
}

// ❌ BAD — ViewModel doing network and formatting
class CartViewModel : ViewModel() {
    fun addItem(product: Product) {
        val formatted = "Rp ${product.price / 1000}.000"  // belongs in CurrencyFormatter
        val response = httpClient.post(…)                  // belongs in Repository
    }
}
```

---

## O — Open/Closed Principle

> Open for extension, closed for modification.

- Add new behaviour via **new classes** (new `RepositoryImpl`, new `ViewModel`) — do not add `if/when` branches to existing classes
- Use **sealed classes** and **extension functions** to extend behaviour without modifying existing types
- New API endpoints → new extension function in `data/remote/api/`, not modifying existing functions

```kotlin
// ✅ GOOD — extend Resource without touching the sealed class
fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) action(data)
    return this
}

// ✅ GOOD — new feature = new repository interface + impl, not patching AdminRepository
interface InventoryRepository { … }
class InventoryRepositoryImpl(…) : InventoryRepository { … }
```

---

## L — Liskov Substitution Principle

> Any `*RepositoryImpl` must be fully substitutable for its interface — no surprises.

- Every method declared in the interface **must** be correctly implemented — no `TODO()`, no `throw UnsupportedOperationException()`
- Return types must honour the contract: `Resource<Unit>` means the caller can always safely pattern-match all three variants
- `409` responses must be treated as `Resource.Success(Unit)` per contract — not `Resource.Error`

```kotlin
// ✅ GOOD — impl is a valid substitute; 409 treated as success
override suspend fun createSale(…): Resource<Sale> = try {
    val response = api.createSale(…)
    when {
        response.isSuccess -> Resource.Success(response.data!!.toDomain())
        response.code == 409 -> Resource.Success(cachedSale)  // idempotent duplicate = success
        else -> Resource.Error(response.message ?: errorMsg)
    }
} catch (e: Exception) { Resource.Error(e.message ?: "Kesalahan jaringan") }
```

---

## I — Interface Segregation Principle

> Clients should not be forced to depend on interfaces they don't use.

- One repository interface per domain: `AuthRepository`, `ProductRepository`, `SaleRepository`, `AdminRepository`, `InventoryRepository`, etc.
- ViewModels only depend on the **specific** repository interface they need
- Never inject `AdminRepositoryImpl` directly — always inject via the `AdminRepository` interface

```kotlin
// ✅ GOOD — ViewModel only depends on what it uses
class ShiftViewModel(
    private val operationsRepository: OperationsRepository  // narrow interface
) : ViewModel()

// ❌ BAD — god-interface
interface AppRepository {
    fun getProducts(): …
    fun createSale(): …
    fun getShift(): …
    // 50 more methods…
}
```

---

## D — Dependency Inversion Principle

> Depend on abstractions, not concretions. High-level modules must not depend on low-level modules.

- **All dependencies are injected via Koin** — never instantiate `*RepositoryImpl` manually inside a ViewModel or Screen
- `commonMain` code depends only on **interfaces** from `domain/repository/` — never on `*RepositoryImpl`
- Platform-specific classes (`PrinterManager`, `SyncManager`, `BarcodeScanner`) must be behind `expect`/`actual` — `commonMain` only references the `expect` declaration

```kotlin
// ✅ GOOD — ViewModel depends on abstraction, injected by Koin
class PosViewModel(
    private val productRepository: ProductRepository,  // interface, not impl
    private val saleRepository: SaleRepository          // interface, not impl
) : ViewModel()

// In AppModule.kt — wire concretions once
single<ProductRepository> { ProductRepositoryImpl(get(), get()) }

// ❌ BAD — coupled to concrete class
class PosViewModel : ViewModel() {
    private val repo = ProductRepositoryImpl(httpClient, tokenManager)
}
```

---

## Rancak-Specific SOLID Rules

### Layer boundaries — never cross them

```
presentation/ → ONLY imports from domain/model/ and domain/repository/
data/         → imports domain/model/, domain/repository/, data/remote/dto/, data/util/
domain/       → ZERO imports from data/ or presentation/
```

- `commonMain` classes must never import `android.*` or Apple (`platform.*`) frameworks
- DTOs (`data/remote/dto/`) must never appear in `presentation/` — always map to domain models first

### Repository helpers — use the shared ones

```kotlin
// Always use from data/util/RepositoryHelpers.kt — do NOT redeclare locally
import id.rancak.app.data.util.safe
import id.rancak.app.data.util.safeUnit
import id.rancak.app.data.util.safeList
```

### ViewModel state — immutable updates only

```kotlin
// ✅ GOOD
_uiState.update { it.copy(isLoading = true) }

// ❌ BAD — mutable property exposed outside ViewModel
val uiState = MutableStateFlow(UiState())
```

### Error messages — always Bahasa Indonesia

```kotlin
errorMsg = "Gagal memuat produk"   // ✅
errorMsg = "Failed to load"        // ❌
```

### Money — always Long, always formatted

```kotlin
val price: Long = 35_000L                                    // ✅ stored as Long
val display = CurrencyFormatter.formatRupiah(price)          // ✅ "Rp 35.000"
val price: Double = 35000.0                                  // ❌
val display = "Rp ${price}"                                  // ❌
```

### Role gating — always use `UserRole.atLeast()`

```kotlin
if (currentRole.atLeast(UserRole.ADMIN)) { AdminOnlyButton() }   // ✅
if (currentRole == UserRole.OWNER || currentRole == UserRole.ADMIN) { … } // ❌ fragile
```

### New class checklist

- [ ] Registered in `AppModule.kt` or platform `PlatformModule`?
- [ ] Depends only on interfaces, not concrete implementations?
- [ ] Does exactly one thing?
- [ ] No `android.*` imports in `commonMain`?
- [ ] All `Resource<T>` branches handled at every call site?
