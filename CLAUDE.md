# Rancak POS — Project Instructions

## What is this project?

**Rancak POS** is a multi-tenant Point of Sale app for restaurants, cafes, and retail. Built with Compose Multiplatform (shared UI for Android + iOS). Backend is Rust/Axum — do not modify it.

Key traits: **offline-first** (sales work without internet), **multi-tenant** (all API calls are scoped to a `tenant_uuid`), **role-based** (Staff → Admin → Owner with progressively more access).

---

## Tech Stack

- **UI**: Compose Multiplatform (`commonMain`) — shared screens for Android and iOS
- **Networking**: Ktor Client 2.x
- **DI**: Koin 3.x
- **Storage**: `multiplatform-settings` (no SQLDelight — offline queue uses in-memory + settings)
- **Navigation**: Compose Navigation with `@Serializable` type-safe routes
- **QR Code**: QRose (for QRIS payment display)
- **Android extras**: WorkManager (sync), CameraX + ML Kit (barcode), Credential Manager (Google SSO)
- **iOS extras**: BGTaskScheduler (sync), AVFoundation (barcode), CoreBluetooth (printer)

---

## Project Structure

```
composeApp/src/
├── commonMain/kotlin/id/rancak/app/
│   ├── data/
│   │   ├── local/          # TokenManager, OfflineSaleQueue, PendingSale
│   │   ├── mapper/         # DtoMappers.kt (DTO → domain)
│   │   ├── printing/       # EscPosBuilder, PrinterManager (expect), SaleReceiptMapper
│   │   ├── remote/         # HttpClientFactory, RancakApiService, dto/
│   │   ├── repository/     # AuthRepositoryImpl, ProductRepositoryImpl, SaleRepositoryImpl,
│   │   │                   # OperationsRepositoryImpl, FinanceRepositoryImpl
│   │   └── sync/           # SyncManager (expect)
│   ├── di/                 # AppModule.kt, PlatformModule.kt (expect)
│   ├── domain/
│   │   ├── model/          # Auth, Product, Sale, Operations, Finance, Resource
│   │   └── repository/     # interfaces: AuthRepository, ProductRepository, SaleRepository,
│   │                       #             OperationsRepository, FinanceRepository
│   └── presentation/
│       ├── auth/           # GoogleSignInButton (expect)
│       ├── barcode/        # BarcodeScanner (expect)
│       ├── components/     # Buttons, ProductCard, QrCodeComposable, StateScreens, etc.
│       ├── designsystem/   # Color.kt, Theme.kt, Typography.kt
│       ├── navigation/     # Screen.kt (routes), RancakNavHost.kt
│       ├── ui/             # LoginScreen, TenantPickerScreen, PosScreen, CartScreen,
│       │                   # PaymentScreen, ShiftScreen, TableMapScreen, KdsScreen,
│       │                   # OrderBoardScreen, SalesHistoryScreen, CashExpenseScreen, ReportScreen
│       ├── util/           # CurrencyFormatter.kt
│       └── viewmodel/      # All ViewModels (Login, TenantPicker, Pos, Cart, Payment,
│                           # Shift, Table, Kds, OrderBoard, SalesHistory, CashExpense, Report)
├── androidMain/            # PrinterManager.android, SyncManager.android, SyncWorker,
│                           # GoogleSignInButton.android, BarcodeScanner.android, PlatformModule.android
└── iosMain/                # PrinterManager.ios, SyncManager.ios, IosSyncRunner,
                            # GoogleSignInButton.ios, BarcodeScanner.ios, PlatformModule.ios
```

---

## Architecture

Clean Architecture + MVVM. All layers live in `commonMain` and are shared between platforms.

```
Compose UI  →  ViewModel (StateFlow)  →  Repository  →  RancakApiService / local storage
```

- **Domain models** (`domain/model/`) — pure Kotlin data classes, no platform imports
- **Repository interfaces** (`domain/repository/`) — contracts; implementations in `data/repository/`
- **ViewModels** (`presentation/viewmodel/`) — fully shared, use `StateFlow` + `UiState`
- **Screens** (`presentation/ui/`) — every screen split into `Screen` (holds ViewModel) + `Content` (pure UI, no VM)
- **Platform code** — always behind `expect`/`actual` (printer, barcode, sync, Google SSO)

### UiState pattern
```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```
Always handle all four variants in every screen.

---

## API

**Base URL**: `https://api.rancak.id`

**Tenant-scoped pattern**: `/tenants/:tenant_uuid/<resource>`
- `tenantUuid` always comes from `TokenManager.tenantUuid` — never hardcode it

**Auth headers** on every protected request (handled by Ktor `Auth` plugin automatically):
```
Authorization: Bearer <access_token>
Content-Type:  application/json
```

**Response envelope**:
```json
{ "status": "ok", "data": { ... } }
{ "status": "error", "message": "...", "code": 404 }
```

**Key HTTP rules**:
- `401` → auto-refresh token → retry (never logout on 401)
- `409` → idempotency duplicate → **treat as success**, not error
- `422` → business error (e.g., stok habis) → show message to user
- `POST /sales` always requires header `X-Idempotency-Key: <UUID v4>`

---

## Offline-First

- Transactions created offline go into `OfflineSaleQueue` with `synced = false`
- `SyncManager` + WorkManager (Android) / BGTaskScheduler (iOS) uploads them via `POST /tenants/:id/sales/batch` when online
- On app start: call `GET /tenants/:id/sync/status` → if server data is newer, trigger delta sync via `GET /tenants/:id/sync/catalog?updated_after=...`
- **QRIS payment is online-only** — always check connectivity before showing QRIS option

---

## Key Product Rules

- **Roles**: Staff (cashier only) → Admin (+ product/table management) → Owner (+ financial config). Hide Admin/Owner UI from Staff.
- **Shift**: Cashier must open a shift before creating any sale. Block sale creation if no open shift.
- **Currency**: All prices are `Long` (integer Rupiah). Display with `CurrencyFormatter.formatRupiah()` → `Rp 35.000`
- **Error messages**: Always show errors to the user in **Bahasa Indonesia**
- **Device ID**: Use `TokenManager.deviceId` (stable UUID) on every sale payload as `device_id`
- **Table status**: Changes automatically (`occupied` on sale created, `available` on void/serve/cancel)
- **86**: Product marked as out-of-stock for the day. Resets automatically each day server-side.

---

## Design System

Colors, spacing, and typography are defined in `presentation/designsystem/`. Never hardcode values.

Key semantic tokens to use (defined in `Color.kt`):
- `Primary` = Teal `#0D9373`, `Secondary` = Warm Orange `#E8772E`
- `StatusAvailable / Occupied / Reserved / Maintenance` — for table status chips
- `PaymentCash / Card / Qris / Transfer` — for payment method badges

Spacing: `Spacing.xs(4dp) / sm(8dp) / md(16dp) / lg(24dp) / xl(32dp)`

---

## Checklist (before submitting code)

- [ ] No `android.*` or Apple imports in `commonMain`
- [ ] All repository methods return `AppResult<T>`
- [ ] ViewModels expose `StateFlow`, not `MutableStateFlow`
- [ ] All four `UiState` variants handled in every screen
- [ ] New classes registered in Koin `AppModule` or platform `PlatformModule`
- [ ] `POST /sales` sends `X-Idempotency-Key`
- [ ] `409` treated as success; `401` triggers refresh not logout
- [ ] Prices as `Long`, displayed via `CurrencyFormatter`
- [ ] Error messages in Bahasa Indonesia
- [ ] Role-gated UI: Staff cannot see Admin/Owner features
