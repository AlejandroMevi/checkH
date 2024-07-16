package com.venturessoft.human.core.camera

import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.blinkDetection
import com.venturessoft.human.main.ui.activitys.PrincipalActivity.Companion.faceDetection

class GraphicFaceTracker internal constructor(private val mOverlay: GraphicOverlay) : Tracker<Face>() {
    private val mFaceGraphic: FaceGraphic = FaceGraphic(mOverlay)
    private var leftEyeOpenProbability = -1.0
    private var rightEyeOpenProbability = -1.0
    private var currentEyess: Boolean = false
    companion object {
        var rostroDetectado: Boolean? = false
        var detectarParpadeo: Boolean? = false
        var nParpadeo: Int = 0
        fun resetBlink() {
            nParpadeo = -1
        }
    }
    override fun onNewItem(faceId: Int, item: Face) {
        rostroDetectado = true
        faceDetection.postValue(rostroDetectado)
        mFaceGraphic.setId(faceId)
    }
    override fun onUpdate(p0: Detector.Detections<Face>, face: Face) {
        mOverlay.add(mFaceGraphic)
        face.let { mFaceGraphic.updateFace(it) }
        val currentLeftEyeOpenProbability: Float = face.isLeftEyeOpenProbability
        val currentRightEyeOpenProbability: Float = face.isRightEyeOpenProbability
        if (currentLeftEyeOpenProbability < 0.5 && currentRightEyeOpenProbability < 0.5) {
            currentEyess = true
        }
        if (leftEyeOpenProbability > 0.5 && rightEyeOpenProbability > 0.5 && currentEyess) {
            detectarParpadeo = true
            blinkDetection.postValue(detectarParpadeo)
            nParpadeo += 1
            leftEyeOpenProbability = currentLeftEyeOpenProbability.toDouble()
            rightEyeOpenProbability = currentRightEyeOpenProbability.toDouble()
            currentEyess = false
        } else {
            leftEyeOpenProbability = currentLeftEyeOpenProbability.toDouble()
            rightEyeOpenProbability = currentRightEyeOpenProbability.toDouble()
        }
    }
    override fun onMissing(p0: Detector.Detections<Face>) {
        mOverlay.remove(mFaceGraphic)
        nParpadeo = 0
    }
    override fun onDone() {
        nParpadeo = 0
        rostroDetectado = false
        detectarParpadeo = false
        faceDetection.postValue(rostroDetectado)
        blinkDetection.postValue(detectarParpadeo)
        FaceGraphic.finRostro()
        mOverlay.remove(mFaceGraphic)
    }
}