package com.leshen.letseatmobile.restaurantList

data class RestaurantListModel(
    val latitude: Double,
    val longitude: Double,
    val openingHours: String,
    val photoLink: String,
    val restaurantCategory: String,
    val restaurantId: Int,
    val restaurantName: String,
    val distance:String,
    val stars: Double,
    val tables: List<Table>
)