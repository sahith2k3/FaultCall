package com.faultcall.mobile.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import com.faultcall.shared.model.AnnouncementCategory
import com.faultcall.shared.model.AnnouncementEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.PriorityQueue

sealed class PlaybackState {
    object Idle : PlaybackState()
    data class Playing(val event: AnnouncementEvent, val providerName: String) : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}

/**
 * Central announcement dispatcher and priority queue manager.
 * Enforces strict priority hierarchy, preemption of ordinary scores by FAULT calls,
 * automatic audio ducking/attenuation for background music apps (e.g. Spotify),
 * and watchdog timers to prevent audio lockups.
 */
class AnnouncementManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    private val tag = "AnnouncementManager"

    val localAudioProvider = LocalAudioProvider(context)
    val elevenLabsProvider = ElevenLabsVoiceProvider(context)
    val ttsProvider = AndroidTtsVoiceProvider(context)

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var hasDuckingFocus = false
    var isDuckingEnabled: Boolean = true

    private val duckingFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        Log.d(tag, "Audio focus change notification: $focusChange")
    }

    private val queue = PriorityQueue<AnnouncementEvent>(compareBy { it.priority })
    private var isPlaying = false
    private var currentEvent: AnnouncementEvent? = null
    private var watchdogJob: Job? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    @Synchronized
    private fun requestDuckingFocus(): Boolean {
        if (!isDuckingEnabled) return true
        if (hasDuckingFocus && focusRequest != null) {
            Log.d(tag, "Audio ducking focus already active; retaining focus for queued announcement.")
            return true
        }

        return try {
            // Safety: Abandon any prior orphaned focus request before creating a new one
            focusRequest?.let { oldReq ->
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioManager.abandonAudioFocusRequest(oldReq)
                    }
                } catch (_: Exception) {}
            }
            focusRequest = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(false)
                    .setOnAudioFocusChangeListener(duckingFocusListener)
                    .build()
                val res = audioManager.requestAudioFocus(request)
                if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    focusRequest = request
                    hasDuckingFocus = true
                    Log.d(tag, "Audio ducking focus requested: GRANTED (res=$res, attenuating music to ~5%)")
                    true
                } else {
                    focusRequest = null
                    hasDuckingFocus = false
                    Log.w(tag, "Audio ducking focus requested: FAILED (res=$res)")
                    false
                }
            } else {
                @Suppress("DEPRECATION")
                val res = audioManager.requestAudioFocus(
                    duckingFocusListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
                hasDuckingFocus = (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                Log.d(tag, "Audio ducking focus requested (legacy): $hasDuckingFocus")
                hasDuckingFocus
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to request audio ducking focus: ${e.message}")
            focusRequest = null
            hasDuckingFocus = false
            false
        }
    }

    @Synchronized
    private fun releaseDuckingFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { req ->
                    val res = audioManager.abandonAudioFocusRequest(req)
                    Log.d(tag, "Audio ducking focus released: res=$res (1=GRANTED, music volume smoothly restored).")
                }
            } else {
                @Suppress("DEPRECATION")
                val res = audioManager.abandonAudioFocus(duckingFocusListener)
                Log.d(tag, "Audio ducking focus released (legacy): res=$res")
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to release audio ducking focus: ${e.message}")
        } finally {
            focusRequest = null
            hasDuckingFocus = false
        }
    }

    companion object {
        /**
         * Ensures a clean space between every word and an exclamation mark at the end,
         * completely eliminating any hyphen glitches in ElevenLabs (e.g. "Love-Fifteen" -> "Love Fifteen!")
         */
        fun sanitizeSpeech(text: String): String {
            var cleaned = text.replace(Regex("[-_—–]"), " ")
            cleaned = cleaned.replace(",", " ").replace(".", " ")
            cleaned = cleaned.replace("!", "")
            cleaned = cleaned.trim().split(Regex("\\s+")).joinToString(" ")
            return if (cleaned.isNotEmpty()) "$cleaned!" else "FAULT!"
        }
    }

    fun enqueue(event: AnnouncementEvent) {
        scope.launch {
            val formattedText = sanitizeSpeech(event.spokenText)
            val cleanEvent = event.copy(spokenText = formattedText)
            Log.d(tag, "Enqueuing announcement: '${cleanEvent.spokenText}' (Priority: ${cleanEvent.priority})")

            // If a high-priority call arrives (Priority 1: FAULT), preempt lower priority
            if (cleanEvent.priority == 1 && isPlaying && (currentEvent?.priority ?: 5) > 1) {
                Log.i(tag, "Preempting lower priority announcement with FAULT call!")
                watchdogJob?.cancel()
                watchdogJob = null
                localAudioProvider.stop()
                elevenLabsProvider.stop()
                ttsProvider.stop()
                isPlaying = false
                currentEvent = null
                _playbackState.value = PlaybackState.Idle
            }

            queue.add(cleanEvent)
            processNext()
        }
    }

    fun purgeObsoleteScoreAnnouncements() {
        scope.launch(Dispatchers.Main) {
            val removed = queue.removeAll { it.category == AnnouncementCategory.SCORE || it.category == AnnouncementCategory.POINT_AWARDED }
            if (removed) {
                Log.d(tag, "Purged obsolete score announcements from queue.")
            }
        }
    }

    private fun processNext() {
        if (isPlaying || queue.isEmpty()) return

        val nextEvent = queue.poll() ?: return
        isPlaying = true
        currentEvent = nextEvent

        // Start a 7.0 second watchdog timer to prevent the queue from ever locking up
        // if an utterance callback is missed by Android TTS or Bluetooth routing change
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            delay(7000L)
            if (isPlaying && currentEvent?.id == nextEvent.id) {
                Log.w(tag, "Watchdog: Announcement '${nextEvent.spokenText}' timed out after 7.0s; releasing queue lock.")
                onEventFinished()
            }
        }

        // Request audio focus to duck background music (e.g. Spotify) down to ~5%
        requestDuckingFocus()

        playWithFallbackChain(nextEvent)
    }

    private fun playWithFallbackChain(event: AnnouncementEvent) {
        if (localAudioProvider.hasClipFor(event.category)) {
            _playbackState.value = PlaybackState.Playing(event, localAudioProvider.name)
            localAudioProvider.speak(
                event = event,
                onComplete = { onEventFinished() },
                onError = { error ->
                    Log.w(tag, "Local audio failed ($error); trying ElevenLabs or TTS")
                    tryElevenLabsOrTts(event)
                }
            )
            return
        }

        tryElevenLabsOrTts(event)
    }

    private fun tryElevenLabsOrTts(event: AnnouncementEvent) {
        if (elevenLabsProvider.hasCachedAudio(event.spokenText) || elevenLabsProvider.isAvailable) {
            _playbackState.value = PlaybackState.Playing(event, elevenLabsProvider.name)
            elevenLabsProvider.speak(
                event = event,
                onComplete = { onEventFinished() },
                onError = { error ->
                    Log.w(tag, "ElevenLabs failed ($error); falling back to Android TTS")
                    fallbackToTts(event)
                }
            )
            return
        }

        fallbackToTts(event)
    }

    private fun fallbackToTts(event: AnnouncementEvent) {
        _playbackState.value = PlaybackState.Playing(event, ttsProvider.name)
        ttsProvider.speak(
            event = event,
            onComplete = { onEventFinished() },
            onError = { error ->
                Log.e(tag, "TTS voice provider failed: $error")
                _playbackState.value = PlaybackState.Error(error)
                onEventFinished()
            }
        )
    }

    private fun onEventFinished() {
        scope.launch(Dispatchers.Main) {
            watchdogJob?.cancel()
            watchdogJob = null
            isPlaying = false
            currentEvent = null
            _playbackState.value = PlaybackState.Idle
            if (queue.isEmpty()) {
                releaseDuckingFocus()
            }
            processNext()
        }
    }

    fun stopCurrent() {
        scope.launch(Dispatchers.Main) {
            watchdogJob?.cancel()
            watchdogJob = null
            localAudioProvider.stop()
            elevenLabsProvider.stop()
            ttsProvider.stop()
            isPlaying = false
            currentEvent = null
            _playbackState.value = PlaybackState.Idle
            if (queue.isEmpty()) {
                releaseDuckingFocus()
            }
        }
    }

    fun clearAll() {
        scope.launch(Dispatchers.Main) {
            queue.clear()
            watchdogJob?.cancel()
            watchdogJob = null
            localAudioProvider.stop()
            elevenLabsProvider.stop()
            ttsProvider.stop()
            isPlaying = false
            currentEvent = null
            _playbackState.value = PlaybackState.Idle
            releaseDuckingFocus()
        }
    }

    fun release() {
        clearAll()
        localAudioProvider.release()
        elevenLabsProvider.release()
        ttsProvider.release()
        scope.cancel()
    }
}
