package com.leshen.letseatmobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.leshen.letseatmobile.restaurantPanel.RestaurantPanelModel
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.leshen.letseatmobile.location.LocationService.Companion.TAG
import com.leshen.letseatmobile.reservationPanel.ReservationDTO
import com.leshen.letseatmobile.reservationPanel.ReservedTable
import com.leshen.letseatmobile.restaurantList.TablesAdapter
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RestaurantPanelActivity : AppCompatActivity() {

    private lateinit var restaurantPanelViewModel: RestaurantPanelViewModel
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_restaurant_panel)

        val addOpinionButton = findViewById<Button>(R.id.restaurantAddOpinion)
        addOpinionButton.setOnClickListener {
            showReviewDialog()
        }

        val restaurantId = intent.getIntExtra("restaurantId", -1)
        restaurantPanelViewModel = ViewModelProvider(this).get(RestaurantPanelViewModel::class.java)

        restaurantPanelViewModel.restaurantData.observe(this) { restaurant ->
            updateUI(restaurant)
        }

        restaurantPanelViewModel.errorMessage.observe(this) { errorMessage ->
            Log.d("PanelError", errorMessage)
        }

        if (restaurantPanelViewModel.restaurantData.value == null) {
            lifecycleScope.launch {
                restaurantPanelViewModel.fetchDataFromApi(restaurantId)
            }
        }

        val returnButton = findViewById<ImageButton>(R.id.restaurantPanelReturnButton)
        returnButton.setOnClickListener {
            finish()
        }

        val addReservationButton = findViewById<Button>(R.id.restaurantAddReservation)
        addReservationButton.setOnClickListener {
            showReservationDialog(restaurantId)
        }
    }

    private fun showReviewDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_review_form, null)
        alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Review")
            .setCancelable(true)
            .create()

        val submitButton = dialogView.findViewById<Button>(R.id.buttonSubmitReview)
        val commentEditText = dialogView.findViewById<EditText>(R.id.editTextComment)
        val atmosphereRatingBar = dialogView.findViewById<RatingBar>(R.id.ratingBarAtmosphere)
        val foodRatingBar = dialogView.findViewById<RatingBar>(R.id.ratingBarFood)
        val serviceRatingBar = dialogView.findViewById<RatingBar>(R.id.ratingBarService)

        submitButton.setOnClickListener {
            val comment = commentEditText.text.toString()
            val atmosphereRating = atmosphereRatingBar.rating.toInt()
            val foodRating = foodRatingBar.rating.toInt()
            val serviceRating = serviceRatingBar.rating.toInt()

            lifecycleScope.launch {
                val restaurantId = intent.getIntExtra("restaurantId", -1)
                val firebaseAuth = FirebaseAuth.getInstance()
                val currentUser = firebaseAuth.currentUser
                val token = currentUser?.uid
                if (token != null) {
                    val responseMessage = restaurantPanelViewModel.submitReview(
                        restaurantId,
                        token,
                        comment,
                        atmosphereRating,
                        foodRating,
                        serviceRating
                    )
                    restaurantPanelViewModel.fetchDataFromApi(restaurantId)
                    Toast.makeText(this@RestaurantPanelActivity, responseMessage, Toast.LENGTH_SHORT).show()
                }
                alertDialog.dismiss()
            }
        }

        alertDialog.show()
    }

    private fun showReservationDialog(restaurantId: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reservation_form, null)
        alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Add Reservation")
            .setCancelable(true)
            .create()

        val tablesRecyclerView = dialogView.findViewById<RecyclerView>(R.id.tablesRecyclerView)
        tablesRecyclerView.layoutManager = LinearLayoutManager(this)

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("HTTP_LOG", message)
        }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val apiService = Retrofit.Builder()
            .baseUrl("http://192.168.0.3:8010/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                val tables = apiService.getTablesForRestaurant(restaurantId.toLong())
                val tablesAdapter = TablesAdapter(tables) { table ->
                    removeTable(apiService, restaurantId.toLong(), table.tableId.toLong(), table.token, table.size)
                }
                tablesRecyclerView.adapter = tablesAdapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        alertDialog.show()
    }


    private fun removeTable(apiService: ApiService, restaurantId: Long, tableId: Long, token: String, size: Int) {
        var reservationId: Long?
        var reservedTableId: Long?
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Starting removeTable function")

                // Add reservation
                Log.d(TAG, "Adding reservation")
                val reservationResponse = apiService.addReservation(ReservationDTO(restaurantId))
                if (!reservationResponse.isSuccessful) {
                    showToast("Failed to create reservation")
                    Log.e(TAG, "Failed to create reservation: ${reservationResponse.code()}")
                    return@launch
                }
                reservationId = reservationResponse.body()
                Log.d(TAG, "Reservation created: $reservationId")

                // Add reserved table
                Log.d(TAG, "Adding reserved table")
                val tableResponse = apiService.addReservedTable(ReservedTable(tableId, restaurantId, token, size))
                if (!tableResponse.isSuccessful) {
                    showToast("Failed to reserve table")
                    Log.e(TAG, "Failed to reserve table: ${tableResponse.code()}")
                    return@launch
                }
                reservedTableId = tableResponse.body()
                Log.d(TAG, "Reserved table created: $reservedTableId")

//                 Assign table to reservation
                Log.d(TAG, "Assigning table to reservation")
                val assignTableResponse = apiService.addTableToReservation(reservationId!!, reservedTableId!!)
                if (!assignTableResponse.isSuccessful) {
                    showToast("Failed to assign table to reservation")
                    Log.e(TAG, "Failed to assign table to reservation: ${assignTableResponse.code()}")
                    return@launch
                }
                Log.d(TAG, "Table assigned to reservation")

                // Remove table from restaurant
                Log.d(TAG, "Removing table from restaurant")
                val removeTableResponse = apiService.removeTableFromRestaurant(restaurantId, tableId)
                alertDialog.dismiss()
                if (!removeTableResponse.isSuccessful) {
                    showToast("Failed to remove table from restaurant")
                    Log.e(TAG, "Failed to remove table from restaurant: ${removeTableResponse.code()}")
                    return@launch
                }
                Log.d(TAG, "Table removed from restaurant")

                // All operations successful
                alertDialog.dismiss()
                Toast.makeText(this@RestaurantPanelActivity, "Reservation made successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error occurred while making reservation")
                Log.e(TAG, "Error occurred while making reservation: ${e.message}")
            }
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this@RestaurantPanelActivity, message, Toast.LENGTH_SHORT).show()
    }


    private fun updateUI(restaurant: RestaurantPanelModel) {
        val restaurantNameTextView: TextView = findViewById(R.id.restaurantPanelRestaurantName)
        restaurantNameTextView.text = restaurant.restaurantName
        val starTextView: TextView = findViewById(R.id.restaurantPanelRestaurantStar)
        starTextView.text = if (restaurant.stars == 0.0) "brak ocen" else restaurant.stars.toString()

        val distanceTextView: TextView = findViewById(R.id.restaurantPanelRestaurantDistance)
        distanceTextView.text = intent.getStringExtra("distance")

        val timeTextView: TextView = findViewById(R.id.restaurantPanelRestaurantTime)
        timeTextView.text = restaurant.openingHours

        val locationTextView: TextView = findViewById(R.id.restaurantPanelLocation)
        locationTextView.text = restaurant.location
        val address = restaurant.location

        locationTextView.setOnClickListener {
            openMapWithAddress(address)
        }

        val websiteTextView: TextView = findViewById(R.id.restaurantPanelWebsite)
        websiteTextView.text = restaurant.websiteLink
        val webaddress = restaurant.websiteLink
        websiteTextView.setOnClickListener {
            openWebsite(webaddress)
        }

        val menuTextView: TextView = findViewById(R.id.restaurantPanelMenu)
        menuTextView.text = restaurant.menu?.joinToString("\n") { it.name + " " + it.price.toString() + " zł" } ?: "brak menu"

        val foodTextView: TextView = findViewById(R.id.restaurantPanelFood)
        foodTextView.text = if (restaurant.averageFood == 0.0) "brak ocen" else "Jedzenie: ${restaurant.averageFood} / 5"

        val atmosphereTextView: TextView = findViewById(R.id.restaurantPanelAtmosphere)
        atmosphereTextView.text = if (restaurant.averageAtmosphere == 0.0) "brak ocen" else "Atmosfera: ${restaurant.averageAtmosphere} / 5"

        val serviceTextView: TextView = findViewById(R.id.restaurantPanelService)
        serviceTextView.text = if (restaurant.averageService == 0.0) "brak ocen" else "Obsługa: ${restaurant.averageService} / 5"

        val opinions: TextView = findViewById(R.id.restaurantPanelOpinion)
        opinions.text = restaurant.reviews?.joinToString("\n") {
            val averageRating = ((it.food + it.atmosphere + it.service) / 3.0)
            val roundedAverage = "%.1f".format(averageRating)
            "$roundedAverage ${it.comment}"
        }

        val restaurantImageView: ImageView = findViewById(R.id.restaurantImageView)
        Glide.with(this)
            .load(restaurant.photoLink)
            .placeholder(R.drawable.template_restauracja)
            .error(R.drawable.template_restauracja)
            .centerCrop()
            .into(restaurantImageView)
    }

    private fun openMapWithAddress(address: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=$address")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(this, "Google Maps is not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openWebsite(webaddress: String) {
        if (webaddress.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webaddress))
            startActivity(intent)
        } else {
            Toast.makeText(this, "Pusty adres URL", Toast.LENGTH_SHORT).show()
        }
    }
}
