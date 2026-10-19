package com.example.ersteversion.ui.timetable

import com.example.ersteversion.MainDispatcherRule
import com.example.ersteversion.data.model.Day
import com.example.ersteversion.data.model.Lesson
import com.example.ersteversion.data.model.Rasterslot
import com.example.ersteversion.data.model.Room
import com.example.ersteversion.data.model.Stundenraster
import com.example.ersteversion.data.model.Timetable
import com.example.ersteversion.data.repository.TimetableRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class TimetableViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadTimetable befuellt uiState bei Erfolg`() = runTest {
        val repository = mockk<TimetableRepository>()
        val lessons = listOf(
            Lesson(subject = "Mathe", teacher = "Müller", schoolClass = "9a", room = Room("1", "101"), day = Day.MONDAY, rasterIndex = 1)
        )
        coEvery { repository.getTimetable() } returns Timetable(lessons = lessons)
        coEvery { repository.getStundenraster() } returns Stundenraster(
            slots = listOf(Rasterslot(index = 1, startTime = "08:00", endTime = "08:45", isPause = false))
        )

        val viewModel = TimetableViewModel(repository)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.timetable.lessons.size)
        assertEquals(false, viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `loadTimetable setzt Fehlermeldung bei Exception`() = runTest {
        val repository = mockk<TimetableRepository>()
        coEvery { repository.getTimetable() } throws RuntimeException("Netzwerkfehler")
        coEvery { repository.getStundenraster() } returns Stundenraster(slots = emptyList())

        val viewModel = TimetableViewModel(repository)
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assertEquals("Netzwerkfehler", viewModel.uiState.value.errorMessage)
    }
}