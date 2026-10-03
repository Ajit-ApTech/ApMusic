package com.example.musicplayer.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.musicplayer.MainActivity
import com.example.musicplayer.R

@UnstableApi
object NotificationHelper {
    const val CHANNEL_ID = "apmusic_playback_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ApMusic Playback"
            val descriptionText = "ApMusic background music controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildMediaNotification(
        context: Context,
        mediaSession: MediaSession,
        title: String,
        artist: String,
        artworkBitmap: Bitmap? = null
    ): Notification {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title.ifBlank { "ApMusic" })
            .setContentText(artist.ifBlank { "Playing audio" })
            .setContentIntent(pendingIntent)
            .setOngoing(mediaSession.player.isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // Show Previous (0), Play/Pause (1), Next (2) in compact view
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(mediaSession)
                    .setShowActionsInCompactView(0, 1, 2)
            )

        if (artworkBitmap != null) {
            builder.setLargeIcon(artworkBitmap)
        }

        return builder.build()
    }
}
