package com.example.ersteversion.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ChangeUtilsTest {

    private fun change(id: Int) = Change(id, "AUSFALL", "Titel $id", "Beschreibung $id", false)

    @Test
    fun `alle Aenderungen ungesehen wenn seenIds leer ist`() {
        val changes = listOf(change(1), change(2), change(3))
        val count = countUnseenChanges(changes, emptySet())
        assertEquals(3, count)
    }

    @Test
    fun `bereits gesehene Aenderungen werden nicht gezaehlt`() {
        val changes = listOf(change(1), change(2), change(3))
        val count = countUnseenChanges(changes, setOf("1", "2"))
        assertEquals(1, count)
    }

    @Test
    fun `keine Aenderungen bedeutet null ungesehene`() {
        val count = countUnseenChanges(emptyList(), setOf("1"))
        assertEquals(0, count)
    }

    @Test
    fun `alle Aenderungen bereits gesehen ergibt null`() {
        val changes = listOf(change(1), change(2))
        val count = countUnseenChanges(changes, setOf("1", "2"))
        assertEquals(0, count)
    }
}