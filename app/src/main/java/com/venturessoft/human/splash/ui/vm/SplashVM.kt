package com.venturessoft.human.splash.ui.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.User
import com.venturessoft.human.core.DataUser
import com.venturessoft.human.login.data.local.LoginRepoLocal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashVM @Inject constructor(
    private val localRepo: LoginRepoLocal
): ViewModel() {
    var getExistEmploye = MutableLiveData<Boolean>()
        private set
    init {
        getUserLastTime()
    }
    private fun getUserLastTime(){
        viewModelScope.launch {
            val userData = localRepo.getSavedUser()
            if(userData != null){
                val dataUser = localRepo.gettUser(userData.employeeId.toString(), userData.company)
                if (dataUser != null){
                    DataUser.userData = dataUser
                }
                getExistEmploye.value = dataUser != null
            }else{
                getExistEmploye.value = false
            }
        }
    }
}