# Rancak POS

Multi-tenant Point of Sale for Android & iOS — built with Compose Multiplatform, Kotlin, and an Offline-First architecture.

## Stack

- **UI**: Compose Multiplatform (shared Android + iOS)
- **Networking**: Ktor Client · **DI**: Koin · **Storage**: multiplatform-settings
- **Android**: WorkManager, CameraX + ML Kit, Credential Manager
- **iOS**: BGTaskScheduler, AVFoundation, CoreBluetooth
- **Backend** (reference only): Rust + Axum + PostgreSQL @ `https://api.rancak.id`

## Build

```shell
# Android
./gradlew :composeApp:assembleDebug

# iOS — open iosApp/ in Xcode and run
```

## Developer Guide

All architecture rules, API reference, offline-first patterns, product rules, and the implementation roadmap are in **[CLAUDE.md](./CLAUDE.md)**.
