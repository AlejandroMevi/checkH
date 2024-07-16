package com.venturessoft.human.core

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.venturessoft.human.main.data.models.BSSIDModel
import com.venturessoft.human.main.data.models.BeaconsModel
import com.venturessoft.human.main.data.models.LocationModel

class Converters {
    @TypeConverter
    fun arrayBeaconsStringToString(listString: ArrayList<String>): String {
        return try {
            Gson().toJson(listString)
        }catch (e:java.lang.Exception){
            return try {
                Gson().toJson(listString[0])
            }catch (e:java.lang.Exception){
                ""
            }
        }
    }
    @TypeConverter
    fun stringToArrayBeacons(string: String): ArrayList<String> {
        return try {
            Gson().fromJson(string, ArrayList<String>()::class.java)
        }catch (e:java.lang.Exception){
            ArrayList()
        }
    }
    @TypeConverter
    fun locationToString(location: LocationModel):String{
        return try {
            Gson().toJson(location)
        }catch (e:java.lang.Exception){
            ""
        }
    }
    @TypeConverter
    fun stringToLocation(string: String):LocationModel{
        return try {
            Gson().fromJson(string, LocationModel::class.java)
        }catch (e:java.lang.Exception){
            LocationModel()
        }
    }
    @TypeConverter
    fun beaconToString(beaconsModel: BeaconsModel):String{
        return try {
            Gson().toJson(beaconsModel)
        }catch (e:java.lang.Exception){
            return try {
                Gson().toJson(beaconsModel.beacons[0])
            }catch (e:java.lang.Exception){
                ""
            }
        }
    }
    @TypeConverter
    fun stringToBeacon(string: String):BeaconsModel{
        return try {
            Gson().fromJson(string, BeaconsModel::class.java)
        }catch (e:java.lang.Exception){
            BeaconsModel()
        }
    }
    @TypeConverter
    fun bssIDModelToString(bSSIDModel: BSSIDModel):String{
        return try {
            Gson().toJson(bSSIDModel)
        }catch (e:java.lang.Exception){
            return try {
                Gson().toJson(bSSIDModel.bssid[0])
            }catch (e:java.lang.Exception){
                ""
            }
        }
    }
    @TypeConverter
    fun stringTbssID(string: String):BSSIDModel{
        return try {
            Gson().fromJson(string, BSSIDModel::class.java)
        }catch (e:java.lang.Exception){
            BSSIDModel()
        }
    }
}
