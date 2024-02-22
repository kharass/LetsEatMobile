package com.leshen.letseatmobile.login

import android.content.Intent
import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.leshen.letseatmobile.MainActivity
import com.leshen.letseatmobile.databinding.ActivityGetStartedBinding

class GetStartedActivity : AppCompatActivity() {
    private var binding:ActivityGetStartedBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            0
        )
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.cvGetStarted?.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
        val auth = Firebase.auth
        if (auth.currentUser!= null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}