package com.leshen.letseatmobile

import android.app.Application
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.leshen.letseatmobile.restaurantList.RestaurantListModel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FavouritesViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext: Context = application.applicationContext

    private val _restaurants = MutableLiveData<List<RestaurantListModel>>()

    val restaurants: LiveData<List<RestaurantListModel>> get() = _restaurants

    private val _selectedRange = MutableLiveData<Float>().apply {
        value = 1.0f
    }
    val selectedRange: LiveData<Float> get() = _selectedRange

    private val _latitude = MutableLiveData<Double>()

    val latitude: LiveData<Double> get() = _latitude

    private val _longitude = MutableLiveData<Double>()

    val longitude: LiveData<Double> get() = _longitude

    init {
        updateLatitude(0.0)
        updateLongitude(0.0)
        val sharedPreferences = appContext.getSharedPreferences("RangeSelectorPrefs", Context.MODE_PRIVATE)
        val lastSelectedRange = sharedPreferences.getFloat("lastSelectedRange", 1.0f)
        _selectedRange.value = lastSelectedRange
        fetchFavoriteRestaurants()
    }

    fun fetchFavoriteRestaurants() {
        viewModelScope.launch {
            try {
                val latitude = _latitude.value ?: 0.0
                val longitude = _longitude.value ?: 0.0
                val radius = _selectedRange.value ?: 0.0f

                val loggingInterceptor = HttpLoggingInterceptor { message ->
                    Log.d("HTTP_LOG", message)
                }
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

                val client = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()

                val apiService = Retrofit.Builder()
                    .baseUrl("http://172.19.240.156:8010/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(ApiService::class.java)

                val favoriteRestaurants = apiService.getFavoriteRestaurants(
                    latitude = latitude,
                    longitude = longitude,
                    radius = radius
                )

                Log.d("HTTP_RESPONSE_LOG", "Received data from API: $favoriteRestaurants")

                _restaurants.value = favoriteRestaurants

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    Log.e("HTTP_ERROR_404", "Resource not found (HTTP 404)", e)
                } else {
                    Log.e("HTTP_ERROR_Else", "HTTP error: ${e.code()}", e)
                }
            } catch (e: Exception) {
                Log.e("HTTP_ERROR_OTHER", "Error fetching favorite restaurants from API", e)
            }
        }
    }


    fun updateSelectedRange(range: Float) {
        _selectedRange.value = range
        appContext.getSharedPreferences("RangeSelectorPrefs", Context.MODE_PRIVATE)
            .edit()
            .putFloat("lastSelectedRange", range)
            .apply()
        fetchFavoriteRestaurants()
    }

    fun updateLatitude(latitude: Double) {
        _latitude.value = latitude
        fetchFavoriteRestaurants()
    }

    fun updateLongitude(longitude: Double) {
        _longitude.value = longitude
        fetchFavoriteRestaurants()
    }
}
