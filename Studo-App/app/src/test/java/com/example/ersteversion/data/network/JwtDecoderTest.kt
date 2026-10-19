package com.example.ersteversion.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JwtDecoderTest {

    @Test
    fun `extractRole liest Rolle aus gueltigem Token`() {
        val token = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJ3ZWIiLCJyb2xlIjoiTEVIUktSQUZUIn0.fake_signature"

        val role = JwtDecoder.extractRole(token)

        assertEquals("LEHRKRAFT", role)
    }

    @Test
    fun `extractRole gibt null zurueck bei kaputtem Token`() {
        val role = JwtDecoder.extractRole("kein.gueltiges.jwt!!!")
        assertNull(role)
    }

    @Test
    fun `extractRole gibt null zurueck bei leerem String`() {
        val role = JwtDecoder.extractRole("")
        assertNull(role)
    }
}