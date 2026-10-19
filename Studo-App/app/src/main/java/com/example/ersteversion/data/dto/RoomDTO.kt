package com.example.ersteversion.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoomDto(val id: Long, val bezeichnung: String, val art: String, val kapazitaet: Int)