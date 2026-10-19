package com.example.ersteversion.data.repository

import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.network.ApiService
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.create
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

class ChangeHistoryRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var repository: ChangeHistoryRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        apiService = retrofit.create()
        repository = ChangeHistoryRepository(apiService, NutzerRolle.LEHRKRAFT)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getChanges filtert offene Aenderungen heraus`() = runTest {
        val jsonResponse = """
            [
                {"id":1,"typ":"RAUMAENDERUNG","titel":"Erledigt","beschreibung":"...","offen":false},
                {"id":2,"typ":"AUSFALL","titel":"Noch offen","beschreibung":"...","offen":true}
            ]
        """.trimIndent()

        server.enqueue(MockResponse().setBody(jsonResponse).setResponseCode(200))

        val changes = repository.getChanges()

        assertEquals(1, changes.size)
        assertTrue(changes.none { it.open })
        assertEquals("Erledigt", changes.first().title)
    }

    @Test
    fun `getChanges gibt leere Liste zurueck wenn alle offen sind`() = runTest {
        val jsonResponse = """
            [{"id":1,"typ":"AUSFALL","titel":"Offen","beschreibung":"...","offen":true}]
        """.trimIndent()

        server.enqueue(MockResponse().setBody(jsonResponse).setResponseCode(200))

        val changes = repository.getChanges()

        assertTrue(changes.isEmpty())
    }
}