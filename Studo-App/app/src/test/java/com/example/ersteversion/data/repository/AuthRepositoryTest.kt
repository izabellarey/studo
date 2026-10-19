package com.example.ersteversion.data.repository

import com.example.ersteversion.data.datastore.TokenDiskDataSource
import com.example.ersteversion.data.dto.LoginResponseDto
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.network.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {

    private lateinit var service: ApiService
    private lateinit var tokenDiskDataSource: TokenDiskDataSource
    private lateinit var repository: AuthRepository

    // Payload: {"sub":"web","role":"LEHRKRAFT"}
    private val fakeToken = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ3ZWIiLCJyb2xlIjoiTEVIUktSQUZUIn0.sig"

    @Before
    fun setUp() {
        service = mockk()
        tokenDiskDataSource = mockk(relaxed = true)
        repository = AuthRepository(service, tokenDiskDataSource)
    }

    @Test
    fun `login speichert Token und gibt Rolle zurueck`() = runTest {
        coEvery { service.login(any()) } returns LoginResponseDto(token = fakeToken, name = "Weber", vorname = "Thomas")

        val rolle = repository.login("web", "start1234")

        assertEquals(NutzerRolle.LEHRKRAFT, rolle)
        coVerify { tokenDiskDataSource.saveToken(fakeToken) }
    }

    @Test
    fun `getStoredRole gibt null zurueck wenn kein Token gespeichert`() = runTest {
        coEvery { tokenDiskDataSource.getToken() } returns null

        val rolle = repository.getStoredRole()

        assertNull(rolle)
    }

    @Test
    fun `getStoredRole liest Rolle aus gespeichertem Token`() = runTest {
        coEvery { tokenDiskDataSource.getToken() } returns fakeToken

        val rolle = repository.getStoredRole()

        assertEquals(NutzerRolle.LEHRKRAFT, rolle)
    }

    @Test
    fun `logout loescht den Token`() = runTest {
        repository.logout()
        coVerify { tokenDiskDataSource.clearToken() }
    }
}