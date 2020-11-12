package com.example.urun.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.urun.data.RunDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunDB(@ApplicationContext context: Context) = Room.databaseBuilder(
            context,
            RunDB::class.java,
            "run_database"
    )
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun provideDao(db: RunDB) = db.getRunDao()

    @Singleton
    @Provides
    fun sharedPrefs(@ApplicationContext context: Context) = context.getSharedPreferences("shared_prefs", MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideFirstTimeRun(sharedPreferences: SharedPreferences) = sharedPreferences.getBoolean("keyFirstTime", true)

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) = sharedPreferences.getFloat("keyWeight", 80f)

    @Singleton
    @Provides
    fun provideSpinnerPosition(sharedPreferences: SharedPreferences) = sharedPreferences.getInt("spinner", 0)


}