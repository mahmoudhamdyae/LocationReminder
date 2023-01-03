package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.isDeviceLocationEnabled
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMyLocationButtonClickListener,
    OnMyLocationClickListener, OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val baseViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map: GoogleMap

    private var permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val locationPermissionResultRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true || result[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                baseViewModel.showToast.value = getString(R.string.location_permission_granted)
                getMyCurrentLocation()
            } else {
                baseViewModel.showSnackBarInt.value = R.string.location_permission_denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // If true = user denied the request 2 times
            showAlertDialogForLocationPermission()
        }
    }

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

        return binding.root
    }

    private fun updateLocation(latLng: LatLng, location: String) {
        baseViewModel.latitude.value = latLng.latitude
        baseViewModel.longitude.value = latLng.longitude
        baseViewModel.reminderSelectedLocationStr.value = location

        binding.saveButton.visibility = View.VISIBLE
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
        if (isPermissionGranted() && isDeviceLocationEnabled()) {
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

        setMapClick(map) // Set a long click listener for the map.
        setPoiClick(map) // Set a click listener for points of interest.
        setMapStyle(map) // Set the custom map style.

        enableMyLocation() // Enable location tracking.

        map.isMyLocationEnabled = true
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
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            map.clear()
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
    private fun isPermissionGranted() = (ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

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
            locationPermissionResultRequest.launch(permissions)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private val locationSettingPermissionResultRequest =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                //
            } else {
                baseViewModel.showErrorMessage.value = getString(R.string.deny_to_open_location)
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
            baseViewModel.showSnackBar.value = "successhaha"
            getMyCurrentLocation()
        }

        // Location not Enabled
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                try {
                    locationSettingPermissionResultRequest.launch(
                        IntentSenderRequest.Builder(exception.resolution.intentSender).build()
                    )
                } catch (_: IntentSender.SendIntentException) {
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getMyCurrentLocation() {
        if (::map.isInitialized){
            map.isMyLocationEnabled = true
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
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

    override fun onMyLocationButtonClick(): Boolean {
        enableMyLocation()
        return false
    }

    override fun onMyLocationClick(location: Location) {
        updateLocation(LatLng(location.latitude, location.longitude), "My Location")
    }

    private fun showAlertDialogForLocationPermission() {
        AlertDialog.Builder(requireContext()).apply {
            setIcon(R.drawable.attention)
            setTitle(getString(R.string.location_required_error))
            setMessage(getString(R.string.permission_denied_explanation))
            setPositiveButton("OK") { _, _ ->
                locationPermissionResultRequest.launch(permissions)
            }
        }.show()
    }
}
