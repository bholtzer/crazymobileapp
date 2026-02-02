package com.crazymobileapp.android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : Activity() {
    private val preferenceName = "emergency_settings"
    private val numberKey = "emergency_number"
    private val customNumberKey = "custom_emergency_number"
    private val locationPermissionRequest = 101

    private lateinit var locationStatus: TextView
    private lateinit var selectedNumberDisplay: TextView
    private lateinit var customNumberInput: EditText
    private lateinit var sendLocationButton: Button
    private lateinit var emergencyNumberSpinner: Spinner

    private var lastLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationStatus = findViewById(R.id.locationStatus)
        selectedNumberDisplay = findViewById(R.id.selectedNumberDisplay)
        customNumberInput = findViewById(R.id.customNumberInput)
        sendLocationButton = findViewById(R.id.sendLocationButton)
        emergencyNumberSpinner = findViewById(R.id.emergencyNumberSpinner)

        val emergencyOptions = listOf("911", "112", "999", "Custom")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, emergencyOptions)
        emergencyNumberSpinner.adapter = adapter

        val preferences = getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        val savedNumber = preferences.getString(numberKey, emergencyOptions.first()) ?: emergencyOptions.first()
        val savedCustomNumber = preferences.getString(customNumberKey, "") ?: ""

        val customIndex = emergencyOptions.indexOf("Custom").coerceAtLeast(0)
        val initialIndex = emergencyOptions.indexOf(savedNumber).takeIf { it >= 0 } ?: customIndex
        emergencyNumberSpinner.setSelection(initialIndex)
        customNumberInput.setText(savedCustomNumber)
        customNumberInput.isEnabled = emergencyOptions[initialIndex] == "Custom"
        updateSelectedNumberDisplay(getSelectedEmergencyNumber())

        emergencyNumberSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = emergencyOptions[position]
                customNumberInput.isEnabled = selected == "Custom"
                if (selected != "Custom") {
                    saveNumberPreference(selected, "")
                    updateSelectedNumberDisplay(selected)
                } else {
                    saveNumberPreference(selected, customNumberInput.text.toString())
                    updateSelectedNumberDisplay(customNumberInput.text.toString())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        customNumberInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (emergencyNumberSpinner.selectedItem == "Custom") {
                    val customNumber = s?.toString().orEmpty()
                    saveNumberPreference("Custom", customNumber)
                    updateSelectedNumberDisplay(customNumber)
                }
            }
        })

        sendLocationButton.setOnClickListener {
            val number = getSelectedEmergencyNumber()
            if (number.isBlank()) {
                toast("Enter an emergency number to send location.")
                return@setOnClickListener
            }
            val locationMessage = buildLocationMessage(lastLocation)
            sendLocation(number, locationMessage)
        }

        requestLocationAndSendOnLaunch()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionRequest && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            requestLocationAndSendOnLaunch()
        } else {
            locationStatus.text = "Location: permission denied."
        }
    }

    private fun requestLocationAndSendOnLaunch() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                locationPermissionRequest
            )
            return
        }

        lastLocation = getBestLastKnownLocation()
        locationStatus.text = lastLocation?.let { location ->
            "Location: ${location.latitude}, ${location.longitude}"
        } ?: "Location: unavailable."

        val number = getSelectedEmergencyNumber()
        if (number.isNotBlank()) {
            sendLocation(number, buildLocationMessage(lastLocation))
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun getBestLastKnownLocation(): Location? {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val location = if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                locationManager.getLastKnownLocation(provider)
            } else {
                null
            }

            if (location != null && (bestLocation == null || location.accuracy < bestLocation.accuracy)) {
                bestLocation = location
            }
        }
        return bestLocation
    }

    private fun sendLocation(number: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$number")
            putExtra("sms_body", message)
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            toast("No messaging app available to send location.")
        }
    }

    private fun buildLocationMessage(location: Location?): String {
        return if (location != null) {
            val mapsLink = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
            "Current location: ${location.latitude}, ${location.longitude}. $mapsLink"
        } else {
            "Current location unavailable."
        }
    }

    private fun getSelectedEmergencyNumber(): String {
        val selected = emergencyNumberSpinner.selectedItem?.toString().orEmpty()
        return if (selected == "Custom") {
            customNumberInput.text?.toString().orEmpty()
        } else {
            selected
        }
    }

    private fun updateSelectedNumberDisplay(number: String) {
        selectedNumberDisplay.text = if (number.isNotBlank()) {
            "Selected: $number"
        } else {
            "Selected: --"
        }
    }

    private fun saveNumberPreference(selected: String, customNumber: String) {
        getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit()
            .putString(numberKey, selected)
            .putString(customNumberKey, customNumber)
            .apply()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
