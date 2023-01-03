package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

@SuppressLint("MissingPermission")
@Suppress("DEPRECATION")
class SelectLocationFragment : BaseFragment(), OnMyLocationButtonClickListener,
    OnMyLocationClickListener, OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val baseViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return binding.root
    }

    private fun updateLocation(latLng: LatLng, location: String) {
        baseViewModel.latitude.value = latLng.latitude
        baseViewModel.longitude.value = latLng.longitude
        baseViewModel.reminderSelectedLocationStr.value = location

        binding.saveButton.setOnClickListener {
            baseViewModel.navigateBack()
        }
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

    /**
     * Triggered when the map is ready to be used.
     *
     * @param googleMap The GoogleMap object representing the Google Map.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        setMapClick(map) // Set a long click listener for the map.
        setPoiClick(map) // Set a click listener for points of interest.
        setMapStyle(map) // Set the custom map style.

        enableMyLocation() // Enable location tracking.

        map.setOnMyLocationButtonClickListener(this)
        map.setOnMyLocationClickListener(this)
    }

    /**
     * Adds a blue marker to the map when the user long clicks on it.
     *
     * @param map The GoogleMap to attach the listener to.
     */
    private fun setMapClick(map: GoogleMap) {
        // Add a blue marker to the map when the user performs a long click.
        map.setOnMapClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                latLng.latitude,
                latLng.longitude
            )

            map.clear()
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            updateLocation(latLng, getString(R.string.dropped_pin))
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
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()

            updateLocation(poi.latLng , poi.name)
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

    // Checks that users have given permission
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks for location permissions, and requests them if they are missing.
     * Otherwise, enables the location layer.
     */
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            zoomToCurrentLocation(true)
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                R.string.location_required_error,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(android.R.string.ok) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                }.show()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun zoomToCurrentLocation(resolve: Boolean) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                startIntentSenderForResult(
                    exception.resolution.intentSender,
                    REQUEST_TURN_DEVICE_LOCATION_ON,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    zoomToCurrentLocation(true)
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            val zoomLevel = 15f
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            map.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    currentLatLng,
                                    zoomLevel
                                )
                            )
                        }
                    }
            }
        }
    }

    // Callback for the result from requesting permissions.
    // This method is invoked for every call on requestPermissions(android.app.Activity, String[],
    // int).
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                map.isMyLocationEnabled = true
                zoomToCurrentLocation(false)
            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        enableMyLocation()
        return false
    }

    override fun onMyLocationClick(location: Location) {
        updateLocation(LatLng(location.latitude, location.longitude), "My Location")
    }

    companion object {
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 1002
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
