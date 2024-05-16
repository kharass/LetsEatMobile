package com.leshen.letseatmobile

import com.leshen.letseatmobile.restaurantList.RestaurantListModel
import com.leshen.letseatmobile.restaurantList.Table
import com.leshen.letseatmobile.restaurantPanel.RestaurantPanelModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/api/restaurants/search")
    suspend fun getRestaurants(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Float
    ): List<RestaurantListModel>

    @GET("/api/restaurants/panel/{id}")
    suspend fun getRestaurantPanelData(@Path("id") restaurantId: Int): RestaurantPanelModel

    @POST("/api/reviews")
    suspend fun submitReview(@Body requestBody: okhttp3.RequestBody): Response<Any>

    @GET("/api/restaurants/favorite/search")
    suspend fun getFavoriteRestaurants(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Float
    ): List<RestaurantListModel>

    @POST("/api/restaurants/{restaurantId}/favorite")
    suspend fun addToFavorites(@Path("restaurantId") restaurantId: Long): Response<Unit>

    @DELETE("/api/restaurants/{restaurantId}/favorite")
    suspend fun removeFromFavorites(@Path("restaurantId") restaurantId: Long): Response<Unit>

    @GET("/api/tables/restaurant/{restaurantId}")
    suspend fun getTablesForRestaurant(@Path("restaurantId") restaurantId: Long): List<Table>

    @DELETE("/api/tables/{restaurantId}/{tableId}")
    suspend fun removeTableFromRestaurant(
        @Path("restaurantId") restaurantId: Long,
        @Path("tableId") tableId: Long
    ): Response<Unit>
}