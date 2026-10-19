package com.example.ersteversion.ui.changehistory

import com.example.ersteversion.MainDispatcherRule
import com.example.ersteversion.data.model.Change
import com.example.ersteversion.data.repository.ChangeHistoryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ChangeHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `loadChanges befuellt uiState mit Aenderungen`() = runTest {
        val repository = mockk<ChangeHistoryRepository>()
        coEvery { repository.getChanges() } returns listOf(
            Change(1, "AUSFALL", "Sport fällt aus", "Beschreibung", false)
        )

        val viewModel = ChangeHistoryViewModel(repository)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.changes.size)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadChanges setzt Fehlermeldung bei Exception`() = runTest {
        val repository = mockk<ChangeHistoryRepository>()
        coEvery { repository.getChanges() } throws RuntimeException("Serverfehler")

        val viewModel = ChangeHistoryViewModel(repository)
        advanceUntilIdle()

        assertEquals("Serverfehler", viewModel.uiState.value.errorMessage)
    }
}