package com.example.musicplayer.service

import android.content.Intent
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.musicplayer.util.AudioEffectManager
import com.example.musicplayer.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var telephonyManager: TelephonyManager? = null
    private var wasPlayingBeforeCall = false

    @Inject
    lateinit var audioEffectManager: AudioEffectManager

    // Legacy phone state listener for Android < 12
    @Suppress("DEPRECATION")
    private val phoneStateListener = object : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            handleCallState(state)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        // Create notification channel first — must happen before any notification is shown
        NotificationHelper.createNotificationChannel(this)

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                20_000,  // Min buffer: 20s
                50_000,  // Max buffer: 50s
                1_500,   // Playback start buffer: 1.5s
                3_000    // Buffer after rebuffer: 3s
            )
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true)   // Pause on headphone unplug
            .setWakeMode(C.WAKE_MODE_NETWORK)    // Keep CPU + WiFi alive during playback
            .setLoadControl(loadControl)
            .build()

        player.volume = 1.0f

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                    try {
                        audioEffectManager.attachToAudioSession(audioSessionId)
                    } catch (e: Exception) {
                        // AudioEffect not supported on this device/ROM — non-fatal
                    }
                }
            }
        })

        try {
            if (player.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                audioEffectManager.attachToAudioSession(player.audioSessionId)
            }
        } catch (e: Exception) {
            // Non-fatal: audio effects may not be supported on gaming/restricted ROMs
        }

        mediaSession = MediaSession.Builder(this, player).build()

        val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(NotificationHelper.CHANNEL_ID)
            .setNotificationId(NotificationHelper.NOTIFICATION_ID)
            .build()
        setMediaNotificationProvider(notificationProvider)

        // Register phone state listener safely — some ROMs block TelephonyManager
        setupPhoneStateListenerSafely()
    }

    private fun setupPhoneStateListenerSafely() {
        try {
            telephonyManager = getSystemService(TELEPHONY_SERVICE) as? TelephonyManager
            if (telephonyManager == null) return  // Service not available on this device

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager?.registerTelephonyCallback(
                    mainExecutor,
                    object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                        override fun onCallStateChanged(state: Int) {
                            handleCallState(state)
                        }
                    }
                )
            } else {
                @Suppress("DEPRECATION")
                telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            }
        } catch (e: Exception) {
            // READ_PHONE_STATE not granted or gaming mode blocked it — non-fatal
            // Music will still work, just won't auto-pause/resume on calls
        }
    }

    private fun handleCallState(state: Int) {
        try {
            val player = mediaSession?.player ?: return
            when (state) {
                TelephonyManager.CALL_STATE_RINGING,
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    if (player.isPlaying) {
                        wasPlayingBeforeCall = true
                        player.pause()
                    }
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    if (wasPlayingBeforeCall) {
                        wasPlayingBeforeCall = false
                        player.play()
                    }
                }
            }
        } catch (e: Exception) {
            // Non-fatal
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    @Suppress("DEPRECATION")
    override fun onDestroy() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }

        try {
            audioEffectManager.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
