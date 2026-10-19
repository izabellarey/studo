package com.example.ersteversion.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChangeDto(
    val id: Int,
    val typ: String,
    val titel: String,
    val beschreibung: String,
    val offen: Boolean
)