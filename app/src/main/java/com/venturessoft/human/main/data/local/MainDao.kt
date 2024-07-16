package com.venturessoft.human.main.data.local

import androidx.room.*
import com.venturessoft.human.main.data.models.SuccesModel
import com.venturessoft.human.main.data.models.UserLocalPicture

@Dao
interface MainDao {
    @Query("SELECT * FROM offline_service_table")
    suspend fun getServicesOffline(): List<SuccesModel>
    @Query("DELETE FROM offline_service_table WHERE id = :id")
    suspend fun deleteServicesOffline(id:Int)
    @Update(entity = SuccesModel::class)
    suspend fun updateUser(succesModel: SuccesModel)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServicesOffline(succesModel: SuccesModel)


    @Query("SELECT * FROM offline_service_table_local")
    suspend fun getServicesOfflineLocal(): List<UserLocalPicture>
    @Query("DELETE FROM offline_service_table_local WHERE id = :id")
    suspend fun deleteServicesOfflineLocal(id:Int)
    @Update(entity = UserLocalPicture::class)
    suspend fun updateUserLocal(userLocalPicture: UserLocalPicture)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServicesOfflineLocal(userLocalPicture: UserLocalPicture)
}