package de.cau.sp.stundenplanung.solver;

import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Lerngruppe;
import de.cau.sp.stundenplanung.solver.model.PlanungsEinheit;
import de.cau.sp.stundenplanung.solver.model.Stundenplanproblem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LehrkraftBlockMoveListFactoryTest {

    private final LehrkraftBlockMoveListFactory factory = new LehrkraftBlockMoveListFactory();

    private final Lehrkraft a = new Lehrkraft();
    private final Lehrkraft b = new Lehrkraft();
    private final Lehrkraft c = new Lehrkraft();
    private final Lerngruppe g5a = new Lerngruppe();
    private final Lerngruppe g5b = new Lerngruppe();
    private final Fach mathe = fach(1L);
    private final Fach deutsch = fach(2L);

    private static Fach fach(long id) {
        Fach f = new Fach();
        f.setId(id);
        return f;
    }

    private static PlanungsEinheit einheit(Lerngruppe gruppe, Fach fach, List<Lehrkraft> moeglich, Lehrkraft aktuell) {
        PlanungsEinheit e = new PlanungsEinheit(1L, gruppe, fach, 1, null, 8, moeglich, List.of(), List.of());
        e.setLehrkraft(aktuell);
        return e;
    }

    private static Stundenplanproblem problem(PlanungsEinheit... einheiten) {
        return new Stundenplanproblem(List.of(), List.of(), List.of(), List.of(), List.of(), List.of(einheiten));
    }

    @Test
    void createMoveListErzeugtEinenZugJeAndererLehrkraft() {
        List<Lehrkraft> moeglich = List.of(a, b, c);

        var zuege = factory.createMoveList(problem(
                einheit(g5a, mathe, moeglich, a),
                einheit(g5a, mathe, moeglich, a)));

        /* a hat den Block schon komplett, also nur Züge zu b und c */
        assertEquals(2, zuege.size());
    }

    @Test
    void createMoveListNimmtBeiGemischterBesetzungAlleKandidaten() {
        List<Lehrkraft> moeglich = List.of(a, b, c);

        var zuege = factory.createMoveList(problem(
                einheit(g5a, mathe, moeglich, a),
                einheit(g5a, mathe, moeglich, b)));

        assertEquals(3, zuege.size());
    }

    @Test
    void createMoveListBildetBloeckeJeLerngruppeUndFach() {
        var zuege = factory.createMoveList(problem(
                einheit(g5a, mathe, List.of(a, b, c), a),
                einheit(g5a, mathe, List.of(a, b, c), a),
                einheit(g5a, deutsch, List.of(a, b), a),
                einheit(g5b, mathe, List.of(a, b, c), null)));

        /* 5a Mathe: 2, 5a Deutsch: 1, 5b Mathe noch unbesetzt: 3 */
        assertEquals(6, zuege.size());
    }

    @Test
    void createMoveListOhneEinheitenIstLeer() {
        assertTrue(factory.createMoveList(problem()).isEmpty());
    }
}
