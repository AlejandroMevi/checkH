package com.venturessoft.human.login.ui.activitys

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.venturessoft.human.R
import com.venturessoft.human.core.ApiResponceStatus
import com.venturessoft.human.core.ConnectionLiveData
import com.venturessoft.human.core.DataUser
import com.venturessoft.human.core.utils.Constants
import com.venturessoft.human.core.utils.Constants.Companion.TRUE
import com.venturessoft.human.core.utils.DialogGeneral
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.core.utils.Utilities.Companion.checkPermissionStorage
import com.venturessoft.human.core.utils.Utilities.Companion.textcode
import com.venturessoft.human.databinding.ActivityLoginBinding
import com.venturessoft.human.login.ui.fragments.AvisoPrivacidadDialog
import com.venturessoft.human.login.ui.vm.LoginVM
import com.venturessoft.human.main.ui.activitys.PrincipalActivity
import com.venturessoft.human.pictureLocal.LocalPictureActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginVM: LoginVM by viewModels()
    private var modoAvionOk: Boolean = false
    private lateinit var network: ConnectionLiveData
    private var tokenFirebase=""
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        network = ConnectionLiveData(this)
        network.observe(this){
            dataConnection.postValue(it)
        }
        initView()
    }
    companion object{
        var dataConnection = MutableLiveData(false)
    }
    private fun initView() {
        editTokenSharedPreferences()
        checkGPSPermission()
        showSesionClosed()
        views()
    }
    private fun showSesionClosed() {
        val isFromLogOut = intent.getBooleanExtra(Constants.FROM_LOG_OUT, false)
        if (isFromLogOut) {
            val dialogSesion = DialogGeneral(getString(R.string.logout),getString(R.string.message_successful_logout),getString(R.string.accept),null,null)
            dialogSesion.show(this.supportFragmentManager, "dialog")
        }
    }
    private fun checkGPSPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.PERMISSION_FINE_LOCATION_REQUEST_CODE
            )
        }
    }
    private fun views() {
        binding.enterButton.setOnClickListener {
            valudateData()
        }
    }
    private fun valudateData() {
        validateSetting()
        if (modoAvionOk) {
            val employee = binding.employeeTextField.text?.trim().toString()
            val company = binding.companyTextField.text?.trim().toString().uppercase()
            val checkFieldsMessage = camposVacios(company, employee, this@LoginActivity)
            if (checkFieldsMessage == TRUE) {
                if (checkPermissionStorage(this)){
                    startServices(company, employee)
                }
            } else {
                Toast.makeText(this, checkFieldsMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun camposVacios(company:String, employee:String, context: Activity): String {
        if (company.isEmpty()) {
            return context.getString(R.string.error_company_empty)
        }
        if (employee.isEmpty()) {
            return context.getString(R.string.error_employee_empty)
        }
        return TRUE
    }
    private fun validateSetting() {
        when (Settings.System.getInt(this.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 0) {
            false -> desactivarModoAvion()
            true -> modoAvionOk = true
        }
    }
    private fun showLoading(isShowing: Boolean) {
        binding.loadAnimation.root.isVisible = isShowing
        if (isShowing) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }
    private fun desactivarModoAvion() {
        val dialogGPS = DialogGeneral(null, getString(R.string.desactive_airplane_comfirm), null, null, {
                changeValue()
            },{
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
        )
        dialogGPS.show(this.supportFragmentManager, "dialog")
    }
    private fun changeValue(){
        modoAvionOk = false
    }
    @SuppressLint("LongLogTag")
    fun startServices(compania: String, empleado: String) {
        hideKeyboard(currentFocus ?: View(this))
        loginVM.statusData.observe(this) { status ->
            if (status != null){
                when (status) {
                    is ApiResponceStatus.Loading -> showLoading(true)
                    is ApiResponceStatus.Success -> stopObserverService()
                    is ApiResponceStatus.Error -> {
                        stopObserverService()
                        val text = textcode(status.messageId, this)
                        Utilities.showErrorDialog(text, this.supportFragmentManager)
                    }
                }
            }
        }
        loginVM.getExistEmploye.observe(this) { existEmploye ->
            if (existEmploye!=null) {
                if (existEmploye){
                    loginVM.getExistEmploye.value = null
                    loginVM.getExistEmploye.removeObservers(this)
                    if (DataUser.userData.fotoLocal){
                        if (DataUser.userData.localPictureUriActual.isNotEmpty() || DataUser.userData.localPictureUriPending.isNotEmpty()){
                            goToMainActivity()
                        }else{
                            goToLocalPicture()
                        }
                    }else{
                        goToMainActivity()
                    }
                }
            }
        }
        loginVM.dataPolitica.observe(this){politica->
            if (politica!=null) {
                val fullScreenDialogFragment = AvisoPrivacidadDialog(politica,this, empleado, compania)
                fullScreenDialogFragment.show(supportFragmentManager, "FullScreenDialogFragment")
                loginVM.dataPolitica.removeObservers(this)
                loginVM.dataPolitica.value = null
            }
        }
        loginVM.funGetEmployeDBLocal(this, empleado, compania,tokenFirebase)
    }
    private fun editTokenSharedPreferences() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            tokenFirebase = task.result ?: ""
        }
    }
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun stopObserverService(){
        loginVM.statusData.value = null
        loginVM.statusData.removeObservers(this)
        showLoading(false)
    }
    private fun goToMainActivity() {
        val intent = Intent(this@LoginActivity, PrincipalActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun goToLocalPicture() {
        val intent = Intent(this@LoginActivity, LocalPictureActivity::class.java)
        intent.putExtra("login",true)
        startActivity(intent)
        finish()
    }
}