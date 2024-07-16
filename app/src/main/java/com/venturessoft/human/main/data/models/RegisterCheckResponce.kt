package com.venturessoft.human.main.data.models

data class RegisterCheckResponce(
    var root: Root
)

data class Root(
    var codigo: String,
    var estacion: String
)
