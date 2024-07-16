package com.venturessoft.human.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.venturessoft.human.core.Converters
import com.venturessoft.human.login.data.local.LoginDao
import com.venturessoft.human.login.data.models.KeyWordEntity
import com.venturessoft.human.login.data.models.UserEntity
import com.venturessoft.human.login.data.models.UserSavedEntity
import com.venturessoft.human.main.data.local.MainDao
import com.venturessoft.human.main.data.models.SuccesModel
import com.venturessoft.human.main.data.models.UserLocalPicture

@Database(entities = [
    UserEntity::class,
    SuccesModel::class,
    UserSavedEntity::class,
    KeyWordEntity::class,
    UserLocalPicture::class], version = 8)
@TypeConverters(Converters::class)
abstract class DB:RoomDatabase() {
    abstract fun userDao(): LoginDao
    abstract fun mainDao(): MainDao
}