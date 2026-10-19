package com.example.ersteversion.data.network

import com.example.ersteversion.data.dto.*
import com.example.ersteversion.data.model.*
import java.time.LocalTime

// Verknüpft DTOs mit Model-Klassen

private fun String.toDay(): Day = when (this) {
    "MONTAG" -> Day.MONDAY
    "DIENSTAG" -> Day.TUESDAY
    "MITTWOCH" -> Day.WEDNESDAY
    "DONNERSTAG" -> Day.THURSDAY
    "FREITAG" -> Day.FRIDAY
    else -> throw IllegalArgumentException("Unbekannter Wochentag: $this")
}

private fun String.shortTime(): String = take(5)
// "08:00:00" -> "08:00"

fun LessonDto.toLesson(): Lesson = Lesson(
    id = id,
    subject = fachName,
    teacher = lehrkraftKuerzel,
    schoolClass = lerngruppeKuerzel,
    room = Room(id = raumBezeichnung, name = raumBezeichnung),
    day = wochentag.toDay(),
    rasterIndex = stundennummer,
    changed = geaendert,
    changeReason = grund
)

fun RoomLessonDto.toLesson(room: Room): Lesson = Lesson(
    subject = fachName,
    teacher = lehrkraftKuerzel,
    schoolClass = lerngruppeKuerzel,
    room = room,
    day = wochentag.toDay(),
    rasterIndex = stundennummer,
    changed = geaendert
)

fun RoomDto.toRoom(): Room = Room(id = id.toString(), name = bezeichnung)

fun StundenrasterDto.toStundenraster(): Stundenraster {
    val slots = mutableListOf<Rasterslot>()

    zeitslots.forEach { slot ->
        slots.add(
            Rasterslot(
                index = slot.stundennummer,
                startTime = slot.start.shortTime(),
                endTime = slot.ende.shortTime(),
                isPause = false
            )
        )

        val pause = pausen.find { it.nachStunde == slot.stundennummer }
        if (pause != null) {
            val pauseStart = LocalTime.parse(slot.ende)
            val pauseEnd = pauseStart.plusMinutes(pause.dauerMinuten.toLong())
            slots.add(
                Rasterslot(
                    index = 0,
                    startTime = pauseStart.toString().shortTime(),
                    endTime = pauseEnd.toString().shortTime(),
                    isPause = true
                )
            )
        }
    }
    return Stundenraster(slots = slots)
}

fun ChangeDto.toChange(): Change = Change(
    id = id,
    type = typ,
    title = titel,
    description = beschreibung,
    open = offen
)