package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.isDeviceLocationEnabled
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val REQUEST_LOCATION_PERMISSION = 1
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val baseViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var location2: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = baseViewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        binding.saveButton.setOnClickListener {
            onLocationSelected()
            baseViewModel.navigateBack()
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    private fun setLocation(latLng: LatLng, location: String = "") {
        latitude = latLng.latitude
        longitude = latLng.longitude
        location2 = getLocation(location)
    }

    private fun getLocation(location: String): String {
        if (location.isEmpty()) {
            location2 = getString(R.string.dropped_pin)
            createDialog()
        }
        return location
    }

    private fun onLocationSelected() {
        baseViewModel.latitude.value = latitude
        baseViewModel.longitude.value = longitude
        baseViewModel.reminderSelectedLocationStr.value = location2
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    /**
     * Triggered when the map is ready to be used.
     *
     * @param googleMap The GoogleMap object representing the Google Map.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        if (isPermissionGranted()) {
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location->
                    if (location != null) {
                        val homeLatLng = LatLng(location.latitude, location.longitude)
                        val zoomLevel = 18f
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
                        map.addMarker(MarkerOptions().position(homeLatLng))
                    }
                }
        }

        setMapLongClick(map) // Set a long click listener for the map.
        setPoiClick(map) // Set a click listener for points of interest.
        setMapStyle(map) // Set the custom map style.

        enableMyLocation() // Enable location tracking.
    }

    /**
     * Adds a blue marker to the map when the user long clicks on it.
     *
     * @param map The GoogleMap to attach the listener to.
     */
    private fun setMapLongClick(map: GoogleMap) {
        // Add a blue marker to the map when the user performs a long click.
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                latLng.latitude,
                latLng.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            setLocation(latLng)
        }
    }

    /**
     * Adds a marker when a place of interest (POI) is clicked with the name of
     * the POI and immediately shows the info window.
     *
     * @param map The GoogleMap to attach the listener to.
     */
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()

            setLocation(poi.latLng , poi.name)
        }
    }

    /**
     * Loads a style from the map_style.json file to style the Google Map. Log
     * the errors if the loading fails.
     *
     * @param map The GoogleMap object to style.
     */
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    context,
                    R.raw.map_style
                )
            )
        } catch (_: Resources.NotFoundException) {
        }
    }

    private fun createDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Title")

        // Set up the input
        val input = EditText(requireContext())
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.hint = getString(R.string.dialog_text)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton(getString(R.string.ok_button)) { _, _ ->
            // Here you get get input text from the Edit Text
            location2 = input.text.toString()
        }
        builder.setNegativeButton(getString(R.string.cancel_button)) { dialog, _ ->
            location2 = getString(R.string.dropped_pin)
            dialog.cancel()
        }

        builder.show()
    }

    // Checks that users have given permission
    private fun isPermissionGranted() : Boolean {
        @Suppress("DEPRECATED_IDENTITY_EQUALS")
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks for location permissions, and requests them if they are missing.
     * Otherwise, enables the location layer.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (isDeviceLocationEnabled()) {
                getMyCurrentLocation()
            } else {
                enableDeviceLocation()
            }
        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    // Callback for the result from requesting permissions.
    // This method is invoked for every call on requestPermissions(android.app.Activity, String[],
    // int).
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    /**
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun enableDeviceLocation(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        // Location Enabled
        locationSettingsResponseTask.addOnSuccessListener {
            getMyCurrentLocation()
        }

        // Location not Enabled
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    exception.startResolutionForResult(requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (_: IntentSender.SendIntentException) {
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful) {
                map.isMyLocationEnabled = true
                val myLocation =
                    LatLng(it.result.latitude, it.result.longitude)
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        myLocation, 18f
                    )
                )
            } else {
                map.isMyLocationEnabled = false
            }
        }
    }

    /**
     *  When we get the result from asking the user to turn on device location, we call
     *  checkDeviceLocationSettingsAndStartGeofence again to make sure it's actually on, but
     *  we don't resolve the check to keep the user from seeing an endless loop.
     */
    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            enableDeviceLocation(false)
        }
    }
}
