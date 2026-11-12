package com.faultcall.shared.scoring

import com.faultcall.shared.model.*
import java.util.ArrayDeque

/**
 * Result returned after every point scoring action.
 */
data class ScoringResult(
    val state: MatchState,
    val announcementText: String,
    val category: AnnouncementCategory,
    val gameEnded: Boolean = false,
    val setEnded: Boolean = false,
    val matchEnded: Boolean = false
)

/**
 * Complete, independent tennis scoring engine.
 * Fully decoupled from UI and Android platform APIs.
 */
class TennisScoringEngine(
    initialConfig: MatchConfig = MatchConfig()
) {
    private var currentState: MatchState = MatchState(
        config = initialConfig,
        status = MatchStatus.IN_PROGRESS,
        currentServer = initialConfig.startingServer,
        initialServerOfCurrentSet = initialConfig.startingServer
    )

    // Historical stack of match states for unlimited undo
    private val historyStack = ArrayDeque<MatchState>()

    fun getState(): MatchState = currentState

    fun canUndo(): Boolean = historyStack.isNotEmpty()

    /**
     * Resets or starts a new match with new configuration or starting server.
     */
    fun reset(
        newConfig: MatchConfig? = null,
        newStartingServer: Player? = null
    ): MatchState {
        historyStack.clear()
        val config = newConfig ?: currentState.config
        val server = newStartingServer ?: config.startingServer
        currentState = MatchState(
            config = config,
            status = MatchStatus.IN_PROGRESS,
            currentServer = server,
            initialServerOfCurrentSet = server
        )
        return currentState
    }

    /**
     * Manually overrides the current server (for corrections).
     */
    fun setServer(player: Player): MatchState {
        historyStack.push(currentState)
        currentState = currentState.copy(currentServer = player)
        return currentState
    }

    /**
     * Replaces state directly (e.g. when synced from remote).
     */
    fun restoreState(state: MatchState) {
        historyStack.push(currentState)
        currentState = state
    }

    /**
     * Awards one point to the specified player and executes complete tennis rules.
     */
    fun awardPoint(scorer: Player): ScoringResult {
        if (currentState.status == MatchStatus.FINISHED) {
            // Match already finished; return current state without mutating
            return ScoringResult(
                state = currentState,
                announcementText = "Match finished. Winner: ${currentState.config.nameOf(currentState.winner ?: scorer)}.",
                category = AnnouncementCategory.INFO,
                matchEnded = true
            )
        }

        // Save current snapshot for undo
        historyStack.push(currentState)

        return if (currentState.isTiebreak) {
            handleTiebreakPoint(scorer)
        } else {
            handleStandardGamePoint(scorer)
        }
    }

    /**
     * Undoes the last scoring action and restores the exact previous state.
     */
    fun undo(): MatchState? {
        if (historyStack.isEmpty()) return null
        currentState = historyStack.pop()
        return currentState
    }

    // -------------------------------------------------------------
    // Standard Game Point Handler
    // -------------------------------------------------------------
    private fun handleStandardGamePoint(scorer: Player): ScoringResult {
        var ptsA = currentState.playerAPoints
        var ptsB = currentState.playerBPoints
        val noAd = currentState.config.advantageRule == AdvantageRule.NO_AD

        if (scorer == Player.PLAYER_A) {
            ptsA++
        } else {
            ptsB++
        }

        // Check if game is won
        var gameWonBy: Player? = null

        if (noAd) {
            // No-ad: at 40-40 (ptsA=3, ptsB=3), receiver's choice / next point wins
            if (ptsA >= 4 && scorer == Player.PLAYER_A) {
                gameWonBy = Player.PLAYER_A
            } else if (ptsB >= 4 && scorer == Player.PLAYER_B) {
                gameWonBy = Player.PLAYER_B
            }
        } else {
            // Advantage scoring:
            // Must have at least 4 points and lead by 2
            if (ptsA >= 4 && ptsA >= ptsB + 2) {
                gameWonBy = Player.PLAYER_A
            } else if (ptsB >= 4 && ptsB >= ptsA + 2) {
                gameWonBy = Player.PLAYER_B
            } else if (ptsA >= 3 && ptsB >= 3 && ptsA == ptsB) {
                // If pts were 4-3 and B scored -> 4-4, normalized back to 3-3 (Deuce)
                ptsA = 3
                ptsB = 3
            }
        }

        return if (gameWonBy != null) {
            handleGameWon(gameWonBy)
        } else {
            // Game continues
            val isDeuceNow = ptsA >= 3 && ptsB >= 3 && ptsA == ptsB
            val hasAdA = ptsA > ptsB && ptsA >= 4
            val hasAdB = ptsB > ptsA && ptsB >= 4

            val announcement: String
            val category: AnnouncementCategory

            val nameA = currentState.config.playerAName
            val nameB = currentState.config.playerBName

            if (isDeuceNow) {
                announcement = "Deuce!"
                category = AnnouncementCategory.DEUCE
            } else if (hasAdA) {
                announcement = "Advantage $nameA!"
                category = AnnouncementCategory.ADVANTAGE
            } else if (hasAdB) {
                announcement = "Advantage $nameB!"
                category = AnnouncementCategory.ADVANTAGE
            } else {
                announcement = formatScoreAnnouncement(
                    ptsA = ptsA,
                    ptsB = ptsB,
                    server = currentState.currentServer
                )
                category = AnnouncementCategory.SCORE
            }

            currentState = currentState.copy(
                playerAPoints = ptsA,
                playerBPoints = ptsB,
                lastAnnouncement = announcement,
                lastCategory = category,
                timestamp = System.currentTimeMillis()
            )

            ScoringResult(
                state = currentState,
                announcementText = announcement,
                category = category,
                gameEnded = false,
                setEnded = false,
                matchEnded = false
            )
        }
    }

    // -------------------------------------------------------------
    // Tiebreak Point Handler
    // -------------------------------------------------------------
    private fun handleTiebreakPoint(scorer: Player): ScoringResult {
        var tbA = currentState.tiebreakPointsA
        var tbB = currentState.tiebreakPointsB

        if (scorer == Player.PLAYER_A) tbA++ else tbB++

        val totalPoints = tbA + tbB
        val targetPoints = if (isFinalSet() && currentState.config.finalSetSuperTiebreak) {
            TiebreakRule.SUPER_10.pointsToWin
        } else {
            TiebreakRule.STANDARD_7.pointsToWin
        }

        // Check if tiebreak is won
        var tiebreakWinner: Player? = null
        if (tbA >= targetPoints && tbA >= tbB + 2) {
            tiebreakWinner = Player.PLAYER_A
        } else if (tbB >= targetPoints && tbB >= tbA + 2) {
            tiebreakWinner = Player.PLAYER_B
        }

        if (tiebreakWinner != null) {
            return handleTiebreakWon(tiebreakWinner, tbA, tbB)
        }

        // Tiebreak continues: rotate server
        // Rule: First server serves point 1. Then server alternates every 2 points.
        val firstServer = currentState.serverBeforeTiebreak ?: currentState.currentServer
        val nextServer = getTiebreakServer(totalPoints, firstServer)

        val serverScore = if (nextServer == Player.PLAYER_A) "$tbA $tbB" else "$tbB $tbA"
        val announcement = if (tbA == tbB) {
            "$tbA all!"
        } else {
            val leaderName = if (tbA > tbB) currentState.config.playerAName else currentState.config.playerBName
            "$serverScore $leaderName leads!"
        }

        val category = if (tbA >= targetPoints - 1 || tbB >= targetPoints - 1) {
            AnnouncementCategory.SET_POINT
        } else {
            AnnouncementCategory.SCORE
        }

        currentState = currentState.copy(
            tiebreakPointsA = tbA,
            tiebreakPointsB = tbB,
            currentServer = nextServer,
            lastAnnouncement = announcement,
            lastCategory = category,
            timestamp = System.currentTimeMillis()
        )

        return ScoringResult(
            state = currentState,
            announcementText = announcement,
            category = category,
            gameEnded = false,
            setEnded = false,
            matchEnded = false
        )
    }

    private fun getTiebreakServer(totalPointsPlayed: Int, firstServer: Player): Player {
        // totalPointsPlayed is points finished so far.
        // Point 1 (total=0): firstServer
        // Point 2 (total=1): opponent
        // Point 3 (total=2): opponent
        // Point 4 (total=3): firstServer
        // Point 5 (total=4): firstServer ...
        if (totalPointsPlayed == 0) return firstServer
        val cycle = (totalPointsPlayed - 1) / 2
        return if (cycle % 2 == 0) firstServer.opponent() else firstServer
    }

    // -------------------------------------------------------------
    // Game Won Handler
    // -------------------------------------------------------------
    private fun handleGameWon(winner: Player): ScoringResult {
        var gA = currentState.playerAGames
        var gB = currentState.playerBGames

        if (winner == Player.PLAYER_A) gA++ else gB++

        val winnerName = currentState.config.nameOf(winner)
        val nextServer = currentState.currentServer.opponent()

        // Check for set win or tiebreak entry
        var setWonBy: Player? = null
        var enterTiebreak = false

        if (gA >= 6 && gA >= gB + 2) {
            setWonBy = Player.PLAYER_A
        } else if (gB >= 6 && gB >= gA + 2) {
            setWonBy = Player.PLAYER_B
        } else if (gA == 6 && gB == 6) {
            enterTiebreak = true
        }

        if (setWonBy != null) {
            return handleSetWon(setWonBy, gA, gB, null, null)
        }

        if (enterTiebreak) {
            val announcement = "Six games all. Tiebreak."
            currentState = currentState.copy(
                playerAPoints = 0,
                playerBPoints = 0,
                playerAGames = 6,
                playerBGames = 6,
                isTiebreak = true,
                tiebreakPointsA = 0,
                tiebreakPointsB = 0,
                serverBeforeTiebreak = nextServer,
                currentServer = nextServer,
                lastAnnouncement = announcement,
                lastCategory = AnnouncementCategory.INFO,
                timestamp = System.currentTimeMillis()
            )

            return ScoringResult(
                state = currentState,
                announcementText = announcement,
                category = AnnouncementCategory.INFO,
                gameEnded = true,
                setEnded = false,
                matchEnded = false
            )
        }

        // Regular game won, set continues
        val announcement = "Game $winnerName! Score is $gA games to $gB!"
        currentState = currentState.copy(
            playerAPoints = 0,
            playerBPoints = 0,
            playerAGames = gA,
            playerBGames = gB,
            currentServer = nextServer,
            lastAnnouncement = announcement,
            lastCategory = AnnouncementCategory.GAME,
            timestamp = System.currentTimeMillis()
        )

        return ScoringResult(
            state = currentState,
            announcementText = announcement,
            category = AnnouncementCategory.GAME,
            gameEnded = true,
            setEnded = false,
            matchEnded = false
        )
    }

    // -------------------------------------------------------------
    // Tiebreak Won Handler
    // -------------------------------------------------------------
    private fun handleTiebreakWon(winner: Player, tbA: Int, tbB: Int): ScoringResult {
        val finalGamesA = if (winner == Player.PLAYER_A) 7 else 6
        val finalGamesB = if (winner == Player.PLAYER_B) 7 else 6
        return handleSetWon(winner, finalGamesA, finalGamesB, tbA, tbB)
    }

    // -------------------------------------------------------------
    // Set Won Handler
    // -------------------------------------------------------------
    private fun handleSetWon(
        winner: Player,
        gamesA: Int,
        gamesB: Int,
        tbPointsA: Int?,
        tbPointsB: Int?
    ): ScoringResult {
        val setScore = SetScore(
            playerAGames = gamesA,
            playerBGames = gamesB,
            playerATiebreakPoints = tbPointsA,
            playerBTiebreakPoints = tbPointsB
        )

        val updatedSets = currentState.completedSets + setScore
        val setsWonA = updatedSets.count { it.winner == Player.PLAYER_A }
        val setsWonB = updatedSets.count { it.winner == Player.PLAYER_B }
        val requiredSets = currentState.config.matchFormat.setsToWin

        val winnerName = currentState.config.nameOf(winner)
        val setNumberWord = when (updatedSets.size) {
            1 -> "first"
            2 -> "second"
            3 -> "third"
            4 -> "fourth"
            5 -> "fifth"
            else -> "set ${updatedSets.size}"
        }

        val matchWon = setsWonA == requiredSets || setsWonB == requiredSets

        if (matchWon) {
            val announcement = "Game set and match $winnerName!"
            currentState = currentState.copy(
                playerAPoints = 0,
                playerBPoints = 0,
                playerAGames = 0,
                playerBGames = 0,
                isTiebreak = false,
                tiebreakPointsA = 0,
                tiebreakPointsB = 0,
                serverBeforeTiebreak = null,
                completedSets = updatedSets,
                status = MatchStatus.FINISHED,
                winner = winner,
                lastAnnouncement = announcement,
                lastCategory = AnnouncementCategory.MATCH,
                timestamp = System.currentTimeMillis()
            )

            return ScoringResult(
                state = currentState,
                announcementText = announcement,
                category = AnnouncementCategory.MATCH,
                gameEnded = true,
                setEnded = true,
                matchEnded = true
            )
        }

        // Set won, match continues into next set
        val scoreAnnouncement = if (tbPointsA != null && tbPointsB != null) {
            val winnerPts = if (winner == Player.PLAYER_A) tbPointsA else tbPointsB
            val loserPts = if (winner == Player.PLAYER_A) tbPointsB else tbPointsA
            "Game and $setNumberWord set $winnerName! Seven games to six tiebreak $winnerPts points to $loserPts!"
        } else {
            val winG = if (winner == Player.PLAYER_A) gamesA else gamesB
            val loseG = if (winner == Player.PLAYER_A) gamesB else gamesA
            "Game and $setNumberWord set $winnerName! $winG games to $loseG!"
        }

        // Server for the new set:
        // If set ended on tiebreak, receiver of first tiebreak point serves game 1.
        // Otherwise, opponent of the last game's server serves game 1.
        val nextSetServer = if (currentState.isTiebreak) {
            (currentState.serverBeforeTiebreak ?: currentState.currentServer).opponent()
        } else {
            currentState.currentServer.opponent()
        }

        currentState = currentState.copy(
            playerAPoints = 0,
            playerBPoints = 0,
            playerAGames = 0,
            playerBGames = 0,
            isTiebreak = false,
            tiebreakPointsA = 0,
            tiebreakPointsB = 0,
            serverBeforeTiebreak = null,
            currentServer = nextSetServer,
            initialServerOfCurrentSet = nextSetServer,
            completedSets = updatedSets,
            lastAnnouncement = scoreAnnouncement,
            lastCategory = AnnouncementCategory.SET,
            timestamp = System.currentTimeMillis()
        )

        return ScoringResult(
            state = currentState,
            announcementText = scoreAnnouncement,
            category = AnnouncementCategory.SET,
            gameEnded = true,
            setEnded = true,
            matchEnded = false
        )
    }

    private fun isFinalSet(): Boolean {
        val setsPlayed = currentState.completedSets.size
        return when (currentState.config.matchFormat) {
            MatchFormat.BEST_OF_3 -> setsPlayed == 2
            MatchFormat.BEST_OF_5 -> setsPlayed == 4
        }
    }

    /**
     * Announces current score according to tennis conventions:
     * Server's point first!
     */
    fun getCurrentScoreAnnouncement(): String {
        if (currentState.status == MatchStatus.FINISHED) {
            val winnerName = currentState.config.nameOf(currentState.winner ?: Player.PLAYER_A)
            return "Game set and match $winnerName!"
        }

        if (currentState.isTiebreak) {
            val tbA = currentState.tiebreakPointsA
            val tbB = currentState.tiebreakPointsB
            return if (tbA == tbB) {
                "$tbA all!"
            } else {
                val serverTb = if (currentState.currentServer == Player.PLAYER_A) tbA else tbB
                val receiverTb = if (currentState.currentServer == Player.PLAYER_A) tbB else tbA
                "$serverTb $receiverTb!"
            }
        }

        val ptsA = currentState.playerAPoints
        val ptsB = currentState.playerBPoints

        if (ptsA >= 3 && ptsB >= 3) {
            return when {
                ptsA == ptsB -> "Deuce!"
                ptsA > ptsB -> "Advantage ${currentState.config.playerAName}!"
                else -> "Advantage ${currentState.config.playerBName}!"
            }
        }

        return formatScoreAnnouncement(ptsA, ptsB, currentState.currentServer)
    }

    private fun formatScoreAnnouncement(ptsA: Int, ptsB: Int, server: Player): String {
        val serverPts = if (server == Player.PLAYER_A) ptsA else ptsB
        val receiverPts = if (server == Player.PLAYER_A) ptsB else ptsA

        val serverWord = pointToWord(serverPts)
        val receiverWord = pointToWord(receiverPts)

        return if (serverPts == receiverPts) {
            when (serverPts) {
                0 -> "Love all!"
                1 -> "Fifteen all!"
                2 -> "Thirty all!"
                else -> "Deuce!"
            }
        } else {
            "$serverWord $receiverWord!"
        }
    }

    private fun pointToWord(pts: Int): String = when (pts) {
        0 -> "Love"
        1 -> "Fifteen"
        2 -> "Thirty"
        3 -> "Forty"
        else -> "Forty"
    }
}
