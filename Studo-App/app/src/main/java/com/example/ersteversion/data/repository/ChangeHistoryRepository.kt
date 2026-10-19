package com.example.ersteversion.data.repository

import com.example.ersteversion.data.model.Change
import com.example.ersteversion.data.model.NutzerRolle
import com.example.ersteversion.data.network.ApiService
import com.example.ersteversion.data.network.toChange

// holt die Liste der erledigten Änderungen vom Backend
class ChangeHistoryRepository(
    private val service: ApiService,
    private val role: NutzerRolle
) {
    suspend fun getChanges(): List<Change> {
        val dtos = when (role) {
            NutzerRolle.LEHRKRAFT -> service.getLehrkraftAenderungen()
            NutzerRolle.SCHUELER -> service.getSchuelerAenderungen()
        }
        return dtos.map { it.toChange() }.filter { !it.open }
    }
}