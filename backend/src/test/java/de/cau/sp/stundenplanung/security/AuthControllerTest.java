package de.cau.sp.stundenplanung.security;

import de.cau.sp.stundenplanung.dto.security.LoginRequest;
import de.cau.sp.stundenplanung.dto.security.LoginResponse;
import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.model.Rolle;
import de.cau.sp.stundenplanung.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock AuthenticationManager authenticationManager;
    @Mock JwtService jwtService;
    @Mock AccountRepository accountRepository;
    @InjectMocks AuthController controller;

    @Test
    void successfulLoginAuthenticatesLoadsAccountAndReturnsTokenAndNames() {
        LoginRequest request = new LoginRequest("mmustermann", "secret");
        Account account = new Account();
        account.setBenutzername("mmustermann");
        account.setName("Mustermann");
        account.setVorname("Max");
        account.setRolle(Rolle.LEHRKRAFT);
        when(accountRepository.findByBenutzername("mmustermann")).thenReturn(Optional.of(account));
        when(jwtService.generateToken("mmustermann", "LEHRKRAFT")).thenReturn("jwt");

        ResponseEntity<LoginResponse> response = controller.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("jwt", response.getBody().token());
        assertEquals("Mustermann", response.getBody().name());
        assertEquals("Max", response.getBody().vorname());
        assertEquals(Rolle.LEHRKRAFT.name(), response.getBody().rolle());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken("mmustermann", "LEHRKRAFT");
    }

    @Test
    void failedAuthenticationPreventsTokenGeneration() {
        LoginRequest request = new LoginRequest("mmustermann", "wrong");
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("bad credentials"));

        assertThrows(RuntimeException.class, () -> controller.login(request));
        verifyNoInteractions(accountRepository, jwtService);
    }
}
