package com.leshen.letseatmobile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leshen.letseatmobile.restaurantList.RestaurantListModel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeViewModel : ViewModel() {

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
        _selectedRange.value = 1.0f
        fetchDataFromApi()
    }

    fun fetchDataFromApi() {
        viewModelScope.launch {
            try {
                val latitude = _latitude.value ?: 0.0
                val longitude = _longitude.value ?: 0.0
                val radius = _selectedRange.value ?: 0.0f

                // Dodaj interceptor do logowania URL-ów zapytań
                val loggingInterceptor = HttpLoggingInterceptor { message ->
                    Log.d("HTTP_LOG", message)
                }
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

                val client = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()

                val apiService = Retrofit.Builder()
                    .baseUrl("http://192.168.0.2:8010/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(ApiService::class.java)

                val restaurants = apiService.getRestaurants(
                    latitude = latitude,
                    longitude = longitude,
                    radius = radius
                )

                Log.d("HTTP_RESPONSE_LOG", "Received data from API: $restaurants")

                _restaurants.value = restaurants

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    Log.e("HTTP_ERROR_404", "Resource not found (HTTP 404)", e)
                } else {
                    Log.e("HTTP_ERROR_Else", "HTTP error: ${e.code()}", e)
                }
            } catch (e: Exception) {
                Log.e("HTTP_ERROR_OTHER", "Error fetching data from API", e)
            }
        }
    }

    fun updateSelectedRange(range: Float) {
        _selectedRange.value = range
        fetchDataFromApi()
    }

    fun updateLatitude(latitude: Double) {
        _latitude.value = latitude
        fetchDataFromApi()
    }

    fun updateLongitude(longitude: Double) {
        _longitude.value = longitude
        fetchDataFromApi()
    }
}