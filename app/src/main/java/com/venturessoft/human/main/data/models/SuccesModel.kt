package com.venturessoft.human.main.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.venturessoft.human.core.utils.Constants.Companion.PENDING
@Entity(tableName = "offline_service_table")
data class SuccesModel(
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,
    var connection:String = "",
    var station:String = "",
    var date:String = "",
    var time:String = "",
    var typeofCheck:String = "",
    var photoEmployee:String = "",
    var statusOffline:Int = PENDING,
    var sGeolocalizacion: LocationModel = LocationModel(),
    var sBeacons: BeaconsModel = BeaconsModel(),
    var sBssids: BSSIDModel = BSSIDModel(),
    var numeroCompania: String = "",
    var empleado: String = "",
    var fechaHoraChecada: String = "",
    var tipoChecada: String = "",
    var foto: String = "",
    var prioridad: String = "",
):java.io.Serializable
@Entity(tableName = "offline_service_table_local")
data class UserLocalPicture (
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,
    var statusOffline:Int = PENDING,
    var numeroCompania: String = "",
    var empleado: String = "",
    var fechaHoraChecada: String = "",
    var tipoChecada: String = "",
    var prioridad: String = "",
    var dispositivo:String = "",
    var sGeolocalizacion: LocationModel = LocationModel(),
    var sBeacons: BeaconsModel = BeaconsModel(),
    var foto: String = "",
    var fotoLocal: String = "",
    var sBssids: BSSIDModel = BSSIDModel()
)
