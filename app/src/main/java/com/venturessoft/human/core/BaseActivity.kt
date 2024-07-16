package com.venturessoft.human.core

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.venturessoft.human.main.data.models.LocationModel
import com.venturessoft.human.main.data.models.WifiModel
import com.venturessoft.human.main.ui.interfaces.BaseInterface
import io.github.inflationx.viewpump.ViewPumpContextWrapper

open class BaseActivity : AppCompatActivity(), BaseInterface {
    private lateinit var bssid:BSSID
    private lateinit var beacons:Beacons
    private lateinit var location:Location
    private lateinit var network:ConnectionLiveData
    companion object {
        var dataLocation = MutableLiveData<LocationModel?>(null)
        var dataListBeacons = MutableLiveData<ArrayList<String>?>(null)
        var dataBSSIDConected = MutableLiveData<WifiModel?>(null)
        var dataBSSIDList = MutableLiveData<List<WifiModel>?>(null)
        var dataConnection = MutableLiveData<Boolean?>(null)
        var tokenFirebase = ""
    }
    override fun onStart() {
        super.onStart()
        editTokenSharedPreferences()

        bssid = BSSID(this)
        beacons = Beacons(this)
        location = Location(this)
        network = ConnectionLiveData(this)

        beacons.setUpBluetoothManager()
        location.getLocation()
        network.observe(this){
            dataConnection.postValue(it)
        }
    }
    override fun onResume() {
        super.onResume()
        getBSSID(false)
        getBeacons(true)
        getLocation()
    }
    override fun onPause() {
        super.onPause()
        dataListBeacons.value = null
        dataLocation.value = null
        dataBSSIDConected.value = null
    }
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }
    override fun getLocation() {
        location.validateLocation()
    }

    override fun stopLocation(){
        location.stopLocation()
    }
    override fun getBSSID(restart:Boolean) {
        bssid.getBssid(restart)
    }
    override fun stopBSSID() {
        dataBSSIDConected.postValue(null)
        dataBSSIDList.postValue(null)
    }

    override fun isLocationEnabled():Boolean {
        return location.isLocationEnabled()
    }

    override fun getBeacons(clearBeacons:Boolean) {
        beacons.getBeacons(clearBeacons)
    }
    override fun stopBeacons() {
        beacons.stopBeacons()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBSSID()
        stopBeacons()
        stopLocation()
        dataLocation.removeObservers(this)
        dataListBeacons.removeObservers(this)
        dataBSSIDConected.removeObservers(this)
        dataBSSIDList.removeObservers(this)
        dataConnection.removeObservers(this)
    }
    private fun editTokenSharedPreferences() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            tokenFirebase = task.result ?: ""
        }
    }
}
