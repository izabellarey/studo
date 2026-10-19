package com.example.ersteversion.ui.common

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ersteversion.R
import com.example.ersteversion.data.model.Day
import com.example.ersteversion.data.model.Lesson
import com.example.ersteversion.data.model.Rasterslot
import com.example.ersteversion.data.model.Room
import com.example.ersteversion.data.model.Stundenraster
import org.junit.Rule
import org.junit.Test

class TimetableGridUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val raster = Stundenraster(
        slots = listOf(Rasterslot(index = 1, startTime = "08:00", endTime = "08:45", isPause = false))
    )
    private val lesson = Lesson(
        subject = "Mathe", teacher = "Müller", schoolClass = "9a",
        room = Room("1", "101"), day = Day.MONDAY, rasterIndex = 1
    )

    @Test
    fun ausfall_zeigtLabel() {
        val ausfallLesson = lesson.copy(changed = true, changeReason = "AUSFALL")
        composeTestRule.setContent {
            TimetableGrid(
                stundenraster = raster,
                lessonAt = { _, day -> if (day == Day.MONDAY) ausfallLesson else null },
                subtitleLines = { _, _ -> listOf("Müller") }
            )
        }
        val erwarteterText = context.getString(R.string.ausfall)
        composeTestRule.onNodeWithText(erwarteterText).assertExists()
    }

    @Test
    fun aenderung_zeigtLabel_aberNichtAusfallLabel() {
        val aenderungLesson = lesson.copy(changed = true, changeReason = "RAUMAENDERUNG")
        composeTestRule.setContent {
            TimetableGrid(
                stundenraster = raster,
                lessonAt = { _, day -> if (day == Day.MONDAY) aenderungLesson else null },
                subtitleLines = { _, _ -> listOf("Müller") }
            )
        }
        val aenderungText = context.getString(R.string.nderung)
        val ausfallText = context.getString(R.string.ausfall)
        composeTestRule.onNodeWithText(aenderungText).assertExists()
        composeTestRule.onNodeWithText(ausfallText).assertDoesNotExist()
    }
}