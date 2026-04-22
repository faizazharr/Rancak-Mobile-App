package id.rancak.app.data.remote

/**
 * Platform-specific API key accessor.
 * - Android → injected at build time from local.properties via BuildConfig
 * - iOS     → read from Info.plist (key: RancakApiKey)
 */
expect val rancakApiKey: String
