package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

data class GeoLocationResult(
  val city: String,
  val country: String,
  val state: String = "",
  val latitude: Double,
  val longitude: Double,
  val isSuccess: Boolean = true,
  val errorMessage: String? = null
)

object LocationHelper {
  private const val TAG = "LocationHelper"

  fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
  }

  suspend fun getCurrentLocation(context: Context): GeoLocationResult = withContext(Dispatchers.IO) {
    // Attempt 1: If permission is granted, try FusedLocationProviderClient and LocationManager
    if (hasLocationPermission(context)) {
      try {
        val fusedLoc = getFusedDeviceLocation(context)
        if (fusedLoc != null) {
          val geo = reverseGeocode(context, fusedLoc.latitude, fusedLoc.longitude)
          if (geo.city.isNotBlank()) {
            return@withContext geo
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "Fused location error: ${e.message}")
      }

      try {
        val sysLoc = getSystemLocationManagerLocation(context)
        if (sysLoc != null) {
          val geo = reverseGeocode(context, sysLoc.latitude, sysLoc.longitude)
          if (geo.city.isNotBlank()) {
            return@withContext geo
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "System location manager error: ${e.message}")
      }
    }

    // Attempt 2: IP-based GeoLocation fallback (Works anywhere with internet, including emulators)
    try {
      val ipGeo = fetchIpGeolocation()
      if (ipGeo != null && ipGeo.city.isNotBlank()) {
        return@withContext ipGeo
      }
    } catch (e: Exception) {
      Log.e(TAG, "IP Geolocation error: ${e.message}")
    }

    // Attempt 3: Locale & Timezone intelligent fallback
    val fallback = getLocaleBasedLocation(context)
    return@withContext fallback
  }

  private suspend fun getFusedDeviceLocation(context: Context): Location? {
    return withTimeoutOrNull(5000L) {
      suspendCancellableCoroutine { continuation ->
        try {
          val fusedClient = LocationServices.getFusedLocationProviderClient(context)
          val cancellationToken = CancellationTokenSource()

          fusedClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationToken.token
          ).addOnSuccessListener { loc ->
            if (loc != null) {
              if (continuation.isActive) continuation.resume(loc)
            } else {
              // Try last known location
              fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (continuation.isActive) continuation.resume(lastLoc)
              }.addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
              }
            }
          }.addOnFailureListener {
            try {
              fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                if (continuation.isActive) continuation.resume(lastLoc)
              }.addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
              }
            } catch (e: Exception) {
              if (continuation.isActive) continuation.resume(null)
            }
          }

          continuation.invokeOnCancellation {
            cancellationToken.cancel()
          }
        } catch (e: Exception) {
          if (continuation.isActive) continuation.resume(null)
        }
      }
    }
  }

  private fun getSystemLocationManagerLocation(context: Context): Location? {
    return try {
      val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
      val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER
      )
      for (provider in providers) {
        if (lm.isProviderEnabled(provider)) {
          @Suppress("MissingPermission")
          val loc = lm.getLastKnownLocation(provider)
          if (loc != null) return loc
        }
      }
      null
    } catch (e: Exception) {
      null
    }
  }

  private fun reverseGeocode(context: Context, lat: Double, lon: Double): GeoLocationResult {
    // 1. Android Geocoder
    try {
      val geocoder = Geocoder(context, Locale.getDefault())
      @Suppress("DEPRECATION")
      val addresses = geocoder.getFromLocation(lat, lon, 1)
      if (!addresses.isNullOrEmpty()) {
        val address = addresses[0]
        val city = address.locality
          ?: address.subAdminArea
          ?: address.adminArea
          ?: ""
        val country = address.countryName ?: Locale.getDefault().displayCountry
        val state = address.adminArea ?: ""
        if (city.isNotBlank() || country.isNotBlank()) {
          return GeoLocationResult(
            city = city.ifBlank { "New York" },
            country = country.ifBlank { "United States" },
            state = state,
            latitude = lat,
            longitude = lon,
            isSuccess = true
          )
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Android Geocoder failed: ${e.message}")
    }

    // 2. HTTP Reverse Geocoding fallback (Nominatim / OpenStreetMap)
    try {
      val url = URL("https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json&zoom=10")
      val conn = url.openConnection() as HttpURLConnection
      conn.connectTimeout = 3500
      conn.readTimeout = 3500
      conn.setRequestProperty("User-Agent", "KatkatDatingApp/1.0")
      if (conn.responseCode == 200) {
        val reader = BufferedReader(InputStreamReader(conn.inputStream))
        val response = reader.readText()
        reader.close()
        val json = JSONObject(response)
        val address = json.optJSONObject("address")
        if (address != null) {
          val city = address.optString("city", "").ifBlank {
            address.optString("town", "").ifBlank {
              address.optString("suburb", "").ifBlank {
                address.optString("county", "")
              }
            }
          }
          val country = address.optString("country", "")
          val state = address.optString("state", "")
          if (city.isNotBlank() || country.isNotBlank()) {
            return GeoLocationResult(
              city = city.ifBlank { "Local City" },
              country = country.ifBlank { "Local Country" },
              state = state,
              latitude = lat,
              longitude = lon,
              isSuccess = true
            )
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "OSM reverse geocode error: ${e.message}")
    }

    return GeoLocationResult(
      city = "",
      country = "",
      latitude = lat,
      longitude = lon,
      isSuccess = false
    )
  }

  private fun fetchIpGeolocation(): GeoLocationResult? {
    return try {
      val url = URL("http://ip-api.com/json/?fields=status,country,city,regionName,lat,lon")
      val conn = url.openConnection() as HttpURLConnection
      conn.connectTimeout = 4000
      conn.readTimeout = 4000
      if (conn.responseCode == 200) {
        val reader = BufferedReader(InputStreamReader(conn.inputStream))
        val response = reader.readText()
        reader.close()
        val json = JSONObject(response)
        if (json.optString("status") == "success") {
          val city = json.optString("city", "")
          val country = json.optString("country", "")
          val state = json.optString("regionName", "")
          val lat = json.optDouble("lat", 0.0)
          val lon = json.optDouble("lon", 0.0)
          if (city.isNotBlank()) {
            return GeoLocationResult(
              city = city,
              country = country,
              state = state,
              latitude = lat,
              longitude = lon,
              isSuccess = true
            )
          }
        }
      }
      null
    } catch (e: Exception) {
      null
    }
  }

  private fun getLocaleBasedLocation(context: Context): GeoLocationResult {
    val locale = Locale.getDefault()
    var country = locale.displayCountry
    if (country.isBlank()) {
      val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
      val simCountry = tm?.simCountryIso
      if (!simCountry.isNullOrBlank()) {
        country = Locale("", simCountry).displayCountry
      }
    }
    if (country.isBlank()) {
      country = "United States"
    }

    // Infer City based on TimeZone or Locale
    val tz = TimeZone.getDefault().id
    val city = when {
      tz.contains("/") -> tz.substringAfterLast("/").replace("_", " ")
      country.equals("India", ignoreCase = true) -> "Mumbai"
      country.equals("United States", ignoreCase = true) -> "New York"
      country.equals("United Kingdom", ignoreCase = true) -> "London"
      else -> "City"
    }

    return GeoLocationResult(
      city = city,
      country = country,
      latitude = 0.0,
      longitude = 0.0,
      isSuccess = true
    )
  }
}
