package com.venturessoft.human.core.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import java.io.IOException
class CameraSourcePreview(private val mContext: Context, attrs: AttributeSet) : ViewGroup(mContext, attrs) {
    private val mSurfaceView: SurfaceView
    private var mStartRequested: Boolean = false
    private var mSurfaceAvailable: Boolean = false
    private var mCameraSource: CameraSource? = null
    private var mOverlay: GraphicOverlay? = null
    private val isPortraitMode: Boolean
        get() {
            val orientation = mContext.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }
            return false
        }
    init {
        mStartRequested = false
        mSurfaceAvailable = false
        mSurfaceView = SurfaceView(mContext)
        mSurfaceView.holder.addCallback(SurfaceCallback())
        addView(mSurfaceView)
    }
    @Throws(IOException::class)
    fun start(cameraSource: CameraSource?) {
        if (cameraSource == null) {
            stop()
        }
        mCameraSource = cameraSource
        if (mCameraSource != null) {
            mStartRequested = true
            startIfReady()
        }
    }
    @Throws(IOException::class)
    fun start(cameraSource: CameraSource, overlay: GraphicOverlay) {
        mOverlay = overlay
        start(cameraSource)
    }
    private fun stop() {
        if (mCameraSource != null) {
            mCameraSource?.stop()
        }
    }
    @SuppressLint("MissingPermission")
    @Throws(IOException::class)
    private fun startIfReady() {
        if (mStartRequested && mSurfaceAvailable) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return
            mCameraSource?.start(mSurfaceView.holder)
            if (mOverlay != null) {
                val size = mCameraSource?.previewSize
                val min = size?.width?.coerceAtMost(size.height)?:0
                val max = size?.width?.coerceAtLeast(size.height)?:0
                if (isPortraitMode) {
                    mOverlay?.setCameraInfo(min, max, mCameraSource?.cameraFacing?:0)
                } else {
                    mOverlay?.setCameraInfo(max, min, mCameraSource?.cameraFacing?:0)
                }
                mOverlay?.clear()
            }
            mStartRequested = false
        }
    }
    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            mSurfaceAvailable = true
            try {
                startIfReady()
            } catch (_: IOException) { }
        }
        override fun surfaceDestroyed(surface: SurfaceHolder) {
            mSurfaceAvailable = false
        }
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var previewWidth = 320
        var previewHeight = 240
        if (mCameraSource != null) {
            val size = mCameraSource?.previewSize
            if (size != null) {
                previewWidth = size.width
                previewHeight = size.height
            }
        }
        if (isPortraitMode) {
            val tmp = previewWidth
            previewWidth = previewHeight
            previewHeight = tmp
        }
        val viewWidth = right - left
        val viewHeight = bottom - top
        val childWidth: Int
        val childHeight: Int
        var childXOffset = 0
        var childYOffset = 0
        val widthRatio = viewWidth.toFloat() / previewWidth.toFloat()
        val heightRatio = viewHeight.toFloat() / previewHeight.toFloat()
        if (widthRatio > heightRatio) {
            childWidth = viewWidth
            childHeight = (previewHeight.toFloat() * widthRatio).toInt()
            childYOffset = (childHeight - viewHeight) / 2
        } else {
            childWidth = (previewWidth.toFloat() * heightRatio).toInt()
            childHeight = viewHeight
            childXOffset = (childWidth - viewWidth) / 2
        }
        for (i in 0 until childCount) {
            getChildAt(i).layout(-1 * childXOffset, -1 * childYOffset, childWidth - childXOffset, childHeight - childYOffset)
        }
        try {
            startIfReady()
        } catch (_: IOException) { }
    }
}