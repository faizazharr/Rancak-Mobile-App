package id.rancak.app.data.mapper

import id.rancak.app.data.remote.dto.reservation.ReservationDto
import id.rancak.app.domain.model.Reservation

fun ReservationDto.toDomain(): Reservation = Reservation(
    uuid = uuid,
    customerName = customerName,
    customerPhone = customerPhone,
    partySize = partySize,
    reservedAt = reservedAt,
    durationMinutes = durationMinutes,
    status = status,
    tableUuid = tableUuid,
    tableName = tableName,
    note = note,
    confirmedAt = confirmedAt,
    seatedAt = seatedAt,
    completedAt = completedAt,
    cancelledAt = cancelledAt,
    cancelReason = cancelReason,
    createdBy = createdBy,
    createdAt = createdAt
)
