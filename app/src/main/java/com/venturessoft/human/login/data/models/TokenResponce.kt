package com.venturessoft.human.login.data.models

data class TokenResponse (
    var user:String,
    var token:String,
    var tokenLiveness:String,
    var numCia:String,
    var codigo:String,
    var error:Boolean,
    var palabraClave:String? = null
)