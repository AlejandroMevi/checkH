package com.venturessoft.human.core.di

import android.content.Context
import androidx.room.Room
import com.venturessoft.human.core.db.DB
import com.venturessoft.human.login.data.local.LoginDao
import com.venturessoft.human.main.data.local.MainDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
class DBModule {
    @Provides
    fun provideUserDao(database: DB): LoginDao {
        return database.userDao()
    }
    @Provides
    fun provideMainDao(database: DB): MainDao {
        return database.mainDao()
    }
    @Provides
    @Singleton
    fun provideUserDataBase(@ApplicationContext context: Context): DB {
        return Room
            .databaseBuilder(context, DB::class.java,"HumaneTimeDB")
            .fallbackToDestructiveMigration()
            .build()
    }
}