---
description: "Dependency injection (Koin) and navigation patterns for Rancak POS: module registration, ViewModel binding, type-safe routes."
applyTo: "composeApp/src/**/{di,navigation}/**/*.kt"
---

# DI & Navigation Patterns — Rancak POS

## Koin Registration (`di/AppModule.kt`)

Always use the shorthand DSL. Check `AppModule.kt` before adding — verify the class is not already registered.

```kotlin
// Repositories → repositoryModule
singleOf(::XxxRepositoryImpl) bind XxxRepository::class

// When constructor needs explicit ordering or named params:
single<XxxRepository> { XxxRepositoryImpl(get(), get()) }

// ViewModels → viewModelModule (NEVER use single — that creates a shared singleton)
viewModelOf(::XxxViewModel)
```

**Common mistakes:**

| ❌ Wrong | ✅ Correct | Why |
|---|---|---|
| `singleOf(::XxxRepositoryImpl)` | `singleOf(::XxxRepositoryImpl) bind XxxRepository::class` | Without `bind`, Koin can't resolve by interface type |
| `single { XxxViewModel(get()) }` | `viewModelOf(::XxxViewModel)` | ViewModel must NOT be a singleton — state must reset per screen |
| Registering the same class twice | Check before adding | Causes Koin `DefinitionOverrideException` at runtime |

### Module structure in `AppModule.kt`

```kotlin
val repositoryModule = module {
    singleOf(::AuthRepositoryImpl)    bind AuthRepository::class
    singleOf(::ProductRepositoryImpl) bind ProductRepository::class
    // ... all repositories here
    singleOf(::XxxRepositoryImpl)     bind XxxRepository::class  // ← add new ones here
}

val viewModelModule = module {
    viewModelOf(::AuthViewModel)
    viewModelOf(::PosViewModel)
    // ... all ViewModels here
    viewModelOf(::XxxViewModel)  // ← add new ones here
}
```

---

## Navigation (`navigation/Screen.kt` + `navigation/RancakNavHost.kt`)

Every new route requires **two registrations**: the route class in `Screen.kt` and the composable in `RancakNavHost.kt`.

### Define the route

```kotlin
// navigation/Screen.kt
@Serializable
data class XxxRoute(
    val id: String? = null      // pass arguments as data class fields, not as shared state
) : Screen()
```

### Register the composable

Pass route arguments directly to the Screen's constructor parameters.
Only add parameters that `XxxScreen` actually declares:

```kotlin
// navigation/RancakNavHost.kt
composable<XxxRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<XxxRoute>()
    XxxScreen(
        // Map route fields to Screen parameters — only what XxxScreen accepts
        onNavigateBack = { navController.popBackStack() }
        // Add route.id or other fields only if XxxScreen declares those params
    )
}
```

> **Rule:** Never pass a route argument to a Screen parameter that doesn't exist.
> If a Screen needs the `id` to load data, add `val id: String?` to the Screen signature
> AND call `viewModel.load(id)` inside `LaunchedEffect(id)`, not by navigating with state.

**Rules:**
- Route class is always `@Serializable data class` extending `Screen`
- Arguments are always in the data class fields — never passed via shared ViewModel or global state
- Back navigation always calls `navController.popBackStack()` — never `finish()` or manual back stack manipulation
- Both registrations must be added together — a route in `Screen.kt` with no entry in `RancakNavHost.kt` will crash at runtime
