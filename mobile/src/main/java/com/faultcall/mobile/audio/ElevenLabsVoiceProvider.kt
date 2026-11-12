package com.faultcall.mobile.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.faultcall.shared.model.AnnouncementEvent
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * AI Voice Provider integrating ElevenLabs through a secure backend proxy or direct endpoint.
 * Features an intelligent local disk cache to guarantee instant playback on repeat announcements
 * and prevent redundant network requests.
 */
class ElevenLabsVoiceProvider(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) : VoiceProvider {

    private val tag = "ElevenLabsProvider"
    private var mediaPlayer: MediaPlayer? = null

    // Cache directory for downloaded mp3 files
    private val cacheDir = File(context.cacheDir, "faultcall_voice_cache").apply {
        if (!exists()) mkdirs()
    }

    init {
        // Clear previous disk cache to purge any glitched audio files (like old hyphenated clips)
        clearCache()
    }

    // Configurable backend endpoint or direct ElevenLabs API
    var backendProxyUrl: String = ""
    var apiKey: String = ""
    var voiceId: String = "JBFqnCBsd6RMkjVDRZzb" // Default: George - Raspy British Umpire
    var isEnabled: Boolean = false

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override val name: String = "ElevenLabs AI Voice ('The Authority')"

    override val isAvailable: Boolean
        get() = isEnabled && (apiKey.isNotBlank() || backendProxyUrl.isNotBlank())

    /**
     * Checks if audio for the given text is already cached on disk.
     */
    fun hasCachedAudio(text: String): Boolean {
        val cacheFile = getCacheFile(text)
        return cacheFile.exists() && cacheFile.length() > 0
    }

    private fun getCacheFile(text: String): File {
        val key = "$voiceId:${text.trim().lowercase()}"
        val hash = MessageDigest.getInstance("SHA-256")
            .digest(key.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(cacheDir, "$hash.mp3")
    }

    override fun speak(
        event: AnnouncementEvent,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cacheFile = getCacheFile(event.spokenText)

        // 1. If cached on disk, play immediately with 0 latency
        if (cacheFile.exists() && cacheFile.length() > 0) {
            Log.d(tag, "Serving announcement from local disk cache: ${event.spokenText}")
            playLocalFile(cacheFile, onComplete, onError)
            return
        }

        // 2. If not available, fail over to fallback provider
        if (!isAvailable) {
            onError("ElevenLabs provider not configured or disabled; using fallback")
            return
        }

        // 3. Asynchronously fetch from direct ElevenLabs API or proxy
        scope.launch {
            try {
                val targetUrl = if (apiKey.isNotBlank()) {
                    "https://api.elevenlabs.io/v1/text-to-speech/$voiceId"
                } else {
                    backendProxyUrl
                }

                val jsonPayload = if (apiKey.isNotBlank()) {
                    JSONObject().apply {
                        put("text", event.spokenText)
                        put("model_id", "eleven_turbo_v2_5")
                        put("voice_settings", JSONObject().apply {
                            put("stability", 0.40)
                            put("similarity_boost", 0.85)
                            put("style", 0.55)
                            put("use_speaker_boost", true)
                        })
                    }.toString()
                } else {
                    JSONObject().apply {
                        put("text", event.spokenText)
                        put("voice_id", voiceId)
                        put("category", event.category.name)
                    }.toString()
                }

                val reqBuilder = Request.Builder()
                    .url(targetUrl)
                    .post(jsonPayload.toRequestBody("application/json".toMediaType()))

                if (apiKey.isNotBlank()) {
                    reqBuilder.addHeader("xi-api-key", apiKey.trim())
                    reqBuilder.addHeader("Accept", "audio/mpeg")
                }

                val response = httpClient.newCall(reqBuilder.build()).execute()
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.w(tag, "ElevenLabs API HTTP ${response.code}: $errBody")
                    withContext(Dispatchers.Main) {
                        onError("ElevenLabs HTTP ${response.code}")
                    }
                    return@launch
                }

                val responseBody = response.body
                if (responseBody == null) {
                    withContext(Dispatchers.Main) {
                        onError("Empty response body from voice provider")
                    }
                    return@launch
                }

                // Stream response bytes directly into cache file
                val tempFile = File(cacheDir, "${cacheFile.name}.tmp")
                FileOutputStream(tempFile).use { output ->
                    responseBody.byteStream().use { input ->
                        input.copyTo(output)
                    }
                }
                tempFile.renameTo(cacheFile)

                withContext(Dispatchers.Main) {
                    playLocalFile(cacheFile, onComplete, onError)
                }
            } catch (e: Exception) {
                Log.w(tag, "Failed to generate AI speech via ElevenLabs: ${e.message}")
                withContext(Dispatchers.Main) {
                    onError("ElevenLabs synthesis error: ${e.message}")
                }
            }
        }
    }

    private fun playLocalFile(
        file: File,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            stop()
            val mp = MediaPlayer()
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            mp.setAudioAttributes(audioAttributes)
            mp.setDataSource(context, Uri.fromFile(file))

            mp.setOnCompletionListener {
                it.release()
                mediaPlayer = null
                onComplete()
            }

            mp.setOnErrorListener { _, what, extra ->
                mediaPlayer = null
                onError("MediaPlayer error playing cached audio: what=$what extra=$extra")
                true
            }

            mp.prepare()
            mp.start()
            mediaPlayer = mp
        } catch (e: Exception) {
            onError("Failed to play audio file: ${e.message}")
        }
    }

    override fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.w(tag, "Error stopping audio: ${e.message}")
        }
    }

    override fun release() {
        stop()
        scope.cancel()
    }

    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    fun getCacheSizeMb(): Double {
        val totalBytes = cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        return totalBytes / (1024.0 * 1024.0)
    }
}
