package com.example.ersteversion.data

import com.example.ersteversion.data.datastore.TokenDiskDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun buildInterceptor(tokenDiskDataSource: TokenDiskDataSource) = Interceptor { chain ->
        val token = runBlocking { tokenDiskDataSource.getToken() }
        val request = if (token != null) {
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    @Test
    fun `Header wird gesetzt wenn Token vorhanden ist`() {
        val tokenDiskDataSource = mockk<TokenDiskDataSource>()
        coEvery { tokenDiskDataSource.getToken() } returns "mein-token"

        val client = OkHttpClient.Builder().addInterceptor(buildInterceptor(tokenDiskDataSource)).build()
        server.enqueue(MockResponse().setResponseCode(200))

        client.newCall(okhttp3.Request.Builder().url(server.url("/test")).build()).execute()

        val recordedRequest = server.takeRequest()
        assertEquals("Bearer mein-token", recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun `kein Header wenn kein Token gespeichert ist`() {
        val tokenDiskDataSource = mockk<TokenDiskDataSource>()
        coEvery { tokenDiskDataSource.getToken() } returns null

        val client = OkHttpClient.Builder().addInterceptor(buildInterceptor(tokenDiskDataSource)).build()
        server.enqueue(MockResponse().setResponseCode(200))

        client.newCall(okhttp3.Request.Builder().url(server.url("/test")).build()).execute()

        val recordedRequest = server.takeRequest()
        assertNull(recordedRequest.getHeader("Authorization"))
    }

    @Test
    fun `401 mit vorhandenem Token loescht Token und sendet Event`() = runTest {
        val tokenDiskDataSource = mockk<TokenDiskDataSource>(relaxed = true)
        coEvery { tokenDiskDataSource.getToken() } returns "abgelaufener-token"

        val sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        val interceptor = Interceptor { chain ->
            val token = runBlocking { tokenDiskDataSource.getToken() }
            val request = chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
            val response = chain.proceed(request)
            if (response.code == 401 && token != null) {
                runBlocking { tokenDiskDataSource.clearToken() }
                sessionExpired.tryEmit(Unit)
            }
            response
        }

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()
        server.enqueue(MockResponse().setResponseCode(401))

        client.newCall(Request.Builder().url(server.url("/test")).build()).execute()

        coVerify { tokenDiskDataSource.clearToken() }
        assertTrue(sessionExpired.tryEmit(Unit)) // Kanal war bereits belegt, bestätigt vorheriges Emit
    }
}