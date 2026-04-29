package id.rancak.app.data.repository

import id.rancak.app.data.local.TokenManager
import id.rancak.app.data.mapper.toDomain
import id.rancak.app.data.remote.api.RancakApiService
import id.rancak.app.data.remote.api.cancelReservation
import id.rancak.app.data.remote.api.completeReservation
import id.rancak.app.data.remote.api.confirmReservation
import id.rancak.app.data.remote.api.createReservation
import id.rancak.app.data.remote.api.deleteReservation
import id.rancak.app.data.remote.api.getReservation
import id.rancak.app.data.remote.api.getReservations
import id.rancak.app.data.remote.api.seatReservation
import id.rancak.app.data.remote.api.updateReservation
import id.rancak.app.data.remote.dto.reservation.CreateReservationRequest
import id.rancak.app.data.remote.dto.reservation.UpdateReservationRequest
import id.rancak.app.domain.model.Reservation
import id.rancak.app.domain.model.ReservationInput
import id.rancak.app.domain.model.Resource
import id.rancak.app.domain.repository.ReservationRepository
import id.rancak.app.data.util.safe
import id.rancak.app.data.util.safeUnit

class ReservationRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : ReservationRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

    override suspend fun getReservations(status: String?, date: String?): Resource<List<Reservation>> = safe(
        block    = { api.getReservations(tenantUuid, status, date) },
        map      = { list -> list.map { it.toDomain() } },
        errorMsg = "Gagal memuat reservasi"
    )

    override suspend fun getReservation(reservationId: String): Resource<Reservation> = safe(
        block    = { api.getReservation(tenantUuid, reservationId) },
        map      = { it.toDomain() },
        errorMsg = "Reservasi tidak ditemukan"
    )

    override suspend fun createReservation(input: ReservationInput): Resource<Reservation> = safe(
        block    = {
            api.createReservation(
                tenantUuid,
                CreateReservationRequest(
                    customerName    = input.customerName,
                    customerPhone   = input.customerPhone,
                    partySize       = input.partySize,
                    reservedAt      = input.reservedAt,
                    durationMinutes = input.durationMinutes,
                    tableUuid       = input.tableUuid,
                    note            = input.note
                )
            )
        },
        map      = { it.toDomain() },
        errorMsg = "Gagal membuat reservasi"
    )

    override suspend fun updateReservation(
        reservationId: String,
        customerName: String?,
        customerPhone: String?,
        partySize: Int?,
        reservedAt: String?,
        durationMinutes: Int?,
        tableUuid: String?,
        note: String?
    ): Resource<Reservation> = safe(
        block    = {
            api.updateReservation(
                tenantUuid,
                reservationId,
                UpdateReservationRequest(
                    customerName, customerPhone, partySize, reservedAt, durationMinutes, tableUuid, note
                )
            )
        },
        map      = { it.toDomain() },
        errorMsg = "Gagal memperbarui reservasi"
    )

    override suspend fun deleteReservation(reservationId: String): Resource<Unit> = safeUnit(
        block    = { api.deleteReservation(tenantUuid, reservationId) },
        errorMsg = "Gagal menghapus reservasi"
    )

    override suspend fun confirmReservation(reservationId: String): Resource<Reservation> = safe(
        block    = { api.confirmReservation(tenantUuid, reservationId) },
        map      = { it.toDomain() },
        errorMsg = "Gagal konfirmasi reservasi"
    )

    override suspend fun seatReservation(reservationId: String, tableUuid: String): Resource<Reservation> = safe(
        block    = { api.seatReservation(tenantUuid, reservationId, tableUuid) },
        map      = { it.toDomain() },
        errorMsg = "Gagal seat reservasi"
    )

    override suspend fun completeReservation(reservationId: String): Resource<Reservation> = safe(
        block    = { api.completeReservation(tenantUuid, reservationId) },
        map      = { it.toDomain() },
        errorMsg = "Gagal menyelesaikan reservasi"
    )

    override suspend fun cancelReservation(reservationId: String, reason: String?): Resource<Reservation> = safe(
        block    = { api.cancelReservation(tenantUuid, reservationId, reason) },
        map      = { it.toDomain() },
        errorMsg = "Gagal membatalkan reservasi"
    )
}
