package com.udacity.project4.locationreminders

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.gms.location.*
import com.udacity.project4.R
import com.udacity.project4.locationreminders.geofence.*
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
@SuppressLint("UnspecifiedImmutableFlag")
class RemindersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                nav_host_fragment.findNavController().navigateUp()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
