package com.venturessoft.human.main.data.models

import com.google.gson.annotations.SerializedName
class ResponseGeneral<T>{
    @SerializedName("codigo")
    var codigo: String? = ""
    @SerializedName("error")
    var error: Boolean? = false
    @SerializedName("result")
    var result: T? = null
}