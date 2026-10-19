package com.example.ersteversion.data.network

import com.example.ersteversion.data.dto.ChangeDto
import com.example.ersteversion.data.dto.LessonDto
import com.example.ersteversion.data.dto.RoomDto
import com.example.ersteversion.data.dto.RoomLessonDto
import com.example.ersteversion.data.model.Day
import com.example.ersteversion.data.model.Room
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappingTest {

    @Test
    fun `LessonDto wird korrekt zu Lesson gemappt`() {
        val dto = LessonDto(
            id = 7,
            wochentag = "MONTAG",
            stundennummer = 1,
            start = "08:00:00",
            ende = "08:45:00",
            fachName = "D",
            raumBezeichnung = "R102",
            lehrkraftKuerzel = "WEB",
            lerngruppeKuerzel = "5a",
            geaendert = true,
            freistunde = false,
            offeneAenderung = false,
            grund = "RAUMAENDERUNG"
        )

        val lesson = dto.toLesson()

        assertEquals(7, lesson.id)
        assertEquals("D", lesson.subject)
        assertEquals("WEB", lesson.teacher)
        assertEquals("5a", lesson.schoolClass)
        assertEquals("R102", lesson.room.name)
        assertEquals(Day.MONDAY, lesson.day)
        assertEquals(1, lesson.rasterIndex)
        assertEquals(true, lesson.changed)
        assertEquals("RAUMAENDERUNG", lesson.changeReason)
    }

    @Test
    fun `RoomLessonDto wird korrekt zu Lesson gemappt, Raum kommt von aussen`() {
        val dto = RoomLessonDto(
            wochentag = "DIENSTAG",
            stundennummer = 2,
            start = "08:45:00",
            ende = "09:30:00",
            fachName = "E",
            lehrkraftKuerzel = "KLE",
            lerngruppeKuerzel = "5a",
            geaendert = false
        )
        val room = Room(id = "1", name = "101")

        val lesson = dto.toLesson(room)

        assertEquals("E", lesson.subject)
        assertEquals("KLE", lesson.teacher)
        assertEquals(room, lesson.room)
        assertEquals(Day.TUESDAY, lesson.day)
        assertEquals(false, lesson.changed)
        assertNull(lesson.changeReason)
    }

    @Test
    fun `RoomDto wird korrekt zu Room gemappt`() {
        val dto = RoomDto(id = 1, bezeichnung = "R101", art = "KLASSENRAUM", kapazitaet = 32)

        val room = dto.toRoom()

        assertEquals("1", room.id)
        assertEquals("R101", room.name)
    }

    @Test
    fun `ChangeDto wird korrekt zu Change gemappt`() {
        val dto = ChangeDto(
            id = 1,
            typ = "RAUMAENDERUNG",
            titel = "Raumänderung",
            beschreibung = "Beschreibung",
            offen = false
        )

        val change = dto.toChange()

        assertEquals(1, change.id)
        assertEquals("RAUMAENDERUNG", change.type)
        assertEquals("Raumänderung", change.title)
        assertEquals("Beschreibung", change.description)
        assertEquals(false, change.open)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `unbekannter Wochentag wirft Exception`() {
        val dto = LessonDto(
            id = 1, wochentag = "SONNTAG", stundennummer = 1, start = "08:00:00", ende = "08:45:00",
            fachName = "D", raumBezeichnung = "R1", lehrkraftKuerzel = "X", lerngruppeKuerzel = "5a",
            geaendert = false, freistunde = false, offeneAenderung = false, grund = null
        )
        dto.toLesson()
    }
    @Test
    fun `StundenrasterDto berechnet Pausen korrekt zwischen den Stunden`() {
        val dto = com.example.ersteversion.data.dto.StundenrasterDto(
            id = 1, schulbeginn = "08:00:00", stundendauer = 45, stundenProTag = 2,
            pausen = listOf(com.example.ersteversion.data.dto.PauseDto(id = 1, nachStunde = 1, dauerMinuten = 20)),
            zeitslots = listOf(
                com.example.ersteversion.data.dto.ZeitslotDto(id = 1, stundennummer = 1, start = "08:00:00", ende = "08:45:00"),
                com.example.ersteversion.data.dto.ZeitslotDto(id = 2, stundennummer = 2, start = "09:05:00", ende = "09:50:00")
            )
        )

        val raster = dto.toStundenraster()

        assertEquals(3, raster.slots.size)
        val pause = raster.slots.find { it.isPause }
        assertEquals("08:45", pause?.startTime)
        assertEquals("09:05", pause?.endTime)
    }
}