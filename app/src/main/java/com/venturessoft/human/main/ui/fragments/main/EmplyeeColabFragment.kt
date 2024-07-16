package com.venturessoft.human.main.ui.fragments.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.venturessoft.human.BuildConfig
import com.venturessoft.human.R
import com.venturessoft.human.core.ApiResponceStatus
import com.venturessoft.human.core.BaseActivity.Companion.dataBSSIDConected
import com.venturessoft.human.core.BaseActivity.Companion.dataBSSIDList
import com.venturessoft.human.core.BaseActivity.Companion.dataListBeacons
import com.venturessoft.human.core.BaseActivity.Companion.dataLocation
import com.venturessoft.human.core.DataUser.Companion.employeeData
import com.venturessoft.human.core.camera.Camera
import com.venturessoft.human.core.camera.ConfigCameraVision
import com.venturessoft.human.core.camera.Exif
import com.venturessoft.human.core.camera.GraphicFaceTracker
import com.venturessoft.human.core.utils.Constants
import com.venturessoft.human.core.utils.Constants.Companion.SUCCESS_DATA
import com.venturessoft.human.core.utils.DialogGeneral
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.core.utils.Utilities.Companion.isDevMode
import com.venturessoft.human.core.utils.Utilities.Companion.observeOnce
import com.venturessoft.human.core.utils.Utilities.Companion.reverseOrderOfWords
import com.venturessoft.human.databinding.FragmentEmplyeeColabBinding
import com.venturessoft.human.login.data.models.UserEntity
import com.venturessoft.human.main.data.models.BSSIDModel
import com.venturessoft.human.main.data.models.BeaconsModel
import com.venturessoft.human.main.data.models.FaceDetectionRequest
import com.venturessoft.human.main.data.models.LocationModel
import com.venturessoft.human.main.data.models.RootRegister
import com.venturessoft.human.main.data.models.SuccesModel
import com.venturessoft.human.main.ui.activitys.PrincipalActivity
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.blinkDetection
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.isSpeach
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.statusNet
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.textSpeach
import com.venturessoft.human.main.ui.interfaces.BaseInterface
import com.venturessoft.human.main.ui.interfaces.MainInterface
import com.venturessoft.human.main.ui.vm.EmployeVM
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.LocalDate
import java.util.TimeZone

@AndroidEntryPoint
class EmplyeeColabFragment : Fragment() {

    private lateinit var binding: FragmentEmplyeeColabBinding
    private lateinit var camera: Camera
    private var mainInterface: MainInterface? = null
    private var baseInterface: BaseInterface? = null
    private val employeVM: EmployeVM by activityViewModels()

    companion object {
        var checkTypeColab = ""
        var dataSucces = SuccesModel()
        var mainFaceDetectionColab = FaceDetectionRequest()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEmplyeeColabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        camera = Camera(this, binding.cameraSourcePreview, binding.graphicOverlay)
        setOnClickEvents()
    }

    @SuppressLint("SetTextI18n")
    private fun setDataUser() {
        employeVM.dataMovementColab.observe(viewLifecycleOwner) { movement ->
            if (movement != null) {
                try {
                    var tipoChec = movement.tipoChecada
                    tipoChec =
                        if (tipoChec == "E") getString(R.string.checkin) else getString(R.string.checkout)
                    binding.checkType.text = "${getString(R.string.last)} $tipoChec:"
                    binding.checkDate.text = reverseOrderOfWords(movement.horaChecada)
                    binding.checkTime.text = movement.horaChecada.substring(11, 16)
                    binding.checkType.isVisible = true
                    binding.checkDate.isVisible = true
                    binding.checkTime.isVisible = true
                    binding.at.text = getString(R.string.at)
                } catch (exeption: java.lang.Exception) {
                    binding.checkType.isVisible = false
                    binding.checkDate.isVisible = false
                    binding.checkTime.isVisible = false
                    binding.at.text = getString(R.string.message_validate_check)
                }
            } else {
                binding.checkType.isVisible = false
                binding.checkDate.isVisible = false
                binding.checkTime.isVisible = false
                binding.at.text = getString(R.string.message_validate_check)
            }
            getDate()
            getName()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getDate() {
        val localDate = LocalDate.now()
        val date = localDate.toString(Constants.FORMAT_DATE)
        binding.tvDay.text = Utilities.getDayOfDate(requireContext())
        binding.tvDate.text = date
    }

    private fun getName() {
        binding.tvName.text = employeeData.name
    }

    private fun getDataToCheck() {
        getBeacons()
        getLocation()
        getBSSID()
    }

    private fun getBeacons() {
        dataListBeacons.observe(viewLifecycleOwner) { lisBeaconsResult ->
            if (lisBeaconsResult.isNullOrEmpty()) {
                binding.icBeaconsSelected.imageTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.viewColor4)
            } else {
                binding.icBeaconsSelected.imageTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.viewColor5)
            }
        }
    }

    private fun getLocation() {
        dataLocation.observe(viewLifecycleOwner) { locationResult ->
            if (locationResult != null && locationResult.latitud != "0.0") {
                binding.icLocationSelected.imageTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.viewColor5)
            } else {
                binding.icLocationSelected.imageTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.viewColor4)
            }
        }
    }

    private fun getBSSID() {
        dataBSSIDList.observe(viewLifecycleOwner) { listBssidResult ->
            dataBSSIDConected.observe(viewLifecycleOwner) { bssidResult ->
                if (!bssidResult?.bssid.isNullOrEmpty() || !listBssidResult.isNullOrEmpty()) {
                    binding.icBssidSelected.imageTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.viewColor5)
                } else {
                    binding.icBssidSelected.imageTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.viewColor4)
                }
            }
        }
    }

    private fun setOnClickEvents() {
        binding.btnBeacon.setOnClickListener {
            changeCamera()
        }
        binding.btnIn.setOnClickListener {
            checkTypeColab = "E"
            validateData()
        }
        binding.btnOut.setOnClickListener {
            checkTypeColab = "S"
            validateData()
        }
    }

    private fun changeCamera() {
        camera.mCameraSource?.release()
        GraphicFaceTracker.resetBlink()
        if (camera.mCameraSource?.cameraFacing == CameraSource.CAMERA_FACING_FRONT) {
            camera.createCameraSource(CameraSource.CAMERA_FACING_BACK)
        } else {
            camera.createCameraSource(CameraSource.CAMERA_FACING_FRONT)
        }
    }

    private fun validateData() {
        if (!BuildConfig.DEBUG) {
            if (isDevMode(requireContext())) {
                val dialogDeveloper =
                    DialogGeneral(null, getString(R.string.error_developer_mode), null, null,
                        {
                            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            requireContext().startActivity(intent)
                        }
                    )
                dialogDeveloper.show(childFragmentManager, "dialog")
                return
            }
            if (!Utilities.isAutoTimeEnabled(requireActivity())) {
                val dialogDate = DialogGeneral(null, getString(R.string.active_date), null, null,
                    {
                        val intent = Intent(Settings.ACTION_DATE_SETTINGS)
                        requireContext().startActivity(intent)
                    }
                )
                dialogDate.show(childFragmentManager, "dialog")
                return
            }
            if (!Utilities.isAutoTimeZoneEnabled(requireActivity())) {
                val dialogZone = DialogGeneral(null, getString(R.string.active_zone), null, null,
                    {
                        val intent = Intent(Settings.ACTION_DATE_SETTINGS)
                        requireContext().startActivity(intent)
                    }
                )
                dialogZone.show(childFragmentManager, "dialog")
                return
            }
        }
        if (dataListBeacons.value.isNullOrEmpty() || dataListBeacons.value?.size == 0) {
            baseInterface?.getBeacons(false)
        }
        if (dataBSSIDConected.value?.bssid.isNullOrEmpty()) {
            baseInterface?.getBSSID(false)
        }
        if (dataLocation.value == null || dataLocation.value?.latitud.isNullOrEmpty() || dataLocation.value?.longitud.isNullOrEmpty() || dataLocation.value?.latitud == "0.0" || dataLocation.value?.longitud == "0.0") {
            if (baseInterface?.isLocationEnabled() == true) {
                Utilities.showErrorDialog(
                    getString(R.string.error_no_location_found),
                    childFragmentManager
                )
            }
            baseInterface?.getLocation()
            return
        }
        mainInterface?.stopSpeach()
        takeFotoAuto()
    }

    private fun takeFotoAuto() {
        binding.tvDetectFace.isVisible = true
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.anim_alpha_infinite)
        if (!blinkDetection.hasObservers()) {
            binding.tvDetectFace.startAnimation(animation)
            blinkDetection.observe(viewLifecycleOwner) { isDetected ->
                if (isDetected) {
                    mainInterface?.showDialogProgress(true, checkTypeColab)
                    blinkDetection.removeObservers(viewLifecycleOwner)
                    isEnableButton(false)
                    takePhoto()
                    binding.tvDetectFace.clearAnimation()
                }
            }
        }
    }

    private fun takePhoto() {
        if (camera.mCameraSource != null) {
            camera.mCameraSource?.takePicture(null) { bytearray ->
                try {
                    val orientacion = Exif.getOrientation(bytearray)
                    val bitmap = BitmapFactory.decodeByteArray(bytearray, 0, bytearray.size)
                    val oreintacionCam = ConfigCameraVision.orientacionCamara(orientacion, Matrix())
                    val photoBitmap = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
                        oreintacionCam,
                        false
                    )
                    val scaledImage1 = Utilities.reduceImageSize(photoBitmap, 800)
                    val registerModel = getRegisterModel(scaledImage1)
                    mainFaceDetectionColab =
                        FaceDetectionRequest(Utilities.bitmapToBase64(scaledImage1))
                    employeVM.funFaceDetection(requireActivity(), registerModel)
                    binding.tvDetectFace.isVisible = false
                    getStatus()
                } catch (_: OutOfMemoryError) {
                    camera = Camera(this, binding.cameraSourcePreview, binding.graphicOverlay)
                    checkCameraPermission()
                    mainInterface?.showDialogProgress(false, null)
                    isEnableButton(true)
                    Utilities.showErrorDialog(getString(R.string.retry_error), childFragmentManager)
                }
            }
        } else {
            camera = Camera(this, binding.cameraSourcePreview, binding.graphicOverlay)
            checkCameraPermission()
            mainInterface?.showDialogProgress(false, null)
            isEnableButton(true)
            Utilities.showErrorDialog(getString(R.string.retry_error), childFragmentManager)
        }
    }

    private fun getRegisterModel(photoBitmap: Bitmap): RootRegister {
        val listBSSID = mutableListOf<String>()
        dataBSSIDConected.value?.bssid?.let { fisrtBssid ->
            listBSSID.add(fisrtBssid)
        }
        dataBSSIDList.value?.forEach { bssid ->
            listBSSID.add(bssid.bssid)
        }
        val registerCheckModel = RootRegister()
        registerCheckModel.sGeolocalizacion = dataLocation.value ?: LocationModel()
        registerCheckModel.sBeacons = BeaconsModel(dataListBeacons.value ?: arrayListOf())
        registerCheckModel.sBssids = BSSIDModel(listBSSID)
        registerCheckModel.numeroCompania = employeeData.companyId.toString()
        registerCheckModel.empleado = employeeData.employeeId.toString()
        registerCheckModel.tipoChecada = checkTypeColab
        registerCheckModel.dispositivo = Utilities.getNameDevice(requireContext())
        registerCheckModel.foto = Utilities.bitmapToBase64(photoBitmap)
        registerCheckModel.prioridad = employeeData.priority.toString()
        return registerCheckModel
    }

    private fun getStatus() {
        employeVM.statusDataColab.observe(viewLifecycleOwner) { status ->
            if (status != null) {
                when (status) {
                    is ApiResponceStatus.Loading -> {}
                    is ApiResponceStatus.Success -> {
                        if (statusNet.value == true) {
                            employeVM.funGetMovement(requireActivity())
                        }
                        stopObserverService(true)
                    }

                    is ApiResponceStatus.Error -> {
                        stopObserverService(false)
                        val text = Utilities.textcode(status.messageId, requireContext())
                        Utilities.showErrorDialog(text, childFragmentManager)
                    }
                }
            }
        }
        employeVM.isRegisterCheck.observeOnce(viewLifecycleOwner) { isRegister ->
            if (isRegister != null && isRegister) {
                employeVM.isRegisterCheck.removeObservers(viewLifecycleOwner)
                employeVM.isRegisterCheck.value = null
                val bundle = Bundle()
                bundle.putSerializable(SUCCESS_DATA, dataSucces)
                mainInterface?.showDialogProgress(false, null)
                findNavController().navigate(R.id.action_mainFragment_to_succesFragment, bundle)
            }
        }
    }

    private fun stopObserverService(cleanBeacons: Boolean) {
        baseInterface?.getBeacons(cleanBeacons)
        baseInterface?.getLocation()
        baseInterface?.getBSSID(false)
        employeVM.statusDataColab.removeObservers(this)
        employeVM.statusDataColab.value = null
        isEnableButton(true)
        mainInterface?.showDialogProgress(false, null)
    }

    private fun isEnableButton(isEnable: Boolean) {
        binding.tvDetectFace.isVisible = false
        binding.btnIn.isEnabled = isEnable
        binding.btnOut.isEnabled = isEnable
        binding.btnIn.isActivated = isEnable
        binding.btnOut.isActivated = isEnable
        binding.btnIn.isCheckable = isEnable
        binding.btnOut.isCheckable = isEnable
    }

    override fun onResume() {
        super.onResume()
        camera.mCameraSource?.release()
        validateNet()
        setDataUser()
        getDataToCheck()
        checkCameraPermission()
        mainInterface?.setupActionBarWithNavController(false)
        employeVM.dataUserColab.observe(viewLifecycleOwner) { employee ->
            if (employee != null) {
                employeeData = employee
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (dataBSSIDConected.value?.bssid.isNullOrEmpty()) {
                baseInterface?.getBSSID(false)
            }
            if (dataListBeacons.value.isNullOrEmpty()) {
                baseInterface?.getBeacons(false)
            }
            if (dataLocation.value == null) {
                baseInterface?.getLocation()
            }
        }, 5000)
        if (TimeZone.getDefault().id == "America/Mexico_City") {
            binding.tvTime.format12Hour = "hh:mm:ss a"
            binding.tvTime.timeZone = "GMT-06:00"
        }
    }

    private fun validateNet() {
        statusNet.observe(viewLifecycleOwner) { status ->
            if (status == true) {
                employeVM.funGetMovement(requireActivity())
                if (isSpeach) {
                    isStartSpeach()
                }
            } else {
                if (isSpeach) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.assistant_disable),
                        Toast.LENGTH_SHORT
                    ).show()
                    mainInterface?.stopSpeach()
                }
            }
        }
    }

    private fun checkCameraPermission() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                Constants.PERMISSION_CAMERA_REQUEST_CODE
            )
        } else {
            camera.createCameraSource(CameraSource.CAMERA_FACING_FRONT)
        }
    }

    private fun isStartSpeach() {
        mainInterface?.startSpeach()
        PrincipalActivity.isActiveSpeach = true
        textSpeach.observe(viewLifecycleOwner) { speach ->
            if (!speach.isNullOrEmpty()) {
                when (speach.uppercase()) {
                    getString(R.string.checkin_text).uppercase() -> {
                        binding.btnIn.callOnClick()
                    }

                    getString(R.string.checkout_text).uppercase() -> {
                        binding.btnOut.callOnClick()
                    }

                    else -> {
                        textSpeach.value = null
                        mainInterface?.startSpeach()
                    }
                }
                textSpeach.value = null
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isSpeach) {
            mainInterface?.stopSpeach()
        }
        dataListBeacons.removeObservers(this)
        dataLocation.removeObservers(this)
        dataBSSIDList.removeObservers(this)
        statusNet.removeObservers(this)
        textSpeach.removeObservers(this)
        camera.mCameraSource?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        employeeData = UserEntity()
        employeVM.dataUserColab.value = null
        employeVM.dataMovementColab.value = null
        if (isSpeach) {
            mainInterface?.stopSpeach()
        }
        camera.mCameraSource?.stop()
        baseInterface?.getBeacons(true)
        baseInterface?.getLocation()
        baseInterface?.getBSSID(false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainInterface) {
            mainInterface = context
        }
        if (context is BaseInterface) {
            baseInterface = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainInterface = null
        baseInterface = null
    }
}