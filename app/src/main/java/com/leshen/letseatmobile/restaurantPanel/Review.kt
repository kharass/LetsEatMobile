package com.leshen.letseatmobile.restaurantPanel

data class Review(
    val atmosphere: Int,
    val comment: String,
    val date: Any,
    val food: Int,
    val restaurantId: Any,
    val reviewId: Int,
    val service: Int,
    val token: String
)