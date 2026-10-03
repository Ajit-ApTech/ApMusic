package com.example.musicplayer.util

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import androidx.media3.common.C
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class EqualizerState(
    val isEnabled: Boolean = true,
    val selectedPreset: String = "Flat",
    val bandLevels: List<Int> = listOf(0, 0, 0, 0, 0), // -15 to +15 dB equivalent
    val bassBoost: Int = 30, // 0 to 100%
    val loudnessGain: Int = 6 // 0 to 12 dB gain
)

@Singleton
class AudioEffectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("audio_effects_prefs", Context.MODE_PRIVATE)

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentSessionId: Int = C.AUDIO_SESSION_ID_UNSET

    private val _state = MutableStateFlow(loadSavedState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    fun attachToAudioSession(audioSessionId: Int) {
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId == currentSessionId) return
        currentSessionId = audioSessionId

        release()

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
                applyStateToHardware(this, _state.value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
                setStrength((_state.value.bassBoost * 10).toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = _state.value.isEnabled
                setTargetGain(_state.value.loudnessGain * 100) // 100mB per dB
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(isEnabled = enabled)
        saveState(_state.value)
        applyCurrentState()
    }

    fun setPreset(presetName: String) {
        val newBands = when (presetName) {
            "Bass Booster" -> listOf(10, 7, 3, 0, -2)
            "Treble Booster" -> listOf(-3, 0, 4, 8, 12)
            "Vocal" -> listOf(-2, 2, 8, 6, 1)
            "Rock" -> listOf(8, 4, -2, 4, 8)
            "Pop" -> listOf(4, 6, 2, -2, 4)
            "Jazz" -> listOf(6, 3, 0, 3, 6)
            "Classical" -> listOf(8, 6, 2, 4, 7)
            "Flat" -> listOf(0, 0, 0, 0, 0)
            else -> _state.value.bandLevels
        }
        val newBass = if (presetName == "Bass Booster") 75 else _state.value.bassBoost

        _state.value = _state.value.copy(
            selectedPreset = presetName,
            bandLevels = newBands,
            bassBoost = newBass
        )
        saveState(_state.value)
        applyCurrentState()
    }

    fun setBandLevel(bandIndex: Int, levelDb: Int) {
        if (bandIndex !in 0..4) return
        val current = _state.value.bandLevels.toMutableList()
        current[bandIndex] = levelDb.coerceIn(-15, 15)

        _state.value = _state.value.copy(
            selectedPreset = "Custom",
            bandLevels = current
        )
        saveState(_state.value)
        applyCurrentState()
    }

    fun setBassBoost(percent: Int) {
        val value = percent.coerceIn(0, 100)
        _state.value = _state.value.copy(bassBoost = value)
        saveState(_state.value)

        try {
            bassBoost?.enabled = _state.value.isEnabled
            bassBoost?.setStrength((value * 10).toShort())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setLoudnessGain(gainDb: Int) {
        val value = gainDb.coerceIn(0, 12)
        _state.value = _state.value.copy(loudnessGain = value)
        saveState(_state.value)

        try {
            loudnessEnhancer?.enabled = _state.value.isEnabled
            loudnessEnhancer?.setTargetGain(value * 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyCurrentState() {
        val currentState = _state.value
        try {
            equalizer?.let { eq ->
                eq.enabled = currentState.isEnabled
                applyStateToHardware(eq, currentState)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            bassBoost?.let { bb ->
                bb.enabled = currentState.isEnabled
                bb.setStrength((currentState.bassBoost * 10).toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            loudnessEnhancer?.let { le ->
                le.enabled = currentState.isEnabled
                le.setTargetGain(currentState.loudnessGain * 100)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun applyStateToHardware(eq: Equalizer, currentState: EqualizerState) {
        val numBands = eq.numberOfBands.toInt().coerceAtMost(5)
        val minRange = eq.bandLevelRange?.get(0)?.toInt() ?: -1500
        val maxRange = eq.bandLevelRange?.get(1)?.toInt() ?: 1500

        for (i in 0 until numBands) {
            val userLevel = currentState.bandLevels.getOrElse(i) { 0 } // -15 to +15 dB
            val hwLevel = (userLevel * 100).coerceIn(minRange, maxRange).toShort()
            eq.setBandLevel(i.toShort(), hwLevel)
        }
    }

    fun release() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
            loudnessEnhancer?.release()
            loudnessEnhancer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSavedState(): EqualizerState {
        val isEnabled = prefs.getBoolean("eq_enabled", true)
        val preset = prefs.getString("preset_name", "Flat") ?: "Flat"
        val bands = (0..4).map { i -> prefs.getInt("band_$i", 0) }
        val bass = prefs.getInt("bass_boost", 30)
        val loudness = prefs.getInt("loudness_gain", 6)

        return EqualizerState(
            isEnabled = isEnabled,
            selectedPreset = preset,
            bandLevels = bands,
            bassBoost = bass,
            loudnessGain = loudness
        )
    }

    private fun saveState(s: EqualizerState) {
        prefs.edit().apply {
            putBoolean("eq_enabled", s.isEnabled)
            putString("preset_name", s.selectedPreset)
            s.bandLevels.forEachIndexed { i, level -> putInt("band_$i", level) }
            putInt("bass_boost", s.bassBoost)
            putInt("loudness_gain", s.loudnessGain)
            apply()
        }
    }
}
