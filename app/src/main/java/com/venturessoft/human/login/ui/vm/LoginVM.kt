package com.venturessoft.human.login.ui.vm

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venturessoft.human.core.ApiResponceStatus
import com.venturessoft.human.core.DataUser
import com.venturessoft.human.core.utils.Constants.Companion.ERRORHTTP
import com.venturessoft.human.core.utils.Constants.Companion.ERRORUSERLOCAL
import com.venturessoft.human.core.utils.Constants.Companion.TIMEOUT
import com.venturessoft.human.core.utils.Preferences
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.core.utils.Utilities.Companion.deleteAllFiles
import com.venturessoft.human.core.utils.Utilities.Companion.obtenerBeacons
import com.venturessoft.human.core.utils.Utilities.Companion.saveImageToStorage
import com.venturessoft.human.login.data.local.LoginRepoLocal
import com.venturessoft.human.login.data.models.TokenResponse2Model
import com.venturessoft.human.login.data.models.UserEntity
import com.venturessoft.human.login.data.models.UserSavedEntity
import com.venturessoft.human.login.ui.activitys.LoginActivity.Companion.dataConnection
import com.venturessoft.human.main.data.net.MainRepoNet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginVM @Inject constructor(
    private val mainRepoNet: MainRepoNet,
    private val loginRepoLocal: LoginRepoLocal
) : ViewModel() {
    var statusData = MutableLiveData<ApiResponceStatus<Any>>(null)
        private set
    var getExistEmploye = MutableLiveData<Boolean>(null)
        private set
    var dataPolitica = MutableLiveData<TokenResponse2Model>(null)
        private set

    fun funGetToken(activity: Activity, user: String, cia: String,tokenFirebase: String) {
        viewModelScope.launch {
            statusData.value = ApiResponceStatus.Loading()
            val resultGetToken = mainRepoNet.funGetToken(user, cia)
            if (resultGetToken is ApiResponceStatus.Success) {
                funGetToken2(activity, user, resultGetToken.data.numCia, cia, tokenFirebase)
            } else {
                resultGetToken as ApiResponceStatus.Error
                if (dataConnection.value == false) {
                    funGetEmployeDB(user, cia, ApiResponceStatus.Error(ERRORUSERLOCAL))
                } else {
                    if (resultGetToken.messageId.contains(TIMEOUT)) {
                        funGetEmployeDB(user, cia, ApiResponceStatus.Error(TIMEOUT))
                    } else if (resultGetToken.messageId.contains(ERRORHTTP)) {
                        funGetEmployeDB(user, cia, ApiResponceStatus.Error(ERRORHTTP))
                    } else {
                        statusData.value = resultGetToken as ApiResponceStatus<Any>
                    }
                }
            }
        }
    }

    private fun funGetToken2(activity: Activity, user: String, numCia: String, nameCia: String,tokenFirebase: String) {
        viewModelScope.coroutineContext.cancelChildren()
        viewModelScope.launch {
            val result = mainRepoNet.funGetToken2(DataUser.userData.companyId)
            if (result is ApiResponceStatus.Success) {
                  if (Preferences().getAviso(activity) == null) {
                      dataPolitica.value = result.data
                      statusData.value = result as ApiResponceStatus<Any>
                  }else{
                      funGetEmploye(activity, user, numCia, nameCia,tokenFirebase)
                  }
            } else {
                result as ApiResponceStatus.Error
                if (dataConnection.value == false) {
                    funGetEmployeDB(user, nameCia, ApiResponceStatus.Error(ERRORUSERLOCAL))
                } else {
                    if (result.messageId.contains(TIMEOUT)) {
                        funGetEmployeDB(user, nameCia, ApiResponceStatus.Error(TIMEOUT))
                    } else if (result.messageId.contains(ERRORHTTP)) {
                        funGetEmployeDB(user, nameCia, ApiResponceStatus.Error(ERRORHTTP))
                    } else {
                        statusData.value = result as ApiResponceStatus<Any>
                    }
                }
            }
        }
    }
    private fun funGetEmploye(activity: Activity, user: String, numCia: String, nameCia: String,tokenFirebase: String) {
        viewModelScope.launch {
            val resultGetEmploye = mainRepoNet.funGetEmploye(numCia, user,tokenFirebase)
            if (resultGetEmploye is ApiResponceStatus.Success) {
                deleteAllFiles(activity, user)
                val userEntity = UserEntity(
                    name = resultGetEmploye.data.nombre ?: "",
                    employeeId = user.toLong(),
                    company = nameCia,
                    companyId = resultGetEmploye.data.numeroCompania?.toInt() ?: -1,
                    beacon = obtenerBeacons(resultGetEmploye.data.beacons),
                    photoUrl = if (resultGetEmploye.data.foto.isNullOrEmpty()) "" else saveImageToStorage(
                        activity,
                        Utilities.base64ToBitmap(resultGetEmploye.data.foto),
                        user,
                        resultGetEmploye.data.nombre ?: ""
                    ),
                    palabraClave = if (!resultGetEmploye.data.palabraClave.isNullOrEmpty()) resultGetEmploye.data.palabraClave else "",
                    fotoLocal = resultGetEmploye.data.fotoLocal ?: false,
                )
                DataUser.userData = userEntity
                loginRepoLocal.insertUser(userEntity)
                loginRepoLocal.insertSavedUser(UserSavedEntity(user.toLong(), nameCia))
                getExistEmploye.value = true
            } else {
                resultGetEmploye as ApiResponceStatus.Error
                if (dataConnection.value == false) {
                    funGetEmployeDB(user, nameCia, ApiResponceStatus.Error(ERRORUSERLOCAL))
                } else {
                    if (resultGetEmploye.messageId.contains(TIMEOUT)) {
                        funGetEmployeDB(user, nameCia, ApiResponceStatus.Error(TIMEOUT))
                    } else if (resultGetEmploye.messageId.contains(ERRORHTTP)) {
                        funGetEmployeDB(user, nameCia, ApiResponceStatus.Error(ERRORHTTP))
                    } else {
                        statusData.value = resultGetEmploye as ApiResponceStatus<Any>
                    }
                }
            }
        }
    }

    private fun funGetEmployeDB(user: String, cia: String, status: ApiResponceStatus<Any>) {
        viewModelScope.launch {
            val userData = loginRepoLocal.gettUser(user, cia)
            if (userData != null) {
                DataUser.userData = userData
                loginRepoLocal.insertSavedUser(
                    UserSavedEntity(
                        userData.employeeId,
                        userData.company
                    )
                )
                getExistEmploye.value = true
            } else {
                getExistEmploye.value = false
                statusData.value = status
            }
        }
    }

    fun funGetEmployeDBLocal(activity: Activity, user: String, cia: String,tokenFirebase: String) {
        viewModelScope.launch {
            val userData = loginRepoLocal.gettUser(user, cia)
            if (userData != null) {
                DataUser.userData = userData
                loginRepoLocal.insertSavedUser(
                    UserSavedEntity(
                        userData.employeeId,
                        userData.company
                    )
                )
                getExistEmploye.value = true
            } else {
                funGetToken(activity, user, cia,tokenFirebase)
            }
        }
    }
}
