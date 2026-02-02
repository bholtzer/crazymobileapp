package com.crazymobileapp.app

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.telephony.SmsManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var emergencySpinner: Spinner
    private lateinit var customNumberInput: EditText
    private lateinit var statusText: TextView
    private lateinit var prefs: SharedPreferences

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            sendLocationUpdateIfPossible()
        } else {
            updateStatus(getString(R.string.permissions_required))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        emergencySpinner = findViewById(R.id.emergency_spinner)
        customNumberInput = findViewById(R.id.custom_number_input)
        statusText = findViewById(R.id.status_text)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            resources.getStringArray(R.array.emergency_numbers)
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        emergencySpinner.adapter = adapter

        restoreSavedNumber()

        findViewById<Button>(R.id.save_button).setOnClickListener {
            saveEmergencyNumber()
        }

        findViewById<Button>(R.id.call_button).setOnClickListener {
            val number = getSelectedNumber()
            if (number.isNullOrBlank()) {
                showToast(getString(R.string.enter_number_prompt))
            } else {
                startCall(number)
            }
        }

        requestPermissionsIfNeeded()
    }

    private fun restoreSavedNumber() {
        val savedNumber = prefs.getString(PREFS_KEY_NUMBER, null)
        if (savedNumber.isNullOrBlank()) {
            return
        }
        val numbers = resources.getStringArray(R.array.emergency_numbers)
        val index = numbers.indexOf(savedNumber)
        if (index >= 0) {
            emergencySpinner.setSelection(index)
        } else {
            emergencySpinner.setSelection(numbers.lastIndex)
            customNumberInput.setText(savedNumber)
        }
    }

    private fun saveEmergencyNumber() {
        val selected = getSelectedNumber()
        if (selected.isNullOrBlank()) {
            showToast(getString(R.string.enter_number_prompt))
            return
        }
        prefs.edit().putString(PREFS_KEY_NUMBER, selected).apply()
        showToast(getString(R.string.saved_message))
        sendLocationUpdateIfPossible()
    }

    private fun getSelectedNumber(): String? {
        val numbers = resources.getStringArray(R.array.emergency_numbers)
        val selected = emergencySpinner.selectedItem?.toString()?.trim().orEmpty()
        return if (selected == numbers.last()) {
            customNumberInput.text?.toString()?.trim()
        } else {
            selected
        }
    }

    private fun requestPermissionsIfNeeded() {
        if (hasAllPermissions()) {
            sendLocationUpdateIfPossible()
            return
        }
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.SEND_SMS
            )
        )
    }

    private fun hasAllPermissions(): Boolean {
        val permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun sendLocationUpdateIfPossible() {
        val number = getSelectedNumber()
        if (number.isNullOrBlank()) {
            updateStatus(getString(R.string.no_number_status))
            return
        }
        if (!hasAllPermissions()) {
            updateStatus(getString(R.string.permissions_required))
            return
        }
        updateStatus(getString(R.string.fetching_location))
        fetchLocation { location ->
            if (location == null) {
                updateStatus(getString(R.string.location_unavailable))
                return@fetchLocation
            }
            val message = buildLocationMessage(location)
            sendSms(number, message)
            updateStatus(getString(R.string.location_sent, number))
        }
    }

    private fun fetchLocation(onLocation: (Location?) -> Unit) {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val lastKnown = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
            .mapNotNull { provider ->
                runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
            }
            .maxByOrNull { it.time }

        if (lastKnown != null) {
            onLocation(lastKnown)
            return
        }

        val criteria = Criteria().apply { accuracy = Criteria.ACCURACY_FINE }
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onLocation(location)
            }

            override fun onProviderDisabled(provider: String) = Unit
            override fun onProviderEnabled(provider: String) = Unit
        }

        try {
            locationManager.requestSingleUpdate(criteria, listener, Looper.getMainLooper())
        } catch (exception: SecurityException) {
            onLocation(null)
        }
    }

    private fun buildLocationMessage(location: Location): String {
        val mapLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        return getString(R.string.location_message_template, mapLink)
    }

    private fun sendSms(number: String, message: String) {
        val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }
        smsManager.sendTextMessage(number, null, message, null, null)
    }

    private fun startCall(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        startActivity(intent)
    }

    private fun updateStatus(message: String) {
        statusText.text = message
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PREFS_NAME = "emergency_settings"
        private const val PREFS_KEY_NUMBER = "emergency_number"
    }
}
