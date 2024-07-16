package com.venturessoft.human.pictureLocal.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.venturessoft.human.R
import com.venturessoft.human.core.ApiResponceStatus
import com.venturessoft.human.core.DataUser
import com.venturessoft.human.core.camera.ConfigCameraVision
import com.venturessoft.human.core.camera.Exif
import com.venturessoft.human.core.utils.DialogGeneral
import com.venturessoft.human.core.utils.Utilities
import com.venturessoft.human.databinding.FragmentLocalPicturePreviewBinding
import com.venturessoft.human.main.ui.vm.MainVM
import com.venturessoft.human.pictureLocal.data.models.LocalPictureRquest
import com.venturessoft.human.pictureLocal.ui.interfaces.PictureLocalInterface
import dagger.hilt.android.AndroidEntryPoint
import java.nio.ByteBuffer
import java.util.Arrays

@AndroidEntryPoint
class LocalPicturePreviewFragment : Fragment() {

    private lateinit var binding: FragmentLocalPicturePreviewBinding
    private var bytearray: ByteArray? = null
    private var interfaces: PictureLocalInterface? = null
    private val mainVM: MainVM by activityViewModels()
    private var uriPendiente = ""
    private var hash = ""
    private var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            bytearray = bundle.getByteArray("image")
        }
        editTokenSharedPreferences()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalPicturePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (bytearray != null) {
            if (DataUser.userData.localPictureUriActual.isNotEmpty()) {
                val profileUri =
                    Utilities.uriToBase64(Uri.parse(DataUser.userData.localPictureUriActual))
                val bitmap: Bitmap = Utilities.base64ToBitmap(profileUri)
                Glide.with(this).load(bitmap).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(binding.imgActual)
                binding.llActual.isVisible = true
            } else {
                binding.llActual.isVisible = false
            }
            val orientacion = Exif.getOrientation(bytearray)
            val bitmap = BitmapFactory.decodeByteArray(bytearray, 0, bytearray!!.size)
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
            hash = getUniqueBitmapFileName(scaledImage1)
            uriPendiente = Utilities.saveImageToStorage(
                requireActivity(),
                scaledImage1,
                "LocalPicture${DataUser.userData.employeeId}",
                "LocalPicture${DataUser.userData.employeeId}"
            )
            Glide.with(this).load(scaledImage1).diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(binding.imgNueva)
        } else {
            if (DataUser.userData.localPictureUriActual.isNotEmpty()) {
                val profileUri =
                    Utilities.uriToBase64(Uri.parse(DataUser.userData.localPictureUriActual))
                val bitmap: Bitmap = Utilities.base64ToBitmap(profileUri)
                Glide.with(this).load(bitmap).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(binding.imgActual)
            } else {
                binding.llActual.isVisible = false
            }
            if (DataUser.userData.localPictureUriPending.isNotEmpty()) {
                binding.llNueva.isVisible = true
                binding.tvStatus.text = DataUser.userData.statusFoto
                when (DataUser.userData.statusFoto){
                    "P"-> {
                        binding.tvStatusPicture.text = getString(R.string.local_picure_picture_pending)
                    }
                    else -> {
                    }
                }
                val profileUri = Utilities.uriToBase64(Uri.parse(DataUser.userData.localPictureUriPending))
                val bitmap: Bitmap = Utilities.base64ToBitmap(profileUri)
                val scaledImage1 = Utilities.reduceImageSize(bitmap, 800)
                hash = getUniqueBitmapFileName(scaledImage1)
                uriPendiente = Utilities.saveImageToStorage(
                    requireActivity(),
                    scaledImage1,
                    "LocalPicture${DataUser.userData.employeeId}",
                    "LocalPicture${DataUser.userData.employeeId}"
                )
                Glide.with(this).load(bitmap).diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).into(binding.imgNueva)
            } else {
                binding.llNueva.isVisible = false
            }
            binding.btnAcept.text = getString(R.string.local_picure_picture_change)
        }
        binding.btnAcept.setOnClickListener {
            val userEntity = DataUser.userData
            if (binding.btnAcept.text == getString(R.string.local_picure_picture_change)) {
                val text = if (DataUser.userData.statusFoto != "A") getString(R.string.local_picure_replace_description) else getString(R.string.local_picure_new_picture)
                val dialogDate = DialogGeneral(null, text, getString(R.string.accept), getString(R.string.cancel), {
                    if (DataUser.userData.statusFoto != "A") {
                        if (DataUser.userData.localPictureUriPending.isNotEmpty()) {
                            Utilities.deleteFile(userEntity.localPictureUriPending)
                            userEntity.localPictureUriPending = ""
                            userEntity.fotoValidaPendiente = ""
                            mainVM.setDataUser(userEntity, requireActivity())
                        }
                    }
                    if (DataUser.userData.localPictureUriActual.isEmpty()){
                        findNavController().clearBackStack(R.id.action_localPictureFragment)
                    }
                    findNavController().navigate(R.id.action_localPictureFragment)
                })
                dialogDate.show(childFragmentManager, "dialog")
            } else {
                userEntity.statusFoto = "P"
                userEntity.localPictureUriPending = uriPendiente
                userEntity.fotoValidaPendiente = hash
                val base64Actual = Utilities.uriToBase64(Uri.parse(DataUser.userData.localPictureUriActual))
                val base64Nueva = Utilities.uriToBase64(Uri.parse(uriPendiente))
                val localPictureRquest = LocalPictureRquest()
                localPictureRquest.idCia = DataUser.userData.companyId.toLong()
                localPictureRquest.idEmpleado = DataUser.userData.employeeId
                localPictureRquest.fotoActual = base64Actual
                localPictureRquest.fotoNueva = base64Nueva
                localPictureRquest.hash = hash
                localPictureRquest.token = token
                mainVM.setEnrolarLocal(localPictureRquest, userEntity, requireActivity())
                getStatus()
            }
        }
        binding.btnCancel.setOnClickListener {
            interfaces?.backPressed()
        }
    }

    private fun getStatus() {
        mainVM.statusData.observe(viewLifecycleOwner) { status ->
            if(status != null){
                when (status) {
                    is ApiResponceStatus.Loading -> interfaces?.showLoading(true)
                    is ApiResponceStatus.Success -> interfaces?.showLoading(false)
                    is ApiResponceStatus.Error -> {
                        interfaces?.showLoading(false)
                        val text = Utilities.textcode(status.messageId, requireContext())
                        Utilities.showErrorDialog(text,childFragmentManager)
                    }
                }
            }
        }
    }

    private fun getUniqueBitmapFileName(bitmap: Bitmap): String {
        val buffer = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(buffer)
        return Arrays.hashCode(buffer.array()).toString()
    }

    private fun editTokenSharedPreferences() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String?> ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            token = task.result ?: ""
        }
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is PictureLocalInterface) {
            interfaces = context
        }
    }
    override fun onDetach() {
        super.onDetach()
        interfaces = null
    }
}