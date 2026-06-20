# Rancak POS — Project Instructions

**Rancak POS** is a multi-tenant Point of Sale app (restaurants, cafes, retail). Compose Multiplatform — single shared UI for Android + iOS. Backend is Rust/Axum — **do not modify it**.

Key traits: offline-first, multi-tenant (`tenant_uuid` scopes every API call), role-based (Staff → Admin → Owner).

---

## Tech Stack

| Layer | Library |
|---|---|
| UI | Compose Multiplatform (`commonMain`) |
| Network | Ktor Client 2.x |
| DI | Koin 3.x |
| Storage | `multiplatform-settings` (no SQLDelight) |
| Navigation | Compose Navigation + `@Serializable` routes |
| Sync (Android) | WorkManager |
| Sync (iOS) | BGTaskScheduler |
| Barcode | CameraX + ML Kit (Android) / AVFoundation (iOS) |
| Auth (Android) | Credential Manager (Google SSO) |
| Bluetooth print | CoreBluetooth (iOS) |

---

## Architecture

Clean Architecture + MVVM. All layers in `commonMain`.

```
Compose UI  →  ViewModel (StateFlow<UiState>)  →  Repository  →  RancakApiService / local storage
```

- `domain/model/` — pure Kotlin data classes, zero platform imports
- `domain/repository/` — interfaces; implementations in `data/repository/`
- `presentation/viewmodel/` — fully shared, expose `StateFlow`, never `MutableStateFlow`
- `presentation/ui/` — each screen split: `*Screen` (holds VM) + `*Content` (pure UI, testable)
- Platform code always behind `expect`/`actual` (printer, barcode, sync, Google SSO)
- `di/AppModule.kt` + platform `PlatformModule.kt` — every new class must be registered here

---

## API

**Base URL**: `https://api.rancak.id` — tenant-scoped: `/tenants/:tenant_uuid/<resource>`

`tenantUuid` always from `TokenManager.tenantUuid` — never hardcode.

Auth headers handled automatically by Ktor `Auth` plugin (Bearer token).

**Key HTTP rules:**
- `401` → auto-refresh token → retry. Never logout on 401.
- `409` → idempotency duplicate → **treat as success**, not error
- `422` → business error → show message to user in Bahasa Indonesia
- `POST /sales` requires header `X-Idempotency-Key: <UUID v4>`

---

## Offline-First

- Offline sales go into `OfflineSaleQueue` (`synced = false`)
- `SyncManager` uploads via `POST /tenants/:id/sales/batch` when online
- App start: `GET /tenants/:id/sync/status` → delta sync via `GET /tenants/:id/sync/catalog?updated_after=...`
- QRIS is **online-only** — always check connectivity before showing QRIS

---

## Key Product Rules

- **Roles**: Staff (cashier only) → Admin (+ product/table mgmt) → Owner (+ financial config). Gate UI accordingly.
- **Shift**: Must be open before any sale. Block sale creation if no open shift.
- **Currency**: All prices `Long` (integer Rupiah). Display via `CurrencyFormatter.formatRupiah()` → `Rp 35.000`
- **Device ID**: `TokenManager.deviceId` on every sale payload as `device_id`
- **Table status**: Auto-changes (`occupied` on sale create, `available` on void/serve/cancel)
- **86**: Product out-of-stock for the day. Resets server-side each day.
- **Error messages**: Always in **Bahasa Indonesia**

---

## Design System

Never hardcode colors, spacing, or typography — always use design system tokens from `presentation/designsystem/`.

- Primary = Teal `#0D9373`, Secondary = Warm Orange `#E8772E`
- `StatusAvailable / Occupied / Reserved / Maintenance` — table status chips
- `PaymentCash / Card / Qris / Transfer` — payment method badges
- Spacing: `Spacing.xs(4dp) / sm(8dp) / md(16dp) / lg(24dp) / xl(32dp)`

---

## Coding Rules (enforced every time)

### 1. No FQCN — always use `import`

Never write package paths inline in code. Add an `import`, use the simple name.

```kotlin
// WRONG
border = androidx.compose.foundation.BorderStroke(1.dp, color)
```
```kotlin
// CORRECT
import androidx.compose.foundation.BorderStroke
border = BorderStroke(1.dp, color)
```

**Edit tool safety**: never use `replace_all: true` on a FQCN string — it mangles the import line. Never use `sed` or `perl` for multi-line replacements — they corrupt import statements. Use `Edit` per-occurrence.

---

### 2. `suspend fun` never inside `_uiState.update {}`

`update {}` takes a non-suspend `(T) -> T` lambda. Any `suspend` call inside won't compile.

```kotlin
// WRONG — compile error
_uiState.update { it.copy(searchQuery = q).recompute() }

// CORRECT
viewModelScope.launch {
    _uiState.value = _uiState.value.copy(searchQuery = q).recompute()
}
```

Use `_uiState.update {}` only for simple non-suspend `copy()`. Wrap in `launch` whenever a suspend call is needed.

---

### 3. Parallelization inside `launch`

```kotlin
viewModelScope.launch {
    coroutineScope {
        val a = async { repo.getA() }
        val b = async { repo.getB() }
        process(a.await(), b.await())
    }
}
```

---

### 4. `ImmutableList<T>` for all list fields in UiState and Composable params

`List<T>` causes a Compose **runtime stability warning** (`MutableList` also implements `List`). Use `ImmutableList<T>` for compile-time stability.

```kotlin
// WRONG
data class MyUiState(val items: List<Product> = emptyList())
```
```kotlin
// CORRECT
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class MyUiState(val items: ImmutableList<Product> = persistentListOf())

// Every .map{} / .filter{} / .sortedBy{} must end with .toImmutableList()
_uiState.update { it.copy(items = result.data.toImmutableList()) }
_uiState.update { it.copy(items = it.items.filter { p -> p.isActive }.toImmutableList()) }
```

**Exception**: if the field nullable-defaults to `null`, the default stays `null` — don't add `persistentListOf()`. Only add `toImmutableList` import if actually used in the file.

---

### 5. `@Stable` for form state holders

A class with `var` properties shows **"Has N mutable (var) properties — Unstable"** unless annotated `@Stable`. Valid only when **every** `var` is backed by `mutableStateOf()`.

```kotlin
@Stable
private class ProductFormState {
    var name by mutableStateOf("")
    var price by mutableStateOf("")
}
```

Do **not** use `@Stable` if any `var` is a plain Kotlin field — that's a false annotation.

---

### 6. KDoc `[Symbol]` — only for public importable symbols

`[SymbolName]` triggers **"Cannot resolve symbol"** warning for private/internal/cross-package references. Use backticks instead.

- `` `KEY_MIGRATION_DONE` `` not `[KEY_MIGRATION_DONE]` — private const
- `` `RancakApiService` `` not `[RancakApiService]` — cross-package, not imported
- `` `clearBearerToken` `` not `[clearBearerToken]` — private constructor param

FQN `[id.rancak.app.data.remote.api.RancakApiService]` resolves if the class exists.

---

## Checklist (before submitting code)

- [ ] No `android.*` or Apple imports in `commonMain`
- [ ] No FQCN in code body — every class uses a proper `import`
- [ ] All repository methods return `Resource<T>`
- [ ] ViewModels expose `StateFlow`, not `MutableStateFlow`
- [ ] All four `UiState` variants handled in every screen
- [ ] `suspend fun` never called inside `_uiState.update {}` — use `launch` instead
- [ ] All `List<T>` in UiState/Composable params → `ImmutableList<T>` + `persistentListOf()` default
- [ ] All `.map {}` / `.filter {}` / `.sortedBy {}` on list fields end with `.toImmutableList()`
- [ ] Only import `persistentListOf` / `toImmutableList` if actually used in the file
- [ ] Form state holders with `var mutableStateOf` annotated `@Stable`
- [ ] KDoc `[Symbol]` only for importable public symbols — backticks for private/internal
- [ ] New classes registered in Koin `AppModule` or platform `PlatformModule`
- [ ] `POST /sales` sends `X-Idempotency-Key`
- [ ] `409` treated as success; `401` triggers refresh not logout
- [ ] Prices as `Long`, displayed via `CurrencyFormatter`
- [ ] Error messages in Bahasa Indonesia
- [ ] Role-gated UI: Staff cannot see Admin/Owner features
