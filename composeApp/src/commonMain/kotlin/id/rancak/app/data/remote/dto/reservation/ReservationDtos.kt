package id.rancak.app.data.remote.dto.reservation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReservationDto(
    val uuid: String,
    @SerialName("customer_name")    val customerName: String,
    @SerialName("customer_phone")   val customerPhone: String? = null,
    @SerialName("party_size")       val partySize: Int,
    @SerialName("reserved_at")      val reservedAt: String,
    @SerialName("duration_minutes") val durationMinutes: Int = 90,
    val status: String,                                          // pending|confirmed|seated|completed|cancelled|no_show
    @SerialName("table_uuid")       val tableUuid: String? = null,
    @SerialName("table_name")       val tableName: String? = null,
    val note: String? = null,
    @SerialName("confirmed_at")     val confirmedAt: String? = null,
    @SerialName("seated_at")        val seatedAt: String? = null,
    @SerialName("completed_at")     val completedAt: String? = null,
    @SerialName("cancelled_at")     val cancelledAt: String? = null,
    @SerialName("cancel_reason")    val cancelReason: String? = null,
    @SerialName("created_by")       val createdBy: String? = null,
    @SerialName("created_at")       val createdAt: String
)

@Serializable
data class CreateReservationRequest(
    @SerialName("customer_name")    val customerName: String,
    @SerialName("customer_phone")   val customerPhone: String? = null,
    @SerialName("party_size")       val partySize: Int,
    @SerialName("reserved_at")      val reservedAt: String,
    @SerialName("duration_minutes") val durationMinutes: Int = 90,
    @SerialName("table_uuid")       val tableUuid: String? = null,
    val note: String? = null
)

@Serializable
data class UpdateReservationRequest(
    @SerialName("customer_name")    val customerName: String? = null,
    @SerialName("customer_phone")   val customerPhone: String? = null,
    @SerialName("party_size")       val partySize: Int? = null,
    @SerialName("reserved_at")      val reservedAt: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("table_uuid")       val tableUuid: String? = null,
    val note: String? = null
)

@Serializable
data class SeatReservationRequest(
    @SerialName("table_uuid") val tableUuid: String
)

@Serializable
data class CancelReservationRequest(
    val reason: String? = null
)
