package com.example.ersteversion.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StundenrasterDto(
    val id: Int,
    val schulbeginn: String,
    val stundendauer: Int,
    val stundenProTag: Int,
    val pausen: List<PauseDto>,
    val zeitslots: List<ZeitslotDto>
)

@Serializable
data class PauseDto(
    val id: Int,
    val nachStunde: Int,
    val dauerMinuten: Int
)

@Serializable
data class ZeitslotDto(
    val id: Int,
    val stundennummer: Int,
    val start: String,
    val ende: String
)