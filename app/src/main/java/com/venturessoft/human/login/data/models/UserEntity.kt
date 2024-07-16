package com.venturessoft.human.login.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "user_table")
data class UserEntity (
    @PrimaryKey
    var employeeId:Long = 0,
    var employeeIdColab:String = "",
    var company:String = "",
    var companyId:Int = 0,
    var tipoUsuario:String = "Base",
    var name:String = "",
    var nameColab:String = "",
    var photoUrl:String = "",
    var photoBase64:String = "",
    var priority:Int = 3,
    var priorityColab:Int = 0,
    var beacon:ArrayList<String> = arrayListOf(),
    var ultimaChecada:String = "",
    var slavee:Boolean = false,
    var deviceType:String = "",
    var palabraClave:String = "",
    var fotoLocal:Boolean = false,
    var fotoValidaActual:String = "",
    var fotoValidaPendiente:String = "",
    var statusFoto:String = "",
    var localPictureUriActual:String = "",
    var localPictureUriPending:String = "",
    var urlAvisoPriv: String = ""
)
@Entity(tableName = "user_saved_table")
data class UserSavedEntity (
    @PrimaryKey
    val employeeId:Long = 0,
    val company: String = ""
)
@Entity(tableName = "user_key_word")
data class KeyWordEntity (
    @PrimaryKey
    val employeeId:Long = 0,
    val keyword: String = ""
)

