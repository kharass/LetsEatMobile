package com.leshen.letseatmobile.restaurantPanel

data class Menu(
    val menuId: Int,
    val name: String,
    val price: Double,
    val restaurantId: Any,
    val token: String
)