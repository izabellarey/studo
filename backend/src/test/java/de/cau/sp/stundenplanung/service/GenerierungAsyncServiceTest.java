package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.model.Generierungslauf;
import de.cau.sp.stundenplanung.model.GenerierungsStatus;
import de.cau.sp.stundenplanung.repository.GenerierungslaufRepository;
import de.cau.sp.stundenplanung.solver.StundenplanGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerierungAsyncServiceTest {

    @Mock GenerierungslaufRepository generierungslaufRepository;
    @Mock StundenplanGenerator stundenplanGenerator;
    @InjectMocks GenerierungAsyncService service;

    @Test
    void generierungAsyncStartenSetztStatusAufFertigBeiErfolg() {
        Generierungslauf lauf = new Generierungslauf();
        lauf.setId(1L);
        lauf.setStatus(GenerierungsStatus.LAEUFT);

        when(generierungslaufRepository.findById(1L)).thenReturn(Optional.of(lauf));

        service.generierungAsyncStarten(1L);

        assertEquals(GenerierungsStatus.FERTIG, lauf.getStatus());
        assertNotNull(lauf.getFertigAm());
        verify(stundenplanGenerator).generieren(1L);
    }

    @Test
    void generierungAsyncStartenSetztStatusAufFehlerWennSolverWirft() {
        Generierungslauf lauf = new Generierungslauf();
        lauf.setId(1L);
        lauf.setStatus(GenerierungsStatus.LAEUFT);

        doThrow(new RuntimeException("Keine Lehrkraft unterrichtet ..."))
            .when(stundenplanGenerator).generieren(1L);
        when(generierungslaufRepository.findById(1L)).thenReturn(Optional.of(lauf));

        service.generierungAsyncStarten(1L);

        assertEquals(GenerierungsStatus.FEHLER, lauf.getStatus());
    }

    @Test
    void generierungAsyncStartenWirftKeineExceptionWennLaufBeiFehlerNichtGefundenWird() {
        doThrow(new RuntimeException("Solver-Fehler"))
            .when(stundenplanGenerator).generieren(1L);
        when(generierungslaufRepository.findById(1L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.generierungAsyncStarten(1L));
    }
}
