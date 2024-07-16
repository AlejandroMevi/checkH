package com.venturessoft.human.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.venturessoft.human.core.BaseActivity.Companion.dataBSSIDConected
import com.venturessoft.human.core.BaseActivity.Companion.dataBSSIDList
import com.venturessoft.human.main.data.models.WifiModel

class BSSID (private val context:Context) {

    private val mWifiManager: WifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val mWifi = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
    fun getBssid(restart: Boolean) {
        if (mWifi.isWifiEnabled){
            try {
                if(mWifiManager.connectionInfo.bssid != null && mWifiManager.connectionInfo.bssid != "00:00:00:00:00:00"){
                    dataBSSIDConected.postValue(WifiModel(mWifiManager.connectionInfo.ssid,mWifiManager.connectionInfo.bssid))
                }else{
                    dataBSSIDConected.postValue(null)
                }

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    val listBSSID = mutableListOf<WifiModel>()
                    mWifiManager.scanResults.forEach {
                        listBSSID.add(WifiModel(it.SSID,it.BSSID))
                    }
                    dataBSSIDList.postValue(listBSSID)
                }
            }catch (_:Exception){ }
            if (restart && dataBSSIDList.value.isNullOrEmpty()){
                Handler(Looper.getMainLooper()).postDelayed({
                    getBssid(true)
                } , 2500)
            }
        }
    }
}