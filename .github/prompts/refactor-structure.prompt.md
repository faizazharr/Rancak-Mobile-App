# Refactor Structure — Rancak POS

Use this prompt when you want the agent to think at an architectural level:
audit the codebase, identify structural problems, and propose or execute refactors
at any scale — from a single class to an entire layer.

---

## Context This Prompt Depends On

`copilot-instructions.md` is **always pre-loaded** by Copilot — you already have access to:
- Clean Architecture layer rules (what belongs in domain, data, presentation)
- All three ViewModel patterns and when each is correct
- Non-negotiable rules (no platform imports in `commonMain`, `Resource<T>` return types, etc.)
- Koin registration patterns and the Canonical Reference Files table

**Do not re-read `copilot-instructions.md`.**

Structural refactors frequently span multiple layers. Before starting Phase 1, identify
which layers are in scope, then read the prompt files that cover those layers:

| Scope of refactor | Also read |
|---|---|
| Data layer (repository, DTO, mapper, API extension) | `.github/prompts/integrate-api.prompt.md` — full data-layer contract |
| Presentation layer (ViewModel, Screen, adaptive layout) | `.github/prompts/adaptive-layout.prompt.md` — layout rules that constrain structure |
| Full-stack refactor (all layers) | Read both files above before Phase 1 |
| Evaluating result quality | `.github/prompts/code-review.prompt.md` — run the checklist after refactoring |
| Topic-specific lookup (e.g., "what files reference safe()") | `.github/CONTEXT_INDEX.md` |

> **Rule:** When a refactor touches the data layer AND the presentation layer,
> read `integrate-api.prompt.md` AND `adaptive-layout.prompt.md` before writing a single line.
> You will miss constraints if you only read one of them.

---

## How to Use

Describe the scope you want examined, for example:

> "Audit the ViewModel layer — are there any that are too large, violate Clean Architecture,
> or handle concerns they shouldn't?"

> "I want to add offline support to the Inventory feature. What structural changes are needed
> across all layers?"

> "The ProductRepository is getting too big. How should we split it?"

> "Review the entire data layer for inconsistencies in how we handle errors."

The agent must **think before touching any code**. Follow the reasoning steps below.

---

## Reasoning Protocol — Follow Every Time

### Phase 1: Read and Map

Before forming any opinion, read the relevant files. Build a mental map:

- What does each class actually do (not just what its name implies)?
- Where are the boundaries between layers being respected or violated?
- Which classes are changing together frequently? (Symptom of incorrect coupling.)
- Which patterns appear multiple times with slight variations? (Symptom of missing abstraction.)

**Do not proceed to Phase 2 until you can describe the current state accurately.**

### Phase 2: Identify and Classify Problems

For each problem found, classify it:

| Type | Description | Example in this codebase |
|---|---|---|
| **Layer violation** | Logic placed in the wrong layer | API call inside a ViewModel; business rule inside a Screen |
| **Missing abstraction** | The same logic duplicated across 2+ files | Identical error mapping in 3 repositories |
| **Inconsistency** | Same concept implemented differently with no reason | Some ViewModels use `UiState`, others use separate `StateFlow` fields |
| **Coupling** | Class knows too much about another | Repository importing a ViewModel type |
| **Incomplete pattern** | Pattern partially applied | Screen that is not split into Screen + Content |
| **Scope creep** | Class doing more than one job | ViewModel that also manages local DB writes AND triggers sync |
| **Stale code** | Dead code, unused fields, redundant branches | Commented-out API calls, `TODO` blocks from months ago |

For each problem: name the file, the specific location, the classification, and why it's a problem — not just that it looks wrong.

### Phase 3: Propose the Full Change Plan

Write a structured plan **before touching any file**:

```
## Refactor Plan: [short name]

### Problem summary
[1-3 sentences describing the root cause, not symptoms]

### Files affected
- `path/to/FileA.kt` — [what changes]
- `path/to/FileB.kt` — [what changes]
- `di/AppModule.kt`  — [any registration changes]

### Change sequence (order matters for compilation)
1. [First change — usually domain layer]
2. [Second change — data layer]
3. [Third change — DI]
4. [Fourth change — presentation]

### Ripple effects
- [Any other files that import or depend on the changed classes]
- [Navigation changes if routes are affected]
- [Koin module changes if new classes are introduced]

### Risk level
[ ] Low — isolated change, no API surface changes, no navigation changes
[ ] Medium — multiple files, some shared types change
[ ] High — public interface changes, affects multiple features, cross-layer impact

### Rollback strategy
[What to revert if this breaks something]
```

**Do not write any code until the plan is approved or you have confirmed it is safe.**

### Phase 4: Execute in Dependency Order

Always make changes in this sequence to avoid compilation errors mid-refactor:

```
1. domain/model/        — pure data classes, no dependencies
2. domain/repository/   — interfaces only
3. data/mapper/         — extension functions
4. data/remote/dto/     — DTOs
5. data/remote/api/     — API service extensions
6. data/repository/     — implementations
7. di/AppModule.kt      — Koin registration
8. presentation/viewmodel/  — ViewModels
9. presentation/ui/         — Screens
10. navigation/             — Screen routes and NavHost
```

For each file change: state what you're changing and why before writing the new code.

### Phase 5: Verify

After all changes are made:

1. **Check compilation order** — would these files compile in sequence without circular imports?
2. **Check Koin graph** — is every new class registered? Is anything registered twice?
3. **Check navigation** — is every new route in both `Screen.kt` and `RancakNavHost.kt`?
4. **Run the pre-submission checklist** (see bottom of this file)
5. **Read the changed files back** — does the final state match your stated plan?

---

## Common Refactor Patterns for This Codebase

### Splitting an oversized ViewModel

**Signal**: ViewModel has more than ~200 lines, or handles unrelated concerns.

```
Before: ProductViewModel handles product list + cart management + price calculation
After:
  - ProductViewModel       → list, search, 86 toggle
  - CartViewModel          → add/remove/update items (already exists — check first)
  - PricingViewModel       → pricing rules, discounts
```

Rule: One ViewModel per user-facing workflow, not per data entity.

### Extracting a missing abstraction from repositories

**Signal**: The same `try/catch + map` block appears in 3+ repositories.

```kotlin
// Before — repeated in every RepositoryImpl:
try {
    val response = api.getX(tenantId)
    if (response.isSuccess) Resource.Success(response.data!!.toDomain())
    else Resource.Error(response.message ?: "Error")
} catch (e: Exception) { Resource.Error(e.message ?: "Network error") }

// After — already abstracted in RepositoryHelpers.kt:
safe(block = { api.getX(tenantId) }, map = { it.toDomain() }, errorMsg = "Gagal memuat data")
```

If a repository is not using `safe`/`safeList`/`safeUnit`, migrate it.

### Fixing a Layer Violation (business logic in UI)

**Signal**: A Screen or ViewModel contains `if` logic that belongs in the domain layer.

```kotlin
// Wrong — business rule in ViewModel
fun canPay(): Boolean = cartItems.isNotEmpty() && cartItems.sumOf { it.price } > 0

// Right — business rule belongs in a domain model method or a use case
// Domain model:
data class Cart(val items: List<CartItem>) {
    fun isPayable(): Boolean = items.isNotEmpty() && totalAmount > 0
    val totalAmount: Long get() = items.sumOf { it.price * it.quantity }
}
```

### Extracting a shared composable from duplicated UI code

**Signal**: The same composable block appears in 3+ Screen files.

```
Action: Extract to presentation/components/XxxComponent.kt
Rule: Only extract when the component has stable props and is used 3+ times.
      Do not over-extract single-use components.
```

### Fixing an incomplete Screen split

**Signal**: A Screen composable directly reads from a ViewModel inside the Content composable,
or the Content composable takes a ViewModel as a parameter.

```kotlin
// Wrong
@Composable
fun ProductContent(viewModel: ProductViewModel) { // ← ViewModel in Content
    val state by viewModel.uiState.collectAsState()
    // ...
}

// Right
@Composable
fun ProductContent(
    uiState: ProductUiState,    // plain data
    onAddToCart: (Product) -> Unit  // plain lambda
) { /* ... */ }
```

### Normalising inconsistent error handling

**Signal**: Some screens swallow errors silently, others crash, others show different UI patterns.

Standard pattern:
```kotlin
// In ViewModel: expose errors through UiState.error
is Resource.Error -> _uiState.update { it.copy(error = result.message, isLoading = false) }

// In Screen: clear error after showing it
uiState.error?.let {
    LaunchedEffect(it) {
        snackbarHostState.showSnackbar(it)
        viewModel.clearError()
    }
}
```

---

## When to Refactor vs. When to Leave Alone

**Refactor:**
- The code violates a hard rule (wrong layer, exposed MutableStateFlow, hardcoded values)
- The code is being actively worked on in this session
- The problem causes real bugs or makes future work harder

**Leave alone (note it, don't touch it):**
- The code is old and works correctly — touching it is pure risk with no immediate benefit
- The "inconsistency" is intentional (e.g., a screen that is simpler and doesn't need a full split)
- The fix would require changing 10+ files for cosmetic reasons

When you decide to leave something alone, say so explicitly and explain why.

---

## Pre-Submission Checklist

- [ ] No `android.*` or Apple imports in `commonMain`
- [ ] All repository methods return `Resource<T>`
- [ ] ViewModels expose `StateFlow`, not `MutableStateFlow`
- [ ] All four `Resource` variants handled in every `when` block
- [ ] All screens split into `XxxScreen` (has VM) + `XxxContent` (pure UI)
- [ ] Every new class registered in `AppModule.kt`
- [ ] Every new route registered in `Screen.kt` and `RancakNavHost.kt`
- [ ] All prices use `Long`, displayed via `formatRupiah()`
- [ ] All user-facing error messages in Bahasa Indonesia
- [ ] Role gating applied where required
- [ ] No hardcoded colors, spacing, or typography
- [ ] Refactor plan was stated before code was written
- [ ] Files were changed in dependency order
- [ ] Changed files were read back to confirm they match the plan
