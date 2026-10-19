package de.cau.sp.stundenplanung.security;

import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.model.Rolle;
import de.cau.sp.stundenplanung.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock AccountRepository accountRepository;
    @InjectMocks CustomUserDetailsService service;

    @Test
    void loadsActiveUserWithMappedAuthority() {
        Account account = account("max", "hash", Rolle.LEHRKRAFT, true);
        when(accountRepository.findByBenutzername("max")).thenReturn(Optional.of(account));

        UserDetails details = service.loadUserByUsername("max");

        assertEquals("max", details.getUsername());
        assertEquals("hash", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_LEHRKRAFT")));
    }

    @Test
    void disabledAccountProducesDisabledUser() {
        Account account = account("max", "hash", Rolle.SCHUELER, false);
        when(accountRepository.findByBenutzername("max")).thenReturn(Optional.of(account));

        UserDetails details = service.loadUserByUsername("max");

        assertFalse(details.isEnabled());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SCHUELER")));
    }

    @Test
    void unknownUserThrowsUsernameNotFoundException() {
        when(accountRepository.findByBenutzername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("unknown"));
    }

    private static Account account(String username, String password, Rolle role, boolean active) {
        Account a = new Account();
        a.setBenutzername(username);
        a.setPasswort(password);
        a.setRolle(role);
        a.setAktiv(active);
        return a;
    }
}
