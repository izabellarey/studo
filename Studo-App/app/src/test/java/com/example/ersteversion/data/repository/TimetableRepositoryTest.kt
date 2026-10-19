package com.example.ersteversion.data.repository

import com.example.ersteversion.data.model.Day
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.network.ApiService
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.create
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

class TimetableRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var apiService: ApiService
    private lateinit var repository: TimetableRepository

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
        repository = TimetableRepository(apiService, NutzerRolle.LEHRKRAFT)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `getTimetable liefert gemappte Lessons fuer Lehrkraft`() = runTest {
        val jsonResponse = """
            [
                {
                    "id":7,"wochentag":"MONTAG","stundennummer":1,"start":"08:00:00","ende":"08:45:00",
                    "fachName":"D","raumBezeichnung":"R102","lehrkraftKuerzel":"WEB","lerngruppeKuerzel":"5a",
                    "geaendert":true,"freistunde":false,"offeneAenderung":false,"grund":"RAUMAENDERUNG"
                }
            ]
        """.trimIndent()

        server.enqueue(MockResponse().setBody(jsonResponse).setResponseCode(200))

        val timetable = repository.getTimetable()

        assertEquals(1, timetable.lessons.size)
        val lesson = timetable.lessons.first()
        assertEquals("D", lesson.subject)
        assertEquals(Day.MONDAY, lesson.day)
    }

    @Test
    fun `getStundenraster liefert gemapptes Raster mit Pausen`() = runTest {
        val jsonResponse = """
            {
                "id":1,"schulbeginn":"08:00:00","stundendauer":45,"stundenProTag":2,
                "pausen":[{"id":1,"nachStunde":1,"dauerMinuten":20}],
                "zeitslots":[
                    {"id":1,"stundennummer":1,"start":"08:00:00","ende":"08:45:00"},
                    {"id":2,"stundennummer":2,"start":"09:05:00","ende":"09:50:00"}
                ]
            }
        """.trimIndent()

        server.enqueue(MockResponse().setBody(jsonResponse).setResponseCode(200))

        val raster = repository.getStundenraster()

        // 2 Stunden + 1 Pause = 3 Slots
        assertEquals(3, raster.slots.size)
        assertEquals(true, raster.slots.any { it.isPause })
    }
}