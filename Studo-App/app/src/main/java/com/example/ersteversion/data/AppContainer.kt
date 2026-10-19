package com.example.ersteversion.data

import android.content.Context
import com.example.ersteversion.data.datastore.SeenChangesDataSource
import com.example.ersteversion.data.datastore.ServerUrlDataSource
import com.example.ersteversion.data.datastore.TokenDiskDataSource
import com.example.ersteversion.data.network.ApiService
import com.example.ersteversion.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

// Zentrale Bereitstellungsstelle der App

class AppContainer(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val appContext = context.applicationContext

    val tokenDiskDataSource = TokenDiskDataSource(appContext)
    val serverUrlDataSource = ServerUrlDataSource(appContext)
    val seenChangesDataSource = SeenChangesDataSource(appContext)

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val token = runBlocking { tokenDiskDataSource.getToken() }
        val request = if (token != null) {
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }

        val response = chain.proceed(request)

        if (response.code == 401 && token != null) {
            runBlocking { tokenDiskDataSource.clearToken() }
            _sessionExpired.tryEmit(Unit)
        }

        response
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private fun buildRetrofit(): Retrofit {
        val baseUrl = runBlocking { serverUrlDataSource.getUrl() }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    val apiService: ApiService
        get() = buildRetrofit().create()

    val authRepository: AuthRepository
        get() = AuthRepository(apiService, tokenDiskDataSource)
}