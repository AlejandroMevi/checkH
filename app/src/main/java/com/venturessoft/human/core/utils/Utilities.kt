package com.venturessoft.human.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Base64
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.venturessoft.human.R
import com.venturessoft.human.core.utils.Constants.Companion.ERRORHTTP
import com.venturessoft.human.core.utils.Constants.Companion.FACE_FAIL
import com.venturessoft.human.core.utils.Constants.Companion.FACE_NO_DETECTED
import com.venturessoft.human.core.utils.Constants.Companion.REQUEST_PERMISSION_WRITE_STORAGE
import com.venturessoft.human.core.utils.Constants.Companion.SMALL_FACE
import com.venturessoft.human.core.utils.Constants.Companion.TIMEOUT
import com.venturessoft.human.main.data.models.LocationModel
import com.venturessoft.human.main.data.models.TimeZoneResponce
import com.venturessoft.human.models.response.BeaconsItem
import org.joda.time.LocalDate
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.ThreadLocalRandom


class Utilities {
    companion object {
        fun showErrorDialog(message: String, fragmentManager: FragmentManager) {
            val dialogError = DialogGeneral(null,message,null,null,null)
            dialogError.show(fragmentManager, "dialog")
        }
        fun bitmapToBase64(bitmap: Bitmap, quality: Int = 100): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return byteArrayToBase64(byteArray)
        }
        fun base64ToBitmap(encodedImage: String): Bitmap {
            val decodedString: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        }
        private fun byteArrayToBase64(array: ByteArray): String {
            return Base64.encodeToString(array, Base64.NO_WRAP)
        }
        fun textcode(codigo: String, context: Context): String {
            var textvalue: String
            try {
                val contextoPaquete: String = context.packageName
                val indentificadorMensaje = context.resources.getIdentifier(codigo, "string", contextoPaquete)
                textvalue = if (indentificadorMensaje > 0) {
                    context.getString(indentificadorMensaje)
                } else {
                    if (codigo.contains(TIMEOUT)){
                        context.getString(R.string.error_timeout)
                    }else if (codigo.contains(ERRORHTTP)){
                        context.getString(R.string.error_timeout)
                    }
                    else{
                        when(codigo){
                            FACE_FAIL -> context.getString(R.string.error_in_face)
                            SMALL_FACE -> context.getString(R.string.small_face)
                            FACE_NO_DETECTED -> context.getString(R.string.face_no_detected)
                            else -> codigo
                        }
                    }
                }
            } catch (exception: java.lang.Exception) {
                textvalue = context.getString(R.string.error_server_connection)
            }
            return textvalue
        }
        @SuppressLint("SimpleDateFormat")
        fun getDateTime(timeZone: TimeZoneResponce?): ArrayList<String> {
            val dateTimeList = ArrayList<String>()
            if (timeZone != null) {
                val timeFormat = timeZone.formatted
                val dataTime = timeFormat.split(" ")
                dateTimeList.add(dataTime[0])
                dateTimeList.add(dataTime[1])
            } else {
                val zone = if (TimeZone.getDefault().id == "America/Mexico_City"){
                    "GMT-06:00"
                }else{
                    TimeZone.getDefault().id
                }
                val tz2 = TimeZone.getTimeZone(zone)
                val simpleDateFormat = SimpleDateFormat(Constants.DATE_FORMAT)
                simpleDateFormat.timeZone = tz2
                val date = SimpleDateFormat(Constants.PATTERN_DATE_FORMAT)
                date.timeZone = tz2
                dateTimeList.add(date.format(Date()))
                dateTimeList.add(simpleDateFormat.format(Date()))
            }
            return dateTimeList
        }
        fun checkPermissionStorage(activity: Activity): Boolean {
            var isPermission = false
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    isPermission = true
                } else {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_WRITE_STORAGE)
                }
            } else {
                isPermission = true
            }
            return isPermission
        }
        fun saveImageToStorage(activity: Activity, bitmap: Bitmap, folder: String, fileName: String): String {
            var uri = ""
            if (checkPermissionStorage(activity)) {
                val newName = fileName.replace("\\s".toRegex(), "")
                val path = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + folder
                val storagedir = File(path)
                storagedir.mkdirs()
                var fos: OutputStream? = null
                val image = File.createTempFile(newName, ".jpg", storagedir)
                try {
                    fos = FileOutputStream(image)
                } catch (_: Exception) { }
                if (fos != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                }
                if (fos != null) {
                    try {
                        fos.flush()
                        fos.close()
                    } catch (_: Exception) { }
                }
                uri = image.path
            } else {
                Toast.makeText(activity, "No cuentas con permisos de almacenamiento", Toast.LENGTH_SHORT).show()
            }
            return uri
        }
        fun deleteAllFiles(activity: Activity, folder: String) {
            if (checkPermissionStorage(activity)) {
                try {
                    val path = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + folder
                    val file = File(path)
                    file.list()?.forEach {
                        val newFile = File("${file.path}/${it}")
                        if (newFile.exists()) {
                            newFile.delete()
                        }
                    }
                }catch (_:java.lang.Exception){ }
            } else {
                Toast.makeText(activity, "No cuentas con permisos de almacenamiento", Toast.LENGTH_SHORT).show()
            }
        }
        fun uriToBase64(uri: Uri): String {
            var base64 = ""
            try {
                val file = uri.path?.let { File(it) }
                if (file?.exists() == true){
                    val imageStream: InputStream = file.inputStream()
                    val bitMap: Bitmap = BitmapFactory.decodeStream(imageStream)
                    base64 = combertImageToBase64(bitMap)
                    base64 = base64.replace("\n","")
                }
            } catch (_: FileNotFoundException) { }
            return base64
        }
        fun reduceImageSize(bitmap: Bitmap, maxSize: Int): Bitmap {
            val newWidth =( bitmap.height * maxSize)/bitmap.width
            return Bitmap.createScaledBitmap(bitmap, maxSize, newWidth, true)
        }
        fun deleteFile(folder: String) {
            try {
                val uri = Uri.parse(folder)
                val file = uri.path?.let { File(it) }
                file?.delete()
                if (file?.exists() == true) {
                    file.list()?.forEach {
                        val newFile = File("${file.path}/${it}")
                        if (newFile.exists()) {
                            newFile.delete()
                        }
                    }
                }
            } catch (_: java.lang.Exception) {
            }
        }
        private fun combertImageToBase64(bitmapImage: Bitmap?): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageBytes: ByteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(imageBytes, Base64.DEFAULT)
        }
        fun isDevMode(context: Context): Boolean {
            return run {
                Settings.Secure.getInt(context.contentResolver,
                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0
            }
        }
        fun isAutoTimeEnabled(activity: Activity) =
            Settings.Global.getInt(activity.contentResolver, Settings.Global.AUTO_TIME) == 1

        fun isAutoTimeZoneEnabled(activity: Activity) =
            Settings.Global.getInt(activity.contentResolver, Settings.Global.AUTO_TIME_ZONE) == 1
        fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
            observe(lifecycleOwner, object : Observer<T> {
                override fun onChanged(value: T) {
                    observer.onChanged(value)
                    if (value != null) {
                        removeObserver(this)
                    }
                }
            })
        }
        fun <T : Serializable?> getSerializable(bundle: Bundle, key: String, className: Class<T>): T {
            return if (Build.VERSION.SDK_INT >= 33)
                bundle.getSerializable(key, className)!!
            else
                bundle.getSerializable(key) as T
        }
        fun getNameDevice(context: Context): String {
            val fabricante = Build.MANUFACTURER
            val modelo = Build.MODEL
            val deviceId: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            return if (modelo.startsWith(fabricante)) {
                "${primeraLetraMayuscula(modelo)}/$deviceId"
            } else {
                "${primeraLetraMayuscula(fabricante) + " " + modelo}/$deviceId"
            }
        }
        private fun primeraLetraMayuscula(cadena: String?): String {
            if (cadena.isNullOrEmpty()) {
                return ""
            }
            val primeraLetra = cadena[0]
            return if (Character.isUpperCase(primeraLetra)) {
                cadena
            } else {
                primeraLetra.uppercaseChar().toString() + cadena.substring(1)
            }
        }

        fun obtenerBeacons(beacon: List<BeaconsItem?>?): ArrayList<String> {
            val parseArray = ArrayList<String>()
            beacon.let {
                beacon?.forEach { beacons ->
                    beacons?.id?.let {
                        parseArray.add(it)
                    }
                }
            }
            return parseArray
        }

        fun getLocationHome(): LocationModel {
            return LocationModel(randomDouble(19.3944313).toString(),(-randomDouble(99.0890689)).toString())
        }
        private fun randomDouble(value: Double): Double {
            return ThreadLocalRandom.current().nextDouble((value-0.000041),(value+0.000041))
        }

        fun getDayOfDate(context: Context):String {
            val localDate = LocalDate.now()
            return context.resources.getStringArray(R.array.day_of_the_week_array)[localDate.dayOfWeek - 1]
        }
        fun reverseOrderOfWords(s: String):String {
            val date = s.substring(0, 10)
            return date.split("-").reversed().joinToString("/")
        }
        fun removeLastChar(str: String?): String? {
            return str?.replaceFirst(".$".toRegex(), "")
        }
    }
}