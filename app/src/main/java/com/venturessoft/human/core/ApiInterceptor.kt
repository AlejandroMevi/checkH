package com.venturessoft.human.core

import com.venturessoft.human.core.utils.Constants.Companion.AUTH_TOKEN
import com.venturessoft.human.core.utils.Constants.Companion.NEEDS_AUTH_HEADER_KEY
import okhttp3.Interceptor
import okhttp3.Response
object ApiInterceptor:Interceptor {
    private var token:String? = null
    fun setToken(userToken:String){
        this.token = userToken
    }
    fun existToken():Boolean{
        return token != null
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBuilder = request.newBuilder()
        if(request.header(NEEDS_AUTH_HEADER_KEY) != null){
            token?.let {
                requestBuilder.addHeader(AUTH_TOKEN, it)
            }
        }
        return chain.proceed(requestBuilder.build())
    }
}