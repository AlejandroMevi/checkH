package com.venturessoft.human.core

import android.Manifest
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.venturessoft.human.R
import com.venturessoft.human.core.BaseActivity.Companion.dataLocation
import com.venturessoft.human.core.utils.Constants.Companion.DELAY_TIME
import com.venturessoft.human.core.utils.Constants.Companion.DELAY_TIME_LOCATION
import com.venturessoft.human.core.utils.DialogGeneral
import com.venturessoft.human.main.data.models.LocationModel

class Location(private val activity: Activity) {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    fun getLocation(){
        val manager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlertLocation()
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        validateLocation()
    }
    fun validateLocation() {
        getLocationFInal()
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        getLocationUpdates()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }
    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, DELAY_TIME)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(DELAY_TIME_LOCATION)
            .setMaxUpdateDelayMillis(DELAY_TIME_LOCATION)
            .setMinUpdateDistanceMeters(0f)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    locationResult.lastLocation?.let {location->
                        dataLocation.postValue(LocationModel(location.latitude.toString(),location.longitude.toString()))
                    }
                }
            }
        }
    }
    private fun showAlertLocation() {
        val dialogGPS = DialogGeneral(
            activity.getString(R.string.title_gps_off),
            activity.getString(R.string.message_location_activated),
            activity.getString(R.string.configuration),
            null,
            {
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(myIntent)
            }
        )
        dialogGPS.show((activity as FragmentActivity).supportFragmentManager, "dialog")
    }
    fun getLocationFInal() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                mFusedLocationClient.lastLocation.addOnCompleteListener(activity) { task ->
                    try {
                        val location: Location? = task.result
                        if (location != null) {
                            dataLocation.postValue(LocationModel(location.latitude.toString(),location.longitude.toString()))
                        }
                    }catch (e:Exception){
                        Log.i("error",e.message.toString())
                    }
                }
            } else {
                showAlertLocation()
            }
        } else {
            requestPermissions()
        }
    }
    fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 2)
    }

    fun stopLocation(){
        fusedLocationClient.removeLocationUpdates(locationCallback)
        dataLocation.postValue(null)
    }
}