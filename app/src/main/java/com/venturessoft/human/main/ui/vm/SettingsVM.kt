package com.venturessoft.human.main.ui.vm

import android.annotation.SuppressLint
import android.net.Uri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venturessoft.human.core.ApiResponceStatus
import com.venturessoft.human.core.DataUser
import com.venturessoft.human.core.utils.Constants
import com.venturessoft.human.core.utils.Constants.Companion.ERROR
import com.venturessoft.human.core.utils.Constants.Companion.ERRORHTTP
import com.venturessoft.human.core.utils.Constants.Companion.FACE_NO_DETECTED
import com.venturessoft.human.core.utils.Constants.Companion.PENDING
import com.venturessoft.human.core.utils.Constants.Companion.SUCCES
import com.venturessoft.human.core.utils.Constants.Companion.TIMEOUT
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.core.utils.Utilities.Companion.deleteFile
import com.venturessoft.human.main.data.local.MainRepoLocal
import com.venturessoft.human.main.data.models.*
import com.venturessoft.human.main.data.net.MainRepoNet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsVM @Inject constructor(
    private val mainRepoNet: MainRepoNet,
    private var mainRepoLocal: MainRepoLocal
) : ViewModel() {
    var statusDataOffline = MutableLiveData<ApiResponceStatus<Any>?>(null)
        private set
    private val listdataOffline = mutableListOf<SuccesModel>()
    private var listIndex = MutableLiveData<Int?>(null)
    fun getOfflineServices(lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            statusDataOffline.value = ApiResponceStatus.Loading()
            val resultGetToken = mainRepoNet.funPostAutenticacion(DataUser.userData.employeeId.toString(), DataUser.userData.companyId.toString())
            if (resultGetToken is ApiResponceStatus.Success) {
                getServicesOffline(resultGetToken.data.token, lifecycleOwner)
            } else {
                statusDataOffline.value = resultGetToken as ApiResponceStatus<Any>
            }
        }
    }
    private fun getServicesOffline(token: String, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            listdataOffline.addAll(mainRepoLocal.getServicesOffline())
            if (listdataOffline.isNotEmpty()) {
                listIndex.observe(lifecycleOwner) { idexList ->
                    if (idexList != null) {
                        if (listdataOffline[idexList].statusOffline == PENDING) {
                            funFaceDetection(listdataOffline[idexList],listdataOffline[idexList].foto, token, listdataOffline.size == idexList + 1, lifecycleOwner)
                        } else {
                            evaluatePosition(listdataOffline.size == idexList + 1, lifecycleOwner)
                        }
                    }
                }
                listIndex.value = 0
            } else {
                statusDataOffline.value = ApiResponceStatus.Success("")
            }
        }
    }
    private fun funFaceDetection(dataOffline: SuccesModel, picture: String, token: String, isLast: Boolean, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            val faceDetectionRequest = FaceDetectionRequest(Utilities.uriToBase64(Uri.parse(picture)))
            val resultFacceTime = mainRepoNet.funFaceDetectionBoolean(faceDetectionRequest)
            if (resultFacceTime is ApiResponceStatus.Success) {
                if (resultFacceTime.data.data.result.lowercase() == "genuine") {
                    funPostRegisterCheck(dataOffline, token, picture,isLast, lifecycleOwner)
                } else {
                    errortype(dataOffline, isLast, lifecycleOwner,picture)
                }
            } else {
                if ((resultFacceTime as ApiResponceStatus.Error).messageId.contains(ERRORHTTP)) {
                    funPostRegisterCheck(dataOffline, token, picture,isLast, lifecycleOwner)
                }else{
                    evaluatePosition(isLast, lifecycleOwner)
                }
            }
        }
    }
    private fun errortype(dataOffline: SuccesModel, isLast: Boolean, lifecycleOwner: LifecycleOwner, picture: String) {
        deleteFile(picture)
        dataOffline.statusOffline = ERROR
        dataOffline.station = FACE_NO_DETECTED
        updateStatus(dataOffline, isLast, lifecycleOwner)
    }
    @SuppressLint("SimpleDateFormat")
    private fun funPostRegisterCheck(dataOffline: SuccesModel, token: String, picture: String,isLast: Boolean, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            val registerCheckModel = RootRegister()
            registerCheckModel.sGeolocalizacion = dataOffline.sGeolocalizacion
            registerCheckModel.sBeacons = dataOffline.sBeacons
            registerCheckModel.sBssids = dataOffline.sBssids
            registerCheckModel.numeroCompania = dataOffline.numeroCompania
            registerCheckModel.empleado = dataOffline.empleado
            registerCheckModel.fechaHoraChecada = dataOffline.fechaHoraChecada
            registerCheckModel.tipoChecada = dataOffline.tipoChecada
            registerCheckModel.foto = Utilities.uriToBase64(Uri.parse(picture))
            registerCheckModel.prioridad = dataOffline.prioridad
            if (DataUser.userData.fotoLocal){
                registerCheckModel.fotoModelo = Utilities.uriToBase64(Uri.parse(DataUser.userData.localPictureUriActual))
                registerCheckModel.token = DataUser.userData.fotoValidaActual
            }
            val resultRegisterCheck = if (DataUser.userData.fotoLocal){
                mainRepoNet.funPostRegisterCheck2(RegisterCheckModel(registerCheckModel), token)
            }else{
                mainRepoNet.funPostRegisterCheck(RegisterCheckModel(registerCheckModel), token)
            }
            if (resultRegisterCheck is ApiResponceStatus.Success) {
                if (resultRegisterCheck.data.root.codigo == Constants.ET000) {
                    dataOffline.statusOffline = SUCCES
                } else {
                    dataOffline.statusOffline = ERROR
                }
                deleteFile(picture)
                dataOffline.station = resultRegisterCheck.data.root.codigo
                updateStatus(dataOffline, isLast, lifecycleOwner)
            } else {
                if (!(resultRegisterCheck as ApiResponceStatus.Error).messageId.contains(TIMEOUT) || !resultRegisterCheck.messageId.contains(ERRORHTTP)) {
                    deleteFile(picture)
                    dataOffline.station = resultRegisterCheck.messageId
                    dataOffline.statusOffline = ERROR
                }
                updateStatus(dataOffline, isLast, lifecycleOwner)
            }
        }
    }
    private fun updateStatus(dataOffline: SuccesModel, isLast: Boolean, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            mainRepoLocal.updateUser(dataOffline)
            evaluatePosition(isLast, lifecycleOwner)
        }
    }
    private fun evaluatePosition(isLast: Boolean, lifecycleOwner: LifecycleOwner) {
        if (isLast) {
            listIndex.removeObservers(lifecycleOwner)
            listIndex.value = null
            statusDataOffline.value = ApiResponceStatus.Success("")
        } else {
            listIndex.value = listIndex.value?.plus(1)
        }
    }
}