package com.venturessoft.human.login.data.models

import com.venturessoft.human.models.response.BeaconsItem

data class EmployeResponse(
    val codigo: String? = null,
    val foto: String? = null,
    val fechaFoto: String? = null,
    val numeroCompania: String? = null,
    val errorMessage: String? = null,
    val error: Boolean? = null,
    val nombre: String? = null,
    val status: String? = null,
    val beacons: List<BeaconsItem>? = null,
    val palabraClave:String? = null,
    val fotoLocal:Boolean? = null,
    val statusFoto:String? = null,
    val fotoValida:String? = null
)