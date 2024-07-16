package com.venturessoft.human.main.data.models

data class RegisterCheckModel(
    var root: RootRegister
)
data class RootRegister (
    var numeroCompania: String = "",
    var empleado: String = "",
    var fechaHoraChecada: String = "",
    var tipoChecada: String = "",
    var prioridad: String = "",
    var sGeolocalizacion: LocationModel = LocationModel(),
    var sBeacons: BeaconsModel = BeaconsModel(),
    var sBssids: BSSIDModel = BSSIDModel(),
    var dispositivo:String = "",
    var foto:String = "",
    var fotoModelo:String ?= null,
    var token:String ?= null,
)


