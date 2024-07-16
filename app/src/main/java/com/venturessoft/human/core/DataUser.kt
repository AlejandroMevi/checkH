package com.venturessoft.human.core

import android.app.Application
import com.venturessoft.human.login.data.models.UserEntity

class DataUser: Application() {
    companion object{
        var userData:UserEntity = UserEntity()
        var employeeData:UserEntity = UserEntity()
    }
}