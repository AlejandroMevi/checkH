package com.venturessoft.human.core.camera

import android.media.ExifInterface

class ConfigCameraVision {
    companion object {
        fun orientacionCamara(orientacion: Int, matrix: android.graphics.Matrix): android.graphics.Matrix {
            when (orientacion) {
                ExifInterface.ORIENTATION_NORMAL -> matrix.postRotate((0).toFloat())
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale((-1).toFloat(), (1).toFloat())
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate((180).toFloat())
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                    matrix.setRotate((180).toFloat())
                    matrix.postScale((-1).toFloat(), (1).toFloat())
                }
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.setRotate((90).toFloat())
                    matrix.postScale((-1).toFloat(), (1).toFloat())
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    matrix.setRotate((90).toFloat())
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.setRotate((-90).toFloat())
                    matrix.postScale((-1).toFloat(), (1).toFloat())
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    matrix.setRotate((-90).toFloat())
                }
            }
            return matrix
        }
    }
}