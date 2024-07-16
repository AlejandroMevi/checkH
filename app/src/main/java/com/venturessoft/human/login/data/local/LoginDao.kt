package com.venturessoft.human.login.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.venturessoft.human.login.data.models.KeyWordEntity
import com.venturessoft.human.login.data.models.UserEntity
import com.venturessoft.human.login.data.models.UserSavedEntity

@Dao
interface LoginDao {
    @Query("SELECT * FROM user_table WHERE employeeId = :user AND company = :cia ")
    suspend fun getUser(user:String,cia: String): UserEntity?
    @Query("SELECT * FROM user_table")
    suspend fun getAllUser(): List<UserEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(modelUser: UserEntity)
    @Query("SELECT * FROM user_saved_table")
    suspend fun getSavedUser(): UserSavedEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedUser(userSavedEntity: UserSavedEntity)
    @Query("DELETE FROM user_saved_table")
    suspend fun deleteSavedUser()
    @Query("DELETE FROM user_table")
    suspend fun deleteAllSavedUser()
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyWord(keyWordEntity: KeyWordEntity)
    @Query("SELECT * FROM user_key_word WHERE keyword = :word")
    suspend fun getKeyWord(word:String): KeyWordEntity?
    @Query("DELETE FROM user_key_word")
    suspend fun deleteAllKeyWord()


    @Query("SELECT * FROM user_saved_table")
    fun getSavedUserNotification(): UserSavedEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSavedUserNotification(userSavedEntity: UserSavedEntity)
}