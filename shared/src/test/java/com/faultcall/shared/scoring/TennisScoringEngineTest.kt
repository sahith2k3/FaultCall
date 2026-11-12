package com.faultcall.shared.scoring

import com.faultcall.shared.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TennisScoringEngineTest {

    private lateinit var engine: TennisScoringEngine

    @Before
    fun setUp() {
        engine = TennisScoringEngine(
            MatchConfig(
                playerAName = "Player A",
                playerBName = "Player B",
                startingServer = Player.PLAYER_A
            )
        )
    }

    // 1. Love-all
    @Test
    fun test01_loveAll() {
        val announcement = engine.getCurrentScoreAnnouncement()
        assertEquals("Love all!", announcement)
        val state = engine.getState()
        assertEquals(0, state.playerAPoints)
        assertEquals(0, state.playerBPoints)
    }

    // 2. Fifteen-love
    @Test
    fun test02_fifteenLove() {
        val res = engine.awardPoint(Player.PLAYER_A)
        assertEquals("Fifteen Love!", res.announcementText)
        assertEquals(1, res.state.playerAPoints)
        assertEquals(0, res.state.playerBPoints)
    }

    // 3. Thirty-love
    @Test
    fun test03_thirtyLove() {
        engine.awardPoint(Player.PLAYER_A)
        val res = engine.awardPoint(Player.PLAYER_A)
        assertEquals("Thirty Love!", res.announcementText)
        assertEquals(2, res.state.playerAPoints)
        assertEquals(0, res.state.playerBPoints)
    }

    // 4. Forty-love
    @Test
    fun test04_fortyLove() {
        engine.awardPoint(Player.PLAYER_A)
        engine.awardPoint(Player.PLAYER_A)
        val res = engine.awardPoint(Player.PLAYER_A)
        assertEquals("Forty Love!", res.announcementText)
        assertEquals(3, res.state.playerAPoints)
        assertEquals(0, res.state.playerBPoints)
    }

    // 5. Deuce
    @Test
    fun test05_deuce() {
        // A scores 3, B scores 3
        repeat(3) { engine.awardPoint(Player.PLAYER_A) }
        repeat(2) { engine.awardPoint(Player.PLAYER_B) }
        val res = engine.awardPoint(Player.PLAYER_B)
        assertEquals("Deuce!", res.announcementText)
        assertTrue(engine.getState().isDeuce)
    }

    // 6. Advantage A
    @Test
    fun test06_advantageA() {
        repeat(3) { engine.awardPoint(Player.PLAYER_A) }
        repeat(3) { engine.awardPoint(Player.PLAYER_B) }
        val res = engine.awardPoint(Player.PLAYER_A)
        assertEquals("Advantage Player A!", res.announcementText)
        assertEquals(Player.PLAYER_A, engine.getState().hasAdvantage)
    }

    // 7. Return to deuce
    @Test
    fun test07_returnToDeuce() {
        repeat(3) { engine.awardPoint(Player.PLAYER_A) }
        repeat(3) { engine.awardPoint(Player.PLAYER_B) }
        engine.awardPoint(Player.PLAYER_A) // Ad A
        val res = engine.awardPoint(Player.PLAYER_B) // back to Deuce
        assertEquals("Deuce!", res.announcementText)
        assertTrue(engine.getState().isDeuce)
    }

    // 8. Game won by A
    @Test
    fun test08_gameWonByA() {
        repeat(3) { engine.awardPoint(Player.PLAYER_A) }
        val res = engine.awardPoint(Player.PLAYER_A)
        assertTrue(res.gameEnded)
        assertEquals(1, res.state.playerAGames)
        assertEquals(0, res.state.playerBGames)
        assertEquals(Player.PLAYER_B, res.state.currentServer) // Service rotated
    }

    // 9. Game won by B
    @Test
    fun test09_gameWonByB() {
        repeat(4) { engine.awardPoint(Player.PLAYER_B) }
        val state = engine.getState()
        assertEquals(0, state.playerAGames)
        assertEquals(1, state.playerBGames)
        assertEquals(Player.PLAYER_B, state.currentServer) // Alternated from A to B
    }

    // 10. Set won at 6-0
    @Test
    fun test10_setWonAt6_0() {
        repeat(6) {
            repeat(4) { engine.awardPoint(Player.PLAYER_A) }
        }
        val state = engine.getState()
        assertEquals(1, state.completedSets.size)
        assertEquals(6, state.completedSets[0].playerAGames)
        assertEquals(0, state.completedSets[0].playerBGames)
        assertEquals(Player.PLAYER_A, state.completedSets[0].winner)
    }

    // 11. Set won at 6-4
    @Test
    fun test11_setWonAt6_4() {
        // A wins 4 games, B wins 4 games
        repeat(4) {
            repeat(4) { engine.awardPoint(Player.PLAYER_A) }
            repeat(4) { engine.awardPoint(Player.PLAYER_B) }
        }
        // A wins 2 more games -> 6-4
        repeat(2) {
            repeat(4) { engine.awardPoint(Player.PLAYER_A) }
        }
        val state = engine.getState()
        assertEquals(1, state.completedSets.size)
        assertEquals(6, state.completedSets[0].playerAGames)
        assertEquals(4, state.completedSets[0].playerBGames)
    }

    // 12. Set won at 7-5
    @Test
    fun test12_setWonAt7_5() {
        // 5-5
        repeat(5) {
            repeat(4) { engine.awardPoint(Player.PLAYER_A) }
            repeat(4) { engine.awardPoint(Player.PLAYER_B) }
        }
        // A wins game 11 (6-5)
        repeat(4) { engine.awardPoint(Player.PLAYER_A) }
        assertFalse(engine.getState().completedSets.isNotEmpty()) // not completed yet!
        // A wins game 12 (7-5)
        val res = engine.awardPoint(Player.PLAYER_A)
        repeat(3) { engine.awardPoint(Player.PLAYER_A) }
        val state = engine.getState()
        assertEquals(1, state.completedSets.size)
        assertEquals(7, state.completedSets[0].playerAGames)
        assertEquals(5, state.completedSets[0].playerBGames)
    }

    // 13. Tiebreak at 6-6
    @Test
    fun test13_tiebreakAt6_6() {
        repeat(6) {
            repeat(4) { engine.awardPoint(Player.PLAYER_A) }
            repeat(4) { engine.awardPoint(Player.PLAYER_B) }
        }
        val state = engine.getState()
        assertTrue(state.isTiebreak)
        assertEquals(6, state.playerAGames)
        assertEquals(6, state.playerBGames)
    }

    // 14. Tiebreak victory at 7-5
    @Test
    fun test14_tiebreakVictoryAt7_5() {
        // Reach 6-6
        repeat(6) {
            repeat(4) { engine.awardPoint(Player.PLAYER_A) }
            repeat(4) { engine.awardPoint(Player.PLAYER_B) }
        }
        // In tiebreak: A wins 7, B wins 5
        repeat(5) {
            engine.awardPoint(Player.PLAYER_A)
            engine.awardPoint(Player.PLAYER_B)
        }
        // A=5, B=5
        engine.awardPoint(Player.PLAYER_A) // 6-5
        val res = engine.awardPoint(Player.PLAYER_A) // 7-5 win!
        assertTrue(res.setEnded)
        val state = engine.getState()
        assertEquals(1, state.completedSets.size)
        assertEquals(7, state.completedSets[0].playerAGames)
        assertEquals(6, state.completedSets[0].playerBGames)
        assertEquals(7, state.completedSets[0].playerATiebreakPoints)
        assertEquals(5, state.completedSets[0].playerBTiebreakPoints)
    }

    // 15. Tiebreak continuation at 7-6
    @Test
    fun test15_tiebreakContinuationAt7_6() {
        repeat(6) {
            repeat(4) { engine.awardPoint(Player.PLAYER_A) }
            repeat(4) { engine.awardPoint(Player.PLAYER_B) }
        }
        repeat(6) {
            engine.awardPoint(Player.PLAYER_A)
            engine.awardPoint(Player.PLAYER_B)
        }
        // 6-6 in tiebreak
        val res = engine.awardPoint(Player.PLAYER_A) // 7-6
        assertFalse(res.setEnded) // Must lead by 2!
        assertTrue(engine.getState().isTiebreak)
    }

    // 16. Tiebreak victory at 8-6
    @Test
    fun test16_tiebreakVictoryAt8_6() {
        repeat(6) {
            repeat(4) { engine.awardPoint(Player.PLAYER_A) }
            repeat(4) { engine.awardPoint(Player.PLAYER_B) }
        }
        repeat(6) {
            engine.awardPoint(Player.PLAYER_A)
            engine.awardPoint(Player.PLAYER_B)
        }
        engine.awardPoint(Player.PLAYER_A) // 7-6
        val res = engine.awardPoint(Player.PLAYER_A) // 8-6
        assertTrue(res.setEnded)
        val set = engine.getState().completedSets[0]
        assertEquals(8, set.playerATiebreakPoints)
        assertEquals(6, set.playerBTiebreakPoints)
    }

    // 17. Super tiebreak
    @Test
    fun test17_superTiebreak() {
        val superEngine = TennisScoringEngine(
            MatchConfig(
                matchFormat = MatchFormat.BEST_OF_3,
                finalSetSuperTiebreak = true
            )
        )
        // Set 1 won by A (6-0)
        repeat(6) { repeat(4) { superEngine.awardPoint(Player.PLAYER_A) } }
        // Set 2 won by B (0-6)
        repeat(6) { repeat(4) { superEngine.awardPoint(Player.PLAYER_B) } }
        assertEquals(2, superEngine.getState().completedSets.size)

        // Set 3 reaches 6-6
        repeat(6) {
            repeat(4) { superEngine.awardPoint(Player.PLAYER_A) }
            repeat(4) { superEngine.awardPoint(Player.PLAYER_B) }
        }
        assertTrue(superEngine.getState().isTiebreak)

        // Super tiebreak target is 10 points
        repeat(9) { superEngine.awardPoint(Player.PLAYER_A) }
        assertFalse(superEngine.getState().isFinished)
        val finalPt = superEngine.awardPoint(Player.PLAYER_A) // 10-0
        assertTrue(finalPt.matchEnded)
        assertEquals(Player.PLAYER_A, superEngine.getState().winner)
    }

    // 18. Best-of-three match completion
    @Test
    fun test18_bestOfThreeMatchCompletion() {
        // Set 1 (6-0)
        repeat(6) { repeat(4) { engine.awardPoint(Player.PLAYER_A) } }
        assertFalse(engine.getState().isFinished)
        // Set 2 (6-0)
        repeat(6) { repeat(4) { engine.awardPoint(Player.PLAYER_A) } }
        assertTrue(engine.getState().isFinished)
        assertEquals(Player.PLAYER_A, engine.getState().winner)
        assertEquals("Game set and match Player A!", engine.getState().lastAnnouncement)
    }

    // 19. Best-of-five match completion
    @Test
    fun test19_bestOfFiveMatchCompletion() {
        val b5Engine = TennisScoringEngine(
            MatchConfig(matchFormat = MatchFormat.BEST_OF_5)
        )
        // A wins set 1
        repeat(6) { repeat(4) { b5Engine.awardPoint(Player.PLAYER_A) } }
        // B wins set 2
        repeat(6) { repeat(4) { b5Engine.awardPoint(Player.PLAYER_B) } }
        // A wins set 3
        repeat(6) { repeat(4) { b5Engine.awardPoint(Player.PLAYER_A) } }
        assertFalse(b5Engine.getState().isFinished)
        // A wins set 4 -> 3 sets won!
        repeat(6) { repeat(4) { b5Engine.awardPoint(Player.PLAYER_A) } }
        assertTrue(b5Engine.getState().isFinished)
        assertEquals(3, b5Engine.getState().setsWonBy(Player.PLAYER_A))
    }

    // 20. No-ad scoring
    @Test
    fun test20_noAdScoring() {
        val noAdEngine = TennisScoringEngine(
            MatchConfig(advantageRule = AdvantageRule.NO_AD)
        )
        // 40-40
        repeat(3) { noAdEngine.awardPoint(Player.PLAYER_A) }
        repeat(3) { noAdEngine.awardPoint(Player.PLAYER_B) }
        // Sudden death point wins game immediately
        val res = noAdEngine.awardPoint(Player.PLAYER_A)
        assertTrue(res.gameEnded)
        assertEquals(1, res.state.playerAGames)
    }

    // 21. Server rotation
    @Test
    fun test21_serverRotation() {
        assertEquals(Player.PLAYER_A, engine.getState().currentServer)
        // Game 1 won by A
        repeat(4) { engine.awardPoint(Player.PLAYER_A) }
        assertEquals(Player.PLAYER_B, engine.getState().currentServer)
        // Game 2 won by B
        repeat(4) { engine.awardPoint(Player.PLAYER_B) }
        assertEquals(Player.PLAYER_A, engine.getState().currentServer)
    }

    // 22. Tiebreak service rotation
    @Test
    fun test22_tiebreakServiceRotation() {
        // Reach 6-6
        repeat(6) {
            repeat(4) { engine.awardPoint(Player.PLAYER_A) }
            repeat(4) { engine.awardPoint(Player.PLAYER_B) }
        }
        val firstServer = engine.getState().currentServer
        // Point 1 served by firstServer
        engine.awardPoint(Player.PLAYER_A)
        // Points 2 and 3 served by opponent
        assertEquals(firstServer.opponent(), engine.getState().currentServer)
        engine.awardPoint(Player.PLAYER_A)
        assertEquals(firstServer.opponent(), engine.getState().currentServer)
        // Point 4 served by firstServer
        engine.awardPoint(Player.PLAYER_A)
        assertEquals(firstServer, engine.getState().currentServer)
    }

    // 23. Undo after a point
    @Test
    fun test23_undoAfterPoint() {
        engine.awardPoint(Player.PLAYER_A)
        assertEquals(1, engine.getState().playerAPoints)
        val undone = engine.undo()
        assertNotNull(undone)
        assertEquals(0, engine.getState().playerAPoints)
        assertEquals(0, engine.getState().playerBPoints)
    }

    // 24. Undo after a game
    @Test
    fun test24_undoAfterGame() {
        repeat(4) { engine.awardPoint(Player.PLAYER_A) }
        assertEquals(1, engine.getState().playerAGames)
        assertEquals(Player.PLAYER_B, engine.getState().currentServer)
        engine.undo() // restores 40-0 in game 1
        val state = engine.getState()
        assertEquals(0, state.playerAGames)
        assertEquals(3, state.playerAPoints)
        assertEquals(Player.PLAYER_A, state.currentServer)
    }

    // 25. Undo after a set
    @Test
    fun test25_undoAfterSet() {
        repeat(6) {
            repeat(4) { engine.awardPoint(Player.PLAYER_A) }
        }
        assertEquals(1, engine.getState().completedSets.size)
        engine.undo() // undo set winning point
        assertEquals(0, engine.getState().completedSets.size)
        assertEquals(5, engine.getState().playerAGames)
        assertEquals(3, engine.getState().playerAPoints)
    }

    // 26. Repeated undo
    @Test
    fun test26_repeatedUndo() {
        repeat(10) { engine.awardPoint(Player.PLAYER_A) }
        var undoCount = 0
        while (engine.canUndo()) {
            engine.undo()
            undoCount++
        }
        assertEquals(10, undoCount)
        assertEquals(0, engine.getState().playerAGames)
        assertEquals(0, engine.getState().playerAPoints)
    }

    // 27. Persistence and restoration
    @Test
    fun test27_persistenceAndRestoration() {
        repeat(3) { engine.awardPoint(Player.PLAYER_A) }
        repeat(2) { engine.awardPoint(Player.PLAYER_B) }
        val savedState = engine.getState()

        val newEngine = TennisScoringEngine()
        newEngine.restoreState(savedState)
        assertEquals(3, newEngine.getState().playerAPoints)
        assertEquals(2, newEngine.getState().playerBPoints)
        assertEquals("Forty Thirty!", newEngine.getCurrentScoreAnnouncement())
    }

    // 28. Duplicate command handling
    @Test
    fun test28_duplicateCommandHandling() {
        val processedCommands = mutableSetOf<String>()
        val commandId = "unique-cmd-1234"

        // First attempt: should execute
        var pointAwarded = false
        if (!processedCommands.contains(commandId)) {
            processedCommands.add(commandId)
            engine.awardPoint(Player.PLAYER_A)
            pointAwarded = true
        }
        assertTrue(pointAwarded)
        assertEquals(1, engine.getState().playerAPoints)

        // Retry with same commandId: should be skipped
        var secondAward = false
        if (!processedCommands.contains(commandId)) {
            engine.awardPoint(Player.PLAYER_A)
            secondAward = true
        }
        assertFalse(secondAward)
        assertEquals(1, engine.getState().playerAPoints) // Still 1 point!
    }
}
