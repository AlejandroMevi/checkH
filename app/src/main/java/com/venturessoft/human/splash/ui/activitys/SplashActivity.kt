package com.venturessoft.human.splash.ui.activitys

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.venturessoft.human.BuildConfig
import com.venturessoft.human.R
import com.venturessoft.human.core.BaseApplication.Companion.activityVisible
import com.venturessoft.human.core.di.NetModule
import com.venturessoft.human.core.utils.Constants.Companion.EN
import com.venturessoft.human.core.utils.Constants.Companion.FR
import com.venturessoft.human.core.utils.Constants.Companion.LOOTIE_ANIMATION
import com.venturessoft.human.core.utils.Constants.Companion.PACKAGE
import com.venturessoft.human.core.utils.Constants.Companion.PT
import com.venturessoft.human.core.utils.Constants.Companion.TRUE
import com.venturessoft.human.core.utils.Constants.Companion.URL_WARNING
import com.venturessoft.human.core.utils.DialogGeneral
import com.venturessoft.human.core.utils.Preferences
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.databinding.ActivitySplashBinding
import com.venturessoft.human.login.ui.activitys.LoginActivity
import com.venturessoft.human.main.ui.activitys.PrincipalActivity
import com.venturessoft.human.splash.data.AvisoModel
import com.venturessoft.human.splash.data.Lenguage
import com.venturessoft.human.splash.ui.vm.SplashVM
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale


@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val splashVM: SplashVM by viewModels()
    private val preferences = Preferences()
    private lateinit var binding: ActivitySplashBinding
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.FLEXIBLE

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupSnackbarForCompleteUpdate()
        }
    }
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.registerListener(installStateUpdatedListener)
        }
        startActivity()
    }
    private fun checkUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when (updateType) {
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isFlexibleUpdateAllowed
                else -> false
            }
            if (isUpdateAvailable && isUpdateAllowed) {
                appUpdateManager.startUpdateFlowForResult(info, updateType, this, 123)
            } else {
                checkSession()
            }
        }
        appUpdateManager.appUpdateInfo.addOnFailureListener {
            checkSession()
        }
    }
    private fun popupSnackbarForCompleteUpdate() {
        Snackbar.make(binding.root, getString(R.string.actualizacion_desc), Snackbar.LENGTH_INDEFINITE).apply {
            setAction(getString(R.string.restart)) { appUpdateManager.completeUpdate() }
            setActionTextColor(getColor(R.color.colorPrimary))
            show()
        }
    }
    override fun onResume() {
        super.onResume()

        preferences.getClaveCompany(this)

        activityVisible = false
        if (updateType == AppUpdateType.IMMEDIATE) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    appUpdateManager.startUpdateFlowForResult(info, updateType, this, 123)
                } else {
                    checkSession()
                }
            }
            appUpdateManager.appUpdateInfo.addOnFailureListener {
                checkSession()
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (updateType == AppUpdateType.FLEXIBLE) {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }
        splashVM.getExistEmploye.removeObservers(this)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            123 -> {
                checkSession()
            }
        }
    }
    @Suppress("DEPRECATION")
    private fun reloadLaguage() {
        val position = when (Locale.getDefault().language) {
            EN -> 1
            PT -> 2
            FR -> 3
            else -> 0
        }
        var lenguage = Lenguage(Locale.getDefault().language, position, false)
        if (preferences.getLanguage(this) != null) {
            lenguage = preferences.getLanguage(this)!!
        } else {
            preferences.editLenguaje(lenguage, this)
        }
        val locale = Locale(lenguage.idioma)
        Locale.setDefault(locale)
        val config = this.resources.configuration
        config.setLocale(locale)
        this.resources.updateConfiguration(config, this.resources.displayMetrics)
        checkUpdate()
    }
    @SuppressLint("SetTextI18n")
    private fun configVersionEnvironmentApp(ambiente:String) {
        if (!BuildConfig.DEBUG){
            binding.txtViewEnvironment.isVisible = false
            binding.txtDevelopType.isVisible = false
        }
        binding.txtDevelopType.text = ambiente
        binding.txtViewEnvironment.text = "${getString(R.string.version)} ${BuildConfig.environment} ${getString(R.string.build)} ${BuildConfig.VERSION_CODE}"
        binding.txtViewVersionApp.text = BuildConfig.VERSION_NAME
        binding.lottieSuccess.setAnimation(LOOTIE_ANIMATION)
        reloadLaguage()
    }
    private fun startActivity() {
        val permissionsArray = mutableListOf<String>()
        val grented = PackageManager.PERMISSION_GRANTED
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != grented) {
            permissionsArray.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != grented
        ) {
            permissionsArray.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != grented) {
            permissionsArray.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != grented) {
            permissionsArray.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != grented
            ) {
                permissionsArray.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != grented
            ) {
                permissionsArray.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != grented
            ) {
                permissionsArray.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        if (permissionsArray.isEmpty()) {
            getSSID()
        } else {
            ActivityCompat.requestPermissions(this, permissionsArray.toTypedArray(), 1)
        }
    }
    private fun checkSession() {
        splashVM.getExistEmploye.observe(this) { existEmployee ->
            binding.lottieSuccess.playAnimation()
            binding.lottieSuccess.addAnimatorUpdateListener { valueAnimator ->
                val progress = (valueAnimator.animatedValue as Float * 100).toInt()
                if (progress >= 50) {
                    binding.root.setBackgroundResource(R.color.colorPrimary)
                    binding.txtDevelopType.setTextColor(getColor(R.color.miWhite))
                    binding.txtViewEnvironment.setTextColor(getColor(R.color.miWhite))
                    binding.txtViewVersionApp.setTextColor(getColor(R.color.miWhite))
                }
            }
            binding.lottieSuccess.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                }
                override fun onAnimationEnd(animation: Animator) {
                    if (existEmployee) {
                        goToMainActivity()
                    } else {
                        goToLoginActivity()
                    }
                    splashVM.getExistEmploye.removeObservers(this@SplashActivity)
                }
                override fun onAnimationCancel(animation: Animator) {
                }
                override fun onAnimationRepeat(animation: Animator) {
                }
            })
        }
    }
    private fun goToMainActivity() {
        val intent = Intent(this@SplashActivity, PrincipalActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun goToLoginActivity() {
        val intent = Intent(this@SplashActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            var isGranted = true
            grantResults.forEach { permission ->
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    isGranted = false
                }
            }
            if (isGranted) {
                getSSID()
            } else {
                val dialogPermission = DialogGeneral(null,
                    getString(R.string.message_permission),
                    null,
                    getString(R.string.close_app),
                    {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(PACKAGE, this@SplashActivity.packageName, null)
                        )
                        startActivity(intent)
                        this@SplashActivity.finish()
                    },
                    {
                        this@SplashActivity.finish()
                    }
                )
                dialogPermission.show(this.supportFragmentManager, "dialog")
            }
        }
    }
    private fun validateTerms() {
        val mDialog = LayoutInflater.from(this).inflate(R.layout.layout_license, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialog)
        val mAlertDialog = mBuilder.show()
        mAlertDialog.setCancelable(false)
        mAlertDialog.findViewById<TextView>(R.id.txtPolicy)!!.text = HtmlCompat.fromHtml(
            getString(R.string.et_licence_terms_text_link),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        mDialog.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            mAlertDialog.dismiss()
            val intent = Intent(this@SplashActivity, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }
        mDialog.findViewById<Button>(R.id.btnAccept).setOnClickListener {
            if (mDialog.findViewById<CheckBox>(R.id.checkTerms).isChecked) {
                mAlertDialog.dismiss()
                val aviso = AvisoModel(TRUE)
                preferences.editAviso(aviso, this@SplashActivity)
                startActivity()
            }
        }
        mAlertDialog.findViewById<TextView>(R.id.txtPolicy)!!.setOnClickListener {
            try {
                val urlAviso = URL_WARNING
                val webpage = Uri.parse(urlAviso)
                if (urlAviso.trim().isNotBlank()) {
                    val myIntent = Intent(Intent.ACTION_VIEW, webpage)
                    startActivity(myIntent)
                }
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
    }
    private fun getSSID(){
        val mWifiManager: WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var ambiente: String
        try {
            var wifiName = mWifiManager.connectionInfo.ssid
            val wifiNameChar = mWifiManager.connectionInfo.ssid.toCharArray()
            if (wifiNameChar[0] == '"' && wifiNameChar[wifiNameChar.size - 1] == '"') {
                wifiName = Utilities.removeLastChar(wifiName)
                wifiName = wifiName.substring(1)
            }
            when (wifiName) {
                "Wifi-VSM-DEV-MOBILE-2023" -> {
                    NetModule.URL_CONSULTA_MOVIMIENTO = "http://192.168.0.16/"
                    NetModule.URL_REGISTRAR_CHECADA = "http://192.168.0.90/"
                    NetModule.URL_AUTHORIZATION_ACCESS = "http://192.168.0.16:5102/"
                    NetModule.URL_ACCES_LOGIN = "http://192.168.0.16/HumaneTime/api/"
                    NetModule.URL_EMPLOYEE_MODE_OFFLINE = "http://192.168.0.16/HumaneTime/api/"
                    NetModule.CONSULTA_EMPLEADO = "http://192.168.0.16/HumaneTime/api/"
                    ambiente = "Desarrollo"
                }
                "Wifi-VSM-QA-MOBILE-2023" -> {
                    NetModule.URL_CONSULTA_MOVIMIENTO = "http://192.168.0.63/HumaneTime/api/"
                    NetModule.URL_REGISTRAR_CHECADA = "http://192.168.0.63/Human/eTime/Checada/"
                    NetModule.URL_AUTHORIZATION_ACCESS = "http://192.168.0.63:5200/"
                    NetModule.URL_ACCES_LOGIN = "http://192.168.0.63/HumaneTime/api/"
                    NetModule.URL_EMPLOYEE_MODE_OFFLINE = "http://192.168.0.63/HumaneTime/api/"
                    NetModule.CONSULTA_EMPLEADO = "http://192.168.0.63/HumaneTime/api/"
                    ambiente = "ControlCalidad"
                }
                else -> {
                    NetModule.URL_CONSULTA_MOVIMIENTO = "https://eland-dk.humaneland.net/Human/eTime/"
                    NetModule.URL_REGISTRAR_CHECADA = "https://eland-dk.humaneland.net/Human/eTime/Checada/"
                    NetModule.URL_AUTHORIZATION_ACCESS = "https://eland-dk.humaneland.net/Human/eTime/Checada/"
                    NetModule.URL_ACCES_LOGIN = "https://eland-dk.humaneland.net/HumaneTime/api/"
                    NetModule.URL_EMPLOYEE_MODE_OFFLINE = "https://eland-dk.humaneland.net/Human/eTime/AdministrarUsuario/"
                    NetModule.CONSULTA_EMPLEADO = "https://eland-dk.humaneland.net/Human/eTime/"
                    ambiente = "Preproductivo"
                }
            }
        } catch (_: Exception) {
            NetModule.URL_CONSULTA_MOVIMIENTO = "https://eland-dk.humaneland.net/Human/eTime/"
            NetModule.URL_REGISTRAR_CHECADA = "https://eland-dk.humaneland.net/Human/eTime/Checada/"
            NetModule.URL_AUTHORIZATION_ACCESS = "https://eland-dk.humaneland.net/Human/eTime/Checada/"
            NetModule.URL_ACCES_LOGIN = "https://eland-dk.humaneland.net/HumaneTime/api/"
            NetModule.URL_EMPLOYEE_MODE_OFFLINE = "https://eland-dk.humaneland.net/Human/eTime/AdministrarUsuario/"
            NetModule.CONSULTA_EMPLEADO = "https://eland-dk.humaneland.net/Human/eTime/"
            ambiente = "Preproductivo"
        }
        configVersionEnvironmentApp(ambiente)
    }
}

