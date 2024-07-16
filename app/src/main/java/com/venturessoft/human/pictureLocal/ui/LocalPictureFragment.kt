package com.venturessoft.human.pictureLocal.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.venturessoft.human.R
import com.venturessoft.human.core.camera.Camera
import com.venturessoft.human.core.camera.GraphicFaceTracker
import com.venturessoft.human.core.utils.Constants
import com.venturessoft.human.databinding.FragmentLocalPictureBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocalPictureFragment : Fragment() {
    private lateinit var binding: FragmentLocalPictureBinding
    private lateinit var camera: Camera
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLocalPictureBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        camera = Camera(this, binding.cameraSourcePreview, binding.graphicOverlay)
        binding.btnBeacon.setOnClickListener {
            changeCamera()
        }
        binding.btnIn.setOnClickListener {
            takeFotoAuto()
        }
    }
    override fun onResume() {
        super.onResume()
        checkCameraPermission()
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
    private fun takeFotoAuto() {
        if (camera.mCameraSource != null) {
            camera.mCameraSource?.takePicture(null) {bytearray->
                try {
                    val bunbdle = Bundle()
                    bunbdle.putByteArray("image",bytearray)
                    findNavController().navigate(R.id.action_localPicturePreviewFragment,bunbdle)
                } catch (_: OutOfMemoryError) {
                }
            }
        }
    }
    private fun checkCameraPermission() {
        val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), Constants.PERMISSION_CAMERA_REQUEST_CODE)
        } else {
            camera.createCameraSource(CameraSource.CAMERA_FACING_FRONT)
        }
    }
    override fun onStop() {
        super.onStop()
        camera.mCameraSource?.stop()
    }
}