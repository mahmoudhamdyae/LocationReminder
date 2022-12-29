package com.udacity.project4.locationreminders.geofence

import android.content.Context
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import java.util.concurrent.TimeUnit

/**
 * Returns the error string for a geofencing error code.
 */
fun errorMessage(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.unknown_geofence_error)
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

    val LANDMARK_DATA = getArray()

    val NUM_LANDMARKS = LANDMARK_DATA.size
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"

    private fun getArray(): Array<LandmarkDataObject> {
        val r1 = ReminderDataItem(
            "golden_gate_bridge",
            R.string.golden_gate_bridge_hint.toString(),
            R.string.golden_gate_bridge_location.toString(),
            37.819927,
            -122.478256)
        val r2 = ReminderDataItem(
            "ferry_building",
            R.string.ferry_building_hint.toString(),
            R.string.ferry_building_location.toString(),
            37.795490,
            -122.394276)
        val r3 = ReminderDataItem(
            "pier_39",
            R.string.pier_39_hint.toString(),
            R.string.pier_39_location.toString(),
            37.808674,
            -122.409821)
        val r4 = ReminderDataItem(
            "union_square",
            R.string.union_square_hint.toString(),
            R.string.union_square_location.toString(),
            37.788151,
            -122.407570)

        val retArr = arrayOf(
            LandmarkDataObject(
                r1.id,
                r1.description!!.toInt(),
                r1.location!!.toInt(),
                LatLng(r1.latitude!!, r1.longitude!!)),

            LandmarkDataObject(
                r2.id,
                r2.description!!.toInt(),
                r2.location!!.toInt(),
                LatLng(r2.latitude!!, r2.longitude!!)),

            LandmarkDataObject(
                r3.id,
                r3.description!!.toInt(),
                r3.location!!.toInt(),
                LatLng(r3.latitude!!, r3.longitude!!)),

            LandmarkDataObject(
                r4.id,
                r4.description!!.toInt(),
                r4.location!!.toInt(),
                LatLng(r4.latitude!!, r4.longitude!!))
        )

        return retArr
    }
}