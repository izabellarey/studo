package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.accounts.PasswortZuruecksetzenDTO;
import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.repository.AccountRepository;
import de.cau.sp.stundenplanung.repository.LehrkraftRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LehrkraftPasswordResetTest {
    @Mock LehrkraftRepository lehrkraftRepository;
    @Mock AccountRepository accountRepository;
    @Mock Generieren generieren;
    @Mock de.cau.sp.stundenplanung.repository.LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    @Mock de.cau.sp.stundenplanung.repository.LerngruppeRepository lerngruppeRepository;
    @Mock de.cau.sp.stundenplanung.repository.FachRepository fachRepository;
    @Mock AccountService accountService;
    @Mock de.cau.sp.stundenplanung.repository.UnterrichtsstundeRepository unterrichtsstundeRepository;
    @Mock de.cau.sp.stundenplanung.repository.AenderungRepository aenderungRepository;
    @InjectMocks LehrkraftService service;

    @Test
    void delegatesPasswordResetToAccountService() {
        Account account = new Account(); account.setId(10L);
        Lehrkraft teacher = new Lehrkraft(); teacher.setAccount(account);
        PasswortZuruecksetzenDTO dto = new PasswortZuruecksetzenDTO("newPassword");
        when(lehrkraftRepository.findById(5L)).thenReturn(Optional.of(teacher));

        service.passwortZuruecksetzen(5L, dto);

        verify(accountService).passwortZuruecksetzen(10L, dto);
    }

    @Test
    void unknownTeacherThrows() {
        when(lehrkraftRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.passwortZuruecksetzen(5L, new PasswortZuruecksetzenDTO("new")));
        verifyNoInteractions(accountService);
    }
}
