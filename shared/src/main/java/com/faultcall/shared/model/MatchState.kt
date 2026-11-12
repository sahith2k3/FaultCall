package com.faultcall.shared.model

/**
 * Immutable snapshot of the tennis match at any instant.
 * Used for reactive UI rendering, state restoration, undo stack,
 * and Wearable Data Layer cross-device replication.
 */
data class MatchState(
    val matchId: String = java.util.UUID.randomUUID().toString(),
    val config: MatchConfig = MatchConfig(),
    val status: MatchStatus = MatchStatus.NOT_STARTED,
    val currentServer: Player = config.startingServer,
    val initialServerOfCurrentSet: Player = config.startingServer,

    // Points in the current game (0=Love, 1=15, 2=30, 3=40, 4=Ad)
    val playerAPoints: Int = 0,
    val playerBPoints: Int = 0,

    // Games won in current set
    val playerAGames: Int = 0,
    val playerBGames: Int = 0,

    // Tiebreak state
    val isTiebreak: Boolean = false,
    val tiebreakPointsA: Int = 0,
    val tiebreakPointsB: Int = 0,
    val serverBeforeTiebreak: Player? = null,

    // Completed sets
    val completedSets: List<SetScore> = emptyList(),

    // Match outcome
    val winner: Player? = null,
    val lastAnnouncement: String? = null,
    val lastCategory: AnnouncementCategory? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val currentSetNumber: Int
        get() = completedSets.size + 1

    val isFinished: Boolean
        get() = status == MatchStatus.FINISHED

    val isDeuce: Boolean
        get() = !isTiebreak && playerAPoints >= 3 && playerBPoints >= 3 && playerAPoints == playerBPoints

    val hasAdvantage: Player?
        get() = when {
            isTiebreak -> null
            playerAPoints >= 3 && playerBPoints >= 3 -> {
                if (playerAPoints == playerBPoints + 1) Player.PLAYER_A
                else if (playerBPoints == playerAPoints + 1) Player.PLAYER_B
                else null
            }
            else -> null
        }

    fun setsWonBy(player: Player): Int =
        completedSets.count { it.winner == player }

    /**
     * Compact display string of current set score, e.g. "6-4, 3-2"
     */
    fun formattedScoreSummary(): String {
        val completed = completedSets.joinToString(" ") { it.formatted() }
        val current = "$playerAGames-$playerBGames"
        return if (completed.isEmpty()) current else "$completed $current"
    }

    /**
     * Display point string for Player A, e.g. "0", "15", "30", "40", "AD", or "7" (in tiebreak)
     */
    fun displayPoints(player: Player): String {
        if (isTiebreak) {
            return if (player == Player.PLAYER_A) tiebreakPointsA.toString() else tiebreakPointsB.toString()
        }
        val points = if (player == Player.PLAYER_A) playerAPoints else playerBPoints
        val opponentPoints = if (player == Player.PLAYER_A) playerBPoints else playerAPoints

        return when {
            points == 0 -> "0"
            points == 1 -> "15"
            points == 2 -> "30"
            points == 3 -> "40"
            points > opponentPoints -> "AD"
            points == opponentPoints && points >= 3 -> "40"
            else -> "40"
        }
    }
}
