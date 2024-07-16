package com.venturessoft.human.main.data.local

import com.venturessoft.human.core.makeNetworkCall
import com.venturessoft.human.main.data.models.SuccesModel
import com.venturessoft.human.main.data.models.UserLocalPicture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class MainRepoLocal @Inject constructor(private val mainDao: MainDao) {
    suspend fun getServicesOffline(): List<SuccesModel> = mainDao.getServicesOffline()
    suspend fun deleteServicesOffline(id: Int) = mainDao.deleteServicesOffline(id)
    suspend fun updateUser(succesModel: SuccesModel) {
        makeNetworkCall {
            mainDao.updateUser(succesModel)
        }
    }
    suspend fun insertServicesOffline(succesModel: SuccesModel) {
        return withContext(Dispatchers.IO) {
            val deferreds = async { mainDao.insertServicesOffline(succesModel) }
            deferreds.await()
        }
    }

    suspend fun getServicesOfflineLocal(): List<UserLocalPicture> = mainDao.getServicesOfflineLocal()
    suspend fun deleteServicesOfflineLocal(id: Int) = mainDao.deleteServicesOfflineLocal(id)
    suspend fun updateUserLocal(userLocalPicture: UserLocalPicture) {
        makeNetworkCall {
            mainDao.updateUserLocal(userLocalPicture)
        }
    }
    suspend fun insertServicesOfflineLocal(userLocalPicture: UserLocalPicture) {
        return withContext(Dispatchers.IO) {
            val deferreds = async { mainDao.insertServicesOfflineLocal(userLocalPicture) }
            deferreds.await()
        }
    }
}