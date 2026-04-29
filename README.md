# Rancak POS

Multi-tenant Point of Sale for restaurants, cafes, and retail — Android & iOS, built with Compose Multiplatform, Kotlin, and an Offline-First architecture.

---

## Table of Contents

1. [Tech Stack](#tech-stack)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [Key Patterns](#key-patterns)
5. [Domain Model](#domain-model)
6. [API Contract](#api-contract)
7. [Offline-First](#offline-first)
8. [Role & Access Control](#role--access-control)
9. [Design System](#design-system)
10. [Build & Run](#build--run)

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| UI | Compose Multiplatform | 1.10.3 |
| Language | Kotlin | 2.3.20 |
| Async | Kotlinx Coroutines | 1.10.2 |
| Serialization | Kotlinx Serialization | 1.11.0 |
| Date/Time | Kotlinx Datetime | 0.7.1 |
| Networking | Ktor Client | 3.4.2 |
| DI | Koin | 4.2.1 |
| Navigation | Compose Navigation | 2.9.2 |
| Storage | multiplatform-settings | 1.3.0 |
| Image loading | Coil | 3.4.0 |
| QR Code | QRose | 1.1.2 |
| **Android only** | WorkManager | 2.11.2 |
| **Android only** | CameraX | 1.6.0 |
| **Android only** | ML Kit Barcode | 17.3.0 |
| **Android only** | Credential Manager | 1.6.0 |
| **Android only** | EncryptedSharedPreferences | 1.1.0 |
| **iOS only** | BGTaskScheduler (native) | — |
| **iOS only** | AVFoundation (native) | — |
| **iOS only** | CoreBluetooth (native) | — |
| Backend (reference) | Rust + Axum + PostgreSQL | `https://api.rancak.id` |

> Min SDK: 24 · Target/Compile SDK: 36

---

## Architecture

Clean Architecture + MVVM. All business logic and UI live in `commonMain` — shared between platforms.

```
┌───────────────────────────────────────────────────────────┐
│  Presentation Layer (commonMain)                          │
│  Compose Screens  ←→  ViewModel (StateFlow / UiState)     │
└────────────────────────┬──────────────────────────────────┘
                         │  Repository interface (domain)
┌────────────────────────▼──────────────────────────────────┐
│  Data Layer (commonMain)                                  │
│  RepositoryImpl  →  RancakApiService (Ktor)               │
│                  →  TokenManager / OfflineSaleQueue        │
└───────────────────────────────────────────────────────────┘
                         │
┌────────────────────────▼──────────────────────────────────┐
│  Platform Layer (androidMain / iosMain)                   │
│  PrinterManager · SyncManager · BarcodeScanner            │
│  GoogleSignInButton · PlatformModule                      │
└───────────────────────────────────────────────────────────┘
```

**Dependency direction**: Presentation → Domain ← Data. The domain layer has no dependency on data or presentation.

---

## Project Structure

```
composeApp/src/
├── commonMain/kotlin/id/rancak/app/
│   ├── data/
│   │   ├── local/          # TokenManager, OfflineSaleQueue, PendingSale
│   │   ├── mapper/         # DtoMappers.kt — DTO → domain model
│   │   ├── printing/       # EscPosBuilder, PrinterManager (expect), SaleReceiptMapper
│   │   ├── remote/
│   │   │   ├── api/        # RancakApiService + per-domain extension files
│   │   │   └── dto/        # kotlinx.serialization request/response DTOs
│   │   ├── repository/     # *RepositoryImpl (Admin, Auth, Finance, Inventory, …)
│   │   ├── security/       # SecureSettings (expect/actual, EncryptedSharedPrefs on Android)
│   │   ├── sync/           # SyncManager (expect)
│   │   └── util/           # RepositoryHelpers (safe/safeList/safeUnit), DateTimeUtils
│   ├── di/                 # AppModule.kt, PlatformModule.kt (expect)
│   ├── domain/
│   │   ├── model/          # Pure Kotlin data classes + sealed classes (Resource, UserRole…)
│   │   └── repository/     # Repository interfaces (one per domain)
│   └── presentation/
│       ├── auth/           # GoogleSignInButton (expect)
│       ├── barcode/        # BarcodeScanner (expect)
│       ├── components/     # Shared composables (buttons, cards, state screens…)
│       ├── designsystem/   # Color.kt, Theme.kt, Typography.kt
│       ├── navigation/     # Screen.kt (type-safe routes), RancakNavHost.kt
│       ├── ui/             # Screens by feature: auth/, pos/, cart/, payment/,
│       │                   # shift/, tables/, kds/, orderboard/, sales/, inventory/,
│       │                   # pricing/, products/, reports/, settings/, billing/…
│       ├── util/           # CurrencyFormatter.kt
│       └── viewmodel/      # One ViewModel per feature, all in commonMain
├── androidMain/            # actual implementations + SyncWorker (WorkManager)
└── iosMain/                # actual implementations + IosSyncRunner (BGTaskScheduler)
```

---

## Key Patterns

### Resource — async result wrapper

```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}
```

All repository methods return `Resource<T>`. Never throw exceptions across layer boundaries.

### UiState — screen state

Every screen uses a `data class UiState` + `StateFlow`. Each field is updated with `_uiState.update { it.copy(…) }`. All screens handle all states explicitly — no implicit defaults.

### Repository helpers — `data/util/RepositoryHelpers.kt`

```kotlin
// map a successful API response
safe(block, map, errorMsg)      // Resource<R>
safeList(block, errorMsg, map)  // Resource<List<R>>
safeUnit(block, errorMsg)       // Resource<Unit>
```

### expect / actual

Platform-specific code lives behind `expect`/`actual` pairs. `commonMain` only declares the `expect`; never import `android.*` or Apple frameworks in `commonMain`.

### Screen split

Every feature is split into two composables:
- `XxxScreen` — retrieves the `ViewModel`, calls `collectAsStateWithLifecycle()`, calls `XxxContent`
- `XxxContent` — pure UI, receives only plain data and lambdas, no ViewModel reference

---

## Domain Model

| Model file | Key types |
|---|---|
| `Auth.kt` | `User`, `AuthToken`, `Tenant` |
| `Product.kt` | `Product`, `Category`, `Variant`, `VariantGroup`, `Modifier`, `Bundle` |
| `Sale.kt` | `Sale`, `SaleItem`, `PaymentMethod`, `SaleStatus` |
| `Operations.kt` | `Shift`, `Table`, `CashExpense`, `ExpenseCategory` |
| `Finance.kt` | `CashIn`, `CashOut`, `FinanceSummary` |
| `Inventory.kt` | `Supplier`, `PurchaseOrder`, `StockOpname`, `StockOpnameDetail` |
| `UserRole.kt` | `UserRole` — enum with `.atLeast(required)` hierarchy check |
| `Resource.kt` | `Resource<T>` — sealed async result |
| `Billing.kt` | `Plan`, `Invoice`, `SubscriptionState` |

---

## API Contract

**Base URL**: `https://api.rancak.id`

**Tenant scope**: Every protected endpoint is prefixed `/tenants/{tenant_uuid}/…`

```
GET    /tenants/:id/products
POST   /tenants/:id/sales        ← requires X-Idempotency-Key: <UUIDv4>
POST   /tenants/:id/sales/batch  ← offline sync upload
GET    /tenants/:id/sync/status
GET    /tenants/:id/sync/catalog?updated_after=…
```

**Auth**: `Authorization: Bearer <access_token>` on every request — handled automatically by Ktor `Auth` plugin.

**Response envelope**:
```json
{ "status": "ok",    "data": { … } }
{ "status": "error", "message": "…", "code": 422 }
```

**HTTP rules**:

| Status | Handling |
|---|---|
| `401` | Auto-refresh token → retry (never force logout) |
| `409` | Idempotency duplicate → **treat as success** |
| `422` | Business error → surface `message` to user in Bahasa Indonesia |

**Date fields**: All `date-time` fields must be sent as `"YYYY-MM-DDT00:00:00Z"`. Use `String.toDateTimeString()` from `data/util/DateTimeUtils.kt`.

---

## Offline-First

- Sales created offline are stored in `OfflineSaleQueue` with `synced = false`
- `SyncManager` (WorkManager on Android / BGTaskScheduler on iOS) uploads them via `POST /sales/batch` when connectivity is restored
- On app start: `GET /sync/status` → if server data is newer, run delta sync via `GET /sync/catalog?updated_after=…`
- **QRIS payments are online-only** — always check connectivity before showing the QRIS option

---

## Role & Access Control

```
STAFF (level 1)  ⊂  ADMIN (level 2)  ⊂  OWNER (level 3)
```

Use `UserRole.atLeast(required)` to gate UI and actions:

```kotlin
if (currentRole.atLeast(UserRole.ADMIN)) { /* show admin feature */ }
```

| Role | Scope |
|---|---|
| STAFF | POS, cart, payment, shift |
| ADMIN | + product management, table management, pricing |
| OWNER | + financial config, reports, billing |

---

## Design System

All tokens defined in `presentation/designsystem/`. Never hardcode values.

**Color tokens**:
- `Primary` = Teal `#0D9373` · `Secondary` = Warm Orange `#E8772E`
- Table status: `StatusAvailable`, `StatusOccupied`, `StatusReserved`, `StatusMaintenance`
- Payment badges: `PaymentCash`, `PaymentCard`, `PaymentQris`, `PaymentTransfer`

**Spacing scale**: `xs = 4dp` · `sm = 8dp` · `md = 16dp` · `lg = 24dp` · `xl = 32dp`

**Currency**: All prices are `Long` (integer Rupiah). Always display with `CurrencyFormatter.formatRupiah()` → `Rp 35.000`

---

## Build & Run

```shell
# Debug APK
./gradlew :composeApp:assembleDebug

# Install to connected Android device
./gradlew :composeApp:installDebug

# iOS — open iosApp/ in Xcode, select a simulator/device, and Run
```

> Requires: JDK 17+, Android Studio Meerkat or later, Xcode 16+

---

## Developer Guide

Architecture rules, API reference, offline-first patterns, product rules, and implementation checklist: **[CLAUDE.md](./CLAUDE.md)**
