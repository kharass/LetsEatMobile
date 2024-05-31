package com.leshen.letseatmobile.reservationPanel

data class ReservedTable (
    val tableId: Long,
    val restaurantId: Long? = null,
    val token: String,
    val size: Int,
)