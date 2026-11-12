package com.faultcall.shared.command

import java.util.UUID

enum class CommandType {
    ANNOUNCE_FAULT,
    ANNOUNCE_SCORE,
    AWARD_POINT_A,
    AWARD_POINT_B,
    UNDO,
    ANNOUNCE_LET,
    DOUBLE_FAULT,
    ANNOUNCE_GAME,
    ANNOUNCE_SET,
    ANNOUNCE_MATCH,
    PAUSE_ANNOUNCEMENTS,
    RESUME_ANNOUNCEMENTS,
    REQUEST_STATE,
    START_NEW_MATCH,
    SET_SERVER,
    LAUNCH_PHONE_APP
}

/**
 * Standard Wearable Data Layer message sent from Galaxy Watch8 to Phone.
 */
data class WearableCommand(
    val commandId: String = UUID.randomUUID().toString(),
    val type: CommandType,
    val timestamp: Long = System.currentTimeMillis(),
    val matchId: String? = null,
    val payload: String? = null
)

/**
 * Acknowledgment sent from Phone back to Watch8.
 */
data class CommandAcknowledgment(
    val commandId: String,
    val type: CommandType,
    val success: Boolean,
    val message: String? = null,
    val spokenText: String? = null,
    val isDuplicate: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
