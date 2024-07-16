package com.venturessoft.human.core

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import androidx.lifecycle.LiveData
import com.venturessoft.human.core.BaseActivity.Companion.dataBSSIDConected
import com.venturessoft.human.core.BaseActivity.Companion.dataBSSIDList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class ConnectionLiveData(private val context: Context) : LiveData<Boolean>() {

    private lateinit var networkCallback: NetworkCallback
    private val connectivityManager = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val mWifi = (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder().addCapability(NET_CAPABILITY_INTERNET).build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    override fun onInactive() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
    private fun createNetworkCallback() = object : NetworkCallback() {
        override fun onAvailable(network: Network) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
            if (hasInternetCapability == true) {
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) {
                        BSSID(context).getBssid(true)
                    }
                    val hasInternet = DoesNetworkHaveInternet.execute()
                    if (hasInternet) {
                        postValue(true)
                    }
                }
            }
        }
        override fun onUnavailable() {
            if (!mWifi.isWifiEnabled){
                dataBSSIDConected.postValue(null)
                dataBSSIDList.postValue(null)
            }
            postValue(false)
        }
        override fun onLosing(network: Network, maxMsToLive: Int) {
            if (!mWifi.isWifiEnabled){
                dataBSSIDConected.postValue(null)
                dataBSSIDList.postValue(null)
            }
            postValue(false)
        }
        override fun onLost(network: Network) {
            if (!mWifi.isWifiEnabled){
                dataBSSIDConected.postValue(null)
                dataBSSIDList.postValue(null)
            }
            postValue(false)
        }
    }
    object DoesNetworkHaveInternet {
        fun execute(): Boolean {
            return try {
                val p = Runtime.getRuntime().exec("ping -c 1 www.google.es")
                p.waitFor() == 0
            } catch (e: Exception) {
                false
            }
        }
    }
}


