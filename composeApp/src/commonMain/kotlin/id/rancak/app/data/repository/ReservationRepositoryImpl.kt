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

class ReservationRepositoryImpl(
    private val api: RancakApiService,
    private val tokenManager: TokenManager
) : ReservationRepository {

    private val tenantUuid: String
        get() = tokenManager.tenantUuid ?: throw IllegalStateException("Tenant belum dipilih")

    override suspend fun getReservations(status: String?, date: String?): Resource<List<Reservation>> =
        try {
            val response = api.getReservations(tenantUuid, status, date)
            if (response.isSuccess && response.data != null) {
                Resource.Success(response.data.map { it.toDomain() })
            } else {
                Resource.Error(response.message ?: "Gagal memuat reservasi")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Kesalahan jaringan")
        }

    override suspend fun getReservation(reservationId: String): Resource<Reservation> =
        single({ api.getReservation(tenantUuid, reservationId) }, "Reservasi tidak ditemukan")

    override suspend fun createReservation(input: ReservationInput): Resource<Reservation> = single(
        {
            api.createReservation(
                tenantUuid,
                CreateReservationRequest(
                    customerName = input.customerName,
                    customerPhone = input.customerPhone,
                    partySize = input.partySize,
                    reservedAt = input.reservedAt,
                    durationMinutes = input.durationMinutes,
                    tableUuid = input.tableUuid,
                    note = input.note
                )
            )
        },
        "Gagal membuat reservasi"
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
    ): Resource<Reservation> = single(
        {
            api.updateReservation(
                tenantUuid,
                reservationId,
                UpdateReservationRequest(
                    customerName, customerPhone, partySize, reservedAt, durationMinutes, tableUuid, note
                )
            )
        },
        "Gagal memperbarui reservasi"
    )

    override suspend fun deleteReservation(reservationId: String): Resource<Unit> = try {
        val response = api.deleteReservation(tenantUuid, reservationId)
        if (response.isSuccess) Resource.Success(Unit)
        else Resource.Error(response.message ?: "Gagal menghapus reservasi")
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Kesalahan jaringan")
    }

    override suspend fun confirmReservation(reservationId: String): Resource<Reservation> =
        single({ api.confirmReservation(tenantUuid, reservationId) }, "Gagal konfirmasi reservasi")

    override suspend fun seatReservation(reservationId: String, tableUuid: String): Resource<Reservation> =
        single({ api.seatReservation(tenantUuid, reservationId, tableUuid) }, "Gagal seat reservasi")

    override suspend fun completeReservation(reservationId: String): Resource<Reservation> =
        single({ api.completeReservation(tenantUuid, reservationId) }, "Gagal menyelesaikan reservasi")

    override suspend fun cancelReservation(reservationId: String, reason: String?): Resource<Reservation> =
        single({ api.cancelReservation(tenantUuid, reservationId, reason) }, "Gagal membatalkan reservasi")

    private suspend fun single(
        block: suspend () -> id.rancak.app.data.remote.dto.ApiResponse<id.rancak.app.data.remote.dto.reservation.ReservationDto>,
        errorMsg: String
    ): Resource<Reservation> = try {
        val response = block()
        if (response.isSuccess && response.data != null) {
            Resource.Success(response.data.toDomain())
        } else {
            Resource.Error(response.message ?: errorMsg)
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Kesalahan jaringan")
    }
}
