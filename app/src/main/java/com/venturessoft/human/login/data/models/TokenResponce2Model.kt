package com.venturessoft.human.login.data.models

data class TokenResponse2Model (
    var token:String?= null,
    var error:Boolean?= null,
    var codigo:String?= null,
    var claveCompania:String?= null,
    var tokenLiveness:String?= null,
    var numCia:String?= null,
    var fotoLocal:String?=null,
    var urlAvisoPrivacidad:String? = null
)