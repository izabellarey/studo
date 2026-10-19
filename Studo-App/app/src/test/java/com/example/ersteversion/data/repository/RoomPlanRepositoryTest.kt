package com.example.ersteversion.data.repository

import com.example.ersteversion.data.model.Room
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

class RoomPlanRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var repository: RoomPlanRepository

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
        repository = RoomPlanRepository(apiService)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getRoomCategories gruppiert Raeume nach Art`() = runTest {
        val jsonResponse = """
            [
                {"id":1,"bezeichnung":"R101","art":"KLASSENRAUM","kapazitaet":32},
                {"id":2,"bezeichnung":"R102","art":"KLASSENRAUM","kapazitaet":32},
                {"id":13,"bezeichnung":"BIO1","art":"BIOLOGIERAUM","kapazitaet":30}
            ]
        """.trimIndent()

        server.enqueue(MockResponse().setBody(jsonResponse).setResponseCode(200))

        val categories = repository.getRoomCategories()

        assertEquals(2, categories.size)
        val klassenraeume = categories.find { it.id == "KLASSENRAUM" }
        assertEquals(2, klassenraeume?.rooms?.size)
    }

    @Test
    fun `getRoomPlan liefert Lessons mit uebergebenem Raum`() = runTest {
        val jsonResponse = """
            [
                {"wochentag":"DIENSTAG","stundennummer":1,"start":"08:00:00","ende":"08:45:00","fachName":"E","lehrkraftKuerzel":"KLE","lerngruppeKuerzel":"5a","geaendert":false}
            ]
        """.trimIndent()

        server.enqueue(MockResponse().setBody(jsonResponse).setResponseCode(200))

        val room = Room(id = "1", name = "R101")
        val roomPlan = repository.getRoomPlan(room)

        assertEquals(room, roomPlan.room)
        assertEquals(1, roomPlan.lessons.size)
        assertTrue(roomPlan.lessons.first().room == room)
    }
}