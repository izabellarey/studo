package com.example.ersteversion.data.repository

import com.example.ersteversion.data.datastore.TokenDiskDataSource
import com.example.ersteversion.data.dto.LoginRequest
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.network.ApiService
import com.example.ersteversion.data.network.JwtDecoder

class AuthRepository(
    private val service: ApiService,
    private val tokenDiskDataSource: TokenDiskDataSource

) {
    suspend fun login(username: String, password: String): NutzerRolle {
        val response = service.login(LoginRequest(benutzername = username, passwort = password))
        tokenDiskDataSource.saveToken(response.token)
        return roleFromToken(response.token)
    } // kriegt einen Token, speichert diesen und liest die Rolle heraus

    suspend fun logout() {
        tokenDiskDataSource.clearToken()
    } // löscht den gespeicherten Token

    suspend fun getStoredRole(): NutzerRolle? {
        val token = tokenDiskDataSource.getToken() ?: return null
        return try {
            roleFromToken(token)
        } catch (e: Exception) {
            null
        }
    } // prüft, ob ein Token gespeichert ist

    private fun roleFromToken(token: String): NutzerRolle {
        val roleString = JwtDecoder.extractRole(token)
            ?: throw IllegalStateException("Rolle konnte nicht aus dem Token gelesen werden.")

        return when (roleString) {
            "LEHRKRAFT" -> NutzerRolle.LEHRKRAFT
            "SCHUELER" -> NutzerRolle.SCHUELER
            else -> throw IllegalStateException("Unbekannte Rolle: $roleString")
        } // liest die Rolle eines Tokens
    }
}