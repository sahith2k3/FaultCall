package com.faultcall.shared.model

enum class Player(val displayName: String) {
    PLAYER_A("Player A"),
    PLAYER_B("Player B");

    fun opponent(): Player = if (this == PLAYER_A) PLAYER_B else PLAYER_A
}

enum class MatchFormat(val setsToWin: Int) {
    BEST_OF_3(2),
    BEST_OF_5(3)
}

enum class AdvantageRule {
    ADVANTAGE, // Standard tennis: deuce requires 2-point lead
    NO_AD      // Sudden death at 40-40: next point wins game
}

enum class TiebreakRule(val pointsToWin: Int) {
    STANDARD_7(7),
    SUPER_10(10)
}

enum class MatchStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PAUSED,
    FINISHED
}

data class SetScore(
    val playerAGames: Int,
    val playerBGames: Int,
    val playerATiebreakPoints: Int? = null,
    val playerBTiebreakPoints: Int? = null
) {
    val winner: Player?
        get() = when {
            playerAGames > playerBGames -> Player.PLAYER_A
            playerBGames > playerAGames -> Player.PLAYER_B
            else -> null
        }

    fun formatted(): String {
        return if (playerATiebreakPoints != null && playerBTiebreakPoints != null) {
            val minPoints = minOf(playerATiebreakPoints, playerBTiebreakPoints)
            "$playerAGames-$playerBGames($minPoints)"
        } else {
            "$playerAGames-$playerBGames"
        }
    }
}

data class MatchConfig(
    val playerAName: String = "Player A",
    val playerBName: String = "Player B",
    val matchFormat: MatchFormat = MatchFormat.BEST_OF_3,
    val advantageRule: AdvantageRule = AdvantageRule.ADVANTAGE,
    val finalSetSuperTiebreak: Boolean = false,
    val startingServer: Player = Player.PLAYER_A
) {
    fun nameOf(player: Player): String = when (player) {
        Player.PLAYER_A -> playerAName
        Player.PLAYER_B -> playerBName
    }
}
