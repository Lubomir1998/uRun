package com.example.urun.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.urun.R
import com.example.urun.other.Constants
import com.example.urun.ui.activities.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun providePendingIntent(@ApplicationContext context: Context) = PendingIntent.getActivity(context,
            0,
            Intent(context, MainActivity::class.java).also { it.action = Constants.ACTION_SHOW_TRACKING },
            PendingIntent.FLAG_UPDATE_CURRENT)

    @ServiceScoped
    @Provides
    fun provideNotification(@ApplicationContext context: Context, pendingIntent: PendingIntent) = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle("uRun")
            .setContentText("00:00:00")
            .setSmallIcon(R.drawable.your_runs_img)
            .setContentIntent(pendingIntent)

}