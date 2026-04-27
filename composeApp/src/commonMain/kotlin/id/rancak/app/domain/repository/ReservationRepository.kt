package id.rancak.app.domain.repository

import id.rancak.app.domain.model.Reservation
import id.rancak.app.domain.model.ReservationInput
import id.rancak.app.domain.model.Resource

/**
 * Manajemen reservasi meja — pending → confirmed → seated → completed
 * (atau cancelled / no_show).
 */
interface ReservationRepository {
    suspend fun getReservations(status: String? = null, date: String? = null): Resource<List<Reservation>>
    suspend fun getReservation(reservationId: String): Resource<Reservation>
    suspend fun createReservation(input: ReservationInput): Resource<Reservation>
    suspend fun updateReservation(
        reservationId: String,
        customerName: String? = null,
        customerPhone: String? = null,
        partySize: Int? = null,
        reservedAt: String? = null,
        durationMinutes: Int? = null,
        tableUuid: String? = null,
        note: String? = null
    ): Resource<Reservation>
    suspend fun deleteReservation(reservationId: String): Resource<Unit>

    /** Konfirmasi reservasi (status pending → confirmed). */
    suspend fun confirmReservation(reservationId: String): Resource<Reservation>
    /** Tandai tamu sudah hadir & duduk di meja. Otomatis set table jadi reserved/occupied. */
    suspend fun seatReservation(reservationId: String, tableUuid: String): Resource<Reservation>
    suspend fun completeReservation(reservationId: String): Resource<Reservation>
    suspend fun cancelReservation(reservationId: String, reason: String? = null): Resource<Reservation>
}
