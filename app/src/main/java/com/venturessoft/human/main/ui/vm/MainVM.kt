package com.venturessoft.human.main.ui.vm

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.FragmentManager
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
import com.venturessoft.human.core.DataUser.Companion.userData
import com.venturessoft.human.core.utils.Constants.Companion.ERRORHTTP
import com.venturessoft.human.core.utils.Constants.Companion.ERRORUSERLOCAL
import com.venturessoft.human.core.utils.Constants.Companion.ET000
import com.venturessoft.human.core.utils.Constants.Companion.OFFLINE
import com.venturessoft.human.core.utils.Constants.Companion.ONLINE
import com.venturessoft.human.core.utils.Constants.Companion.PENDING
import com.venturessoft.human.core.utils.Constants.Companion.STATE_OFFLINE
import com.venturessoft.human.core.utils.Constants.Companion.SUCCES
import com.venturessoft.human.core.utils.Constants.Companion.TIMEOUT
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.core.utils.Utilities.Companion.getDateTime
import com.venturessoft.human.core.utils.Utilities.Companion.obtenerBeacons
import com.venturessoft.human.login.data.local.LoginRepoLocal
import com.venturessoft.human.login.data.models.KeyWordEntity
import com.venturessoft.human.login.data.models.UserEntity
import com.venturessoft.human.login.data.models.UserSavedEntity
import com.venturessoft.human.main.data.local.MainRepoLocal
import com.venturessoft.human.main.data.models.EmployeeOfflineResponce
import com.venturessoft.human.main.data.models.FaceDetectionRequest
import com.venturessoft.human.main.data.models.MovementResponse
import com.venturessoft.human.main.data.models.RegisterCheckModel
import com.venturessoft.human.main.data.models.RootRegister
import com.venturessoft.human.main.data.models.SuccesModel
import com.venturessoft.human.main.data.models.TimeZoneResponce
import com.venturessoft.human.main.data.net.MainRepoNet
import com.venturessoft.human.main.ui.activitys.PrincipalActivity
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.dataToken
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.statusNet
import com.venturessoft.human.main.ui.fragments.main.MainFragment
import com.venturessoft.human.main.ui.fragments.main.MainFragment.Companion.dataSucces
import com.venturessoft.human.main.ui.fragments.main.MainFragment.Companion.mainFaceDetection
import com.venturessoft.human.pictureLocal.LocalPictureActivity.Companion.isLogin
import com.venturessoft.human.pictureLocal.data.models.LocalPictureRquest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainVM @Inject constructor(
    private val mainRepoNet: MainRepoNet,
    private val loginRepoLocal: LoginRepoLocal,
    private var mainRepoLocal: MainRepoLocal
) : ViewModel() {
    var isRegisterCheck = MutableLiveData<Boolean?>(null)
        private set
    var statusData = MutableLiveData<ApiResponceStatus<Any>?>(null)
        private set
    var dataMovement = MutableLiveData<MovementResponse?>(null)
        private set
    var dataServicesOffline = MutableLiveData<List<SuccesModel>?>(null)
        private set
    var dataExistUser = MutableLiveData(false)
        private set
    private var listBSSID = mutableListOf<String>()
    private var dataTimeZone = MutableLiveData<TimeZoneResponce?>(null)
    var dataEmployees = MutableLiveData<List<UserEntity>?>(null)
    fun getUserLastTime() {
        viewModelScope.launch {
            val userData = loginRepoLocal.getSavedUser()
            if (userData != null) {
                val dataUser =
                    loginRepoLocal.gettUser(userData.employeeId.toString(), userData.company)
                if (dataUser != null) {
                    DataUser.userData = dataUser
                    dataExistUser.value = true
                } else {
                    dataExistUser.value = false
                }
            } else {
                dataExistUser.value = false
            }
        }
    }

    fun funGetToken() {
        viewModelScope.coroutineContext.cancelChildren()
        viewModelScope.launch {
            val result = mainRepoNet.funGetToken2(userData.companyId)
            if (result is ApiResponceStatus.Success) {
                if (!result.data.claveCompania.isNullOrEmpty() && result.data.claveCompania != userData.company){
                    userData.company = result.data.claveCompania!!
                    loginRepoLocal.insertSavedUser(UserSavedEntity(userData.employeeId,userData.company))
                    loginRepoLocal.insertUser(userData)
                }
            }
        }
    }

    fun funGetEmployeLogin2(activity: Activity, supportFragmentManager: FragmentManager) {
        if (ApiInterceptor.existToken()) {
            viewModelScope.launch {
                var hash = ""
                if (userData.statusFoto.isNotEmpty()){
                    hash = when(userData.statusFoto){
                        "P"-> userData.fotoValidaPendiente
                        "A"-> userData.fotoValidaActual
                        else -> ""
                    }
                }
                val resultGetEmploye = mainRepoNet.funGetEmploye(
                    userData.companyId.toString(),
                    userData.employeeId.toString(),
                    tokenFirebase,
                    hash
                )
                if (resultGetEmploye is ApiResponceStatus.Success) {
                    val userEntity = userData
                    userEntity.name = resultGetEmploye.data.nombre ?: ""
                    userEntity.employeeId = userData.employeeId
                    userEntity.company = userData.company
                    userEntity.companyId = resultGetEmploye.data.numeroCompania?.toInt() ?: -1
                    userEntity.beacon = obtenerBeacons(resultGetEmploye.data.beacons)
                    userEntity.photoUrl =
                        if (resultGetEmploye.data.foto.isNullOrEmpty()) "" else Utilities.saveImageToStorage(
                            activity,
                            Utilities.base64ToBitmap(resultGetEmploye.data.foto),
                            userData.employeeId.toString(),
                            resultGetEmploye.data.nombre ?: ""
                        )
                    userEntity.palabraClave = if (!resultGetEmploye.data.palabraClave.isNullOrEmpty()) resultGetEmploye.data.palabraClave else userData.palabraClave
                    userEntity.fotoLocal = resultGetEmploye.data.fotoLocal ?: userData.fotoLocal
                    if (resultGetEmploye.data.fotoLocal == true) {
                        if (resultGetEmploye.data.statusFoto != userData.statusFoto) {
                            if (resultGetEmploye.data.statusFoto == "A" || resultGetEmploye.data.statusFoto == "R") {
                                if (resultGetEmploye.data.statusFoto == "A") {
                                    userEntity.localPictureUriActual = userData.localPictureUriPending
                                    userEntity.fotoValidaActual = resultGetEmploye.data.fotoValida ?: ""
                                } else {
                                    Utilities.showErrorDialog("La fotografia ha sido rechazada, le sugerimos que genere una nueva solicitud de foto de enrolamiento", supportFragmentManager)
                                }
                                userEntity.localPictureUriPending = ""
                                userEntity.fotoValidaPendiente = ""
                            }
                            userEntity.statusFoto = resultGetEmploye.data.statusFoto ?: ""
                        }
                    }
                    userData = userEntity
                    loginRepoLocal.insertUser(userEntity)
                }
            }
        }
    }

    fun funGetMovement() {
        viewModelScope.launch {
            if (ApiInterceptor.existToken()) {
                val resultGetMovement = mainRepoNet.funGetMovement(
                    userData.companyId,
                    userData.employeeId,
                    userData.priority
                )
                if (resultGetMovement is ApiResponceStatus.Success) {
                    dataMovement.value = resultGetMovement.data
                }
            } else {
                viewModelScope.launch {
                    val responceToken =
                        mainRepoNet.funGetToken(userData.employeeId.toString(), userData.company)
                    if (responceToken is ApiResponceStatus.Success) {
                        funGetMovement()
                    }
                }
            }
        }
    }

    fun funGetKeyword(palabraClave: String, text: EditText, context: Context) {
        viewModelScope.launch {
            statusData.value = ApiResponceStatus.Loading()
            if (ApiInterceptor.existToken()) {
                val resultGetMovement =
                    mainRepoNet.funGetKeyword(userData.companyId, userData.employeeId, palabraClave)
                if (resultGetMovement is ApiResponceStatus.Success) {
                    val user = userData
                    user.palabraClave = palabraClave
                    loginRepoLocal.insertUser(user)
                    text.setText(palabraClave)
                }
                statusData.value = resultGetMovement as ApiResponceStatus<Any>
            } else {
                viewModelScope.launch {
                    val responceToken =
                        mainRepoNet.funGetToken(userData.employeeId.toString(), userData.company)
                    if (responceToken is ApiResponceStatus.Success) {
                        funGetKeyword(palabraClave, text, context)
                    }
                }
            }
        }
    }

    private fun funGetTokenUnit(unit: () -> Unit, unitError: (() -> Unit)?) {
        viewModelScope.launch {
            val responceToken =
                mainRepoNet.funGetToken(userData.employeeId.toString(), userData.company)
            if (responceToken is ApiResponceStatus.Success) {
                unit.invoke()
            } else {
                responceToken as ApiResponceStatus.Error
                if (responceToken.messageId.contains(TIMEOUT) || responceToken.messageId.contains(
                        ERRORHTTP
                    )
                ) {
                    unitError?.invoke()
                } else {
                    statusData.value = ApiResponceStatus.Success("")
                }
            }
        }
    }

    private fun funGetTokenCheckUnit(unit: () -> Unit, unitError: (() -> Unit)?) {
        viewModelScope.launch {
            val responceToken = mainRepoNet.funPostAutenticacion(
                userData.employeeId.toString(),
                userData.companyId.toString()
            )
            if (responceToken is ApiResponceStatus.Success) {
                dataToken = responceToken.data.token
                unit.invoke()
            } else {
                responceToken as ApiResponceStatus.Error
                if (responceToken.messageId.contains(TIMEOUT) || responceToken.messageId.contains(
                        ERRORHTTP
                    )
                ) {
                    unitError?.invoke()
                } else {
                    statusData.value = ApiResponceStatus.Success("")
                }
            }
        }
    }

    fun funGetEmploye(activity: Activity, user: String, employeVM: EmployeVM) {
        viewModelScope.launch {
            if (statusNet.value == true) {
                statusData.value = ApiResponceStatus.Loading()
                if (ApiInterceptor.existToken()) {
                    val resultGetEmploye =
                        mainRepoNet.funGetEmploye(userData.companyId.toString(), user, tokenFirebase)
                    if (resultGetEmploye is ApiResponceStatus.Success) {
                        Utilities.deleteAllFiles(activity, user)
                        val userEntity = UserEntity(
                            name = resultGetEmploye.data.nombre ?: "",
                            employeeId = user.toLong(),
                            company = userData.company,
                            companyId = resultGetEmploye.data.numeroCompania?.toInt() ?: 0,
                            photoUrl = if (resultGetEmploye.data.foto.isNullOrEmpty()) "" else Utilities.saveImageToStorage(
                                activity,
                                Utilities.base64ToBitmap(resultGetEmploye.data.foto),
                                userData.companyId.toString(),
                                resultGetEmploye.data.nombre ?: ""
                            ),
                            beacon = obtenerBeacons(resultGetEmploye.data.beacons),
                            palabraClave = if (!resultGetEmploye.data.palabraClave.isNullOrEmpty()) resultGetEmploye.data.palabraClave else ""
                        )
                        employeeData = userEntity
                        employeVM.dataUserColab.value = userEntity
                        loginRepoLocal.insertUser(userEntity)
                        statusData.value = resultGetEmploye as ApiResponceStatus<Any>
                    } else {
                        resultGetEmploye as ApiResponceStatus.Error
                        if (resultGetEmploye.messageId.contains(TIMEOUT)) {
                            funGetEmployeDB(
                                user,
                                userData.company,
                                employeVM,
                                ApiResponceStatus.Error(TIMEOUT)
                            )
                        } else if (resultGetEmploye.messageId.contains(ERRORHTTP)) {
                            funGetEmployeDB(
                                user,
                                userData.company,
                                employeVM,
                                ApiResponceStatus.Error(ERRORHTTP)
                            )
                        } else {
                            statusData.value = resultGetEmploye as ApiResponceStatus<Any>
                        }
                    }
                } else {
                    funGetTokenEmploye(
                        { funGetEmploye(activity, user, employeVM) },
                        user,
                        employeVM
                    )
                }
            } else {
                funGetEmployeDB(
                    user,
                    userData.company,
                    employeVM,
                    ApiResponceStatus.Error(ERRORUSERLOCAL)
                )
            }
        }
    }

    private fun funGetTokenEmploye(unit: () -> Unit, user: String, employeVM: EmployeVM) {
        viewModelScope.launch {
            val responceToken =
                mainRepoNet.funGetToken(userData.employeeId.toString(), userData.company)
            if (responceToken is ApiResponceStatus.Success) {
                unit.invoke()
            } else {
                responceToken as ApiResponceStatus.Error
                val errorString = if (responceToken.messageId.contains(TIMEOUT)) {
                    TIMEOUT
                } else if (responceToken.messageId.contains(ERRORHTTP)) {
                    ERRORHTTP
                } else {
                    responceToken.messageId
                }
                funGetEmployeDB(
                    user,
                    userData.company,
                    employeVM,
                    ApiResponceStatus.Error(errorString)
                )
            }
        }
    }

    private fun funGetEmployeDB(
        user: String,
        cia: String,
        employeVM: EmployeVM,
        status: ApiResponceStatus<Any>
    ) {
        viewModelScope.launch {
            val userData = loginRepoLocal.gettUser(user, cia)
            if (userData != null) {
                employeeData = userData
                employeVM.dataUserColab.value = userData
                statusData.value = ApiResponceStatus.Success("")
            } else {
                statusData.value = status
            }
        }
    }

    fun funFaceDetection(activity: Activity, registerCheckModel: RootRegister) {
        viewModelScope.launch {
            if (ApiInterceptor.existToken()) {
                val resultFacceTime = if (registerCheckModel.tipoChecada == "E") {
                    if (userData.companyId.toString() == "10101" || userData.companyId.toString() == "1038" || userData.companyId.toString() == "15" || userData.companyId.toString() == "721") {
                        mainRepoNet.funFaceDetection(mainFaceDetection)
                    } else {
                        mainRepoNet.funGetTimeToken()
                    }
                } else {
                    mainRepoNet.funGetTimeToken()
                }
                if (resultFacceTime is ApiResponceStatus.Success) {
                    dataTimeZone.value = resultFacceTime.data.timeZone
                    dataToken = resultFacceTime.data.autentication.token
                    funPostRegisterCheck(activity, registerCheckModel)
                } else {
                    resultFacceTime as ApiResponceStatus.Error
           /*         if (resultFacceTime.messageId.contains(FAILED)){
                        Log.i("TIMEZONE", "FAILED")
                        funFaceDetection(activity, registerCheckModel)
                    }*/
                    if (statusNet.value == false || resultFacceTime.messageId.contains(TIMEOUT) || resultFacceTime.messageId.contains(ERRORHTTP)) {
                        val dateTime = getDateTime(null)
                        goToSucces(activity, OFFLINE, STATE_OFFLINE, dateTime[0], dateTime[1], registerCheckModel)
                    } else {
                        statusData.value = resultFacceTime as ApiResponceStatus<Any>
                    }
                }
            } else {
                val dateTime = getDateTime(null)
                funGetTokenUnit(
                    { funFaceDetection(activity, registerCheckModel) }, {
                        goToSucces(
                            activity,
                            OFFLINE,
                            STATE_OFFLINE,
                            dateTime[0],
                            dateTime[1],
                            registerCheckModel
                        )
                    })
            }
        }
    }
    @SuppressLint("SimpleDateFormat")
    private fun funPostRegisterCheck(activity: Activity, registerCheckModel: RootRegister) {
        if (dataToken.isNotEmpty()) {
            viewModelScope.launch {
                val dateTime = getDateTime(dataTimeZone.value)
                registerCheckModel.fechaHoraChecada = "${dateTime[0]}T${dateTime[1]}"
                val resultRegisterCheck = if (userData.fotoLocal) {
                    mainRepoNet.funPostRegisterCheck2(
                        RegisterCheckModel(registerCheckModel),
                        dataToken
                    )
                } else {
                    mainRepoNet.funPostRegisterCheck(
                        RegisterCheckModel(registerCheckModel),
                        dataToken
                    )
                }
                if (resultRegisterCheck is ApiResponceStatus.Success) {
                    if (resultRegisterCheck.data.root.codigo == ET000) {
                        goToSucces(
                            activity,
                            ONLINE,
                            resultRegisterCheck.data.root.estacion,
                            dateTime[0],
                            dateTime[1],
                            registerCheckModel
                        )
                    } else {
                        statusData.value =
                            ApiResponceStatus.Error(resultRegisterCheck.data.root.codigo)
                    }
                } else {
                    resultRegisterCheck as ApiResponceStatus.Error
                    if (statusNet.value == false || resultRegisterCheck.messageId.contains(TIMEOUT) || resultRegisterCheck.messageId.contains(
                            ERRORHTTP
                        )
                    ) {
                        goToSucces(
                            activity,
                            OFFLINE,
                            STATE_OFFLINE,
                            dateTime[0],
                            dateTime[1],
                            registerCheckModel
                        )
                    } else {
                        statusData.value = resultRegisterCheck as ApiResponceStatus<Any>
                    }
                }
            }
        } else {
            val dateTime = getDateTime(null)
            funGetTokenCheckUnit(
                { funPostRegisterCheck(activity, registerCheckModel) }, {
                    goToSucces(
                        activity,
                        OFFLINE,
                        STATE_OFFLINE,
                        dateTime[0],
                        dateTime[1],
                        registerCheckModel
                    )
                })
        }
    }

    private fun goToSucces(
        activity: Activity,
        connection: String,
        station: String,
        date: String,
        time: String,
        registerCheckModel: RootRegister
    ) {
        dataBSSIDConected.value?.bssid?.let { fisrtBssid ->
            listBSSID.add(fisrtBssid)
        }
        dataBSSIDList.value?.forEach {
            listBSSID.add(it.bssid)
        }
        val succes = SuccesModel()
        succes.connection = connection
        succes.station = station
        succes.date = date
        succes.time = time
        succes.typeofCheck = MainFragment.checkType
        succes.photoEmployee = registerCheckModel.foto
        succes.statusOffline = SUCCES
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
                succes.foto = Utilities.saveImageToStorage(
                    activity,
                    Utilities.base64ToBitmap(mainFaceDetection.image),
                    "Offline${userData.employeeId}",
                    "Offline${userData.employeeId}"
                )
                mainRepoLocal.insertServicesOffline(succes)

                statusData.value = ApiResponceStatus.Success("")
            }
        }
        mainFaceDetection = FaceDetectionRequest()
        dataToken = ""
        dataTimeZone.value = null
        isRegisterCheck.value = true
    }

    fun getServicesOffline() {
        viewModelScope.launch {
            dataServicesOffline.value = mainRepoLocal.getServicesOffline()
        }
    }

    fun getEmployees() {
        viewModelScope.launch {
            dataEmployees.value = loginRepoLocal.getAllUser()
        }
    }

    fun funGetEmployees(activity: Activity) {
        viewModelScope.launch {
            statusData.value = ApiResponceStatus.Loading()
            if (ApiInterceptor.existToken()) {
                val resultGetEmployees = mainRepoNet.funGetEmployees(userData.companyId.toString())
                if (resultGetEmployees is ApiResponceStatus.Success) {
                    if (resultGetEmployees.data.result != null) {
                        loginRepoLocal.deleteAllSavedUser()
                        loginRepoLocal.deleteAllKeyWord()
                        funDeleteAllUser(resultGetEmployees.data.result!!, activity)
                    } else {
                        statusData.value = ApiResponceStatus.Success("")
                    }
                } else {
                    resultGetEmployees as ApiResponceStatus.Error
                    if (resultGetEmployees.messageId.contains(TIMEOUT)) {
                        statusData.value = ApiResponceStatus.Error(TIMEOUT)
                    } else if (resultGetEmployees.messageId.contains(ERRORHTTP)) {
                        statusData.value = ApiResponceStatus.Error(ERRORHTTP)
                    } else {
                        statusData.value = resultGetEmployees as ApiResponceStatus<Any>
                    }
                }
            } else {
                funGetTokenEmployees(userData.employeeId.toString(), userData.company, activity)
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
                if (resultGetTokenCollaborato.messageId.contains(TIMEOUT)) {
                    statusData.value = ApiResponceStatus.Error(TIMEOUT)
                } else if (resultGetTokenCollaborato.messageId.contains(ERRORHTTP)) {
                    statusData.value = ApiResponceStatus.Error(ERRORHTTP)
                } else {
                    statusData.value = resultGetTokenCollaborato as ApiResponceStatus<Any>
                }
            }
        }
    }

    fun funDeleteUser() {
        viewModelScope.launch {
            loginRepoLocal.deleteSavedUser()
        }
    }

    private fun funDeleteAllUser(result: ArrayList<EmployeeOfflineResponce>, activity: Activity) {
        viewModelScope.launch {
            result.forEachIndexed { index, employeeresult ->
                if (employeeresult.numEmployee.isNotEmpty()) {
                    Utilities.deleteAllFiles(activity, employeeresult.numEmployee)
                    val userEntity = UserEntity(
                        name = employeeresult.nameFull,
                        employeeId = employeeresult.numEmployee.toLong(),
                        company = userData.company,
                        companyId = userData.companyId,
                    )
                    if (employeeresult.photo.isNotEmpty()) {
                        userEntity.photoUrl = Utilities.saveImageToStorage(
                            activity,
                            Utilities.base64ToBitmap(employeeresult.photo),
                            employeeresult.numEmployee,
                            employeeresult.nameFull
                        )
                    }
                    if (employeeresult.palabraClave != null && employeeresult.palabraClave != "" && employeeresult.palabraClave != " ") {
                        userEntity.palabraClave = employeeresult.palabraClave!!
                        loginRepoLocal.insertKeyWord(
                            KeyWordEntity(
                                employeeresult.numEmployee.toLong(),
                                employeeresult.palabraClave!!
                            )
                        )
                    }
                    loginRepoLocal.insertUser(userEntity)
                    if (result.size == index + 1) {
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.success),
                            Toast.LENGTH_SHORT
                        ).show()
                        dataEmployees.value = loginRepoLocal.getAllUser()
                        statusData.value = ApiResponceStatus.Success("")
                    }
                }
            }
        }
    }

    fun getKeyWord(
        activity: Activity,
        user: String,
        employeVM: EmployeVM,
        etEmplyeColab: AppCompatEditText
    ) {
        viewModelScope.launch {
            val idEmploye = loginRepoLocal.getKeyWord(user)
            if (idEmploye != null) {
                etEmplyeColab.setText(idEmploye.employeeId.toString())
                funGetEmploye(activity, idEmploye.employeeId.toString(), employeVM)
            } else {
                statusData.value = ApiResponceStatus.Error(ERRORUSERLOCAL)
            }
        }
    }

    fun deleteServicesOffline(id: Int) {
        viewModelScope.launch {
            mainRepoLocal.deleteServicesOffline(id)
            getServicesOffline()
        }
    }

    fun setEnrolarLocal(
        localPictureRquest: LocalPictureRquest,
        userEntity: UserEntity,
        activity: Activity
    ) {
        viewModelScope.launch {
            statusData.value = ApiResponceStatus.Loading()
            val resultGetEmployees = mainRepoNet.funEnrolarLocal(localPictureRquest)
            if (resultGetEmployees is ApiResponceStatus.Success) {
                setDataUser(userEntity, activity)
                activity.finish()
            }
            statusData.value = resultGetEmployees as ApiResponceStatus<Any>
        }
    }

    fun setDataUser(userEntity: UserEntity, activity: Activity) {
        viewModelScope.launch {
            userData = userEntity
            loginRepoLocal.insertUser(userEntity)
            if (isLogin) {
                val intent = Intent(activity, PrincipalActivity::class.java)
                activity.startActivity(intent)
            }
        }
    }
}