package com.example.ersteversion.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val token: String,
    val name: String,
    val vorname: String
)