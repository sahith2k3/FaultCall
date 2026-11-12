package com.faultcall.mobile.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import com.faultcall.shared.model.AnnouncementEvent
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * TextToSpeech voice provider configured for "The Authority" signature umpire delivery.
 * Uses STREAM_MUSIC / USAGE_ASSISTANCE_NAVIGATION_GUIDANCE to guarantee audio routes through Bluetooth speakers
 * and plays even when phone ring/notification is set to silent or vibrate.
 * Audio focus and ducking are centrally managed by AnnouncementManager.
 */
class AndroidTtsVoiceProvider(
    private val context: Context,
    private val onInitializedCallback: ((Boolean) -> Unit)? = null
) : VoiceProvider, TextToSpeech.OnInitListener {

    private val tag = "FaultCallTTS"
    private var tts: TextToSpeech? = null
    private val isReady = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())

    var speechRate: Float = 0.98f
        set(value) {
            field = value.coerceIn(0.5f, 2.0f)
            tts?.setSpeechRate(field)
        }

    // Default slightly lower pitch (0.92f) for "The Authority" medium-low resonant British delivery
    var pitch: Float = 0.92f
        set(value) {
            field = value.coerceIn(0.5f, 2.0f)
            tts?.setPitch(field)
        }

    private var activeCallback: Pair<() -> Unit, (String) -> Unit>? = null

    init {
        Log.i(tag, "Initializing Android TextToSpeech engine...")
        tts = TextToSpeech(context.applicationContext, this)
    }

    override val name: String = "Android Text-to-Speech (The Authority Profile)"

    override val isAvailable: Boolean
        get() = isReady.get()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val engine = tts ?: return

            // CRITICAL: Must use USAGE_ASSISTANCE_NAVIGATION_GUIDANCE so audio routes to Bluetooth A2DP media speaker
            // and ducks background music cleanly without interruption
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            engine.setAudioAttributes(audioAttributes)

            // Prefer British English for "The Authority" RP British delivery
            var langResult = engine.setLanguage(Locale.UK)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(tag, "Locale.UK not supported on device; trying Locale.US")
                langResult = engine.setLanguage(Locale.US)
                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(tag, "Locale.US not supported; falling back to default locale")
                    engine.setLanguage(Locale.getDefault())
                }
            }

            selectBestAuthorityVoice(engine)
            engine.setSpeechRate(speechRate)
            engine.setPitch(pitch)

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(tag, "TTS speaking started for utteranceId: $utteranceId")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(tag, "TTS speaking COMPLETED for utteranceId: $utteranceId")
                    mainHandler.post {
                        val cb = activeCallback?.first
                        activeCallback = null
                        cb?.invoke()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    Log.e(tag, "TTS speech failed for utterance: $utteranceId")
                    mainHandler.post {
                        val cb = activeCallback?.second
                        activeCallback = null
                        cb?.invoke("TTS failed for $utteranceId")
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.e(tag, "TTS speech error code $errorCode for utterance: $utteranceId")
                    mainHandler.post {
                        val cb = activeCallback?.second
                        activeCallback = null
                        cb?.invoke("TTS error code $errorCode")
                    }
                }

                override fun onStop(utteranceId: String?, interrupted: Boolean) {
                    Log.d(tag, "TTS speaking stopped/interrupted ($interrupted) for utteranceId: $utteranceId")
                    mainHandler.post {
                        val cb = activeCallback?.first
                        activeCallback = null
                        cb?.invoke()
                    }
                }
            })

            isReady.set(true)
            Log.i(tag, "Android TTS initialized and ready on STREAM_MUSIC")
            onInitializedCallback?.invoke(true)
        } else {
            Log.e(tag, "Failed to initialize Android TextToSpeech, status: $status")
            isReady.set(false)
            onInitializedCallback?.invoke(false)
        }
    }

    private fun selectBestAuthorityVoice(engine: TextToSpeech) {
        try {
            val voices: Set<Voice>? = engine.voices
            if (!voices.isNullOrEmpty()) {
                val enGbVoices = voices.filter { it.locale.language.equals("en", ignoreCase = true) && it.locale.country.equals("GB", ignoreCase = true) }
                val femaleEnGb = enGbVoices.firstOrNull { it.name.contains("female", ignoreCase = true) || it.name.contains("f0", ignoreCase = true) }
                    ?: enGbVoices.firstOrNull()
                    ?: voices.firstOrNull { it.locale.language == "en" }

                if (femaleEnGb != null) {
                    engine.voice = femaleEnGb
                    Log.i(tag, "Selected voice: ${femaleEnGb.name} (${femaleEnGb.locale})")
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not filter installed voices: ${e.message}")
        }
    }

    override fun speak(
        event: AnnouncementEvent,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val engine = tts
        if (engine == null || !isReady.get()) {
            Log.w(tag, "Cannot speak: TTS not ready yet")
            onError("TextToSpeech not ready")
            return
        }

        activeCallback = Pair(onComplete, onError)

        // Pass STREAM_MUSIC explicitly in params
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, event.id)
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        val queueMode = if (event.priority <= 2) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        Log.i(tag, "Calling tts.speak for '${event.spokenText}' (stream=STREAM_MUSIC, id=${event.id})")
        val result = engine.speak(event.spokenText, queueMode, params, event.id)
        if (result != TextToSpeech.SUCCESS) {
            Log.e(tag, "tts.speak returned error code: $result")
            activeCallback = null
            onError("TTS speak failed with code $result")
        }
    }

    override fun stop() {
        try {
            tts?.stop()
            activeCallback = null
        } catch (e: Exception) {
            Log.w(tag, "Error stopping TTS: ${e.message}")
        }
    }

    override fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isReady.set(false)
        } catch (e: Exception) {
            Log.w(tag, "Error releasing TTS: ${e.message}")
        }
    }
}
