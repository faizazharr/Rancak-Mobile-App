package id.rancak.app.data.remote

import platform.Foundation.NSBundle

actual val rancakApiKey: String =
    NSBundle.mainBundle.objectForInfoDictionaryKey("RancakApiKey") as? String
        ?: error("RancakApiKey missing from Info.plist")
