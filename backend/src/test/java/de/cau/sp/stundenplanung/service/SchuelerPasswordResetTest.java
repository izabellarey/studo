package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.accounts.PasswortZuruecksetzenDTO;
import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.model.Schueler;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchuelerPasswordResetTest {
    @Mock SchuelerRepository schuelerRepository;
    @Mock AccountRepository accountRepository;
    @Mock Generieren generieren;
    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock AccountService accountService;
    @Mock StundenplanRepository stundenplanRepository;
    @Mock AenderungRepository aenderungRepository;
    @InjectMocks SchuelerService service;

    @Test
    void delegatesPasswordResetToAccountService() {
        Account account = new Account(); account.setId(10L);
        Schueler student = new Schueler(); student.setAccount(account);
        PasswortZuruecksetzenDTO dto = new PasswortZuruecksetzenDTO("newPassword");
        when(schuelerRepository.findById(5L)).thenReturn(Optional.of(student));

        service.passwortZuruecksetzen(5L, dto);

        verify(accountService).passwortZuruecksetzen(10L, dto);
    }

    @Test
    void unknownStudentThrows() {
        when(schuelerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.passwortZuruecksetzen(5L, new PasswortZuruecksetzenDTO("new")));
        verifyNoInteractions(accountService);
    }
}
