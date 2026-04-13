package id.rancak.app.di

import org.koin.core.module.Module

/**
 * Platform-specific Koin module.
 * Provides [id.rancak.app.data.sync.SyncManager] and other
 * platform-dependent singletons.
 */
expect val platformModule: Module
