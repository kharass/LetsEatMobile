package com.leshen.letseatmobile.restaurantPanel

data class RestaurantPanelModel(
    val averageAtmosphere: Double,
    val averageFood: Double,
    val averageService: Double,
    val latitude: Double,
    val location: String,
    val longitude: Double,
    val menu: List<Menu>,
    val openingHours: String,
    val phoneNumber: String,
    val photoLink: String,
    val restaurantId: Int,
    val restaurantName: String,
    val reviews: List<Review>,
    val stars: Double,
    val websiteLink: String
)