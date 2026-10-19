package de.cau.sp.stundenplanung.solver;

import de.cau.sp.stundenplanung.model.Blockregel;
import de.cau.sp.stundenplanung.model.Lehrerverfuegbarkeit;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Maximalbelastung;
import de.cau.sp.stundenplanung.model.Raum;
import de.cau.sp.stundenplanung.model.Raumverfuegbarkeit;
import de.cau.sp.stundenplanung.repository.BlockregelRepository;
import de.cau.sp.stundenplanung.repository.LehrerverfuegbarkeitRepository;
import de.cau.sp.stundenplanung.repository.LehrkraftRepository;
import de.cau.sp.stundenplanung.repository.MaximalbelastungRepository;
import de.cau.sp.stundenplanung.repository.RaumRepository;
import de.cau.sp.stundenplanung.repository.RaumverfuegbarkeitRepository;
import de.cau.sp.stundenplanung.solver.model.PlanungsEinheit;
import de.cau.sp.stundenplanung.solver.model.Stundenplanproblem;
import de.cau.sp.stundenplanung.solver.model.Zeitfenster;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StundenplanproblemLaderTest {

    @Mock LehrkraftRepository lehrkraftRepository;
    @Mock RaumRepository raumRepository;
    @Mock LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    @Mock RaumverfuegbarkeitRepository raumverfuegbarkeitRepository;
    @Mock BlockregelRepository blockregelRepository;
    @Mock MaximalbelastungRepository maximalbelastungRepository;
    @Mock EinheitenGenerator einheitenGenerator;
    @InjectMocks StundenplanproblemLader lader;

    private static Blockregel blockregel(boolean aktiv) {
        Blockregel b = new Blockregel();
        b.setAktiv(aktiv);
        return b;
    }

    private static Maximalbelastung belastung(boolean aktiv) {
        Maximalbelastung m = new Maximalbelastung();
        m.setAktiv(aktiv);
        return m;
    }

    @Test
    void ladenReichtNurAktiveRegelnWeiterUndBautDasProblemZusammen() {
        Lehrkraft lehrkraft = new Lehrkraft();
        lehrkraft.setFaecher(List.of());
        lehrkraft.setPraeferenzen(List.of());
        Raum raum = new Raum();
        Lehrerverfuegbarkeit lehrerzeit = new Lehrerverfuegbarkeit();
        Raumverfuegbarkeit raumzeit = new Raumverfuegbarkeit();
        Blockregel aktiveBlockregel = blockregel(true);
        Maximalbelastung aktiveBelastung = belastung(true);
        List<Zeitfenster> fenster = List.of(new Zeitfenster());
        List<Zeitfenster> blockfenster = List.of(new Zeitfenster());
        List<PlanungsEinheit> einheiten = List.of(new PlanungsEinheit());

        when(lehrkraftRepository.findByAccountAktivTrue()).thenReturn(List.of(lehrkraft));
        when(raumRepository.findAll()).thenReturn(List.of(raum));
        when(lehrerverfuegbarkeitRepository.findAll()).thenReturn(List.of(lehrerzeit));
        when(raumverfuegbarkeitRepository.findAll()).thenReturn(List.of(raumzeit));
        when(blockregelRepository.findAll()).thenReturn(List.of(aktiveBlockregel, blockregel(false)));
        when(maximalbelastungRepository.findAll()).thenReturn(List.of(aktiveBelastung, belastung(false)));
        when(einheitenGenerator.stundenProTag()).thenReturn(8);
        when(einheitenGenerator.zeitfensterErzeugen()).thenReturn(fenster);
        when(einheitenGenerator.blockfensterErzeugen(fenster)).thenReturn(blockfenster);
        when(einheitenGenerator.einheitenErzeugen(List.of(lehrkraft), List.of(raum),
                List.of(aktiveBlockregel), List.of(aktiveBelastung), fenster, blockfenster, 8))
                .thenReturn(einheiten);

        Stundenplanproblem problem = lader.laden();

        assertEquals(List.of(lehrkraft), problem.getLehrkraefte());
        assertEquals(List.of(raum), problem.getRaeume());
        assertSame(fenster, problem.getZeitfenster());
        assertEquals(List.of(lehrerzeit), problem.getLehrerverfuegbarkeiten());
        assertEquals(List.of(raumzeit), problem.getRaumverfuegbarkeiten());
        assertSame(einheiten, problem.getEinheiten());
        assertNull(problem.getScore());
    }

    @Test
    void ladenOhneAktiveRegelnGibtLeereRegellistenWeiter() {
        List<Zeitfenster> fenster = List.of();
        when(lehrkraftRepository.findByAccountAktivTrue()).thenReturn(List.of());
        when(raumRepository.findAll()).thenReturn(List.of());
        when(lehrerverfuegbarkeitRepository.findAll()).thenReturn(List.of());
        when(raumverfuegbarkeitRepository.findAll()).thenReturn(List.of());
        when(blockregelRepository.findAll()).thenReturn(List.of(blockregel(false)));
        when(maximalbelastungRepository.findAll()).thenReturn(List.of(belastung(false)));
        when(einheitenGenerator.stundenProTag()).thenReturn(6);
        when(einheitenGenerator.zeitfensterErzeugen()).thenReturn(fenster);
        when(einheitenGenerator.blockfensterErzeugen(fenster)).thenReturn(fenster);

        lader.laden();

        verify(einheitenGenerator).einheitenErzeugen(List.of(), List.of(), List.of(), List.of(),
                fenster, fenster, 6);
    }
}
