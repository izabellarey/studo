package com.example.ersteversion.ui.timetable

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ersteversion.data.model.Day
import com.example.ersteversion.data.model.Lesson
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.model.Rasterslot
import com.example.ersteversion.data.model.Room
import com.example.ersteversion.data.model.Stundenraster
import com.example.ersteversion.data.model.Timetable
import com.example.ersteversion.ui.common.TimetableGrid

@Composable
fun TimetableContent(uiState: TimetableUiState, rolle: NutzerRolle) {
    TimetableGrid(
        stundenraster = uiState.stundenraster,
        lessonAt = { slot, day ->
            uiState.timetable.lessons.find { it.day == day && it.rasterIndex == slot.index }
        },
        subtitleLines = { lesson, expanded ->
            val secondLine = when (rolle) {
                NutzerRolle.SCHUELER -> lesson.teacher
                NutzerRolle.LEHRKRAFT -> lesson.schoolClass
            }
            if (expanded) listOf(secondLine, lesson.room.name) else listOf(secondLine)
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true, heightDp = 800, name = "Timetable Tagmodus", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, heightDp = 800, name = "Timetable Nachtmodus", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TimetableContentPreview() {
    val fakeState = TimetableUiState(
        timetable = Timetable(
            lessons = listOf(
                Lesson(
                    subject = "Mathe",
                    teacher = "Frau Müller",
                    schoolClass = "9a",
                    room = Room("1", "101"),
                    day = Day.MONDAY,
                    rasterIndex = 1
                ),
                Lesson(
                    subject = "Englisch",
                    teacher = "Herr Schmidt",
                    schoolClass = "9a",
                    room = Room("2", "102"),
                    day = Day.MONDAY,
                    rasterIndex = 2
                ),
                Lesson(
                    subject = "Sport",
                    teacher = "Herr Klein",
                    schoolClass = "9a",
                    room = Room("3", "Halle"),
                    day = Day.TUESDAY,
                    rasterIndex = 3
                )
            )
        ),
        stundenraster = Stundenraster(
            slots = listOf(
                Rasterslot(index = 1, startTime = "08:00", endTime = "08:45", isPause = false),
                Rasterslot(index = 2, startTime = "08:50", endTime = "09:35", isPause = false),
                Rasterslot(index = 0, startTime = "09:35", endTime = "09:50", isPause = true),
                Rasterslot(index = 3, startTime = "09:50", endTime = "10:35", isPause = false)
            )
        )
    )
    TimetableContent(uiState = fakeState, rolle = NutzerRolle.LEHRKRAFT)
}

@Preview(showBackground = true, heightDp = 800, name = "Mit Ausfall")
@Composable
private fun TimetableContentAusfallPreview() {
    val fakeState = TimetableUiState(
        timetable = Timetable(
            lessons = listOf(
                Lesson(
                    subject = "Mathe",
                    teacher = "Frau Müller",
                    schoolClass = "9a",
                    room = Room("1", "101"),
                    day = Day.MONDAY,
                    rasterIndex = 1
                ),
                Lesson(
                    subject = "Sport",
                    teacher = "Herr Klein",
                    schoolClass = "9a",
                    room = Room("3", "Halle"),
                    day = Day.MONDAY,
                    rasterIndex = 2,
                    changed = true,
                    changeReason = "AUSFALL"
                ),
                Lesson(
                    subject = "Englisch",
                    teacher = "Herr Schmidt",
                    schoolClass = "9a",
                    room = Room("2", "102"),
                    day = Day.TUESDAY,
                    rasterIndex = 1
                )
            )
        ),
        stundenraster = Stundenraster(
            slots = listOf(
                Rasterslot(index = 1, startTime = "08:00", endTime = "08:45", isPause = false),
                Rasterslot(index = 2, startTime = "08:50", endTime = "09:35", isPause = false),
                Rasterslot(index = 0, startTime = "09:35", endTime = "09:50", isPause = true),
                Rasterslot(index = 3, startTime = "09:50", endTime = "10:35", isPause = false)
            )
        )
    )
    TimetableContent(uiState = fakeState, rolle = NutzerRolle.LEHRKRAFT)
}

@Preview(showBackground = true, heightDp = 800, name = "Mit Änderung")
@Composable
private fun TimetableContentAenderungPreview() {
    val fakeState = TimetableUiState(
        timetable = Timetable(
            lessons = listOf(
                Lesson(
                    subject = "Deutsch",
                    teacher = "Frau Weber",
                    schoolClass = "9a",
                    room = Room("1", "R102"),
                    day = Day.MONDAY,
                    rasterIndex = 1,
                    changed = true,
                    changeReason = "RAUMAENDERUNG"
                ),
                Lesson(
                    subject = "Physik",
                    teacher = "Frau Müller",
                    schoolClass = "9a",
                    room = Room("2", "102"),
                    day = Day.WEDNESDAY,
                    rasterIndex = 2
                )
            )
        ),
        stundenraster = Stundenraster(
            slots = listOf(
                Rasterslot(index = 1, startTime = "08:00", endTime = "08:45", isPause = false),
                Rasterslot(index = 2, startTime = "08:50", endTime = "09:35", isPause = false),
                Rasterslot(index = 0, startTime = "09:35", endTime = "09:50", isPause = true),
                Rasterslot(index = 3, startTime = "09:50", endTime = "10:35", isPause = false)
            )
        )
    )
    TimetableContent(uiState = fakeState, rolle = NutzerRolle.LEHRKRAFT)
}