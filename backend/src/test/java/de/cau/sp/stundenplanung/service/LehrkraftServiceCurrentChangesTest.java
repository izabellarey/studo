package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.dashboard.AktuelleAenderungDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LehrkraftServiceCurrentChangesTest {
    @Mock LehrkraftRepository lehrkraftRepository;
    @Mock AccountRepository accountRepository;
    @Mock Generieren generieren;
    @Mock LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock FachRepository fachRepository;
    @Mock AccountService accountService;
    @Mock UnterrichtsstundeRepository unterrichtsstundeRepository;
    @Mock AenderungRepository aenderungRepository;
    @InjectMocks LehrkraftService service;

    @Test
    void returnsCurrentChangesForTeacher() {
        Account account = new Account(); account.setBenutzername("teacher");
        Lehrkraft teacher = new Lehrkraft(); teacher.setId(9L); teacher.setAccount(account);
        Aenderung change = new Aenderung(); change.setId(5L); change.setAusloeser(event(EreignisTyp.ZUSATZSTUNDE));

        when(accountRepository.findByBenutzername("teacher")).thenReturn(Optional.of(account));
        when(lehrkraftRepository.findByAccount(account)).thenReturn(Optional.of(teacher));
        when(aenderungRepository.findAbgeschlosseneByLehrkraftNeuAndDatumBetween(eq(teacher), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(change));
        when(generieren.aenderungLehrkraftTitelGenerieren(change)).thenReturn("Titel");
        when(generieren.aenderungLehrkraftBeschreibungGenerieren(change)).thenReturn("Beschreibung");

        List<AktuelleAenderungDTO> result = service.aktuelleAenderungenAnzeigen("teacher");

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals(EreignisTyp.ZUSATZSTUNDE, result.get(0).getTyp());
        assertEquals("Titel", result.get(0).getTitel());
        assertEquals("Beschreibung", result.get(0).getBeschreibung());
    }

    @Test
    void unknownTeacherThrows() {
        when(accountRepository.findByBenutzername("unknown")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.aktuelleAenderungenAnzeigen("unknown"));
        verifyNoInteractions(lehrkraftRepository, aenderungRepository);
    }

    private static Ereignis event(EreignisTyp typ) { Ereignis e = new Ereignis(); e.setTyp(typ); return e; }
}
