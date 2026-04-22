package id.rancak.app.domain.model

/**
 * Hirarki peran pengguna (aplikasi internal, tidak ada self-register).
 *
 * Urutan hak akses: [STAFF] ⊂ [ADMIN] ⊂ [OWNER].
 * Setiap peran memiliki [level] — peran dengan level lebih tinggi
 * otomatis memiliki semua hak peran di bawahnya.
 */
enum class UserRole(val value: String, val level: Int) {
    STAFF("staff", 1),
    ADMIN("admin", 2),
    OWNER("owner", 3);

    /** Apakah peran ini memenuhi minimum [required]? */
    fun atLeast(required: UserRole): Boolean = this.level >= required.level

    companion object {
        /**
         * Parse string peran dari backend. Mengembalikan [STAFF] sebagai
         * fallback aman bila nilai tidak dikenal — ini mencegah escalation
         * accidental ke peran yang lebih tinggi saat payload tidak valid.
         */
        fun from(value: String?): UserRole {
            val normalized = value?.trim()?.lowercase() ?: return STAFF
            return entries.firstOrNull { it.value == normalized } ?: STAFF
        }
    }
}
