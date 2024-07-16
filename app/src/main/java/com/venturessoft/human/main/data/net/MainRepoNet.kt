package com.venturessoft.human.main.data.net

import com.venturessoft.human.core.ApiInterceptor
import com.venturessoft.human.core.ApiResponceStatus
import com.venturessoft.human.core.BaseActivity.Companion.dataLocation
import com.venturessoft.human.core.DataUser
import com.venturessoft.human.core.evaluateFace
import com.venturessoft.human.core.evaluateResponce
import com.venturessoft.human.core.makeNetworkCall
import com.venturessoft.human.core.utils.Constants
import com.venturessoft.human.core.utils.Constants.Companion.FACE_FAIL
import com.venturessoft.human.core.utils.Constants.Companion.FACE_NO_DETECTED
import com.venturessoft.human.core.utils.Constants.Companion.SMALL_FACE
import com.venturessoft.human.core.utils.Constants.Companion.TIMEOUT
import com.venturessoft.human.core.utils.Constants.Companion.TIME_OUT_DEFERRED
import com.venturessoft.human.login.data.models.EmployeResponse
import com.venturessoft.human.login.data.models.TokenResponse
import com.venturessoft.human.login.data.models.TokenResponse2Model
import com.venturessoft.human.main.data.models.AutenticationResponse
import com.venturessoft.human.main.data.models.EmployeeOfflineResponce
import com.venturessoft.human.main.data.models.FaceDetection
import com.venturessoft.human.main.data.models.FaceDetectionRequest
import com.venturessoft.human.main.data.models.FaceDetectionResponce
import com.venturessoft.human.main.data.models.MovementResponse
import com.venturessoft.human.main.data.models.RegisterCheckModel
import com.venturessoft.human.main.data.models.RegisterCheckResponce
import com.venturessoft.human.main.data.models.ResponseGeneral
import com.venturessoft.human.main.data.models.TimeZoneResponce
import com.venturessoft.human.main.data.models.ValidationResponce
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.tokenLiveness
import com.venturessoft.human.pictureLocal.data.models.LocalPictureResponce
import com.venturessoft.human.pictureLocal.data.models.LocalPictureRquest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class MainRepoNet @Inject constructor(
    private val mainApi: MainApi,
    private val mainTimeZoneApi: MainTimeZoneApi,
    private val mainAutenticationApi: MainAutenticationApi,
    private val mainRegisterCheckApi: MainRegisterCheckApi,
    private val mainEmployeesApi: MainEmployeesApi,
    private val mainFaceDetection: MainFaceDetection
) {
    suspend fun funGetToken2(claveCompania: Int): ApiResponceStatus<TokenResponse2Model> =
        makeNetworkCall {
            val responce = mainApi.funGetToken2(claveCompania)
            responce.token?.let {
                ApiInterceptor.setToken(it)
            }
            try {
                responce.tokenLiveness?.let {
                    tokenLiveness.postValue(it)
                }
            } catch (_: Exception) {
            }
            responce
        }

    suspend fun funGetToken(user: String, cia: String): ApiResponceStatus<TokenResponse> =
        makeNetworkCall {
            val responce = mainApi.funGetToken(user, cia)
            evaluateResponce(responce.codigo)
            if (responce.token.isNotEmpty()) {
                ApiInterceptor.setToken(responce.token)
            }
            try {
                if (responce.tokenLiveness.isNotEmpty()) {
                    tokenLiveness.postValue(responce.tokenLiveness)
                }
            } catch (_: Exception) {
            }
            responce
        }

    suspend fun funGetEmploye(
        ciaNom: String,
        numEmp: String,
        tokenFirebase: String,
        hashFoto: String? = null
    ): ApiResponceStatus<EmployeResponse> = makeNetworkCall {
        val responce = mainApi.funGetEmployee(ciaNom, numEmp,tokenFirebase, "1", hashFoto)
        responce.codigo?.let {
            evaluateResponce(it, responce.errorMessage)
        }
        responce
    }

    suspend fun funGetMovement(
        cia: Int,
        empleado: Long,
        prioridad: Int
    ): ApiResponceStatus<MovementResponse> = makeNetworkCall {
        val responce = mainApi.funGetMovement(cia, empleado, prioridad)
        evaluateResponce(responce.codigo)
        responce
    }

    suspend fun funGetKeyword(
        cia: Int,
        empleado: Long,
        palabraClave: String
    ): ApiResponceStatus<MovementResponse> = makeNetworkCall {
        val responce = mainApi.funGetKeyword(cia, empleado, palabraClave)
        evaluateResponce(responce.codigo)
        responce
    }

    suspend fun funPostAutenticacion(
        user: String,
        cia: String
    ): ApiResponceStatus<AutenticationResponse> = makeNetworkCall {
        val responce = mainAutenticationApi.funPostAutenticacion(user, cia)
        responce
    }

    suspend fun funPostRegisterCheck(
        registerCheckModel: RegisterCheckModel,
        token: String
    ): ApiResponceStatus<RegisterCheckResponce> = makeNetworkCall {
        val responce = mainRegisterCheckApi.funPostRegisterCheck(registerCheckModel, token)
        evaluateResponce(responce.root.codigo)
        responce
    }

    suspend fun funPostRegisterCheck2(
        registerCheckModel: RegisterCheckModel,
        token: String
    ): ApiResponceStatus<RegisterCheckResponce> = makeNetworkCall {
        val responce = mainRegisterCheckApi.funPostRegisterCheck2(registerCheckModel, token)
        evaluateResponce(responce.root.codigo)
        responce
    }

    suspend fun funGetEmployees(cia: String): ApiResponceStatus<ResponseGeneral<ArrayList<EmployeeOfflineResponce>>> =
        makeNetworkCall {
            val responce = mainEmployeesApi.funGetEmployees(cia)
            responce.codigo?.let { codigo ->
                evaluateResponce(codigo)
            }
            responce
        }

    suspend fun funEnrolarLocal(localPictureRquest: LocalPictureRquest): ApiResponceStatus<LocalPictureResponce> =
        makeNetworkCall {
            val responce = mainApi.funEnrolarLocal(localPictureRquest)
            responce.codigo?.let { codigo ->
                evaluateResponce(codigo)
            }
            responce
        }

    private val supervisor = SupervisorJob()
    suspend fun funGetTimeToken(): ApiResponceStatus<ValidationResponce> =
        with(CoroutineScope(Dispatchers.IO + supervisor)) {
            try {
                var timeZone:TimeZoneResponce?
                try {
                    val deferredTime = async {
                        withTimeout(TIME_OUT_DEFERRED) {
                            mainTimeZoneApi.funGetTimeZone(
                                lat = dataLocation.value?.latitud?.toDouble()!!,
                                lng = dataLocation.value?.longitud?.toDouble()!!
                            )
                        }
                    }
                    timeZone = deferredTime.await()
                    evaluateResponce(timeZone.status)
                }catch (_:Exception){
                    timeZone = null
                }
                val deferredToken = async {
                    withTimeout(TIME_OUT_DEFERRED) {
                        mainAutenticationApi.funPostAutenticacion(
                            DataUser.userData.employeeId.toString(),
                            DataUser.userData.companyId.toString()
                        )
                    }
                }
                val autentication = deferredToken.await()
                ApiResponceStatus.Success(ValidationResponce(timeZone, autentication))
            } catch (exception: Exception) {
                exceptionResponce(exception.message.toString())
            }
        }

    suspend fun funFaceDetection(faceDetectionRequest: FaceDetectionRequest): ApiResponceStatus<ValidationResponce> =
        with(CoroutineScope(Dispatchers.IO + supervisor)) {
            try {
                var timeZone:TimeZoneResponce?
                val deferredFace = async {
                    withTimeout(TIME_OUT_DEFERRED) {
                        mainFaceDetection.funFaceDetection(
                            "application/json",
                            tokenLiveness.value.toString(),
                            faceDetectionRequest
                        )
                    }
                }
                try {
                    val deferredTime = async {
                        withTimeout(TIME_OUT_DEFERRED) {
                            mainTimeZoneApi.funGetTimeZone(
                                lat = dataLocation.value?.latitud?.toDouble(),
                                lng = dataLocation.value?.longitud?.toDouble()
                            )
                        }
                    }
                    timeZone = deferredTime.await()
                    evaluateResponce(timeZone.status)
                }catch (_:Exception){
                    timeZone = null
                }
                val deferredToken = async {
                    withTimeout(TIME_OUT_DEFERRED) {
                        mainAutenticationApi.funPostAutenticacion(
                            DataUser.userData.employeeId.toString(),
                            DataUser.userData.companyId.toString()
                        )
                    }
                }
                val autentication = deferredToken.await()
                try {
                    val faceDetection = deferredFace.await()
                    evaluateFace(faceDetection.data.result)
                } catch (e: Exception) {
                    if (e.message == SMALL_FACE || e.message == FACE_NO_DETECTED || e.message == FACE_FAIL) {
                        return exceptionResponce(e.message.toString())
                    }
                }
                ApiResponceStatus.Success(ValidationResponce(timeZone, autentication))
            } catch (exception: Exception) {
                return when (exception.message) {
                    FACE_FAIL -> exceptionResponce(FACE_FAIL)
                    SMALL_FACE -> exceptionResponce(SMALL_FACE)
                    FACE_NO_DETECTED -> exceptionResponce(FACE_NO_DETECTED)
                    else -> exceptionResponce(exception.message.toString())
                }
            }
        }

    private fun exceptionResponce(exception: String): ApiResponceStatus<ValidationResponce> {
        var messageException = exception
        messageException.lowercase().apply {
            if (this.contains("timedout") ||
                this.contains("timed out") ||
                this.contains("timed_out") ||
                this.contains("timeout") ||
                this.contains("time out") ||
                this.contains("time_out") ||
                this.contains("connection aborted") ||
                this.contains("connection abort") ||
                this.contains("failed to connect to") ||
                this.contains("waiting for")
            ) {
                messageException = TIMEOUT
            }
            if (this.contains("unable to resolve host") ||
                this.contains("validation failed") ||
                this.contains("http 400") ||
                this.contains("http 401") ||
                this.contains("http 402") ||
                this.contains("http 403") ||
                this.contains("http 404") ||
                this.contains("http 405") ||
                this.contains("http 406") ||
                this.contains("http 407") ||
                this.contains("http 408") ||
                this.contains("http 409") ||
                this.contains("http 410") ||
                this.contains("http 411") ||
                this.contains("http 412") ||
                this.contains("http 413") ||
                this.contains("http 414") ||
                this.contains("http 415") ||
                this.contains("http 416") ||
                this.contains("http 417") ||
                this.contains("http 418") ||
                this.contains("http 421") ||
                this.contains("http 422") ||
                this.contains("http 423") ||
                this.contains("http 424") ||
                this.contains("http 425") ||
                this.contains("http 426") ||
                this.contains("http 428") ||
                this.contains("http 429") ||
                this.contains("http 431") ||
                this.contains("http 451") ||
                this.contains("http 500") ||
                this.contains("http 501") ||
                this.contains("http 502") ||
                this.contains("http 503") ||
                this.contains("http 504") ||
                this.contains("http 505") ||
                this.contains("http 506") ||
                this.contains("http 507") ||
                this.contains("http 508") ||
                this.contains("http 510") ||
                this.contains("http 511") ||
                this.contains("http 600") ||
                this.contains("http 601") ||
                this.contains("http 605") ||
                this.contains("http 620") ||
                this.contains("http 630") ||
                this.contains("http 631") ||
                this.contains("http 632") ||
                this.contains("http 633") ||
                this.contains("http 634") ||
                this.contains("http 635") ||
                this.contains("http 636") ||
                this.contains("http 637") ||
                this.contains("http 638") ||
                this.contains("http 639") ||
                this.contains("http 640") ||
                this.contains("http 641") ||
                this.contains("http 660") ||
                this.contains("http 661") ||
                this.contains("http 662") ||
                this.contains("http 663") ||
                this.contains("http 699") ||
                this.contains("db003") ||
                this.contains("db210") ||
                this.contains("db211") ||
                this.contains("db212") ||
                this.contains("db213") ||
                this.contains("db214") ||
                this.contains("db215") ||
                this.contains("db216") ||
                this.contains("db217") ||
                this.contains("db218") ||
                this.contains("db219") ||
                this.contains("db220") ||
                this.contains("db221") ||
                this.contains("db222") ||
                this.contains("db223") ||
                this.contains("db224") ||
                this.contains("db225") ||
                this.contains("db226") ||
                this.contains("db227") ||
                this.contains("db229") ||
                this.contains("db230") ||
                this.contains("db231") ||
                this.contains("db232") ||
                this.contains("db233") ||
                this.contains("db250") ||
                this.contains("db400") ||
                this.contains("db401") ||
                this.contains("db402") ||
                this.contains("db403") ||
                this.contains("db404") ||
                this.contains("db405") ||
                this.contains("db407") ||
                this.contains("db408") ||
                this.contains("db409") ||
                this.contains("db410") ||
                this.contains("db411") ||
                this.contains("db412") ||
                this.contains("db413") ||
                this.contains("db414") ||
                this.contains("db415") ||
                this.contains("db416") ||
                this.contains("db417") ||
                this.contains("db418") ||
                this.contains("db419") ||
                this.contains("db420") ||
                this.contains("db421") ||
                this.contains("error_http")
            ) {
                messageException = Constants.ERRORHTTP
            }
        }
        return ApiResponceStatus.Error(messageException)
    }

    suspend fun funFaceDetectionBoolean(faceDetectionRequest: FaceDetectionRequest): ApiResponceStatus<FaceDetectionResponce> =
        makeNetworkCall {
            if (DataUser.userData.companyId.toString() == "10101" || DataUser.userData.companyId.toString() == "1038" || DataUser.userData.companyId.toString() == "15" || DataUser.userData.companyId.toString() == "721") {
                val responce = mainFaceDetection.funFaceDetection(
                    "application/json",
                    tokenLiveness.value.toString(),
                    faceDetectionRequest
                )
                evaluateResponce(responce.status)
                responce
            } else {
                FaceDetectionResponce(data = FaceDetection(result = "genuine"))
            }
        }
}