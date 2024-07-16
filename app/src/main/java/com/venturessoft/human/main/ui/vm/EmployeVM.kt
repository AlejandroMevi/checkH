package com.venturessoft.human.main.ui.vm

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venturessoft.human.R
import com.venturessoft.human.core.ApiInterceptor
import com.venturessoft.human.core.ApiResponceStatus
import com.venturessoft.human.core.BaseActivity.Companion.dataBSSIDConected
import com.venturessoft.human.core.BaseActivity.Companion.dataBSSIDList
import com.venturessoft.human.core.BaseActivity.Companion.tokenFirebase
import com.venturessoft.human.core.DataUser
import com.venturessoft.human.core.DataUser.Companion.employeeData
import com.venturessoft.human.core.utils.Constants
import com.venturessoft.human.core.utils.Constants.Companion.ERRORHTTP
import com.venturessoft.human.core.utils.Constants.Companion.ET000
import com.venturessoft.human.core.utils.Constants.Companion.OFFLINE
import com.venturessoft.human.core.utils.Constants.Companion.ONLINE
import com.venturessoft.human.core.utils.Constants.Companion.PENDING
import com.venturessoft.human.core.utils.Constants.Companion.STATE_OFFLINE
import com.venturessoft.human.core.utils.Constants.Companion.TIMEOUT
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.core.utils.Utilities.Companion.getDateTime
import com.venturessoft.human.core.utils.Utilities.Companion.obtenerBeacons
import com.venturessoft.human.login.data.local.LoginRepoLocal
import com.venturessoft.human.login.data.models.UserEntity
import com.venturessoft.human.main.data.local.MainRepoLocal
import com.venturessoft.human.main.data.models.FaceDetectionRequest
import com.venturessoft.human.main.data.models.MovementResponse
import com.venturessoft.human.main.data.models.RegisterCheckModel
import com.venturessoft.human.main.data.models.RootRegister
import com.venturessoft.human.main.data.models.SuccesModel
import com.venturessoft.human.main.data.models.TimeZoneResponce
import com.venturessoft.human.main.data.net.MainRepoNet
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.dataToken
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.statusNet
import com.venturessoft.human.main.ui.fragments.main.EmplyeeColabFragment.Companion.checkTypeColab
import com.venturessoft.human.main.ui.fragments.main.EmplyeeColabFragment.Companion.dataSucces
import com.venturessoft.human.main.ui.fragments.main.EmplyeeColabFragment.Companion.mainFaceDetectionColab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeVM @Inject constructor(private val mainRepoNet: MainRepoNet, private val loginRepoLocal: LoginRepoLocal, private var mainRepoLocal: MainRepoLocal) : ViewModel() {
    private var listBSSIDColab = mutableListOf<String>()
    var isRegisterCheck = MutableLiveData<Boolean?>(null)
        private set
    var statusDataColab = MutableLiveData<ApiResponceStatus<Any>?>(null)
        private set
    var dataUserColab = MutableLiveData<UserEntity?>(null)
        private set
    var dataMovementColab = MutableLiveData<MovementResponse?>(null)
        private set
    private var dataTimeZoneColab = MutableLiveData<TimeZoneResponce?>(null)
    fun funGetMovement(activity: Activity) {
        viewModelScope.launch {
            if (ApiInterceptor.existToken()) {
                val resultGetMovement = mainRepoNet.funGetMovement(employeeData.companyId, employeeData.employeeId, employeeData.priority)
                if (resultGetMovement is ApiResponceStatus.Success) {
                    dataMovementColab.value = resultGetMovement.data
                }
                funGetEmploye(activity)
            }else{
                viewModelScope.launch {
                    val responceToken = mainRepoNet.funGetToken(employeeData.employeeId.toString(), employeeData.company)
                    if (responceToken is ApiResponceStatus.Success) {
                        funGetMovement(activity)
                    }
                }
            }
        }
    }
    private fun funGetTokenUnit(unit:()->Unit, unitError: (() -> Unit)?) {
        viewModelScope.launch {
            val responceToken = mainRepoNet.funGetToken(employeeData.employeeId.toString(), employeeData.company)
            if (responceToken is ApiResponceStatus.Success) {
                unit.invoke()
            }else{
                responceToken as ApiResponceStatus.Error
                if (responceToken.messageId.contains(TIMEOUT) || responceToken.messageId.contains(ERRORHTTP)) {
                    unitError?.invoke()
                }else{
                    statusDataColab.value = ApiResponceStatus.Success("")
                }
            }
        }
    }
    private fun funGetTokenCheckUnit(unit:()->Unit, unitError: (() -> Unit)?) {
        viewModelScope.launch {
            val responceToken = mainRepoNet.funPostAutenticacion(DataUser.userData.employeeId.toString(), DataUser.userData.companyId.toString())
            if (responceToken is ApiResponceStatus.Success) {
                dataToken = responceToken.data.token
                unit.invoke()
            }else{
                responceToken as ApiResponceStatus.Error
                if (responceToken.messageId.contains(TIMEOUT) || responceToken.messageId.contains(ERRORHTTP)) {
                    unitError?.invoke()
                }else{
                    statusDataColab.value = ApiResponceStatus.Success("")
                }
            }
        }
    }
    private fun funGetEmploye(activity: Activity) {
        viewModelScope.launch {
            val resultGetEmploye = mainRepoNet.funGetEmploye(employeeData.companyId.toString(), employeeData.employeeId.toString(), tokenFirebase)
            if (resultGetEmploye is ApiResponceStatus.Success) {
                Utilities.deleteAllFiles(activity, employeeData.employeeId.toString())
                val userEntity = UserEntity(
                    tipoUsuario = "Base",
                    name = resultGetEmploye.data.nombre?:"",
                    employeeId = employeeData.employeeId.toString().toLong(),
                    company = employeeData.company,
                    companyId = resultGetEmploye.data.numeroCompania?.toInt()?:-1,
                    photoUrl = Utilities.saveImageToStorage(activity, Utilities.base64ToBitmap(resultGetEmploye.data.foto?:""), employeeData.companyId.toString(), resultGetEmploye.data.nombre?:""),
                    priority = 3,
                    beacon = obtenerBeacons(resultGetEmploye.data.beacons),
                    palabraClave = if (!resultGetEmploye.data.palabraClave.isNullOrEmpty()) resultGetEmploye.data.palabraClave else ""
                )
                if(employeeData.name != userEntity.name || employeeData.companyId != userEntity.companyId || employeeData.beacon !=  userEntity.beacon){
                    employeeData = userEntity
                    loginRepoLocal.insertUser(userEntity)
                }
            }
        }
    }
    fun funFaceDetection(activity: Activity, registerCheckModel: RootRegister){
        viewModelScope.launch {
            statusDataColab.value = ApiResponceStatus.Loading()
            if (ApiInterceptor.existToken()) {
                val resultFacceTime = if (registerCheckModel.tipoChecada == "E"){
                    if (DataUser.userData.companyId.toString() == "10101" || DataUser.userData.companyId.toString() == "1038" ||  DataUser.userData.companyId.toString()=="15" || DataUser.userData.companyId.toString() == "721"){
                        mainRepoNet.funFaceDetection(mainFaceDetectionColab)
                    }else{
                        mainRepoNet.funGetTimeToken()
                    }
                }else{
                    mainRepoNet.funGetTimeToken()
                }
                if (resultFacceTime is ApiResponceStatus.Success) {
                    dataTimeZoneColab.value = resultFacceTime.data.timeZone
                    dataToken = resultFacceTime.data.autentication.token
                    funPostRegisterCheck(activity, registerCheckModel)
                } else {
                    resultFacceTime as ApiResponceStatus.Error
                    if (statusNet.value == false || resultFacceTime.messageId.contains(TIMEOUT) || resultFacceTime.messageId.contains(ERRORHTTP)){
                        val dateTime = getDateTime(null)
                        goToSucces(activity, OFFLINE, STATE_OFFLINE, dateTime[0], dateTime[1], registerCheckModel)
                    }else{
                        statusDataColab.value = resultFacceTime as ApiResponceStatus<Any>
                    }
                }
            }else{
                val dateTime = getDateTime(null)
                funGetTokenUnit({funFaceDetection(activity, registerCheckModel) }, {   goToSucces(activity, OFFLINE, STATE_OFFLINE, dateTime[0], dateTime[1], registerCheckModel) })
            }
        }
    }
    @SuppressLint("SimpleDateFormat")
    private fun funPostRegisterCheck(activity: Activity,registerCheckModel:RootRegister) {
        if (dataToken.isNotEmpty()){
            viewModelScope.launch {
                val dateTime = getDateTime(dataTimeZoneColab.value)
                registerCheckModel.fechaHoraChecada = "${dateTime[0]}T${dateTime[1]}"
                val resultRegisterCheck = if (DataUser.userData.fotoLocal){
                    mainRepoNet.funPostRegisterCheck2(RegisterCheckModel(registerCheckModel), dataToken)
                }else{
                    mainRepoNet.funPostRegisterCheck(RegisterCheckModel(registerCheckModel), dataToken)
                }
                if (resultRegisterCheck is ApiResponceStatus.Success) {
                    if (resultRegisterCheck.data.root.codigo == ET000) {
                        goToSucces(activity, ONLINE, resultRegisterCheck.data.root.estacion, dateTime[0], dateTime[1],registerCheckModel)
                    } else {
                        statusDataColab.value = ApiResponceStatus.Error(resultRegisterCheck.data.root.codigo)
                    }
                } else {
                    resultRegisterCheck as ApiResponceStatus.Error
                    if (statusNet.value == false || resultRegisterCheck.messageId.contains(TIMEOUT) || resultRegisterCheck.messageId.contains(ERRORHTTP)) {
                        goToSucces(activity, OFFLINE, STATE_OFFLINE, dateTime[0], dateTime[1], registerCheckModel)
                    } else {
                        statusDataColab.value = resultRegisterCheck as ApiResponceStatus<Any>
                    }
                }
            }
        }else{
            val dateTime = getDateTime(null)
            funGetTokenCheckUnit({ funPostRegisterCheck(activity,registerCheckModel) }, { goToSucces(activity, OFFLINE, STATE_OFFLINE, dateTime[0], dateTime[1], registerCheckModel) })
        }
    }
    private fun goToSucces(activity: Activity, connection: String, station: String, date: String, time: String, registerCheckModel:RootRegister) {
        dataBSSIDConected.value?.bssid?.let { fisrtBssid->
            listBSSIDColab.add(fisrtBssid)
        }
        dataBSSIDList.value?.forEach {
            listBSSIDColab.add(it.bssid)
        }
        val succes = SuccesModel()
        succes.connection = connection
        succes.station = station
        succes.date = date
        succes.time = time
        succes.typeofCheck = checkTypeColab
        succes.photoEmployee = registerCheckModel.foto
        succes.statusOffline = Constants.SUCCES
        succes.sGeolocalizacion = registerCheckModel.sGeolocalizacion
        succes.sBeacons = registerCheckModel.sBeacons
        succes.sBssids = registerCheckModel.sBssids
        succes.numeroCompania = registerCheckModel.numeroCompania
        succes.empleado = registerCheckModel.empleado
        succes.fechaHoraChecada = registerCheckModel.fechaHoraChecada
        succes.tipoChecada = registerCheckModel.tipoChecada
        succes.foto = registerCheckModel.foto
        succes.prioridad = registerCheckModel.prioridad
        dataSucces = succes
        if (connection == OFFLINE) {
            viewModelScope.launch {
                succes.statusOffline = PENDING
                succes.fechaHoraChecada = "${date}T$time"
                succes.foto = Utilities.saveImageToStorage(activity, Utilities.base64ToBitmap(mainFaceDetectionColab.image), "Offline${DataUser.userData.employeeId}", "Offline${DataUser.userData.employeeId}")
                mainRepoLocal.insertServicesOffline(succes)
                statusDataColab.value = ApiResponceStatus.Success("")
            }
        }
        mainFaceDetectionColab = FaceDetectionRequest()
        dataToken = ""
        dataTimeZoneColab.value = null
        isRegisterCheck.value = true
    }
    private fun funGetEmployees(activity: Activity) {
        viewModelScope.launch {
            statusDataColab.value = ApiResponceStatus.Loading()
            if (ApiInterceptor.existToken()) {
                val resultGetEmployees = mainRepoNet.funGetEmployees(employeeData.companyId.toString())
                if (resultGetEmployees is ApiResponceStatus.Success) {
                    resultGetEmployees.data.result?.forEachIndexed { index, employeeresult ->
                        if (employeeresult.numEmployee.isNotEmpty()) {
                            val userData = loginRepoLocal.gettUser(employeeresult.numEmployee, employeeData.company)
                            if (userData == null) {
                                Utilities.deleteAllFiles(activity, employeeresult.numEmployee)
                                val userEntity = UserEntity(
                                    tipoUsuario = "Base",
                                    name = employeeresult.nameFull,
                                    employeeId = employeeresult.numEmployee.toLong(),
                                    company = employeeData.company,
                                    companyId = employeeData.companyId,
                                    priority = 3,
                                    beacon = obtenerBeacons(arrayListOf())
                                )
                                userEntity.photoUrl = Utilities.saveImageToStorage(
                                    activity,
                                    Utilities.base64ToBitmap(employeeresult.photo),
                                    employeeresult.numEmployee,
                                    employeeresult.nameFull
                                )
                                loginRepoLocal.insertUser(userEntity)
                            }
                        }
                        if (resultGetEmployees.data.result?.size == index + 1) {
                            Toast.makeText(activity,activity.getString(R.string.success),Toast.LENGTH_SHORT).show()
                            statusDataColab.value = resultGetEmployees as ApiResponceStatus<Any>
                        }
                    }
                } else {
                    statusDataColab.value = resultGetEmployees as ApiResponceStatus<Any>
                }
            }else{
                funGetTokenEmployees(employeeData.employeeId.toString(), employeeData.company, activity)
            }
        }
    }
    private fun funGetTokenEmployees(user: String, cia: String, activity: Activity) {
        viewModelScope.launch {
            val resultGetTokenCollaborato = mainRepoNet.funGetToken(user, cia)
            if (resultGetTokenCollaborato is ApiResponceStatus.Success) {
                funGetEmployees(activity)
            } else {
                resultGetTokenCollaborato as ApiResponceStatus.Error
                if (resultGetTokenCollaborato.messageId.contains(TIMEOUT)){
                    statusDataColab.value = ApiResponceStatus.Error(TIMEOUT)
                }else if (resultGetTokenCollaborato.messageId.contains(ERRORHTTP)){
                    statusDataColab.value = ApiResponceStatus.Error(ERRORHTTP)
                }else{
                    statusDataColab.value = resultGetTokenCollaborato as ApiResponceStatus<Any>
                }
            }
        }
    }
}
