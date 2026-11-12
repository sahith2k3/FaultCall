package com.faultcall.mobile

import android.app.Application
import com.faultcall.mobile.audio.AnnouncementManager
import com.faultcall.mobile.audio.BluetoothAudioHelper
import com.faultcall.mobile.communication.PhoneWearableManager
import com.faultcall.mobile.data.MatchHistoryRepository
import com.faultcall.shared.scoring.TennisScoringEngine

class FaultCallApplication : Application() {

    lateinit var announcementManager: AnnouncementManager
        private set

    lateinit var bluetoothAudioHelper: BluetoothAudioHelper
        private set

    lateinit var matchRepository: MatchHistoryRepository
        private set

    lateinit var wearableManager: PhoneWearableManager
        private set

    lateinit var scoringEngine: TennisScoringEngine
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        matchRepository = MatchHistoryRepository(this)
        announcementManager = AnnouncementManager(this)
        bluetoothAudioHelper = BluetoothAudioHelper(this)
        scoringEngine = TennisScoringEngine()

        // Restore saved voice settings
        val settings = matchRepository.voiceSettings.value
        announcementManager.ttsProvider.speechRate = settings.speechRate
        announcementManager.ttsProvider.pitch = settings.pitch
        announcementManager.elevenLabsProvider.backendProxyUrl = settings.elevenLabsBackendUrl
        announcementManager.elevenLabsProvider.apiKey = settings.elevenLabsApiKey
        announcementManager.elevenLabsProvider.voiceId = settings.elevenLabsVoiceId
        announcementManager.elevenLabsProvider.isEnabled = settings.useElevenLabsIfAvailable
        announcementManager.isDuckingEnabled = settings.duckBackgroundMusic

        // Restore active match state if app was previously closed mid-match
        matchRepository.loadActiveMatchState()?.let {
            scoringEngine.restoreState(it)
        }

        wearableManager = PhoneWearableManager(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        announcementManager.release()
        bluetoothAudioHelper.unregister()
    }

    companion object {
        lateinit var instance: FaultCallApplication
            private set
    }
}
