package id.rancak.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform