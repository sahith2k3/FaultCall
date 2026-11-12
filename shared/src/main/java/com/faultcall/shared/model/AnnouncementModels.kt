package com.faultcall.shared.model

enum class AnnouncementCategory(val priority: Int) {
    FAULT(1),          // Highest priority - immediate projection
    DOUBLE_FAULT(1),   // Highest priority
    LET(2),            // High priority
    MATCH(3),          // Decisive match call
    SET(3),            // Set completion call
    GAME(3),           // Game completion call
    ADVANTAGE(4),      // Tactical game state
    DEUCE(4),          // Deuce call
    SET_POINT(4),
    MATCH_POINT(4),
    SCORE(5),          // Normal point / score announcement
    POINT_AWARDED(5),
    INFO(6),           // Informational e.g. "Change of ends", "Audio test"
    CUSTOM(6)
}

data class AnnouncementEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val category: AnnouncementCategory,
    val spokenText: String,
    val priority: Int = category.priority,
    val timestamp: Long = System.currentTimeMillis()
)
