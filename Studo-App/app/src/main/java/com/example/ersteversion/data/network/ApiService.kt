package com.example.ersteversion.data.network

import com.example.ersteversion.data.dto.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// bestimmt, welche HTTP-Endpunkte die App aufrufen kann
interface ApiService {

    @GET("api/lehrkraftansicht/stundenplan")
    suspend fun getLehrkraftStundenplan(): List<LessonDto>

    @GET("api/lehrkraftansicht/raeume")
    suspend fun getRaeume(): List<RoomDto>

    @GET("api/lehrkraftansicht/{raumId}")
    suspend fun getRaumplan(@Path("raumId") raumId: Long): List<RoomLessonDto>

    @GET("api/schueleransicht/stundenplan")
    suspend fun getSchuelerStundenplan(): List<LessonDto>

    @GET("api/stammdaten/stundenraster")
    suspend fun getStundenraster(): StundenrasterDto

    @GET("api/lehrkraftansicht/aenderungen")
    suspend fun getLehrkraftAenderungen(): List<ChangeDto>

    @GET("api/schueleransicht/aenderungen")
    suspend fun getSchuelerAenderungen(): List<ChangeDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponseDto
}