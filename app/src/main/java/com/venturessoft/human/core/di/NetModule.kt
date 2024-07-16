package com.venturessoft.human.core.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.venturessoft.human.core.ApiInterceptor
import com.venturessoft.human.core.utils.Constants.Companion.TIME_OUT
import com.venturessoft.human.main.data.net.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module

@InstallIn(SingletonComponent::class)
class NetModule {

    companion object{
        var URL_CONSULTA_MOVIMIENTO = "https://eland-dk.humaneland.net/Human/eTime/"
        var URL_REGISTRAR_CHECADA = "https://eland-dk.humaneland.net/Human/eTime/Checada/"
        var URL_AUTHORIZATION_ACCESS = "https://eland-dk.humaneland.net/Human/eTime/Checada/"
        var URL_ACCES_LOGIN = "https://eland-dk.humaneland.net/HumaneTime/api/"
        var URL_EMPLOYEE_MODE_OFFLINE = "https://eland-dk.humaneland.net/Human/eTime/AdministrarUsuario/"
        var CONSULTA_EMPLEADO = "https://eland-dk.humaneland.net/Human/eTime/"
        var URL_FACE_DETECTION = "https://api.faceonlive.com/zmmss2fybc25ysk9/api/"
        var URL_HORA = "https://vip.timezonedb.com"
    }
    private var gson: Gson = GsonBuilder().setDateFormat("yyyy-MM-dd").serializeNulls().create()

    private val okHttpClient = OkHttpClient
        .Builder()
        .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
        .readTimeout(TIME_OUT, TimeUnit.SECONDS)
        .writeTimeout(TIME_OUT, TimeUnit.SECONDS)
        .addInterceptor(ApiInterceptor)
        .build()
    @Provides
    @Singleton
    fun provideRetrofit():Retrofit{
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(URL_ACCES_LOGIN)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun newUrl(url:String):Retrofit{
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    @Provides
    @Singleton
    fun provideMainApiService(retrofit: Retrofit): MainApi {
        retrofit.newBuilder().baseUrl(URL_CONSULTA_MOVIMIENTO)
        return retrofit.create(MainApi::class.java)
    }
    @Provides
    @Singleton
    fun provideMainTimeZoneApiService(): MainTimeZoneApi {
        val retrofit = newUrl(URL_HORA)
        return retrofit.create(MainTimeZoneApi::class.java)
    }
    @Provides
    @Singleton
    fun provideMainAutenticationApiService(): MainAutenticationApi {
        val retrofit = newUrl(URL_AUTHORIZATION_ACCESS)
        return retrofit.create(MainAutenticationApi::class.java)
    }
    @Provides
    @Singleton
    fun provideMainRegisterCheckApiService(): MainRegisterCheckApi {
        val retrofit = newUrl(URL_REGISTRAR_CHECADA)
        return retrofit.create(MainRegisterCheckApi::class.java)
    }
    @Provides
    @Singleton
    fun provideMainEmployeesApiService(): MainEmployeesApi {
        val retrofit = newUrl(URL_EMPLOYEE_MODE_OFFLINE)
        return retrofit.create(MainEmployeesApi::class.java)
    }
    @Provides
    @Singleton
    fun provideMainFaceDetection(): MainFaceDetection {
        val retrofit = newUrl(URL_FACE_DETECTION)
        return retrofit.create(MainFaceDetection::class.java)
    }
}