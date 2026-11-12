package com.faultcall.mobile.data

import android.content.Context
import android.content.SharedPreferences
import com.faultcall.shared.model.MatchConfig
import com.faultcall.shared.model.MatchState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class CompletedMatchRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val playerAName: String,
    val playerBName: String,
    val winnerName: String,
    val scoreSummary: String,
    val formatDescription: String
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(Date(timestamp))
}

data class VoiceSettings(
    val speechRate: Float = 1.0f,
    val pitch: Float = 0.92f,
    val useElevenLabsIfAvailable: Boolean = false,
    val elevenLabsBackendUrl: String = "",
    val elevenLabsApiKey: String = "",
    val elevenLabsVoiceId: String = "JBFqnCBsd6RMkjVDRZzb", // George - Raspy British Umpire
    val duckBackgroundMusic: Boolean = true
)

/**
 * Handles persistent storage of match history and user settings.
 */
class MatchHistoryRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("faultcall_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _history = MutableStateFlow<List<CompletedMatchRecord>>(loadHistory())
    val history: StateFlow<List<CompletedMatchRecord>> = _history.asStateFlow()

    private val _voiceSettings = MutableStateFlow(loadVoiceSettings())
    val voiceSettings: StateFlow<VoiceSettings> = _voiceSettings.asStateFlow()

    private fun loadHistory(): List<CompletedMatchRecord> {
        val json = prefs.getString("completed_matches", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<CompletedMatchRecord>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCompletedMatch(state: MatchState) {
        val winnerName = state.config.nameOf(state.winner ?: return)
        val record = CompletedMatchRecord(
            id = state.matchId,
            playerAName = state.config.playerAName,
            playerBName = state.config.playerBName,
            winnerName = winnerName,
            scoreSummary = state.formattedScoreSummary(),
            formatDescription = state.config.matchFormat.name
        )
        val updated = listOf(record) + _history.value
        _history.value = updated
        prefs.edit().putString("completed_matches", gson.toJson(updated)).apply()
    }

    fun clearHistory() {
        _history.value = emptyList()
        prefs.edit().remove("completed_matches").apply()
    }

    fun saveActiveMatchState(state: MatchState?) {
        if (state == null) {
            prefs.edit().remove("active_match_state").apply()
        } else {
            prefs.edit().putString("active_match_state", gson.toJson(state)).apply()
        }
    }

    fun loadActiveMatchState(): MatchState? {
        val json = prefs.getString("active_match_state", null) ?: return null
        return try {
            gson.fromJson(json, MatchState::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadVoiceSettings(): VoiceSettings {
        val json = prefs.getString("voice_settings", null) ?: return VoiceSettings()
        return try {
            gson.fromJson(json, VoiceSettings::class.java) ?: VoiceSettings()
        } catch (e: Exception) {
            VoiceSettings()
        }
    }

    fun updateVoiceSettings(settings: VoiceSettings) {
        _voiceSettings.value = settings
        prefs.edit().putString("voice_settings", gson.toJson(settings)).apply()
    }
}
