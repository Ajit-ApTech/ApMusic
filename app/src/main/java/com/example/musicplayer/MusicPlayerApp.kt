package com.example.musicplayer

import android.app.Application
import com.example.musicplayer.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MusicPlayerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Delegate channel creation to NotificationHelper to avoid duplicate/conflicting channels
        NotificationHelper.createNotificationChannel(this)
    }
}
