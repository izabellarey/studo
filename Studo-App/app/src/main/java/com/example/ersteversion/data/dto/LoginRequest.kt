package com.example.ersteversion.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val benutzername: String,
    val passwort: String
)