package com.venturessoft.human.main.ui.interfaces

interface BaseInterface {
    fun getBeacons(clearBeacons:Boolean)
    fun stopBeacons()
    fun getLocation()
    fun stopLocation()
    fun getBSSID(restart:Boolean)
    fun stopBSSID()
    fun isLocationEnabled():Boolean
}