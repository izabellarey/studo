package com.example.ersteversion.data.repository

import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.model.Stundenraster
import com.example.ersteversion.data.model.Timetable
import com.example.ersteversion.data.network.ApiService
import com.example.ersteversion.data.network.toLesson
import com.example.ersteversion.data.network.toStundenraster

class TimetableRepository(
    private val service: ApiService,
    private val rolle: NutzerRolle
) {
    suspend fun getTimetable(): Timetable {
        val dtos = when (rolle) {
            NutzerRolle.LEHRKRAFT -> service.getLehrkraftStundenplan()
            NutzerRolle.SCHUELER -> service.getSchuelerStundenplan()
        }
        return Timetable(lessons = dtos.map { it.toLesson() })
    } // Stellt für jede Person den eigenen Stundenplan bereit

    suspend fun getStundenraster(): Stundenraster {
        return service.getStundenraster().toStundenraster()
    } // Holt das Stundenraster
}