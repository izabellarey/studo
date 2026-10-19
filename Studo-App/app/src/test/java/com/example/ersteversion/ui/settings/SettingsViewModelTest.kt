package com.example.ersteversion.ui.settings

import com.example.ersteversion.MainDispatcherRule
import com.example.ersteversion.data.datastore.ServerUrlDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `ungueltiges Format wird nicht gespeichert`() = runTest {
        val dataSource = mockk<ServerUrlDataSource>(relaxed = true)
        coEvery { dataSource.getUrl() } returns "http://10.0.2.2:8080/"

        val viewModel = SettingsViewModel(dataSource)
        advanceUntilIdle()

        viewModel.onUrlChange("keine-url")
        viewModel.saveUrl()
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.saved)
        coVerify(exactly = 0) { dataSource.saveUrl(any()) }
    }

    @Test
    fun `initialer uiState laedt gespeicherte URL`() = runTest {
        val dataSource = mockk<ServerUrlDataSource>(relaxed = true)
        coEvery { dataSource.getUrl() } returns "http://10.0.2.2:8080/"

        val viewModel = SettingsViewModel(dataSource)
        advanceUntilIdle()

        assertEquals("http://10.0.2.2:8080/", viewModel.uiState.value.serverUrl)
    }
}