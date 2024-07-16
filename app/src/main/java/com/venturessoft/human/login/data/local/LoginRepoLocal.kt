package com.venturessoft.human.login.data.local

import com.venturessoft.human.core.makeNetworkCall
import com.venturessoft.human.login.data.models.KeyWordEntity
import com.venturessoft.human.login.data.models.UserEntity
import com.venturessoft.human.login.data.models.UserSavedEntity
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class LoginRepoLocal @Inject constructor(private val loginDao: LoginDao){
    suspend fun gettUser(user:String,cia: String): UserEntity? = loginDao.getUser(user,cia)
    suspend fun getAllUser(): List<UserEntity> = loginDao.getAllUser()
    suspend fun insertUser(userEntity: UserEntity){
        makeNetworkCall {
            loginDao.insertUser(userEntity)
        }
    }
    suspend fun getSavedUser(): UserSavedEntity? = loginDao.getSavedUser()
    suspend fun deleteSavedUser() = loginDao.deleteSavedUser()
    suspend fun deleteAllSavedUser() = loginDao.deleteAllSavedUser()
    suspend fun insertSavedUser(userSavedEntity: UserSavedEntity){
        makeNetworkCall {
            loginDao.insertSavedUser(userSavedEntity)
        }
    }
    suspend fun insertKeyWord(keyWordEntity: KeyWordEntity){
        makeNetworkCall {
            loginDao.insertKeyWord(keyWordEntity)
        }
    }
    suspend fun getKeyWord(word:String): KeyWordEntity? = loginDao.getKeyWord(word)
    suspend fun deleteAllKeyWord() = loginDao.deleteAllKeyWord()
}