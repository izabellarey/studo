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
class SchuelerServiceCurrentChangesTest {
    @Mock SchuelerRepository schuelerRepository;
    @Mock AccountRepository accountRepository;
    @Mock Generieren generieren;
    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock AccountService accountService;
    @Mock StundenplanRepository stundenplanRepository;
    @Mock AenderungRepository aenderungRepository;
    @InjectMocks SchuelerService service;

    @Test
    void returnsCurrentChangesForStudentsClass() {
        Account account = new Account(); account.setBenutzername("student");
        Lerngruppe group = new Lerngruppe(); group.setId(1L);
        Schueler student = new Schueler(); student.setAccount(account); student.setLerngruppe(group);
        Aenderung change = new Aenderung(); change.setId(5L); change.setAusloeser(event(EreignisTyp.AUSFALL));

        when(accountRepository.findByBenutzername("student")).thenReturn(Optional.of(account));
        when(schuelerRepository.findByAccount(account)).thenReturn(Optional.of(student));
        when(aenderungRepository.findAbgeschlosseneByLerngruppeNeuAndDatumBetween(eq(group), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(change));
        when(generieren.aenderungSchuelerTitelGenerieren(change)).thenReturn("Titel");
        when(generieren.aenderungSchuelerBeschreibungGenerieren(change)).thenReturn("Beschreibung");

        List<AktuelleAenderungDTO> result = service.aktuelleAenderungenAnzeigen("student");

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getId());
        assertEquals(EreignisTyp.AUSFALL, result.get(0).getTyp());
        assertEquals("Titel", result.get(0).getTitel());
        assertEquals("Beschreibung", result.get(0).getBeschreibung());
    }

    @Test
    void unknownAccountThrows() {
        when(accountRepository.findByBenutzername("unknown")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.aktuelleAenderungenAnzeigen("unknown"));
        verifyNoInteractions(schuelerRepository, aenderungRepository);
    }

    private static Ereignis event(EreignisTyp typ) { Ereignis e = new Ereignis(); e.setTyp(typ); return e; }
}
