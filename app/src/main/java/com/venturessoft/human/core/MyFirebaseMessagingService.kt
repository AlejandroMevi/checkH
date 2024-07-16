package com.venturessoft.human.core

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.venturessoft.human.R
import com.venturessoft.human.core.BaseApplication.Companion.activityVisible
import com.venturessoft.human.pictureLocal.LocalPictureActivity
import com.venturessoft.human.splash.ui.activitys.SplashActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {}

    @SuppressLint("LongLogTag")

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.notification != null) {
            mostrarNotificacion(remoteMessage)
        }
    }
    @SuppressLint("InvalidWakeLockTag")
    private fun mostrarNotificacion(remoteMessage: RemoteMessage) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "my_channel_id_01"
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "My Notifications",
                NotificationManager.IMPORTANCE_MAX
            )
            notificationChannel.description = "Channel description"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationIntent = when (activityVisible){
            true -> {
                Intent()
            }
            false -> Intent(this, LocalPictureActivity::class.java)
            else -> Intent(this, SplashActivity::class.java)
        }
        val contentIntent = PendingIntent.getActivity(
            applicationContext, 0,
            notificationIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setSound(uri)
            .setContentInfo("Info")
        notificationBuilder.color = resources.getColor(R.color.colorPrimary)
        notificationBuilder.setContentIntent(contentIntent)
        notificationManager.notify(1, notificationBuilder.build())
        val screenOn = (getSystemService(POWER_SERVICE) as PowerManager).newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "example"
        )
        screenOn.acquire(10*60*1000L)
    }
    @SuppressLint("SuspiciousIndentation")
    private fun setOnwTimeWorkRequest(myFirebaseMessagingService: MyFirebaseMessagingService) {
        val data: Data = Data.Builder()
            .putString("companyKey","MACHA")
            .build()
        val req :OneTimeWorkRequest = OneTimeWorkRequestBuilder<BackService>()
            .setInputData(data)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(myFirebaseMessagingService).enqueue(req)
    }
}