package com.leshen.letseatmobile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leshen.letseatmobile.reservationPanel.ReservationDTO
import com.leshen.letseatmobile.restaurantList.RestaurantListModel
import com.leshen.letseatmobile.restaurantList.Table
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response

class ReservationViewModel(application: Application) : AndroidViewModel(application) {

    private val _reservations = MutableLiveData<List<ReservationDTO>>()
    val reservations: LiveData<List<ReservationDTO>> get() = _reservations

    private val _restaurants = MutableLiveData<List<RestaurantListModel>>()
    val restaurants: LiveData<List<RestaurantListModel>> get() = _restaurants

    var apiService: ApiService

    init {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("HTTP_LOG", message)
        }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl("http://192.168.0.3:8010/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)

        fetchReservations()
    }

    fun fetchReservations() {
        viewModelScope.launch {
            try {
                val response: Response<List<ReservationDTO>> = apiService.getReservations()
                if (response.isSuccessful) {
                    _reservations.postValue(response.body())
                } else {
                    Log.e("HTTP_ERROR", "Failed to fetch reservations: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HTTP_ERROR", "Error fetching reservations from API", e)
            }
        }
    }

    fun fetchRestaurantList() {
        viewModelScope.launch {
            try {
                val response: Response<List<RestaurantListModel>> = apiService.getRestaurantList()
                if (response.isSuccessful) {
                    _restaurants.postValue(response.body())
                } else {
                    Log.e("HTTP_ERROR", "Failed to fetch restaurants: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HTTP_ERROR", "Error fetching restaurants from API", e)
            }
        }
    }


    fun cancelReservation(reservationId: Long, table: Table) {
        viewModelScope.launch {
            try {
                val response: Response<Void> = apiService.deleteReservation(reservationId)
                if (response.isSuccessful) {
                    fetchReservations()
                    val addTableResponse: Response<Long> = apiService.addTable(table)
                    if (addTableResponse.isSuccessful) {
                        fetchReservations()
                    } else {
                        Log.e("HTTP_ERROR", "Failed to add table: ${addTableResponse.code()}")
                    }
                } else {
                    Log.e("HTTP_ERROR", "Failed to cancel reservation: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HTTP_ERROR", "Error cancelling reservation", e)
            }
        }
    }
}
