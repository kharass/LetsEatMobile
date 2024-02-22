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
import com.leshen.letseatmobile.databinding.FragmentHomeBinding
import com.leshen.letseatmobile.restaurantList.RestaurantListAdapter
import com.leshen.letseatmobile.restaurantList.RestaurantListModel
import java.util.Locale

class Home : Fragment() {

    private lateinit var filterLayoutHome: LinearLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantListAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var locationButton: Button
    private var selectedCategory: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val viewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationUpdateReceiver: BroadcastReceiver

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Create an instance of your item click listener
        val itemClickListener = object : RestaurantListAdapter.OnItemClickListener {
            override fun onItemClick(restaurantModel: RestaurantListModel) {
                // Use an explicit intent to start RestaurantPanelActivity
                val intent = Intent(requireContext(), RestaurantPanelActivity::class.java)

                // Pass any necessary data to the activity
                intent.putExtra("restaurantId", restaurantModel.restaurantId)
                intent.putExtra("distance", restaurantModel.distance)
                Log.d("distance", restaurantModel.distance)
                startActivity(intent)
            }

            override fun onFavoriteButtonClick(restaurantId: Int) {
                // Handle favorite button click
            }
        }

        // Pass the item click listener to your adapter
        adapter = RestaurantListAdapter(emptyList(), emptyList(), itemClickListener)
        recyclerView.adapter = adapter

        filterLayoutHome = binding.filterLayoutHome
        swipeRefreshLayout = binding.swipeRefreshLayout
        locationButton = binding.locationButton

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchDataFromApi()
            updateLocationButton()
        }

        locationButton.setOnClickListener {
            checkLocationPermission()
            showRangeSelectorDialog()
        }

        // Initialize the LocationUpdateReceiver
        locationUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "LOCATION_UPDATE_ACTION") {
                    onLocationUpdate()
                }
            }
        }

        // Register the receiver to listen for location updates
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
        filterLayoutHome.removeAllViews()

        for (category in categories) {
            val button = Button(requireContext())
            button.text = category
            button.setOnClickListener {
                toggleCategoryButton(category)
            }
            filterLayoutHome.addView(button)
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
        viewModel.fetchDataFromApi()
        updateLocationButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(locationUpdateReceiver)
        _binding = null
    }
}
