# Context Index — Rancak POS Agent

This file is a **lookup map**: if you need to find where a specific rule, pattern,
or concept lives, check here first. It tells you which file is the **authoritative source**
and which files also reference or use the concept.

`copilot-instructions.md` is always pre-loaded by Copilot. You do not need to re-read it
to access concepts listed as "Source: copilot-instructions.md".

For prompt files, read them only when directed by the routing table in `copilot-instructions.md`
or by the "Context This Prompt Depends On" block in each prompt file.

---

## Design System

### `RancakDesign` — single token entry point
- **Source:** `presentation/designsystem/RancakDesign.kt` + `design-system.instructions.md` (auto-loaded)
- **Access:** `RancakDesign.colors / .type / .shapes / .spacing / .elevation / .sizes / .semantic`
- **Rule:** never hardcode `Color(0xFF...)`, raw `dp`, or `fontSize` — always use a token

### Color palette
- **Source:** `presentation/designsystem/Color.kt` + `design-system.instructions.md`
- **Primary:** `#0D9373` teal — CTAs, top bar, selected state, price text
- **Secondary:** `#E8772E` orange — accent, secondary action
- **Background:** `#F2F3F5` enterprise gray (NOT white) — screen background
- **Surface:** white — card/dialog container
- **Semantic colors:** `RancakColors.semantic.success/warning/info` + status + payment — full table in `design-system.instructions.md`

### Typography scale
- **Source:** `presentation/designsystem/Typography.kt` + `design-system.instructions.md`
- **Key mappings:** monetary values → `titleMedium + Bold`, timestamps → `labelSmall + outline color`, status chip → `labelSmall + SemiBold`, buttons → `labelLarge`
- **Rule:** always `style = MaterialTheme.typography.XxxYyy` — never set `fontSize` directly

### Spacing, Elevation, Shapes
- **Source:** `Spacing.kt` / `Elevation.kt` / `Theme.kt` + `design-system.instructions.md`
- **Spacing:** `xs=4 sm=8 md=16 lg=24 xl=32 xxl=48` via `RancakDesign.spacing`
- **Shapes:** `extraSmall=2dp small=4dp medium=6dp large=8dp extraLarge=10dp` — `medium` is the default for cards, buttons, fields
- **Elevation:** `card=1dp cardSelected=2dp raised=4dp modal=8dp`

### Component catalog
- **Source:** `presentation/components/` + `design-system.instructions.md`
- **Components:** `RancakTopBar(title, icon, onBack/onMenu, subtitle, actions)` · `RancakButton` · `RancakOutlinedButton` · `RancakTextField` · `StatusChip(text, color)` · `RoleGate(minRole)` · `RoleGatedScreen` · `LoadingScreen` · `EmptyScreen` · `ErrorScreen`
- **Top bar rule:** `icon =` param is REQUIRED — never omit it
- **Status chip rule:** always `StatusChip` — never a custom Surface+Text combination

---

## Data Layer

### `Resource<T>` sealed class
- **Source:** `copilot-instructions.md` (always loaded)
- **Variants:** `Success(data)`, `Error(message)`, `Loading`
- **Rule:** all three variants must be handled in every `when` block in ViewModels and Screens
- **Referenced in:** `generate-feature.prompt.md`, `integrate-api.prompt.md`, `code-review.prompt.md`

### `safe()` / `safeList()` / `safeUnit()`
- **Source:** `copilot-instructions.md` (always loaded) + `data/repository/RepositoryHelpers.kt`
- **When to use each:**
  - `safe()` — single-item response, needs custom mapping
  - `safeList()` — list response where each item needs `.toDomain()`
  - `safeUnit()` — DELETE or fire-and-forget (no response body)
- **Referenced in:** `integrate-api.prompt.md` (full examples), `code-review.prompt.md` (checklist)

### `FlexibleLongSerializer`
- **Source:** `copilot-instructions.md` (always loaded) + `data/remote/dto/FlexibleLongSerializer.kt`
- **When to use:** on every `Long` field in a **response DTO** that represents money, price, discount, tax, fee, or balance
- **When NOT to use:** String fields, Int fields, non-financial Longs (e.g., `version: Long`), request DTOs
- **Full template:** `.github/prompts/integrate-api.prompt.md` (§ 3-2, "When to use FlexibleLongSerializer")
- **Referenced in:** `generate-feature.prompt.md` (Step 3), `integrate-api.prompt.md` (Step 3-2), `code-review.prompt.md` (DTO checklist)

### `ApiResponse<T>`
- **Source:** `copilot-instructions.md` (always loaded) + `data/remote/dto/ApiResponse.kt`
- **Format:** `statusCode: Int`, `isSuccess = statusCode in 200..299`, `data: T`
- **Rule:** API extension functions always return `ApiResponse<T>` — never a raw `T` or `List<T>`
- **Referenced in:** `integrate-api.prompt.md` (rules table), `code-review.prompt.md` (API Extension checklist)

### `PaginatedData<T>`
- **Source:** `data/remote/dto/PaginatedData.kt`
- **When to use:** when the endpoint returns a paginated list with `{ items: [...], total: N, ... }`
- **How to unwrap in repository:** use `safe()` with `map = { it.items.map { dto -> dto.toDomain() } }`
  (do NOT use `safeList()` — that expects `ApiResponse<List<T>>`, not `ApiResponse<PaginatedData<T>>`)
- **Full example:** `.github/prompts/integrate-api.prompt.md` (§ 3-2, "When to use PaginatedData")

### `ApiConstants`
- **Source:** `copilot-instructions.md` (always loaded) + `data/remote/api/ApiConstants.kt`
- **Rule:** every URL path string must be a constant here — never inline in extension functions
- **Pattern:** `ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + ApiConstants.XXX`
- **Full usage:** `.github/prompts/integrate-api.prompt.md` (§ 3-1, § 3-3)

### `HttpClientFactory` (auto-added headers)
- **Source:** `copilot-instructions.md` (always loaded) + `data/remote/HttpClientFactory.kt`
- **Rule:** never manually add `X-API-Key` or `Authorization` — both are auto-injected
- **Add manually:** `X-Idempotency-Key` on financial transaction endpoints only
- **Referenced in:** `integrate-api.prompt.md` (rules table, Common Mistake #3)

### `tenantId` computed property
- **Source:** `copilot-instructions.md` (always loaded)
- **Correct:** `private val tenantId get() = tokenManager.tenantUuid` (computed every call)
- **Wrong:** `private val tenantId = tokenManager.tenantUuid` (captured at injection time → stale after tenant switch)
- **Referenced in:** `integrate-api.prompt.md` (§ 3-6, Common Mistake #5)

---

## Domain Layer

### Domain model rules
- **Source:** `copilot-instructions.md` (always loaded)
- **Key rules:** pure Kotlin only, no `@Serializable`, no `android.*`, sensible defaults, business rules as methods/computed properties
- **Referenced in:** `generate-feature.prompt.md` (Step 1), `code-review.prompt.md` (Domain Layer checklist)

### `XxxInput` (write operation input model)
- **Source:** `generate-feature.prompt.md` (Step 1)
- **Rule:** write operations use a separate input model in `domain/model/`, not the domain response model
- **Mapper:** `fun XxxInput.toRequest(): CreateXxxRequest` in `data/mapper/XxxMappers.kt`

---

## Presentation Layer

### Three ViewModel patterns
- **Source:** `copilot-instructions.md` (always loaded)
- **Pattern A — simple:** `MutableStateFlow(UiState())` + `.update { it.copy(...) }` → ref: `ShiftViewModel.kt`
- **Pattern B — derived from multiple flows:** `combine() + stateIn(WhileSubscribed(5_000))` → ref: `SalesHistoryViewModel.kt`
- **Pattern C — pre-computed derived fields:** `recompute()` extension on UiState → ref: `PosViewModel.kt`
- **Rule:** choose the simplest pattern that fits — do not use `combine` when `.update` is sufficient
- **Referenced in:** `generate-feature.prompt.md` (Step 7), `code-review.prompt.md` (ViewModel checklist)

### `MutableStateFlow` exposure rule
- **Source:** `copilot-instructions.md` (always loaded)
- **Rule:** `_uiState: MutableStateFlow` is always private; exposed as `val uiState: StateFlow = _uiState.asStateFlow()`
- **Referenced in:** `code-review.prompt.md` (ViewModel checklist — 🔴 Critical)

### `clearError()` function
- **Source:** `copilot-instructions.md` (always loaded)
- **Rule:** every ViewModel must have `fun clearError()` that sets `error = null`; it must be called from the Screen after the error is displayed
- **Referenced in:** `generate-feature.prompt.md` (Step 7), `code-review.prompt.md` (ViewModel + Screen checklists)

### Screen / Content split
- **Source:** `copilot-instructions.md` (always loaded)
- **Rule:** `XxxScreen` owns the ViewModel and collects state with `collectAsStateWithLifecycle()`; `XxxContent` takes plain data + lambdas only — no ViewModel, no Flow
- **Preview rule:** always preview `XxxContent`, never `XxxScreen`
- **Canonical ref:** `presentation/ui/shift/ShiftScreen.kt`
- **Referenced in:** `generate-feature.prompt.md` (Step 8), `code-review.prompt.md` (Screen checklist)

---

## Adaptive Layout

### `BoxWithConstraints` breakpoint
- **Source:** `copilot-instructions.md` (always loaded) + `adaptive-layout.prompt.md` (full patterns)
- **Tablet threshold:** `maxWidth >= 600.dp` — always this value, nowhere else
- **Landscape / wide-phone:** `isWide = maxWidth >= 600.dp || maxWidth > maxHeight`
- **Rule:** always use `BoxWithConstraints` — never `LocalConfiguration` or display metrics
- **Canonical refs:** `PosScreen.kt` (isWide), `LoginScreen.kt` (conditional branch), `ShiftScreen.kt` (constrained column)
- **Full patterns:** `.github/prompts/adaptive-layout.prompt.md`

### Four layout patterns
- **Source:** `.github/prompts/adaptive-layout.prompt.md`
- **Pattern 1 — Conditional branch** (fundamentally different phone vs tablet structures): `LoginScreen.kt`, `PosScreen.kt`
- **Pattern 2 — Constrained single-column** (same structure, capped width on tablet): `ShiftScreen.kt`
- **Pattern 3 — Adaptive grid** (`GridCells.Adaptive(minSize)`): `KdsScreen.kt`
- **Pattern 4 — Master-detail** (list+detail side-by-side on tablet): `SalesHistoryScreen.kt`

### Spacing and density scale
- **Source:** `copilot-instructions.md` (always loaded) + `adaptive-layout.prompt.md` (§ Spacing and Density)
- **Quick reference:**
  - List item gap: `Arrangement.spacedBy(8.dp)` — NOT 24dp (24dp is for section gaps)
  - Card internal padding: `12/8.dp` compact, `16/12.dp` standard, `20.dp` large detail
  - Screen horizontal margin: `16.dp` phone, `24.dp` inside constrained column on tablet
  - Touch targets: minimum `48.dp` height on all tappable elements

### `EmptyDetailPlaceholder`
- **Source:** `.github/prompts/adaptive-layout.prompt.md` (Pattern 4)
- **Rule:** on tablet master-detail layout, the detail panel must show `EmptyDetailPlaceholder` when nothing is selected — never a blank space

### `GridCells.Adaptive` (never `GridCells.Fixed`)
- **Source:** `.github/prompts/adaptive-layout.prompt.md` (Pattern 3) + `code-review.prompt.md` (Adaptive Layout checklist)
- **minSize reference table:** product compact=140dp, product with image=160dp, order/KDS card=260dp, table chip=108dp

---

## Dependency Injection (Koin)

### Koin registration shorthand
- **Source:** `copilot-instructions.md` (always loaded) + `di/AppModule.kt`
- **Repository:** `singleOf(::XxxRepositoryImpl) bind XxxRepository::class`
- **ViewModel:** `viewModelOf(::XxxViewModel)` — never `single` (would be a singleton)
- **When to use explicit form:** only when constructor arguments require manual wiring: `single<XxxRepository> { XxxRepositoryImpl(get(), get(), get()) }`
- **Referenced in:** `generate-feature.prompt.md` (Step 9), `integrate-api.prompt.md` (§ 3-7), `code-review.prompt.md` (DI checklist)

---

## Navigation

### Route definition
- **Source:** `copilot-instructions.md` (always loaded)
- **Rule:** route is a `@Serializable data class` extending `Screen`, registered in both `Screen.kt` and `RancakNavHost.kt`
- **Back navigation:** `navController.popBackStack()` — not `finish()` or custom back stack manipulation
- **Referenced in:** `generate-feature.prompt.md` (Step 10), `code-review.prompt.md` (Navigation checklist)

---

## Cross-Cutting Rules

### All user-facing strings in Bahasa Indonesia
- **Source:** `copilot-instructions.md` (always loaded)
- **Applies to:** `errorMsg` in repositories, UI labels, empty state messages, error banners
- **Referenced in:** `integrate-api.prompt.md` (§ 3-6), `generate-feature.prompt.md` (checklist), `code-review.prompt.md` (Cross-Cutting checklist)

### All prices as `Long`, displayed via `formatRupiah()`
- **Source:** `copilot-instructions.md` (always loaded)
- **Rule:** never store prices as `Double` or `Float`; never display raw numbers — always `CurrencyFormatter.formatRupiah(amount)`
- **Referenced in:** `code-review.prompt.md` (Cross-Cutting checklist)

### No `android.*` imports in `commonMain`
- **Source:** `copilot-instructions.md` (always loaded)
- **Applies to:** domain models, repository implementations, ViewModels — anything in `commonMain`
- **What to use instead:** `expect`/`actual` pairs in `androidMain` / `iosMain`
- **Referenced in:** `code-review.prompt.md` (🔴 Critical in every checklist)

---

## Canonical Reference Files

| Pattern | File |
|---|---|
| Simple ViewModel | `presentation/viewmodel/ShiftViewModel.kt` |
| Complex ViewModel (combine + stateIn) | `presentation/viewmodel/SalesHistoryViewModel.kt` |
| Derived fields (recompute) | `presentation/viewmodel/PosViewModel.kt` |
| Screen + Content split | `presentation/ui/shift/ShiftScreen.kt` |
| Constrained column (form on tablet) | `presentation/ui/shift/ShiftScreen.kt` |
| Conditional branch phone/tablet | `presentation/ui/auth/LoginScreen.kt` |
| Master-detail tablet layout | `presentation/ui/sales/SalesHistoryScreen.kt` |
| Adaptive grid | `presentation/ui/kds/KdsScreen.kt` |
| Wide/landscape detection | `presentation/ui/pos/PosScreen.kt` |
| Repository with helpers | `data/repository/InventoryRepositoryImpl.kt` |
| Koin registration | `di/AppModule.kt` |
| Card anatomy (list item, left-edge strip) | `presentation/ui/sales/components/SaleCard.kt` |
| KPI card (icon-in-box pattern) | `presentation/ui/reports/components/KpiCards.kt` |
| Settings nav item (selected state) | `presentation/ui/settings/components/SettingsPrimitives.kt` |
| Product list with FAB / tablet inline button | `presentation/ui/products/ProductManagementScreen.kt` |
