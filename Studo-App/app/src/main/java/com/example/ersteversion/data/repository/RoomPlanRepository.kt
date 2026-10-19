package com.example.ersteversion.data.repository

import com.example.ersteversion.data.model.*
import com.example.ersteversion.data.network.ApiService
import com.example.ersteversion.data.network.toLesson
import com.example.ersteversion.data.network.toRoom
import com.example.ersteversion.data.network.toStundenraster

class RoomPlanRepository(private val service: ApiService) {

    // Übernimmt die komplette Datenbeschaffung für den Raumplan-Screen

    suspend fun getRoomCategories(): List<RoomCategory> {
        val rooms = service.getRaeume().map { it.toRoom() to it.art }
        return rooms.groupBy { it.second }
            .map { (art, entries) -> RoomCategory(id = art, name = art.toDisplayName(), rooms = entries.map { it.first }) }
    } // holt alle Raumarten aus der Liste des Backend und sortiert danach die Räume ein

    suspend fun getRoomPlan(room: Room): RoomPlan {
        val dtos = service.getRaumplan(room.id.toLong())
        return RoomPlan(room = room, lessons = dtos.map { it.toLesson(room) })
    } // baut Pläne für jeden Raum

    suspend fun getStundenraster(): Stundenraster {
        return service.getStundenraster().toStundenraster()
    } // holt das Stundenraster
}

private fun String.toDisplayName(): String = when (this) {
    "KLASSENRAUM" -> "Klassenräume"
    "BIOLOGIERAUM" -> "Biologieräume"
    "CHEMIERAUM" -> "Chemieräume"
    "PHYSIKRAUM" -> "Physikräume"
    "KUNSTRAUM" -> "Kunsträume"
    "MUSIKRAUM" -> "Musikräume"
    "INFORMATIKRAUM" -> "Informatikräume"
    "SPORTHALLE" -> "Sporthallen"
    "AULA" -> "Aula"
    else -> this
}