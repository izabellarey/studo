package com.example.ersteversion.ui.login

import com.example.ersteversion.MainDispatcherRule
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Test
    fun `erfolgreicher Login setzt keine Fehlermeldung und ruft onSuccess mit Rolle auf`() = runTest {
        authRepository = mockk()
        coEvery { authRepository.login("web", "start1234") } returns NutzerRolle.LEHRKRAFT

        viewModel = LoginViewModel(authRepository)
        viewModel.onUsernameChange("web")
        viewModel.onPasswordChange("start1234")

        var erhalteneRolle: NutzerRolle? = null
        viewModel.login { rolle -> erhalteneRolle = rolle }
        advanceUntilIdle()

        assertEquals(NutzerRolle.LEHRKRAFT, erhalteneRolle)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `fehlgeschlagener Login setzt Fehlermeldung`() = runTest {
        authRepository = mockk()
        coEvery { authRepository.login(any(), any()) } throws IllegalStateException("Unbekannte Rolle: X")

        viewModel = LoginViewModel(authRepository)
        viewModel.onUsernameChange("web")
        viewModel.onPasswordChange("falsch")

        viewModel.login { }
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isLoading)
        assert(viewModel.uiState.value.errorMessage != null)
    }
}