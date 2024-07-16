package com.venturessoft.human.core.camera

import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.face.FaceDetector
import java.io.IOException
class Camera(
    private val fragment: Fragment,
    private val cameraSourcePreview:CameraSourcePreview,
    private val graphicOverlay:GraphicOverlay
) {
    var mCameraSource: CameraSource? = null
    fun createCameraSource(cameraFacing: Int) {
        val detector = FaceDetector.Builder(fragment.requireContext())
            .setProminentFaceOnly(true)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setMode(FaceDetector.SELFIE_MODE)
            .setTrackingEnabled(true)
            .build()
        detector.setProcessor(
            MultiProcessor.Builder(GraphicFaceTrackerFactory(graphicOverlay)).build()
        )
        mCameraSource = CameraSource.Builder(fragment.requireContext(), detector)
            .setAutoFocusEnabled(true)
            .setFacing(cameraFacing)
            .build()
        startCameraSource()
    }
    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(fragment.requireContext())
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(fragment.requireActivity(), code, 9001)
            dlg?.show()
        }
        try {
            mCameraSource?.let {camera->
                cameraSourcePreview.start(camera, graphicOverlay)
            }
        } catch (e: IOException) {
            mCameraSource?.release()
        }
    }
}
