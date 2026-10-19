package de.cau.sp.stundenplanung.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integrationstests fuer die Autorisierungsregeln aus der SecurityConfig.
 *
 * Die Tests pruefen insbesondere die Rollenberechtigungen fuer
 * GET /api/raeume sowie den authentifizierten Zugriff auf
 * GET /api/stammdaten/stundenraster.
 */
@SpringBootTest(properties = "app.security.jwt-secret=01234567890123456789012345678901")
@AutoConfigureMockMvc
class SecurityConfigIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtService jwtService;

    @Test
    void protectedRoomEndpointRejectsRequestWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/raeume"))
                .andExpect(status().isForbidden());
    }

    @Test
    void teacherMayAccessRooms() throws Exception {
        String token = jwtService.generateToken("teacher", "LEHRKRAFT");

        mockMvc.perform(get("/api/raeume")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void studentMayNotAccessRooms() throws Exception {
        String token = jwtService.generateToken("student", "SCHUELER");

        mockMvc.perform(get("/api/raeume")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void plannerMayAccessRooms() throws Exception {
        String token = jwtService.generateToken("planner", "PLANUNG");

        mockMvc.perform(get("/api/raeume")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void stundenrasterRejectsRequestWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/stammdaten/stundenraster"))
                .andExpect(status().isForbidden());
    }

}