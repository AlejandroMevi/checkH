package com.venturessoft.human.pictureLocal.data.models
data class LocalPictureRquest (
    var idCia:Long?=null,
    var idEmpleado:Long?=null,
    var fotoActual:String?=null,
    var fotoNueva:String?=null,
    var hash:String?=null,
    var token:String?=null
)

data class LocalPictureResponce (
    var error:String?=null,
    var codigo:String?=null
)