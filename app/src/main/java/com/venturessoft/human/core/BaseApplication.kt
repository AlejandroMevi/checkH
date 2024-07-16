package com.venturessoft.human.core

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.google.firebase.analytics.FirebaseAnalytics
import com.venturessoft.human.R
import dagger.hilt.android.HiltAndroidApp
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

@HiltAndroidApp
class BaseApplication : Application() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    companion object{
        var activityVisible:Boolean ?= null
    }

    override fun onCreate() {
        super.onCreate()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath)
                .build()
        )
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        io.github.inflationx.calligraphy3.CalligraphyConfig.Builder()
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())
    }
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}