package de.cau.sp.stundenplanung.solver.model;

import de.cau.sp.stundenplanung.model.Lehrerverfuegbarkeit;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Raum;
import de.cau.sp.stundenplanung.model.Raumverfuegbarkeit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StundenplanproblemTest {

    @Test
    void konstruktorUebernimmtAlleListenUndLaesstScoreLeer() {
        List<Lehrkraft> lehrkraefte = List.of(new Lehrkraft());
        List<Raum> raeume = List.of(new Raum());
        List<Zeitfenster> zeitfenster = List.of(new Zeitfenster());
        List<Lehrerverfuegbarkeit> lehrerzeiten = List.of(new Lehrerverfuegbarkeit());
        List<Raumverfuegbarkeit> raumzeiten = List.of(new Raumverfuegbarkeit());
        List<PlanungsEinheit> einheiten = List.of(new PlanungsEinheit());

        Stundenplanproblem problem = new Stundenplanproblem(
                lehrkraefte, raeume, zeitfenster, lehrerzeiten, raumzeiten, einheiten);

        assertSame(lehrkraefte, problem.getLehrkraefte());
        assertSame(raeume, problem.getRaeume());
        assertSame(zeitfenster, problem.getZeitfenster());
        assertSame(lehrerzeiten, problem.getLehrerverfuegbarkeiten());
        assertSame(raumzeiten, problem.getRaumverfuegbarkeiten());
        assertSame(einheiten, problem.getEinheiten());
        assertNull(problem.getScore());
    }
}
