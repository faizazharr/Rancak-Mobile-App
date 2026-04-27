package id.rancak.app.domain.model

/** Reservasi meja restoran — pending → confirmed → seated → completed. */
data class Reservation(
    val uuid: String,
    val customerName: String,
    val customerPhone: String? = null,
    val partySize: Int,
    /** ISO-8601 datetime. */
    val reservedAt: String,
    val durationMinutes: Int = 90,
    /** "pending" | "confirmed" | "seated" | "completed" | "cancelled" | "no_show" */
    val status: String,
    val tableUuid: String? = null,
    val tableName: String? = null,
    val note: String? = null,
    val confirmedAt: String? = null,
    val seatedAt: String? = null,
    val completedAt: String? = null,
    val cancelledAt: String? = null,
    val cancelReason: String? = null,
    val createdBy: String? = null,
    val createdAt: String
)

data class ReservationInput(
    val customerName: String,
    val customerPhone: String? = null,
    val partySize: Int,
    val reservedAt: String,
    val durationMinutes: Int = 90,
    val tableUuid: String? = null,
    val note: String? = null
)
