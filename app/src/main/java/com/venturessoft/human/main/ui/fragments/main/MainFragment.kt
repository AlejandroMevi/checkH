package com.venturessoft.human.main.ui.fragments.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
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
import com.venturessoft.human.core.DataUser
import com.venturessoft.human.core.DataUser.Companion.employeeData
import com.venturessoft.human.core.camera.Camera
import com.venturessoft.human.core.camera.ConfigCameraVision
import com.venturessoft.human.core.camera.Exif
import com.venturessoft.human.core.camera.GraphicFaceTracker
import com.venturessoft.human.core.utils.Constants
import com.venturessoft.human.core.utils.Constants.Companion.COLLABORATOR
import com.venturessoft.human.core.utils.Constants.Companion.FORMAT_DATE
import com.venturessoft.human.core.utils.Constants.Companion.SUCCESS_DATA
import com.venturessoft.human.core.utils.DialogGeneral
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.core.utils.Utilities.Companion.getDayOfDate
import com.venturessoft.human.core.utils.Utilities.Companion.getNameDevice
import com.venturessoft.human.core.utils.Utilities.Companion.observeOnce
import com.venturessoft.human.core.utils.Utilities.Companion.reduceImageSize
import com.venturessoft.human.core.utils.Utilities.Companion.reverseOrderOfWords
import com.venturessoft.human.databinding.FragmentMainBinding
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
import com.venturessoft.human.main.ui.vm.MainVM
import dagger.hilt.android.AndroidEntryPoint
import org.joda.time.LocalDate
import java.util.TimeZone


@AndroidEntryPoint
class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var camera: Camera
    private var mainInterface: MainInterface? = null
    private var baseInterface: BaseInterface? = null
    private val mainVM: MainVM by activityViewModels()
    private val employeVM: EmployeVM by activityViewModels()
    private var emplyeCollaborator: String? = null

    companion object {
        var checkType = ""
        var dataSucces = SuccesModel()
        var mainFaceDetection = FaceDetectionRequest()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        camera = Camera(this, binding.cameraSourcePreview, binding.graphicOverlay)
        binding.clRoot.setOnClickListener {
            if (binding.etEmplyeColab.isFocusable) {
                val viewEmploe = requireActivity().currentFocus
                if (viewEmploe != null) {
                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(viewEmploe.windowToken, 0)
                }
                requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
                binding.etEmplyeColab.clearFocus()
            }
        }
        setOnClickEvents()
    }

    @SuppressLint("SetTextI18n")
    private fun setDataUser() {
        mainVM.dataMovement.observe(viewLifecycleOwner) { movement ->
            if (movement != null) {
                try {
                    var tipoChec = movement.tipoChecada
                    tipoChec = if (tipoChec == "E") getString(R.string.checkin) else getString(R.string.checkout)
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
        val date = localDate.toString(FORMAT_DATE)
        binding.tvDay.text = getDayOfDate(requireContext())
        binding.tvDate.text = date
    }

    private fun getName() {
        binding.tvName.text = DataUser.userData.name
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
        binding.constraintLayout2.isVisible = emplyeCollaborator.isNullOrEmpty()
        binding.etEmplyeColab.setOnEditorActionListener { _: TextView?, actionId: Int, event: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event?.action
                    ?: -1) == KeyEvent.ACTION_DOWN
            ) {
                if (!binding.etEmplyeColab.text.isNullOrEmpty()) {
                    mainVM.funGetEmploye(
                        requireActivity(),
                        binding.etEmplyeColab.text.toString(),
                        employeVM
                    )
                    getStatus(false)
                } else {
                    val dialogColab = DialogGeneral(
                        getString(R.string.colab_mode),
                        getString(R.string.error_admin_employee_empty)
                    )
                    dialogColab.show(childFragmentManager, "dialog")
                }
            }
            false
        }
        binding.btnBeacon.setOnClickListener {
            changeCamera()
        }
        binding.btnIn.setOnClickListener {
            checkType = "E"
            validateData()
        }
        binding.btnOut.setOnClickListener {
            checkType = "S"
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
            if (Utilities.isDevMode(requireContext())) {
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
        binding.etEmplyeColab.setText("")
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
                    mainInterface?.showDialogProgress(true, checkType)
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
                    val scaledImage1 = reduceImageSize(photoBitmap, 800)
                    val registerModel = getRegisterModel(scaledImage1)
                    mainFaceDetection = FaceDetectionRequest(Utilities.bitmapToBase64(scaledImage1))
                    mainVM.funFaceDetection(requireActivity(), registerModel)
                    binding.tvDetectFace.isVisible = false
                    getStatus(true)
                } catch (_: Exception) {
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
        registerCheckModel.numeroCompania = DataUser.userData.companyId.toString()
        registerCheckModel.empleado = DataUser.userData.employeeId.toString()
        registerCheckModel.tipoChecada = checkType
        registerCheckModel.dispositivo = getNameDevice(requireContext())
        registerCheckModel.foto = Utilities.bitmapToBase64(photoBitmap)
        registerCheckModel.prioridad = DataUser.userData.priority.toString()
        if (DataUser.userData.fotoLocal) {
            registerCheckModel.fotoModelo =
                Utilities.uriToBase64(Uri.parse(DataUser.userData.localPictureUriActual))
            registerCheckModel.token = DataUser.userData.fotoValidaActual
        }
        return registerCheckModel
    }

    private fun getStatus(isDialog: Boolean) {
        mainVM.statusData.observe(viewLifecycleOwner) { status ->
            if (status != null) {
                when (status) {
                    is ApiResponceStatus.Loading -> {
                        if (!isDialog) {
                            mainInterface?.showLoading(true)
                        }
                    }
                    is ApiResponceStatus.Success -> {
                        if (!binding.etEmplyeColab.text.isNullOrEmpty()) {
                            if (statusNet.value == true) {
                                mainVM.funGetMovement()
                            }
                            binding.etEmplyeColab.setText("")
                            goToNextView()
                        }
                        stopObserverService(true, isDialog)
                    }
                    is ApiResponceStatus.Error -> {
                        val text = Utilities.textcode(status.messageId, requireContext())
                        Utilities.showErrorDialog(text, childFragmentManager)
                        stopObserverService(false, isDialog)
                    }
                }
            }
        }
        if (isDialog) {
            mainVM.isRegisterCheck.observeOnce(viewLifecycleOwner) { isRegister ->
                if (isRegister != null && isRegister) {
                    mainVM.isRegisterCheck.removeObservers(viewLifecycleOwner)
                    mainVM.isRegisterCheck.value = null
                    val bundle = Bundle()
                    bundle.putSerializable(SUCCESS_DATA, dataSucces)
                    mainInterface?.showDialogProgress(false, null)
                    findNavController().navigate(R.id.action_mainFragment_to_succesFragment, bundle)
                }
            }
        }
    }

    private fun goToNextView() {
        baseInterface?.getBeacons(true)
        baseInterface?.getLocation()
        baseInterface?.getBSSID(false)
        val bundle = Bundle()
        bundle.putString(COLLABORATOR, binding.etEmplyeColab.text.toString())
        mainInterface?.showCollaborator(bundle)
    }

    private fun stopObserverService(cleanBeacons: Boolean, isDialog: Boolean) {
        baseInterface?.getBeacons(cleanBeacons)
        baseInterface?.getLocation()
        baseInterface?.getBSSID(false)
        mainVM.statusData.removeObservers(this)
        mainVM.statusData.value = null
        binding.etEmplyeColab.setText("")
        isEnableButton(true)
        if (isDialog) {
            mainInterface?.showDialogProgress(false, null)
        }
        mainInterface?.showLoading(false)
    }

    private fun isEnableButton(isEnable: Boolean) {
        binding.tvDetectFace.isVisible = false
        binding.btnIn.isEnabled = isEnable
        binding.btnOut.isEnabled = isEnable
        binding.btnIn.isCheckable = isEnable
        binding.btnOut.isCheckable = isEnable
    }

    override fun onResume() {
        super.onResume()
        mainInterface?.showImageToolbar(true)
        mainVM.dataExistUser.observe(this) {
            validateNet()
            setDataUser()
        }
        employeeData = UserEntity()
        getDataToCheck()
        checkCameraPermission()
        mainInterface?.setupActionBarWithNavController(true)
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
        Handler(Looper.getMainLooper()).postDelayed({
            mainVM.funGetEmployeLogin2(requireActivity(), childFragmentManager)
        }, 1000)
        binding.constraintLayout2.isVisible = !DataUser.userData.fotoLocal
        if (TimeZone.getDefault().id == "America/Mexico_City") {
            binding.tvTime.format12Hour = "hh:mm:ss a"
            binding.tvTime.timeZone = "GMT-06:00"
        }
    }

    private fun validateNet() {
        statusNet.observe(viewLifecycleOwner) { status ->
            if (status == true) {
                mainVM.funGetMovement()
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
                when (speach) {
                    getString(R.string.checkin_text) -> {
                        binding.btnIn.callOnClick()
                    }

                    getString(R.string.checkout_text) -> {
                        binding.btnOut.callOnClick()
                    }

                    else -> {
                        mainVM.getKeyWord(
                            requireActivity(),
                            speach,
                            employeVM,
                            binding.etEmplyeColab
                        )
                        getStatus(false)
                    }
                }
                textSpeach.value = null
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isSpeach) {
            mainInterface?.stopSpeach()
        }
        mainVM.dataMovement.removeObservers(this)
        dataListBeacons.removeObservers(this)
        dataLocation.removeObservers(this)
        dataBSSIDList.removeObservers(this)
        statusNet.removeObservers(this)
        textSpeach.removeObservers(this)
        camera.mCameraSource?.stop()
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


