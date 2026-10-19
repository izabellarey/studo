package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.accounts.PasswortZuruecksetzenDTO;
import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountPasswordResetTest {
    @Mock AccountRepository accountRepository;
    @Mock Generieren generieren;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AccountService service;

    @Test
    void passwordResetEncodesNewPasswordAndSavesAccount() {
        Account account = new Account();
        account.setId(7L);
        account.setPasswort("oldHash");
        PasswortZuruecksetzenDTO dto = new PasswortZuruecksetzenDTO("newPassword");
        when(accountRepository.findById(7L)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode("newPassword")).thenReturn("newHash");

        service.passwortZuruecksetzen(7L, dto);

        assertEquals("newHash", account.getPasswort());
        verify(passwordEncoder).encode("newPassword");
        verify(accountRepository).save(account);
    }

    @Test
    void passwordResetForUnknownAccountThrows() {
        when(accountRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.passwortZuruecksetzen(7L, new PasswortZuruecksetzenDTO("new")));
        verifyNoInteractions(passwordEncoder);
        verify(accountRepository, never()).save(any());
    }
}
