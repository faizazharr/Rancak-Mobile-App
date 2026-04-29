package id.rancak.app.data.remote.api

import id.rancak.app.data.remote.dto.ApiResponse
import id.rancak.app.data.remote.dto.reservation.CancelReservationRequest
import id.rancak.app.data.remote.dto.reservation.CreateReservationRequest
import id.rancak.app.data.remote.dto.reservation.ReservationDto
import id.rancak.app.data.remote.dto.reservation.SeatReservationRequest
import id.rancak.app.data.remote.dto.reservation.UpdateReservationRequest
import id.rancak.app.data.remote.safeBody
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Reservation lifecycle — pending → confirmed → seated → completed (atau cancelled).
 */

private fun reservationsUrl(tenantUuid: String) =
    ApiConstants.BASE_URL + ApiConstants.tenantPath(tenantUuid) + "/reservations"

suspend fun RancakApiService.getReservations(
    tenantUuid: String,
    status: String? = null,
    date: String? = null
): ApiResponse<List<ReservationDto>> =
    client.get(reservationsUrl(tenantUuid)) {
        status?.let { parameter("status", it) }
        date?.let { parameter("date", it) }
    }.safeBody()

suspend fun RancakApiService.getReservation(
    tenantUuid: String,
    reservationId: String
): ApiResponse<ReservationDto> =
    client.get(reservationsUrl(tenantUuid) + "/$reservationId").safeBody()

suspend fun RancakApiService.createReservation(
    tenantUuid: String,
    request: CreateReservationRequest
): ApiResponse<ReservationDto> =
    client.post(reservationsUrl(tenantUuid)) {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.safeBody()

suspend fun RancakApiService.updateReservation(
    tenantUuid: String,
    reservationId: String,
    request: UpdateReservationRequest
): ApiResponse<ReservationDto> =
    client.patch(reservationsUrl(tenantUuid) + "/$reservationId") {
        contentType(ContentType.Application.Json)
        setBody(request)
    }.safeBody()

suspend fun RancakApiService.deleteReservation(
    tenantUuid: String,
    reservationId: String
): ApiResponse<Unit> =
    client.delete(reservationsUrl(tenantUuid) + "/$reservationId").safeBody()

suspend fun RancakApiService.confirmReservation(
    tenantUuid: String,
    reservationId: String
): ApiResponse<ReservationDto> =
    client.post(reservationsUrl(tenantUuid) + "/$reservationId/confirm").safeBody()

suspend fun RancakApiService.seatReservation(
    tenantUuid: String,
    reservationId: String,
    tableUuid: String
): ApiResponse<ReservationDto> =
    client.post(reservationsUrl(tenantUuid) + "/$reservationId/seat") {
        contentType(ContentType.Application.Json)
        setBody(SeatReservationRequest(tableUuid))
    }.safeBody()

suspend fun RancakApiService.completeReservation(
    tenantUuid: String,
    reservationId: String
): ApiResponse<ReservationDto> =
    client.post(reservationsUrl(tenantUuid) + "/$reservationId/complete").safeBody()

suspend fun RancakApiService.cancelReservation(
    tenantUuid: String,
    reservationId: String,
    reason: String? = null
): ApiResponse<ReservationDto> =
    client.post(reservationsUrl(tenantUuid) + "/$reservationId/cancel") {
        contentType(ContentType.Application.Json)
        setBody(CancelReservationRequest(reason))
    }.safeBody()
