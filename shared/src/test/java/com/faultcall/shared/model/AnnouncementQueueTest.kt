package com.faultcall.shared.model

import org.junit.Assert.*
import org.junit.Test
import java.util.PriorityQueue

class AnnouncementQueueTest {

    @Test
    fun testPriorityOrderingFaultPreemptsScore() {
        val queue = PriorityQueue<AnnouncementEvent>(compareBy { it.priority })

        val scoreEvent = AnnouncementEvent(
            category = AnnouncementCategory.SCORE,
            spokenText = "Thirty-fifteen.",
            priority = 5
        )

        val faultEvent = AnnouncementEvent(
            category = AnnouncementCategory.FAULT,
            spokenText = "FAULT!",
            priority = 1
        )

        val gameEvent = AnnouncementEvent(
            category = AnnouncementCategory.GAME,
            spokenText = "Game Player A.",
            priority = 3
        )

        queue.add(scoreEvent)
        queue.add(faultEvent)
        queue.add(gameEvent)

        // FAULT (priority 1) must be polled first!
        assertEquals(AnnouncementCategory.FAULT, queue.poll()?.category)
        // GAME (priority 3) next
        assertEquals(AnnouncementCategory.GAME, queue.poll()?.category)
        // SCORE (priority 5) last
        assertEquals(AnnouncementCategory.SCORE, queue.poll()?.category)
    }

    @Test
    fun testPurgeObsoleteScoreAnnouncementsOnUndo() {
        val queue = PriorityQueue<AnnouncementEvent>(compareBy { it.priority })

        queue.add(AnnouncementEvent(category = AnnouncementCategory.SCORE, spokenText = "Thirty-love."))
        queue.add(AnnouncementEvent(category = AnnouncementCategory.POINT_AWARDED, spokenText = "Point Player A."))
        queue.add(AnnouncementEvent(category = AnnouncementCategory.FAULT, spokenText = "FAULT!"))

        // When undo or new point occurs, remove pending scores
        queue.removeAll { it.category == AnnouncementCategory.SCORE || it.category == AnnouncementCategory.POINT_AWARDED }

        assertEquals(1, queue.size)
        assertEquals(AnnouncementCategory.FAULT, queue.poll()?.category)
    }
}
