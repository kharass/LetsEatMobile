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
class RestaurantPanelActivity : AppCompatActivity() {

    private lateinit var restaurantPanelViewModel: RestaurantPanelViewModel

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
    }
    private fun showReviewDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_review_form, null)
        val alertDialog = AlertDialog.Builder(this)
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
    private fun updateUI(restaurant: RestaurantPanelModel) {

        val restaurantNameTextView: TextView = findViewById(R.id.restaurantPanelRestaurantName)
        restaurantNameTextView.text = restaurant.restaurantName
        val starTextView: TextView = findViewById(R.id.restaurantPanelRestaurantStar)
        if (restaurant.stars == 0.0) {
            starTextView.text = "brak ocen"
        } else {
            starTextView.text = restaurant.stars.toString()
        }

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
        menuTextView.text = restaurant.menu?.joinToString("\n") { it.name +" "+ it.price.toString()+" zł"} ?: "brak menu"

        val foodTextView: TextView = findViewById(R.id.restaurantPanelFood)
        if (restaurant.averageFood == 0.0) {
            foodTextView.text = "brak ocen"
        } else {
            foodTextView.text = "Jedzenie: ${restaurant.averageFood} / 5"
        }

        val atmosphereTextView: TextView = findViewById(R.id.restaurantPanelAtmosphere)
        if (restaurant.averageAtmosphere == 0.0) {
            atmosphereTextView.text = "brak ocen"
        } else {
            atmosphereTextView.text = "Atmosfera: ${restaurant.averageAtmosphere} / 5"
        }

        val serviceTextView: TextView = findViewById(R.id.restaurantPanelService)
        if (restaurant.averageService == 0.0) {
            serviceTextView.text = "brak ocen"
        } else {
            serviceTextView.text = "Obsługa: ${restaurant.averageService} / 5"
        }
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
