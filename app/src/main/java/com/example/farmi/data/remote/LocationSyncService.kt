package com.example.farmi.data.remote

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * Coordinates device UUID registration and location synchronization on initial install.
 * Ported from iOS LocationSyncService.
 */
class LocationSyncService private constructor(private val context: Context) {

    companion object {
        private const val TAG = "LocationSync"
        private const val PREFS_NAME = "farmi_prefs"
        private const val KEY_UUID = "user_device_uuid"
        private const val KEY_SYNCED = "is_installation_synced"

        @Volatile
        private var INSTANCE: LocationSyncService? = null

        fun getInstance(context: Context): LocationSyncService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocationSyncService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun startSyncFlowIfNeeded() {
        val uuid = getOrCreateDeviceUuid()
        if (isSynced()) {
            Log.d(TAG, "Installation already synced for UUID: $uuid")
            return
        }

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            Log.d(TAG, "Location permission granted. Requesting location...")
            requestLocationUpdate()
        } else {
            Log.d(TAG, "Location permission not granted. Syncing with fallback (no GPS details).")
            syncWithFallback()
        }
    }

    private fun getOrCreateDeviceUuid(): String {
        var uuid = sharedPrefs.getString(KEY_UUID, null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            sharedPrefs.edit().putString(KEY_UUID, uuid).apply()
            Log.d(TAG, "Generated new installation UUID: $uuid")
        }
        return uuid
    }

    private fun isSynced(): Boolean {
        return sharedPrefs.getBoolean(KEY_SYNCED, false)
    }

    private fun setSynced(synced: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_SYNCED, synced).apply()
    }

    private fun requestLocationUpdate() {
        try {
            // Check both network and GPS providers
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            if (isNetworkEnabled) {
                locationManager.requestSingleUpdate(
                    LocationManager.NETWORK_PROVIDER,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            onLocationRetrieved(location)
                        }
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    },
                    context.mainLooper
                )
            } else if (isGpsEnabled) {
                locationManager.requestSingleUpdate(
                    LocationManager.GPS_PROVIDER,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            onLocationRetrieved(location)
                        }
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    },
                    context.mainLooper
                )
            } else {
                Log.w(TAG, "No location providers enabled. Falling back.")
                syncWithFallback()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException requesting location updates: ${e.message}")
            syncWithFallback()
        } catch (e: Exception) {
            Log.e(TAG, "Exception requesting location updates: ${e.message}")
            syncWithFallback()
        }
    }

    private fun onLocationRetrieved(location: Location) {
        Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}")
        CoroutineScope(Dispatchers.IO).launch {
            var city: String? = null
            var country: String? = null
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    city = addresses[0].locality
                    country = addresses[0].countryName
                }
            } catch (e: Exception) {
                Log.e(TAG, "Geocoder reverse geocode failed: ${e.message}")
            }

            sendRegistrationToBackend(
                latitude = location.latitude,
                longitude = location.longitude,
                city = city,
                country = country
            )
        }
    }

    private fun syncWithFallback() {
        CoroutineScope(Dispatchers.IO).launch {
            sendRegistrationToBackend(
                latitude = null,
                longitude = null,
                city = null,
                country = null
            )
        }
    }

    private suspend fun sendRegistrationToBackend(
        latitude: Double?,
        longitude: Double?,
        city: String?,
        country: String?
    ) {
        val uuid = getOrCreateDeviceUuid()
        
        // 10.0.2.2 is how the Android emulator accesses localhost of the host machine
        val apiBaseURL = "http://10.0.2.2:8080/api"
        val registerURL = "$apiBaseURL/register"

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val timestamp = sdf.format(Date())

        val payload = JSONObject().apply {
            put("deviceUuid", uuid)
            put("latitude", latitude ?: JSONObject.NULL)
            put("longitude", longitude ?: JSONObject.NULL)
            put("city", city ?: JSONObject.NULL)
            put("country", country ?: JSONObject.NULL)
            put("timestamp", timestamp)
        }

        withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(registerURL)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                Log.d(TAG, "Sending registration to $registerURL... Payload: $payload")

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(payload.toString())
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    Log.d(TAG, "Registration successful. Status: $responseCode")
                    setSynced(true)
                } else {
                    Log.e(TAG, "Registration failed with status: $responseCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error during registration: ${e.message}")
            } finally {
                connection?.disconnect()
            }
        }
    }
}
