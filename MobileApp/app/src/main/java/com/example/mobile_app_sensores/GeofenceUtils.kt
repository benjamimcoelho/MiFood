package com.example.mobile_app_sensores

import android.content.Context
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.maps.model.LatLng
import java.util.concurrent.TimeUnit

/**
 * Returns the error string for a geofencing error code.
 */
fun errorMessage(context: Context, errorCode: Int): String {
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence Not Available"
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Too Many Geofences"
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Too many pending intents"
        else -> "Unknown Error"
    }
}

/**
 * Stores latitude and longitude information along with a hint to help user find the location.
 */
data class LandmarkDataObject(val id: String, val hint: Int, val name: Int, val latLong: LatLng)

internal object GeofencingConstants {

    /**
     * Used to set an expiration time for a geofence. After this amount of time, Location services
     * stops tracking the geofence. For this sample, geofences expire after one hour.
     */
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    val LANDMARK_DATA = arrayOf(
        LandmarkDataObject(//para já meti as da minha casa (cantina: 41.561975, -8.397586) | casa 41.251498, -8.293620
            "cantina_gualtar",
            R.string.app_name,
            R.string.app_name,
            LatLng(41.561942, -8.398410)
        )
    )

    val NUM_LANDMARKS = LANDMARK_DATA.size
    //Qual o raio pretendido
    const val GEOFENCE_RADIUS_IN_METERS = 380f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
}