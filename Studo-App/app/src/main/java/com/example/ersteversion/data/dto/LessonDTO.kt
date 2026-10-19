package com.example.ersteversion.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LessonDto(
    val wochentag: String,
    val stundennummer: Int,
    val start: String,
    val ende: String,
    val fachName: String,
    val raumBezeichnung: String,
    val lehrkraftKuerzel: String,
    val lerngruppeKuerzel: String,
    val geaendert: Boolean,
    val id: Int,
    val freistunde: Boolean,
    val offeneAenderung: Boolean,
    val grund: String? = null
)

@Serializable
data class RoomLessonDto(
    val wochentag: String,
    val stundennummer: Int,
    val start: String,
    val ende: String,
    val fachName: String,
    val lehrkraftKuerzel: String,
    val lerngruppeKuerzel: String,
    val geaendert: Boolean
)