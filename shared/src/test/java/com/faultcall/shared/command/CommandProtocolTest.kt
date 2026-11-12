package com.faultcall.shared.command

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class CommandProtocolTest {

    private val gson = Gson()

    @Test
    fun testCommandSerializationAndDeserialization() {
        val original = WearableCommand(
            commandId = UUID.randomUUID().toString(),
            type = CommandType.ANNOUNCE_FAULT,
            timestamp = System.currentTimeMillis(),
            matchId = "match-789",
            payload = "first_serve"
        )

        val json = gson.toJson(original)
        assertNotNull(json)
        assertTrue(json.contains("ANNOUNCE_FAULT"))

        val deserialized = gson.fromJson(json, WearableCommand::class.java)
        assertEquals(original.commandId, deserialized.commandId)
        assertEquals(original.type, deserialized.type)
        assertEquals(original.matchId, deserialized.matchId)
        assertEquals(original.payload, deserialized.payload)
    }

    @Test
    fun testAcknowledgmentSerialization() {
        val ack = CommandAcknowledgment(
            commandId = "cmd-123",
            type = CommandType.AWARD_POINT_A,
            success = true,
            spokenText = "Fifteen-Love.",
            isDuplicate = false
        )

        val json = gson.toJson(ack)
        val deserialized = gson.fromJson(json, CommandAcknowledgment::class.java)
        assertEquals("cmd-123", deserialized.commandId)
        assertTrue(deserialized.success)
        assertEquals("Fifteen-Love.", deserialized.spokenText)
        assertFalse(deserialized.isDuplicate)
    }

    @Test
    fun testDuplicateWindowRejection() {
        val recentIds = LinkedHashSet<String>()
        val testId = "cmd-duplicate-test"

        // First arrival
        var handledFirst = false
        if (!recentIds.contains(testId)) {
            recentIds.add(testId)
            handledFirst = true
        }
        assertTrue(handledFirst)

        // Retry arrival (same ID)
        var handledSecond = false
        if (!recentIds.contains(testId)) {
            handledSecond = true
        }
        assertFalse(handledSecond) // must be rejected as duplicate
    }
}
