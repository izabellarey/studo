package com.example.ersteversion.ui.roomplan

import com.example.ersteversion.MainDispatcherRule
import com.example.ersteversion.data.model.Room
import com.example.ersteversion.data.model.RoomCategory
import com.example.ersteversion.data.model.RoomPlan
import com.example.ersteversion.data.model.Stundenraster
import com.example.ersteversion.data.repository.RoomPlanRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class RoomPlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadCategories befuellt uiState`() = runTest {
        val repository = mockk<RoomPlanRepository>()
        coEvery { repository.getRoomCategories() } returns listOf(
            RoomCategory(id = "KLASSENRAUM", name = "Klassenräume", rooms = listOf(Room("1", "101")))
        )
        coEvery { repository.getStundenraster() } returns Stundenraster(slots = emptyList())

        val viewModel = RoomPlanViewModel(repository)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.categories.size)
        assertNull(viewModel.uiState.value.selectedRoom)
    }

    @Test
    fun `selectRoom laedt Raumplan und setzt selectedRoom`() = runTest {
        val repository = mockk<RoomPlanRepository>()
        val room = Room("1", "101")
        coEvery { repository.getRoomCategories() } returns emptyList()
        coEvery { repository.getStundenraster() } returns Stundenraster(slots = emptyList())
        coEvery { repository.getRoomPlan(room) } returns RoomPlan(room = room, lessons = emptyList())

        val viewModel = RoomPlanViewModel(repository)
        advanceUntilIdle()

        viewModel.selectRoom(room)
        advanceUntilIdle()

        assertEquals(room, viewModel.uiState.value.selectedRoom)
        assertNotNull(viewModel.uiState.value.selectedRoomPlan)
    }

    @Test
    fun `backToOverview setzt selectedRoom zurueck auf null`() = runTest {
        val repository = mockk<RoomPlanRepository>()
        val room = Room("1", "101")
        coEvery { repository.getRoomCategories() } returns emptyList()
        coEvery { repository.getStundenraster() } returns Stundenraster(slots = emptyList())
        coEvery { repository.getRoomPlan(room) } returns RoomPlan(room = room, lessons = emptyList())

        val viewModel = RoomPlanViewModel(repository)
        advanceUntilIdle()
        viewModel.selectRoom(room)
        advanceUntilIdle()

        viewModel.backToOverview()

        assertNull(viewModel.uiState.value.selectedRoom)
        assertNull(viewModel.uiState.value.selectedRoomPlan)
    }
}