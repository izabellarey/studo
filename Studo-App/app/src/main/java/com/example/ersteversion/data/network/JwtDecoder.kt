package com.example.ersteversion.data.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

// liest die Nutzerrolle aus dem Token heraus
object JwtDecoder {

    fun extractRole(token: String): String? {
        return try {
            val payloadPart = token.split(".")[1]
            val decodedBytes = Base64.getUrlDecoder().decode(addPadding(payloadPart))
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            val jsonObject = Json.parseToJsonElement(decodedString).jsonObject
            jsonObject["role"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }

    private fun addPadding(base64: String): String {
        val padding = (4 - base64.length % 4) % 4
        return base64 + "=".repeat(padding)
    }
}