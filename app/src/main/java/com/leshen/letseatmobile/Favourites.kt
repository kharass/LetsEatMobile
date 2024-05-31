package com.leshen.letseatmobile

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.leshen.letseatmobile.databinding.FragmentFavouritesBinding
import com.leshen.letseatmobile.restaurantList.RestaurantListModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class Favourites : Fragment() {

    private lateinit var filterLayoutFavourites: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantListAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var locationButton: Button
    private var selectedCategory: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: FavouritesViewModel by viewModels()

    private var _binding: FragmentFavouritesBinding? = null

    private val binding get() = _binding!!

    private lateinit var locationUpdateReceiver: BroadcastReceiver

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        val view = binding.root
        recyclerView = binding.favouritesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val itemClickListener = object : RestaurantListAdapter.OnItemClickListener {
            override fun onItemClick(restaurantModel: RestaurantListModel) {
                val intent = Intent(requireContext(), RestaurantPanelActivity::class.java)
                intent.putExtra("restaurantId", restaurantModel.restaurantId)
                intent.putExtra("distance", restaurantModel.distance)
                Log.d("distance", restaurantModel.distance)
                startActivity(intent)
            }


            override fun onFavoriteButtonClick(restaurantId: Int, isFavorite: Boolean) {
                if (!isFavorite) {
                    removeFromFavorites(restaurantId)
                } else {
                    addToFavorites(restaurantId)
                }
            }

        }

        adapter = RestaurantListAdapter(emptyList(), emptyList(), requireContext(), itemClickListener)
        recyclerView.adapter = adapter

        filterLayoutFavourites = binding.filterLayoutFavourites
        swipeRefreshLayout = binding.swipeRefreshLayoutFavourites
        locationButton = binding.locationButtonFavourites

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchFavoriteRestaurants()
            updateLocationButton()
        }

        locationButton.setOnClickListener {
            checkLocationPermission()
            showRangeSelectorDialog()
        }

        locationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "LOCATION_UPDATE_ACTION") {
                    onLocationUpdate()
                }
            }
        }

        val filter = IntentFilter("LOCATION_UPDATE_ACTION")
        requireContext().registerReceiver(locationUpdateReceiver, filter)

        observeViewModel()

        return view
    }

    private fun observeViewModel() {
        viewModel.restaurants.observe(viewLifecycleOwner) { restaurants ->
            adapter.updateData(restaurants)
            swipeRefreshLayout.isRefreshing = false

            val uniqueCategories = restaurants.map { it.restaurantCategory }.distinct()
            generateCategoryButtons(uniqueCategories)
        }

        viewModel.selectedRange.observe(viewLifecycleOwner) { range ->
            updateLocationButton()
            Toast.makeText(requireContext(), "Selected range: $range", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterByCategory(category: String) {
        adapter.filterByCategory(category)
    }

    private fun generateCategoryButtons(categories: List<String>) {
        filterLayoutFavourites.removeAllViews()

        for (category in categories) {
            val button = Button(requireContext())
            button.text = category
            button.setOnClickListener {
                toggleCategoryButton(category)
            }
            filterLayoutFavourites.addView(button)
        }
    }

    private fun toggleCategoryButton(category: String) {
        if (category == selectedCategory) {
            selectedCategory = null
            adapter.resetFilters()
        } else {
            selectedCategory = category
            filterByCategory(category)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            updateLocationButton()
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationButton() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = Pair(location.latitude, location.longitude)
                val address = getAddressFromLocation(latLng)

                val buttonText = "$address\nw  w promieniu ${viewModel.selectedRange.value} km"
                locationButton.text = buttonText
                viewModel.updateLatitude(location.latitude)
                viewModel.updateLongitude(location.longitude)
                Log.d("Location", "Updated location: $address")
            } else {
                Log.e("Location", "Last location is null")
            }
        }
    }

    private fun getAddressFromLocation(latLng: Pair<Double, Double>): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(latLng.first, latLng.second, 1)

        val address = addresses?.getOrNull(0)
        return address?.thoroughfare ?: "Street name not found"
    }

    private fun showRangeSelectorDialog() {
        val rangeSelectorDialog = RangeSelectorDialogFragment()
        rangeSelectorDialog.setRangeSelectorListener(object : RangeSelectorDialogFragment.RangeSelectorListener {
            override fun onRangeSelected(range: Float) {
                viewModel.updateSelectedRange(range)
            }
        })
        rangeSelectorDialog.show(parentFragmentManager, "RangeSelectorDialog")
    }

    private fun onLocationUpdate() {
        viewModel.fetchFavoriteRestaurants()
        updateLocationButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(locationUpdateReceiver)
        _binding = null
    }

    fun addToFavorites(restaurantId: Int, dispatcher: CoroutineDispatcher = Dispatchers.Main) {
        GlobalScope.launch(dispatcher) {
            try {
                val response = apiService.addToFavorites(restaurantId.toLong())
                if (response.isSuccessful) {
                    showToast("Restaurant added to favorites")
                } else {
                    showToast("Failed to add restaurant to favorites")
                }
            } catch (e: Exception) {
                showToast("Network error occurred")
            }
        }
    }


    fun removeFromFavorites(restaurantId: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = apiService.removeFromFavorites(restaurantId.toLong())
                if (response.isSuccessful) {
                    showToast("Restaurant removed from favorites")
                } else {
                    showToast("Failed to remove restaurant from favorites")
                }
            } catch (e: Exception) {
                showToast("Network error occurred")
            }
        }
    }

    var showToastCalled: Boolean = false
    var showToastMessage: String? = null

    fun showToast(message: String) {
        showToastCalled = true
        showToastMessage = message
        if (isAdded && context != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    val apiService = Retrofit.Builder()
        .baseUrl("http://192.168.0.3:8010/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}
