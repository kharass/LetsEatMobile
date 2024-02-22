package com.leshen.letseatmobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.leshen.letseatmobile.databinding.ActivityMainBinding
import com.leshen.letseatmobile.location.LocationService

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        replaceFragment(Home())

        binding?.bottomNavigationView?.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home -> replaceFragment(Home())
                R.id.profile -> replaceFragment(Profile())
            }
            true
        }

        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_START
            startService(this)
        }
    }

    fun wyloguj(view: View) {
        viewModel.signOut(this, auth)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_wrapper, fragment)
        fragmentTransaction.commit()
    }

    override fun onDestroy() {
        Intent(applicationContext, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            startForegroundService(this)
        }
        super.onDestroy()
        binding = null
    }
    override fun onStop() {
        super.onStop()
        Log.d("MyApp", "Application stopped")
    }
}