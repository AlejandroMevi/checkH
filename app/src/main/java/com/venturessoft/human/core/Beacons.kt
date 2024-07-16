package com.venturessoft.human.core

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import androidx.appcompat.app.AppCompatActivity
import com.venturessoft.human.core.BaseActivity.Companion.dataListBeacons

class Beacons(private val activity: Activity) {
    private var btManager: BluetoothManager? = null
    private var btAdapter: BluetoothAdapter? = null
    private var btScanner: BluetoothLeScanner? = null
    private val listBeacons: ArrayList<String> = arrayListOf()
    private val hex = "0123456789ABCDEF".toCharArray()
    var iBeaconUUID = ""

    fun setUpBluetoothManager() {
        if (btManager == null) {
            btManager =
                activity.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
            btAdapter = btManager?.adapter
            btScanner = btAdapter?.bluetoothLeScanner
            if (btManager != null) {
                getBeacons(true)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getBeacons(clearBeacons: Boolean) {
        try {
            iBeaconUUID = ""
            if (btManager == null) {
                btManager =
                    activity.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
            }
            if (btAdapter == null) {
                btAdapter = btManager?.adapter
            }
            if (btScanner == null) {
                btScanner = btAdapter?.bluetoothLeScanner
                btScanner?.stopScan(leScanCallback)
            }

            if (clearBeacons) {
                dataListBeacons.value = null
                listBeacons.clear()
            }

            if (btAdapter?.isEnabled == true) {
                btScanner?.startScan(leScanCallback)
            }
        } catch (_: java.lang.Exception) {

        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val scanRecord = result.scanRecord
            iBeaconUUID = ""
            if (scanRecord != null) {
                val iBeaconManufactureData = scanRecord.getManufacturerSpecificData(0X004c)
                if (iBeaconManufactureData != null && iBeaconManufactureData.size >= 23) {
                    iBeaconUUID = parseUUID(iBeaconManufactureData.copyOfRange(2, 18))
                }
            }
            if (iBeaconUUID.isNotEmpty()) {
                if (!listBeacons.contains(iBeaconUUID)) {
                    listBeacons.add(iBeaconUUID)
                    dataListBeacons.value = listBeacons
                }
            }
            return
        }
    }

    @SuppressLint("MissingPermission")
    fun stopBeacons() {
        dataListBeacons.value = null
        listBeacons.clear()
        try {
            if (btScanner != null) {
                btScanner?.stopScan(leScanCallback)
            }
        } catch (_: Exception) {
        }
    }

    private fun toHexString(bytes: ByteArray): String {
        if (bytes.isEmpty()) {
            return ""
        }
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = (bytes[j].toInt() and 0xFF)
            hexChars[j * 2] = hex[v ushr 4]
            hexChars[j * 2 + 1] = hex[v and 0x0F]
        }
        return String(hexChars)
    }

    private fun parseUUID(bytes: ByteArray): String {
        val hexString = toHexString(bytes)
        return if (hexString.isNotEmpty()) {
            hexString.substring(0, 8) + "-" +
                    hexString.substring(8, 12) + "-" +
                    hexString.substring(12, 16) + "-" +
                    hexString.substring(16, 20) + "-" +
                    hexString.substring(20, 32)
        } else {
            ""
        }
    }
}