package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceHelper
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SaveReminderFragment : BaseFragment() {
    // Get the view model this time as a single to be shared with the another fragment
    override val baseViewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceHelper: GeofenceHelper

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = baseViewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        geofenceHelper = GeofenceHelper(context)

        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            baseViewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = baseViewModel.reminderTitle.value
            val description = baseViewModel.reminderDescription.value
            val location = baseViewModel.reminderSelectedLocationStr.value
            val latitude = baseViewModel.latitude.value
            val longitude = baseViewModel.longitude.value
            val id = UUID.randomUUID().toString()

            val reminderData = ReminderDataItem(title, description, location, latitude, longitude)
            baseViewModel.validateAndSaveReminder(reminderData)

            if (latitude != null && longitude != null && !TextUtils.isEmpty(title)) {
//                addGeofence(LatLng(latitude, longitude), id)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        baseViewModel.onClear()
    }

//    @SuppressLint("MissingPermission")
//    private fun addGeofence(latLng: LatLng, geofenceId: String) {
//
//        val geofence: Geofence = geofenceHelper.getGeofence(
//            geofenceId,
//            latLng,
//            500f,
//            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
//        )
//        val geofencingRequest: GeofencingRequest = geofenceHelper.getGeofencingRequest(geofence)
//        val pendingIntent: PendingIntent? = geofenceHelper.getGeofencePendingIntent()
//
//        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
//            .addOnFailureListener {
//                Toast.makeText(context, "Please give background location permission", Toast.LENGTH_LONG).show()
//            }
//    }
}
