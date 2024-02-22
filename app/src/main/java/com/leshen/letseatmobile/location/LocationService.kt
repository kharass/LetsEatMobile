package com.leshen.letseatmobile.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.leshen.letseatmobile.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: LocationClient

    companion object {
        const val TAG = "LocationService"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_UPDATE_LOCATION = "ACTION_UPDATE_LOCATION"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        // Create a notification channel for Android Oreo and above
        val channel = NotificationChannel(
            "location",
            "Location Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Initialize locationClient
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        Log.d(TAG, "start: Service started")
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: null")
            .setSmallIcon(R.drawable.le_logo)
            .setOngoing(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationClient
            .getLocationUpdates(10000L)
            .catch { e ->
                Log.e(TAG, "Error receiving location updates: ${e.message}")
                e.printStackTrace()
            }
            .onEach { location ->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val updatedNotification = notification.setContentText(
                    "Location: ($lat, $long)"
                )
                notificationManager.notify(1, updatedNotification.build())

                // Send location update to Home fragment
                sendLocationUpdate()
            }
            .launchIn(serviceScope)

        // Set the service as a foreground service
        startForeground(1, notification.build())
    }

    private fun stop() {
        Log.d(TAG, "stop: Service stopped")
        stopForeground(true)
        stopSelf()
    }

    private fun sendLocationUpdate() {
        val intent = Intent("LOCATION_UPDATE_ACTION")
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: Service destroyed")
        super.onDestroy()
        serviceScope.cancel()
    }
}
