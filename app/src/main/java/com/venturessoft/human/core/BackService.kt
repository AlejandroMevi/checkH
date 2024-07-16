package com.venturessoft.human.core

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.venturessoft.human.core.utils.Preferences
import com.venturessoft.human.splash.data.KeyCompany

class BackService(context : Context, params : WorkerParameters): Worker(context,params) {
    override fun doWork(): Result {
        val company:String = inputData.getString("companyKey")?:""
        Preferences().editClaveCompany(KeyCompany(company),applicationContext)
   /*     val company:String = inputData.getString("companyKey")?:""
        val db: DB = DBModule().provideUserDataBase(context)
        val notifDao: LoginDao = db.userDao()
        val dataUser = notifDao.getSavedUserNotification()
        if (dataUser != null) {
            notifDao.insertSavedUserNotification(UserSavedEntity(dataUser.employeeId,company))
        }*/
        return Result.success()
    }
}