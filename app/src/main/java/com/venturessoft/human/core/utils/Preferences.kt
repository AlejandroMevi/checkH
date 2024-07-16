package com.venturessoft.human.core.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.venturessoft.human.core.utils.Constants.Companion.APP_SETTINGS
import com.venturessoft.human.core.utils.Constants.Companion.KEY_AVISO
import com.venturessoft.human.core.utils.Constants.Companion.KEY_COMPANY
import com.venturessoft.human.core.utils.Constants.Companion.KEY_LENGUAJE
import com.venturessoft.human.splash.data.AvisoModel
import com.venturessoft.human.splash.data.KeyCompany
import com.venturessoft.human.splash.data.Lenguage

class Preferences {
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
    }
    fun getLanguage(context: Context): Lenguage? {
        val gson = Gson()
        return try {
            gson.fromJson(getSharedPreferences(context).getString(KEY_LENGUAJE, null), Lenguage::class.java)
        } catch (e: Exception) {
            null
        }
    }
    fun editLenguaje(lenguage: Lenguage, context: Context) {
        val editor = getSharedPreferences(context).edit()
        val gson = Gson()
        val json = gson.toJson(lenguage)
        editor.putString(KEY_LENGUAJE, json)
        editor.apply()
    }
    fun getAviso(context: Context): AvisoModel? {
        val gson = Gson()
        return try {
            gson.fromJson(getSharedPreferences(context).getString(KEY_AVISO, null), AvisoModel::class.java)
        } catch (e: Exception) {
            null
        }
    }
    fun editAviso(aviso: AvisoModel, context: Context) {
        val editor = getSharedPreferences(context).edit()
        val gson = Gson()
        val json = gson.toJson(aviso)
        editor.putString(KEY_AVISO, json)
        editor.apply()
    }

    fun getClaveCompany(context: Context): KeyCompany? {
        val gson = Gson()
        return try {
            gson.fromJson(getSharedPreferences(context).getString(KEY_COMPANY, null), KeyCompany::class.java)
        } catch (e: Exception) {
            null
        }
    }
    fun editClaveCompany(keycompany: KeyCompany, context: Context) {
        val editor = getSharedPreferences(context).edit()
        val gson = Gson()
        val json = gson.toJson(keycompany)
        editor.putString(KEY_COMPANY, json)
        editor.apply()
    }
}
