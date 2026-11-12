package com.faultcall.mobile.audio

import com.faultcall.shared.model.AnnouncementEvent

/**
 * Common interface for all FaultCall voice generators.
 */
interface VoiceProvider {
    val name: String
    val isAvailable: Boolean

    /**
     * Synthesizes and plays the announcement.
     * Invokes onComplete when playback ends, or onError on failure.
     */
    fun speak(
        event: AnnouncementEvent,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    )

    fun stop()
    fun release()
}
