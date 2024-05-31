package com.leshen.letseatmobile.reservationPanel

data class ReservationDTO(
    val restaurantId: Long,
    val reservationId: Long? = null,
    val reservedTables: List<ReservedTable>? = null
)

