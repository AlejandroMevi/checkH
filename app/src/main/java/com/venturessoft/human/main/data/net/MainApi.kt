package com.venturessoft.human.main.data.net

import com.venturessoft.human.core.utils.Constants
import com.venturessoft.human.core.utils.Constants.Companion.BY
import com.venturessoft.human.core.utils.Constants.Companion.FORMAT
import com.venturessoft.human.core.utils.Constants.Companion.KEY
import com.venturessoft.human.login.data.models.EmployeResponse
import com.venturessoft.human.login.data.models.TokenResponse
import com.venturessoft.human.login.data.models.TokenResponse2Model
import com.venturessoft.human.main.data.models.AutenticationResponse
import com.venturessoft.human.main.data.models.EmployeeOfflineResponce
import com.venturessoft.human.main.data.models.FaceDetectionRequest
import com.venturessoft.human.main.data.models.FaceDetectionResponce
import com.venturessoft.human.main.data.models.MovementResponse
import com.venturessoft.human.main.data.models.RegisterCheckModel
import com.venturessoft.human.main.data.models.RegisterCheckResponce
import com.venturessoft.human.main.data.models.ResponseGeneral
import com.venturessoft.human.main.data.models.TimeZoneResponce
import com.venturessoft.human.pictureLocal.data.models.LocalPictureResponce
import com.venturessoft.human.pictureLocal.data.models.LocalPictureRquest
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface MainApi {
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("Authorization/Acceso")
    suspend fun funGetToken(
        @Field("user") user: String,
        @Field("cia") cia: String
    ): TokenResponse
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("Authorization/Compania")
    suspend fun funGetToken2(
        @Field("claveCompania") claveCompania: Int
    ): TokenResponse2Model
    @Headers("${Constants.NEEDS_AUTH_HEADER_KEY}: true")
    @GET("ConsultaEmpleado/Empleado")
    suspend fun funGetEmployee(
        @Query("ciaNom") ciaNom: String,
        @Query("numEmp") numEmp: String,
        @Query("token") token: String,
        @Query("fechaFoto") fechaFoto: String,
        @Query("hashFoto") hashFoto: String? = null
    ): EmployeResponse
    @Headers("${Constants.NEEDS_AUTH_HEADER_KEY}: true")
    @GET("consultaMovimiento/")
    suspend fun funGetMovement(
        @Query("cia") cia: Int,
        @Query("empleado") empleado: Long,
        @Query("prioridad") prioridad: Int
    ): MovementResponse
    @Headers("${Constants.NEEDS_AUTH_HEADER_KEY}: true")
    @POST("ConsultaEmpleado/Empleado/PalabraClave")
    suspend fun funGetKeyword(
        @Query("numCia") numCia: Int,
        @Query("numEmp") numEmp: Long,
        @Query("palabraClave") palabraClave: String
    ): MovementResponse
    @Headers("${Constants.NEEDS_AUTH_HEADER_KEY}: true")
    @POST("AdministraFotoLocal/EnrolarLocal")
    suspend fun funEnrolarLocal(
        @Body localPictureRquest: LocalPictureRquest
    ): LocalPictureResponce
}
interface MainTimeZoneApi {
    @GET("/v2.1/get-time-zone")
    suspend fun funGetTimeZone(
        @Query("key") key: String = KEY,
        @Query("format") format: String = FORMAT,
        @Query("by") by: String = BY,
        @Query("lat") lat: Double?,
        @Query("by") by1: String = BY,
        @Query("lng") lng: Double?,
    ): TimeZoneResponce
}
interface MainAutenticationApi{
    @FormUrlEncoded
    @POST("Autenticacion/user")
    suspend fun funPostAutenticacion(
        @Field("user") user: String,
        @Field("cia") cia: String
    ): AutenticationResponse
}
interface MainRegisterCheckApi{
    @POST("registraChecada/unificado")
    suspend fun funPostRegisterCheck(
        @Body registerCheckModel: RegisterCheckModel,
        @Header("Authorization") auth: String
    ): RegisterCheckResponce

    @POST("registraChecada/")
    suspend fun funPostRegisterCheck2(
        @Body registerCheckModel: RegisterCheckModel,
        @Header("Authorization") auth: String
    ): RegisterCheckResponce
}
interface MainEmployeesApi{
    @Headers("${Constants.NEEDS_AUTH_HEADER_KEY}: true")
    @GET("ConsultaEmpleado/Offline")
    suspend fun funGetEmployees(
        @Query("cia") cia: String
    ): ResponseGeneral<ArrayList<EmployeeOfflineResponce>>
}
interface MainFaceDetection{
    @POST("liveness_base64")
    suspend fun funFaceDetection(
        @Header("Content-Type") contentType:String,
        @Header("X-BLOBR-KEY") key: String,
        @Body faceDetectionRequest: FaceDetectionRequest,
    ): FaceDetectionResponce
}