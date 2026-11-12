package com.faultcall.mobile.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.faultcall.mobile.FaultCallApplication
import com.faultcall.mobile.audio.AudioRouteInfo
import com.faultcall.mobile.audio.PlaybackState
import com.faultcall.mobile.data.CompletedMatchRecord
import com.faultcall.mobile.data.VoiceSettings
import com.faultcall.mobile.service.MatchSessionForegroundService
import com.faultcall.shared.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as FaultCallApplication
    private val scoringEngine = app.scoringEngine
    private val announcementManager = app.announcementManager
    private val wearableManager = app.wearableManager
    private val repository = app.matchRepository
    private val bluetoothHelper = app.bluetoothAudioHelper

    private val _matchState = MutableStateFlow(scoringEngine.getState())
    val matchState: StateFlow<MatchState> = _matchState.asStateFlow()

    val isWatchConnected: StateFlow<Boolean> = wearableManager.isWatchConnected
    val connectedWatchName: StateFlow<String?> = wearableManager.connectedWatchName
    val audioRoute: StateFlow<AudioRouteInfo> = bluetoothHelper.currentRoute
    val playbackState: StateFlow<PlaybackState> = announcementManager.playbackState
    val history: StateFlow<List<CompletedMatchRecord>> = repository.history
    val voiceSettings: StateFlow<VoiceSettings> = repository.voiceSettings

    init {
        // Wire wearable manager callback to update phone UI when watch triggers scoring
        wearableManager.onScoreUpdatedListener = { updatedState ->
            _matchState.value = updatedState
        }

        // Initialize voice providers from saved settings
        val savedSettings = repository.voiceSettings.value
        announcementManager.ttsProvider.speechRate = savedSettings.speechRate
        announcementManager.ttsProvider.pitch = savedSettings.pitch
        announcementManager.elevenLabsProvider.apiKey = savedSettings.elevenLabsApiKey
        announcementManager.elevenLabsProvider.voiceId = savedSettings.elevenLabsVoiceId
        announcementManager.elevenLabsProvider.backendProxyUrl = savedSettings.elevenLabsBackendUrl
        announcementManager.elevenLabsProvider.isEnabled = savedSettings.useElevenLabsIfAvailable
        announcementManager.isDuckingEnabled = savedSettings.duckBackgroundMusic

        // Start foreground session service if match is in progress
        if (_matchState.value.status == MatchStatus.IN_PROGRESS) {
            startSessionService()
        }
    }

    private fun startSessionService() {
        val state = _matchState.value
        MatchSessionForegroundService.start(
            context = app,
            scoreText = state.formattedScoreSummary(),
            statusText = "${state.config.playerAName}: ${state.displayPoints(Player.PLAYER_A)} | ${state.config.playerBName}: ${state.displayPoints(Player.PLAYER_B)}"
        )
    }

    fun awardPoint(player: Player) {
        viewModelScope.launch {
            announcementManager.purgeObsoleteScoreAnnouncements()
            val result = scoringEngine.awardPoint(player)
            _matchState.value = result.state

            announcementManager.enqueue(
                AnnouncementEvent(
                    category = result.category,
                    spokenText = result.announcementText,
                    priority = result.category.priority
                )
            )

            repository.saveActiveMatchState(result.state)
            if (result.state.isFinished) {
                repository.saveCompletedMatch(result.state)
            }

            wearableManager.broadcastStateToWatch(result.state)
            MatchSessionForegroundService.update(
                context = app,
                scoreText = result.state.formattedScoreSummary(),
                statusText = "${result.state.config.playerAName}: ${result.state.displayPoints(Player.PLAYER_A)} | ${result.state.config.playerBName}: ${result.state.displayPoints(Player.PLAYER_B)}"
            )
        }
    }

    fun undo() {
        viewModelScope.launch {
            announcementManager.purgeObsoleteScoreAnnouncements()
            val restored = scoringEngine.undo()
            if (restored != null) {
                _matchState.value = restored
                val score = scoringEngine.getCurrentScoreAnnouncement()
                announcementManager.enqueue(
                    AnnouncementEvent(
                        category = AnnouncementCategory.SCORE,
                        spokenText = "Score restored. $score",
                        priority = 5
                    )
                )
                repository.saveActiveMatchState(restored)
                wearableManager.broadcastStateToWatch(restored)
            }
        }
    }

    fun announceFault() {
        announcementManager.enqueue(
            AnnouncementEvent(
                category = AnnouncementCategory.FAULT,
                spokenText = "FAULT!",
                priority = 1
            )
        )
    }

    fun announceDoubleFault() {
        announcementManager.enqueue(
            AnnouncementEvent(
                category = AnnouncementCategory.DOUBLE_FAULT,
                spokenText = "Double fault.",
                priority = 1
            )
        )
    }

    fun announceLet() {
        announcementManager.enqueue(
            AnnouncementEvent(
                category = AnnouncementCategory.LET,
                spokenText = "Let. First serve.",
                priority = 2
            )
        )
    }

    fun announceScore() {
        val score = scoringEngine.getCurrentScoreAnnouncement()
        announcementManager.enqueue(
            AnnouncementEvent(
                category = AnnouncementCategory.SCORE,
                spokenText = score,
                priority = 5
            )
        )
    }

    fun testSpeaker() {
        bluetoothHelper.updateRoute()
        announcementManager.enqueue(
            AnnouncementEvent(
                category = AnnouncementCategory.INFO,
                spokenText = "FaultCall audio test. The announcement system is ready.",
                priority = 6
            )
        )
    }

    fun startNewMatch(config: MatchConfig) {
        val newState = scoringEngine.reset(newConfig = config, newStartingServer = config.startingServer)
        _matchState.value = newState
        repository.saveActiveMatchState(newState)
        wearableManager.broadcastStateToWatch(newState)
        startSessionService()

        announcementManager.enqueue(
            AnnouncementEvent(
                category = AnnouncementCategory.INFO,
                spokenText = "New match started. ${config.playerAName} versus ${config.playerBName}. ${config.nameOf(config.startingServer)} to serve.",
                priority = 6
            )
        )
    }

    fun switchServer() {
        val current = _matchState.value.currentServer
        val newState = scoringEngine.setServer(current.opponent())
        _matchState.value = newState
        wearableManager.broadcastStateToWatch(newState)
    }

    fun updateVoiceSettings(
        rate: Float,
        pitch: Float,
        elevenLabsUrl: String,
        apiKey: String,
        voiceId: String,
        useElevenLabs: Boolean,
        duckMusic: Boolean = true
    ) {
        val updated = VoiceSettings(
            speechRate = rate,
            pitch = pitch,
            useElevenLabsIfAvailable = useElevenLabs,
            elevenLabsBackendUrl = elevenLabsUrl,
            elevenLabsApiKey = apiKey,
            elevenLabsVoiceId = voiceId,
            duckBackgroundMusic = duckMusic
        )
        repository.updateVoiceSettings(updated)
        announcementManager.ttsProvider.speechRate = rate
        announcementManager.ttsProvider.pitch = pitch
        announcementManager.elevenLabsProvider.backendProxyUrl = elevenLabsUrl
        announcementManager.elevenLabsProvider.apiKey = apiKey
        announcementManager.elevenLabsProvider.voiceId = voiceId
        announcementManager.elevenLabsProvider.isEnabled = useElevenLabs
        announcementManager.isDuckingEnabled = duckMusic
    }

    fun clearHistory() {
        repository.clearHistory()
    }
}
