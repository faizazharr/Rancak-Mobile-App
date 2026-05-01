# Code Review — Rancak POS

Use this prompt when you want the agent to evaluate existing code systematically —
not generate new code, but assess what's already there against the project's standards.

---

## Context This Prompt Depends On

`copilot-instructions.md` is **always pre-loaded** by Copilot — you already have access to:
- All architectural rules (layer ownership, `Resource<T>`, UiState pattern)
- Koin shorthand DSL and registration rules
- `ApiResponse<T>` format and `ApiConstants` patterns
- Non-negotiable rules table (the hard constraints this review enforces)
- Design system tokens (used to catch hardcoded values)

**Do not re-read `copilot-instructions.md`.**

The checklists in this prompt are exhaustive on their own.
You only need to read additional files in these specific situations:

| Situation | Also read |
|---|---|
| Reviewing a Screen for adaptive layout correctness | `.github/prompts/adaptive-layout.prompt.md` — use the full layout checklist there |
| Reviewing an API extension or DTO | `.github/prompts/integrate-api.prompt.md` — especially the "Common Mistakes" section |
| The review reveals structural problems that need fixing (not just flagging) | `.github/prompts/refactor-structure.prompt.md` |
| You encounter an unfamiliar pattern and need to locate the canonical example | `.github/CONTEXT_INDEX.md` |

> **For a full-feature review** (data + ViewModel + Screen), read
> `integrate-api.prompt.md` and `adaptive-layout.prompt.md` before Phase 2.
> Running only the checklists in this file will miss data-layer and layout-specific checks.

---

## How to Use

Attach the file(s) you want reviewed and describe the scope:

> "Review `PosViewModel.kt` for architectural correctness."

> "Review the entire `data/repository/` layer for consistency."

> "Review this new feature I just wrote before I commit it."

> "Do a full audit of the `presentation/ui/inventory/` folder."

The agent must produce a structured report, not a list of vague suggestions.

---

## Review Protocol

Follow every phase in order. Do not skip phases.

### Phase 1 — Read Everything First

Read all files in scope before forming any judgment. For each file, note:
- What is this file's stated purpose (class name, package)?
- What does it actually do (read the implementation)?
- What does it depend on (imports, constructor parameters)?

Do not start Phase 2 until you have read all files in scope.

### Phase 2 — Run the Systematic Checks

For each file, run every check in the categories below. Report every finding,
including files that pass — "No issues found" is a valid result per category.

---

## Checklist: Data Layer

### Repository
- [ ] Every method returns `Resource<T>` — no raw return types, no thrown exceptions
- [ ] Every method uses `safe()` / `safeList()` / `safeUnit()` — no inlined `try/catch`
- [ ] `tenantUuid` is always read from `tokenManager.tenantUuid` — never hardcoded or passed as a parameter from outside
- [ ] `errorMsg` strings are in Bahasa Indonesia
- [ ] HTTP 409 is treated as success (check idempotent endpoints)
- [ ] HTTP 401 triggers token refresh, not logout
- [ ] Class is registered in `repositoryModule` in `AppModule.kt` using `singleOf(::Impl) bind Interface::class`
- [ ] No business logic lives here — the repository only translates between remote/local and domain

### DTO
- [ ] Class is `@Serializable`
- [ ] All fields annotated with `@SerialName` where the JSON name differs from Kotlin naming
- [ ] Every `Long` field representing money, price, fee, discount, tax, or balance has `@Serializable(with = FlexibleLongSerializer::class)` — backend may send `"60000.00"` as a string
- [ ] Optional fields have default values (`= null` or `= 0`) so missing JSON keys don't crash deserialization
- [ ] No domain model types imported — DTOs are self-contained
- [ ] No `android.*` or Apple imports

### Mapper
- [ ] Extension functions only (`fun XxxDto.toDomain(): Xxx`) — no class instantiation
- [ ] Maps every field — no silent omissions
- [ ] Input mapper exists if there are write operations (`fun XxxInput.toRequest(): XxxRequest`)

### API Extension
- [ ] Extension function on `RancakApiService`, not a standalone function
- [ ] URL constructed with `ApiConstants.BASE_URL + ApiConstants.tenantPath(uuid) + ApiConstants.XXX` — no raw strings
- [ ] Query params use `parameter("key", value)` inside the builder — never string-interpolated into the URL
- [ ] No manually added `X-API-Key` or `Authorization` headers — both are auto-injected
- [ ] `POST` endpoints that create transactions include `X-Idempotency-Key`
- [ ] `contentType(ContentType.Application.Json)` set on write operations
- [ ] Return type is always `ApiResponse<T>` — never a raw `T` or `List<T>`

---

## Checklist: Domain Layer

- [ ] Pure Kotlin only — zero imports from `android.*`, `ktor`, `koin`, or any framework
- [ ] No `@Serializable` annotations (those belong in DTOs)
- [ ] Data classes have sensible defaults where appropriate
- [ ] Business rules expressed as methods or computed properties on the model, not scattered in ViewModels or UI
- [ ] Sealed classes / enums used where a field has a fixed set of values

---

## Checklist: Presentation — ViewModel

- [ ] `MutableStateFlow` is private; public exposure is `StateFlow` via `.asStateFlow()`
- [ ] State pattern matches complexity:
  - Simple state → `MutableStateFlow(UiState()) + .update { it.copy(...) }` (ref: `ShiftViewModel`)
  - Derived state from multiple flows → `combine() + stateIn(WhileSubscribed(5_000))` (ref: `SalesHistoryViewModel`)
  - Pre-computed derived fields → `recompute()` extension on UiState (ref: `PosViewModel`)
- [ ] All `Resource` variants handled: `Success`, `Error`, `Loading` (even if `Loading -> {}`)
- [ ] No API calls or Ktor/HTTP code — only repository calls
- [ ] No Compose imports — ViewModels are framework-independent
- [ ] No `android.*` imports
- [ ] Class registered with `viewModelOf(::XxxViewModel)` in `viewModelModule` in `AppModule.kt`
- [ ] `isLoading` and `error` reset correctly at the start of each async operation
- [ ] `clearError()` function exists and is called after error is displayed

---

## Checklist: Presentation — Screen

- [ ] File is split into two composables: `XxxScreen` (has ViewModel) and `XxxContent` (no ViewModel)
- [ ] `XxxScreen` collects state with `collectAsStateWithLifecycle()`
- [ ] `XxxContent` parameters are plain data types and lambdas only — no ViewModel, no Flow
- [ ] `XxxContent` default parameter values are no-ops (`= {}` for lambdas, empty/null for data)
- [ ] Preview composables use `XxxContent`, not `XxxScreen`
- [ ] All four UI states handled: loading, error, empty, and data-present
- [ ] Error is shown to user and cleared — not silently swallowed
- [ ] No hardcoded `Color(0xFF...)` — use `MaterialTheme.colorScheme` or `RancakColors.semantic`
- [ ] `Text` uses `style = MaterialTheme.typography.XxxYyy` — no raw `fontSize`
- [ ] `RancakTopBar` used with required `icon = Icons.Default.Xxx` param — never omitted
- [ ] `StatusChip(text, color)` used for status labels — not a custom Surface+Text combination
- [ ] Role-gating applied for any Admin/Owner-only actions using `currentRole.atLeast(UserRole.XXX)`
- [ ] No `android.*` or Apple imports

---

## Checklist: DI (AppModule.kt)

- [ ] Every new `RepositoryImpl` is registered in `repositoryModule`
- [ ] Every new `ViewModel` is registered in `viewModelModule`
- [ ] Registration uses shorthand DSL where possible: `singleOf(...) bind ...` / `viewModelOf(...)`
- [ ] No ViewModel registered as `single` (would be a singleton, not a per-screen instance)
- [ ] No duplicate registrations

---

## Checklist: Navigation

- [ ] Route is a `@Serializable` data class that extends `Screen`
- [ ] Route is registered in both `Screen.kt` and `RancakNavHost.kt`
- [ ] Back navigation calls `navController.popBackStack()`
- [ ] Arguments passed via route data class fields, not via shared ViewModel or singleton state

---

## Checklist: Adaptive Layout & Responsive Design

- [ ] Screen uses `BoxWithConstraints` to detect available width — not `LocalConfiguration` or display metrics
- [ ] Tablet breakpoint is `maxWidth >= 600.dp` — consistent with the rest of the codebase
- [ ] When phone and tablet layouts differ significantly: separate private composables (`TabletXxxLayout`, `PhoneXxxLayout`) — not deeply nested `if (isTablet)` inline
- [ ] Single-column forms and detail views use `widthIn(max = 560.dp)` on tablet — not `fillMaxWidth`
- [ ] Grids use `GridCells.Adaptive(minSize = X.dp)` — not `GridCells.Fixed(n)`
- [ ] List-with-detail screens use master-detail layout on tablet (side-by-side `Row` with `weight`)
- [ ] `EmptyDetailPlaceholder` shown when nothing is selected in master-detail tablet layout
- [ ] Touch targets are at least `48.dp` high for all tappable elements
- [ ] Screen edge padding: `16.dp` horizontal on phone, `24.dp` inside constrained column on tablet
- [ ] Card internal padding: compact `12/8.dp`, standard `16/12.dp`, large `20.dp`
- [ ] List item spacing: `Arrangement.spacedBy(8.dp)` — not `24.dp` (that is for section gaps)
- [ ] Phone shows `ModalBottomSheet` for item detail where tablet shows a side panel
- [ ] Landscape on wide phones handled if the screen has a split layout (use `isWide = maxWidth >= 600.dp || maxWidth > maxHeight`)

## Checklist: Cross-Cutting

- [ ] All user-facing strings are in Bahasa Indonesia
- [ ] All prices stored and computed as `Long`, displayed via `CurrencyFormatter.formatRupiah()`
- [ ] No platform-specific code in `commonMain` — verified by checking imports
- [ ] `expect`/`actual` pairs exist in both `androidMain` and `iosMain` for any new platform abstraction

---

## Report Format

After completing all phases, write the report in this format:

```
## Code Review Report — [file or scope name]

### Summary
[2-4 sentences: overall quality, most critical findings, recommended priority]

### Findings

#### 🔴 Critical — must fix before merging
[Finding title]
- File: `path/to/File.kt`, line ~N
- Problem: [what is wrong, why it's a problem]
- Fix: [concrete code or instruction]

#### 🟡 Warning — should fix soon
[Finding title]
- File: `path/to/File.kt`
- Problem: [what is wrong]
- Fix: [concrete code or instruction]

#### 🟢 Passed
- [Category]: No issues found
- [Category]: No issues found

### Recommended Fix Order
1. [Most critical fix first — usually architectural violations]
2. [Second fix]
3. ...
```

**Severity definitions:**
- 🔴 Critical — violates a hard rule (wrong layer, exposed MutableStateFlow, platform import in commonMain, missing Koin registration, unhandled Resource variant)
- 🟡 Warning — inconsistency with project patterns, missing safety check, degraded maintainability
- 🟢 Passed — category checked, no issues found (explicitly state this, don't omit it)

---

## What Good Code Looks Like in This Codebase

If you are unsure whether something is correct, look up the canonical reference file in `.github/CONTEXT_INDEX.md` — the "Canonical Reference Files" table at the bottom maps every pattern to the authoritative source file.
