# FaultCall — Automated & Manual Test Results

## Summary
- **Total Tests Executed**: 33
- **Passed**: 33
- **Failed**: 0
- **Skipped**: 0
- **Overall Test Pass Rate**: 100%
- **XML Reports Path**: `FaultCall/shared/build/test-results/test/`

---

## 1. Tennis Scoring Engine Test Suite (`TennisScoringEngineTest`)

| ID | Test Name | Target Behavior | Outcome |
|----|-----------|-----------------|---------|
| 1 | `test01_loveAll` | 0-0 announces "Love-all.", state points 0-0 | **PASSED** |
| 2 | `test02_fifteenLove` | 1-0 announces "Fifteen-Love.", Player A 1 pt | **PASSED** |
| 3 | `test03_thirtyLove` | 2-0 announces "Thirty-Love.", Player A 2 pts | **PASSED** |
| 4 | `test04_fortyLove` | 3-0 announces "Forty-Love.", Player A 3 pts | **PASSED** |
| 5 | `test05_deuce` | 3-3 announces "Deuce.", isDeuce flag true | **PASSED** |
| 6 | `test06_advantageA` | 4-3 announces "Advantage, Player A." | **PASSED** |
| 7 | `test07_returnToDeuce` | 4-4 returns and announces "Deuce.", isDeuce flag true | **PASSED** |
| 8 | `test08_gameWonByA` | 4-point win with 2 lead awards game, rotates server to B | **PASSED** |
| 9 | `test09_gameWonByB` | 4-point win awards game to B, rotates server to B | **PASSED** |
| 10 | `test10_setWonAt6_0` | 6 games to 0 completes first set, records winner A | **PASSED** |
| 11 | `test11_setWonAt6_4` | 6 games to 4 completes set with 2-game lead | **PASSED** |
| 12 | `test12_setWonAt7_5` | 5-5 extends to 7-5 win without entering tiebreak | **PASSED** |
| 13 | `test13_tiebreakAt6_6` | 6-6 triggers tiebreak state, resets game points | **PASSED** |
| 14 | `test14_tiebreakVictoryAt7_5` | 7-5 tiebreak completes set 7-6(5) | **PASSED** |
| 15 | `test15_tiebreakContinuationAt7_6` | 7-6 tiebreak continues because 2-point lead required | **PASSED** |
| 16 | `test16_tiebreakVictoryAt8_6` | 8-6 tiebreak wins set 7-6(6) | **PASSED** |
| 17 | `test17_superTiebreak` | Deciding set 10-point super tiebreak victory | **PASSED** |
| 18 | `test18_bestOfThreeMatchCompletion` | First to 2 sets completes match and declares winner | **PASSED** |
| 19 | `test19_bestOfFiveMatchCompletion` | First to 3 sets completes best-of-5 match | **PASSED** |
| 20 | `test20_noAdScoring` | Sudden death at 40-40 awards game on 1 point | **PASSED** |
| 21 | `test21_serverRotation` | Alternates server every completed game | **PASSED** |
| 22 | `test22_tiebreakServiceRotation` | Point 1 served by first server, points 2 & 3 by opponent, rotates every 2 | **PASSED** |
| 23 | `test23_undoAfterPoint` | Reverts 15-0 back to 0-0 | **PASSED** |
| 24 | `test24_undoAfterGame` | Reverts game win back to 40-0, restores server | **PASSED** |
| 25 | `test25_undoAfterSet` | Reverts set win back to 5 games, 40-0 | **PASSED** |
| 26 | `test26_repeatedUndo` | Multi-step unrolling all the way to start of match | **PASSED** |
| 27 | `test27_persistenceAndRestoration` | Restores snapshot state and continues scoring | **PASSED** |
| 28 | `test28_duplicateCommandHandling` | Duplicate UUIDs rejected, preventing double points | **PASSED** |

---

## 2. Command Protocol Test Suite (`CommandProtocolTest`)

| ID | Test Name | Target Behavior | Outcome |
|----|-----------|-----------------|---------|
| 29 | `testCommandSerializationAndDeserialization` | JSON serialization/deserialization of `WearableCommand` | **PASSED** |
| 30 | `testAcknowledgmentSerialization` | JSON serialization/deserialization of `CommandAcknowledgment` | **PASSED** |
| 31 | `testDuplicateWindowRejection` | Sliding window idempotency prevents duplicate command execution | **PASSED** |

---

## 3. Announcement Queue Test Suite (`AnnouncementQueueTest`)

| ID | Test Name | Target Behavior | Outcome |
|----|-----------|-----------------|---------|
| 32 | `testPriorityOrderingFaultPreemptsScore` | Priority 1 (FAULT) polled before Priority 3 (GAME) and Priority 5 (SCORE) | **PASSED** |
| 33 | `testPurgeObsoleteScoreAnnouncementsOnUndo` | Discards pending score announcements when UNDO or point award occurs | **PASSED** |

---

## 4. Build Artifact Verification

Both debug APKs built and packaged successfully:
- **Phone Application (`:mobile`)**:  
  `c:\Users\sahith\Documents\antigravity\magical-bohr\release_apks\FaultCall-Mobile-v1.0.0.apk`  
  - Size: 17,088,497 bytes (17.08 MB)  
  - Target SDK: 34 (Android 14, Galaxy S24 Ultra)  
  - Verification: `BUILD SUCCESSFUL`

- **Wear OS Application (`:wear`)**:  
  `c:\Users\sahith\Documents\antigravity\magical-bohr\release_apks\FaultCall-GalaxyWatch8-v1.0.0.apk`  
  - Size: 20,665,708 bytes (20.66 MB)  
  - Target SDK: 34 (Wear OS 5, Galaxy Watch8)  
  - Verification: `BUILD SUCCESSFUL`

---

## 5. Physical Hardware Verification Status

Physical devices (Samsung Galaxy S24 Ultra, Galaxy Watch8, Bluetooth speaker) were not physically attached to ADB during the automated build pipeline. Complete step-by-step wireless and wired hardware deployment instructions and an interactive courtside checklist have been documented in [`INSTALLATION_GUIDE.md`](INSTALLATION_GUIDE.md).
