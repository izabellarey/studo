package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerierungAnlegenServiceTest {

    @Mock GenerierungslaufRepository generierungslaufRepository;
    @Mock StundenplanRepository stundenplanRepository;
    @Mock UnterrichtsstundeRepository unterrichtsstundeRepository;
    @InjectMocks GenerierungAnlegenService service;

    @Test
    void generierungAnlegenLegtNeuenLaufAn() {
        when(generierungslaufRepository.findAll()).thenReturn(List.of());
        when(generierungslaufRepository.save(any(Generierungslauf.class)))
            .thenAnswer(inv -> { Generierungslauf l = inv.getArgument(0); l.setId(1L); return l; });

        Long result = service.generierungAnlegen();

        assertEquals(1L, result);
    }

    @Test
    void generierungAnlegenWirftExceptionWennBereitsEinLaufLaeuft() {
        Generierungslauf laufenderLauf = new Generierungslauf();
        laufenderLauf.setStatus(GenerierungsStatus.LAEUFT);

        when(generierungslaufRepository.findAll()).thenReturn(List.of(laufenderLauf));

        assertThrows(RuntimeException.class, () -> service.generierungAnlegen());
        verify(generierungslaufRepository, never()).save(any(Generierungslauf.class));
    }

    @Test
    void generierungAnlegenLoeschtNurEntwuerfeNichtBereitsVeroeffentlichteStunden() {
        Generierungslauf alterLauf = new Generierungslauf();
        alterLauf.setId(1L);
        alterLauf.setStatus(GenerierungsStatus.FERTIG);

        Stundenplan aktiverPlan = new Stundenplan();
        aktiverPlan.setId(100L);
        aktiverPlan.setStatus(PlanStatus.AKTIV);
        aktiverPlan.setUnterrichtsstunden(new ArrayList<>());

        Stundenplan entwurf = new Stundenplan();
        entwurf.setId(101L);
        entwurf.setStatus(PlanStatus.ENTWURF);
        entwurf.setUnterrichtsstunden(new ArrayList<>());

        when(generierungslaufRepository.findAll()).thenReturn(List.of(alterLauf));
        when(stundenplanRepository.findByGenerationId(1L)).thenReturn(List.of(aktiverPlan, entwurf));
        when(generierungslaufRepository.save(any(Generierungslauf.class)))
            .thenAnswer(inv -> { Generierungslauf l = inv.getArgument(0); l.setId(2L); return l; });

        service.generierungAnlegen();

        verify(stundenplanRepository).deleteAll(List.of(entwurf));
        verify(stundenplanRepository, never()).delete(aktiverPlan);
        verify(generierungslaufRepository).delete(alterLauf);
    }

    @Test
    void generierungAnlegenLoeschtAuchFehlgeschlageneLaeufe() {
        Generierungslauf fehlerLauf = new Generierungslauf();
        fehlerLauf.setId(1L);
        fehlerLauf.setStatus(GenerierungsStatus.FEHLER);

        when(generierungslaufRepository.findAll()).thenReturn(List.of(fehlerLauf));
        when(stundenplanRepository.findByGenerationId(1L)).thenReturn(List.of());
        when(generierungslaufRepository.save(any(Generierungslauf.class)))
            .thenAnswer(inv -> { Generierungslauf l = inv.getArgument(0); l.setId(2L); return l; });

        service.generierungAnlegen();

        verify(generierungslaufRepository).delete(fehlerLauf);
    }
}