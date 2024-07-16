package com.venturessoft.human.main.ui.activitys

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.venturessoft.human.R
import com.venturessoft.human.core.ApiResponceStatus
import com.venturessoft.human.core.BaseActivity
import com.venturessoft.human.core.BaseApplication.Companion.activityVisible
import com.venturessoft.human.core.DataUser.Companion.userData
import com.venturessoft.human.core.SpeachRecognition
import com.venturessoft.human.core.utils.Constants
import com.venturessoft.human.core.utils.DialogGeneral
import com.venturessoft.human.core.utils.Preferences
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.core.utils.Utilities.Companion.showErrorDialog
import com.venturessoft.human.databinding.ActivityPrincipalBinding
import com.venturessoft.human.login.data.models.UserEntity
import com.venturessoft.human.login.ui.activitys.LoginActivity
import com.venturessoft.human.main.ui.fragments.main.ProgressDialog
import com.venturessoft.human.main.ui.interfaces.BaseInterface
import com.venturessoft.human.main.ui.interfaces.MainInterface
import com.venturessoft.human.main.ui.vm.MainVM
import com.venturessoft.human.main.ui.vm.SettingsVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrincipalActivity : BaseActivity(), MainInterface{
    private lateinit var binding: ActivityPrincipalBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val preferences = Preferences()
    private var baseInterface: BaseInterface? = null
    private val speachRecognition = SpeachRecognition(this)
    private val mainVM: MainVM by viewModels()
    private val settingsVM: SettingsVM by viewModels()
    private val dialogProgress = ProgressDialog()

    private val countDownTimerProgress = object : CountDownTimer(10000, 1000){
        override fun onTick(p0: Long) {}
        override fun onFinish() {
            if (binding.loadAnimation.root.isVisible) {
                binding.loadAnimation.tvProgress.isVisible = true
            }
        }
    }
    companion object {
        var isSpeach = false
        var isActiveSpeach = true
        var statusNet = MutableLiveData(false)
        var faceDetection: MutableLiveData<Boolean> = MutableLiveData()
        var blinkDetection: MutableLiveData<Boolean> = MutableLiveData()
        var textSpeach = MutableLiveData<String?>(null)
        var tokenLiveness = MutableLiveData<String?>(null)
        var dataToken = ""
    }
    private val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> baseInterface?.stopBeacons()
                    BluetoothAdapter.STATE_ON -> baseInterface?.getBeacons(true)
                }
            }
        }
    }
    private val wifiStateReceiver = object : BroadcastReceiver (){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)){
                WifiManager.WIFI_STATE_ENABLED -> baseInterface?.getBSSID(true)
                WifiManager.WIFI_STATE_DISABLED-> baseInterface?.stopBSSID()
            }
        }
    }

    private val locationReceiver = object : BroadcastReceiver (){
        override fun onReceive(context: Context, intent: Intent) {
            intent.action?.let { act ->
                if (act.matches("android.location.PROVIDERS_CHANGED".toRegex())) {
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        baseInterface?.getLocation()
                    }else{
                        baseInterface?.stopLocation()
                    }
                }
            }
        }
    }
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        baseInterface = this
        binding = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val filterBluetooth = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothReceiver, filterBluetooth)
        val filterWifi = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
        registerReceiver(wifiStateReceiver,filterWifi)
        val filterLocation = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(locationReceiver, filterLocation)
        validateSpeach()
        validateNetwork()
        initToolBar()

    }
    override fun onResume() {
        super.onResume()
        activityVisible = false
        mainVM.getUserLastTime()
        mainVM.dataExistUser.observe(this){
            val header = binding.navigationView.getHeaderView(0)
            val nameUser = header.findViewById<AppCompatTextView>(R.id.tv_name_user)
            val numberUser = header.findViewById<AppCompatTextView>(R.id.tv_number_user)
            val imageUser = header.findViewById<AppCompatImageView>(R.id.imgEmployee)
            nameUser.text = userData.name
            numberUser.text = "${getString(R.string.employee_number_splash)}: ${userData.employeeId}"
            try {
                val profileUri=if (userData.fotoLocal){
                    Utilities.uriToBase64(Uri.parse(userData.localPictureUriActual))
                }else{
                    Utilities.uriToBase64(Uri.parse(userData.photoUrl))
                }
                val bitmap: Bitmap = Utilities.base64ToBitmap(profileUri)
                Glide.with(this).load(bitmap).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(imageUser)
            }catch (_:Exception){ }
        }
    }
    override fun onPause() {
        super.onPause()
        stopSpeach()
    }
    private fun initToolBar() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.toolBar.setNavigationOnClickListener {
            binding.drawerLayout.open()
            val viewEmploe = currentFocus
            if (viewEmploe != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(viewEmploe.windowToken, 0)
            }
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }
        binding.drawerLayout.setScrimColor(getColor(R.color.mTransparentBlue))
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.offlineChecada -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_mainFragment_to_offlineHistoryFragment)
                    binding.drawerLayout.close()
                }
                R.id.nav_about -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_mainFragment_to_aboutFragment)
                    binding.drawerLayout.close()
                }
                R.id.nav_settings -> {
                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_mainFragment_to_settingsFragment2)
                    binding.drawerLayout.close()
                }
            }
            true
        }
        val dialogLogout = DialogGeneral(getString(R.string.logout),getString(R.string.message_confirm_logout),getString(R.string.logout),getString(R.string.cancel), {
            userData = UserEntity()
            mainVM.funDeleteUser()
            val intent = Intent(this@PrincipalActivity, LoginActivity::class.java)
            intent.putExtra(Constants.FROM_LOG_OUT, true)
            startActivity(intent)
            finish()
        })
        binding.btnLogout.setOnClickListener {
            dialogLogout.show(this.supportFragmentManager, "dialog")
            binding.drawerLayout.close()
        }
        setSupportActionBar(binding.toolBar)
        setupActionBarWithNavController(true)
    }
    override fun setupActionBarWithNavController(isPrincipalUser:Boolean) {
        appBarConfiguration = if (isPrincipalUser){
            AppBarConfiguration(navController.graph, binding.drawerLayout)
        }else{
            AppBarConfiguration(setOf())
        }
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
    override fun showLoading(isShowing: Boolean) {
        binding.loadAnimation.root.isVisible = isShowing
        binding.loadAnimation.tvProgress.isVisible = false
        if (isShowing) {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            countDownTimerProgress.cancel()
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
        countDownTimerProgress.start()
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun showCollaborator(bundle: Bundle) {
        findNavController(R.id.nav_host_fragment).navigate(R.id.action_emplyeeColabFragment,bundle)
    }
    private fun validateNetwork() {
        dataConnection.observe(this) { isNetworkAvailable ->
            if (isNetworkAvailable!=null){
                if (isNetworkAvailable == true){
                    mainVM.funGetToken()
                    startServiceOffline()
                    statusNet.value = true
                }else{
                    statusNet.value = false
                }
            }
        }
    }
    override fun startServiceOffline(){
        if (!settingsVM.statusDataOffline.hasObservers()){
            settingsVM.getOfflineServices(this)
            settingsVM.statusDataOffline.observe(this){status->
                if (status != null){
                    when (status) {
                        is ApiResponceStatus.Loading -> {
                            binding.linearProgressIndicator.isVisible = true
                        }
                        is ApiResponceStatus.Success -> stopObserverService()
                        is ApiResponceStatus.Error -> stopObserverService()
                    }
                }
            }
        }
    }
    private fun validateSpeach(){
       val lenguage = preferences.getLanguage(this)
        isSpeach = if (lenguage != null && lenguage.voiceAssistant){
            speachRecognition.initSpeechRecognizer()
            true
        }else{
            false
        }
    }
    override fun startSpeach() {
        speachRecognition.startSpeech()
    }
    override fun stopSpeach(){
        speachRecognition.stopSpeech()
    }
    private fun stopObserverService() {
        settingsVM.statusDataOffline.value = null
        settingsVM.statusDataOffline.removeObservers(this)
        binding.linearProgressIndicator.isVisible = false
        mainVM.getServicesOffline()
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
        unregisterReceiver(wifiStateReceiver)
        unregisterReceiver(locationReceiver)
        speachRecognition.stopSpeech()
        mainVM.dataExistUser.removeObservers(this)
    }
    override fun getDialogNetwork(){
        showErrorDialog(getString(R.string.conexion_internet),this.supportFragmentManager)
    }
    override fun showIconToolbar(isShowing: Boolean, type: Int) {
        binding.btnRefresh.isVisible = isShowing
        when(type){
            1 -> {
                binding.btnRefresh.setOnClickListener {
                    if (statusNet.value == true) {
                        startServiceOffline()
                    }else if(statusNet.value == false){
                        getDialogNetwork()
                    }
                }
                binding.btnRefresh.setIconResource(R.drawable.ic_sync)
            }
            2,3 -> {
                binding.btnRefresh.setOnClickListener {
                    if (statusNet.value == true) {
                        mainVM.funGetEmployees(this)
                        getStatus()
                    }else{
                        getDialogNetwork()
                    }
                }
                if(type == 2){
                    binding.btnRefresh.setIconResource(R.drawable.ic_download)
                }
                if (type == 3){
                    binding.btnRefresh.setIconResource(R.drawable.ic_reload)
                }
            }
        }
    }
    override fun showImageToolbar(isShowing: Boolean) {
        binding.imgToolbar.isVisible = isShowing
    }

    override fun showDialogProgress(isShowing: Boolean,checkType:String?) {
        if (isShowing){
            dialogProgress.checkType.value = if(checkType.equals("E")) R.string.checkin_text else R.string.checkout_text
            dialogProgress.show(supportFragmentManager, "dialog")
        }else{
            dialogProgress.dismiss()
        }
    }

    private fun getStatus() {
        mainVM.statusData.observe(this) { status ->
            if(status != null){
                when (status) {
                    is ApiResponceStatus.Loading -> binding.linearProgressIndicator.isVisible = true
                    is ApiResponceStatus.Success ->{
                        stopObserver()
                    }
                    is ApiResponceStatus.Error -> {
                        stopObserver()
                        val text = Utilities.textcode(status.messageId, this)
                        showErrorDialog(text, this.supportFragmentManager)
                    }
                }
            }
        }
    }
    private fun stopObserver() {
        mainVM.statusData.value = null
        mainVM.statusData.removeObservers(this)
        binding.linearProgressIndicator.isVisible = false
    }
}