package com.faultcall.mobile.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.faultcall.shared.model.AnnouncementCategory
import com.faultcall.shared.model.AnnouncementEvent
import java.io.File

/**
 * Plays pre-recorded, normalized voice clips from local storage or raw assets.
 * Guarantees near-instant (0-latency) playback of signature calls like "FAULT!", "LET!", and "DEUCE".
 */
class LocalAudioProvider(
    private val context: Context
) : VoiceProvider {

    private val tag = "LocalAudioProvider"
    private var mediaPlayer: MediaPlayer? = null
    private val customClipsDir = File(context.filesDir, "audio_library")

    init {
        if (!customClipsDir.exists()) {
            customClipsDir.mkdirs()
        }
    }

    override val name: String = "Local Audio Library (Pre-generated Clips)"
    override val isAvailable: Boolean = true

    fun hasClipFor(category: AnnouncementCategory): Boolean {
        val fileName = getFileNameForCategory(category)
        val customFile = File(customClipsDir, fileName)
        if (customFile.exists() && customFile.length() > 0) return true

        val resId = context.resources.getIdentifier(
            fileName.substringBeforeLast("."),
            "raw",
            context.packageName
        )
        return resId != 0
    }

    private fun getFileNameForCategory(category: AnnouncementCategory): String = when (category) {
        AnnouncementCategory.FAULT -> "fault.mp3"
        AnnouncementCategory.DOUBLE_FAULT -> "double_fault.mp3"
        AnnouncementCategory.LET -> "let.mp3"
        AnnouncementCategory.DEUCE -> "deuce.mp3"
        AnnouncementCategory.ADVANTAGE -> "advantage.mp3"
        AnnouncementCategory.GAME -> "game.mp3"
        AnnouncementCategory.SET -> "set.mp3"
        AnnouncementCategory.MATCH -> "match_over.mp3"
        AnnouncementCategory.MATCH_POINT -> "match_point.mp3"
        AnnouncementCategory.SET_POINT -> "set_point.mp3"
        else -> "generic.mp3"
    }

    override fun speak(
        event: AnnouncementEvent,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        val fileName = getFileNameForCategory(event.category)
        val customFile = File(customClipsDir, fileName)

        try {
            stop()
            val mp = MediaPlayer()
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            mp.setAudioAttributes(audioAttributes)

            if (customFile.exists() && customFile.length() > 0) {
                mp.setDataSource(context, Uri.fromFile(customFile))
            } else {
                val resName = fileName.substringBeforeLast(".")
                val resId = context.resources.getIdentifier(resName, "raw", context.packageName)
                if (resId != 0) {
                    val afd = context.resources.openRawResourceFd(resId)
                    mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                } else {
                    onError("No local audio clip found for $fileName")
                    return
                }
            }

            mp.setOnCompletionListener {
                it.release()
                mediaPlayer = null
                onComplete()
            }

            mp.setOnErrorListener { _, what, extra ->
                mediaPlayer = null
                onError("MediaPlayer error: what=$what extra=$extra")
                true
            }

            mp.prepare()
            mp.start()
            mediaPlayer = mp
            Log.d(tag, "Playing local clip: $fileName for ${event.category}")
        } catch (e: Exception) {
            Log.e(tag, "Failed to play audio clip: ${e.message}", e)
            onError(e.message ?: "Unknown audio error")
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
            Log.w(tag, "Error stopping MediaPlayer: ${e.message}")
        }
    }

    override fun release() {
        stop()
    }
}
