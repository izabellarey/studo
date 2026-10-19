package de.cau.sp.stundenplanung.solver;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Lerngruppe;
import de.cau.sp.stundenplanung.model.PlanStatus;
import de.cau.sp.stundenplanung.model.Raum;
import de.cau.sp.stundenplanung.model.Stundenplan;
import de.cau.sp.stundenplanung.model.Unterrichtsstunde;
import de.cau.sp.stundenplanung.model.Wochentag;
import de.cau.sp.stundenplanung.model.Zeitslot;
import de.cau.sp.stundenplanung.repository.StundenplanRepository;
import de.cau.sp.stundenplanung.repository.UnterrichtsstundeRepository;
import de.cau.sp.stundenplanung.repository.ZeitslotRepository;
import de.cau.sp.stundenplanung.solver.model.PlanungsEinheit;
import de.cau.sp.stundenplanung.solver.model.Stundenplanproblem;
import de.cau.sp.stundenplanung.solver.model.Zeitfenster;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StundenplanSchreiberTest {

    @Mock StundenplanRepository stundenplanRepository;
    @Mock UnterrichtsstundeRepository unterrichtsstundeRepository;
    @Mock ZeitslotRepository zeitslotRepository;
    @InjectMocks StundenplanSchreiber schreiber;

    private final Zeitslot slot1 = slot(1);
    private final Zeitslot slot2 = slot(2);
    private final Zeitslot slot3 = slot(3);
    private final Zeitslot slot4 = slot(4);

    private static Zeitslot slot(int nummer) {
        Zeitslot z = new Zeitslot();
        z.setStundennummer(nummer);
        return z;
    }

    private static Lerngruppe gruppe(String bezeichnung) {
        Lerngruppe g = new Lerngruppe();
        g.setBezeichnung(bezeichnung);
        return g;
    }

    private static PlanungsEinheit einheit(Lerngruppe gruppe, int dauer, Wochentag tag, int stunde) {
        Fach fach = new Fach();
        fach.setKuerzel("M");
        PlanungsEinheit e = new PlanungsEinheit(1L, gruppe, fach, dauer, null, 8,
                List.of(), List.of(), List.of());
        e.setZeitfenster(new Zeitfenster(tag, slot(stunde)));
        e.setLehrkraft(new Lehrkraft());
        e.setRaum(new Raum());
        return e;
    }

    private static Stundenplanproblem loesung(List<PlanungsEinheit> einheiten, HardMediumSoftScore score) {
        Stundenplanproblem p = new Stundenplanproblem(List.of(), List.of(), List.of(), List.of(), List.of(), einheiten);
        p.setScore(score);
        return p;
    }

    /* rechnet die Erwartung aus dem heutigen Datum nach, wie im Schreiber: März bis August = 1. Halbjahr */
    private static String erwartetesSchuljahr() {
        LocalDate heute = LocalDate.now();
        int monat = heute.getMonthValue();
        int start = monat <= 2 ? heute.getYear() - 1 : heute.getYear();
        return start + "/" + String.format("%02d", (start + 1) % 100);
    }

    private static int erwartetesHalbjahr() {
        int monat = LocalDate.now().getMonthValue();
        return monat >= 3 && monat <= 8 ? 1 : 2;
    }

    @Test
    void speichernLegtJeLerngruppeEinenEntwurfMitScoreAn() {
        Lerngruppe g5a = gruppe("5a");
        Lerngruppe g5b = gruppe("5b");
        when(zeitslotRepository.findAll()).thenReturn(List.of(slot1, slot2));

        int anzahl = schreiber.speichern(loesung(List.of(
                        einheit(g5a, 1, Wochentag.MONTAG, 1),
                        einheit(g5a, 1, Wochentag.DIENSTAG, 2),
                        einheit(g5b, 1, Wochentag.MONTAG, 1)),
                HardMediumSoftScore.of(0, -2, -150)), 42L, 3);

        assertEquals(2, anzahl);
        ArgumentCaptor<Stundenplan> captor = ArgumentCaptor.forClass(Stundenplan.class);
        verify(stundenplanRepository, times(2)).save(captor.capture());
        List<Stundenplan> plaene = captor.getAllValues();
        assertSame(g5a, plaene.get(0).getLerngruppe());
        assertSame(g5b, plaene.get(1).getLerngruppe());
        for (Stundenplan plan : plaene) {
            assertEquals(PlanStatus.ENTWURF, plan.getStatus());
            assertEquals(42L, plan.getGenerationId());
            assertEquals(3, plan.getVariante());
            assertEquals(-150, plan.getBewertungsScore());
            assertEquals(2, plan.getKompromisse());
        }
        verify(unterrichtsstundeRepository, times(3)).save(any(Unterrichtsstunde.class));
    }

    @Test
    void speichernSchreibtDoppelstundeAlsZweiUnterrichtsstunden() {
        Lerngruppe g5a = gruppe("5a");
        PlanungsEinheit doppelstunde = einheit(g5a, 2, Wochentag.MITTWOCH, 3);
        when(zeitslotRepository.findAll()).thenReturn(List.of(slot1, slot2, slot3, slot4));

        schreiber.speichern(loesung(List.of(doppelstunde), HardMediumSoftScore.of(0, 0, 0)), 1L, 1);

        ArgumentCaptor<Stundenplan> planCaptor = ArgumentCaptor.forClass(Stundenplan.class);
        verify(stundenplanRepository).save(planCaptor.capture());
        ArgumentCaptor<Unterrichtsstunde> captor = ArgumentCaptor.forClass(Unterrichtsstunde.class);
        verify(unterrichtsstundeRepository, times(2)).save(captor.capture());
        List<Unterrichtsstunde> stunden = captor.getAllValues();

        assertSame(slot3, stunden.get(0).getZeitslot());
        assertSame(slot4, stunden.get(1).getZeitslot());
        for (Unterrichtsstunde u : stunden) {
            assertEquals(Wochentag.MITTWOCH, u.getWochentag());
            assertSame(doppelstunde.getLehrkraft(), u.getLehrkraft());
            assertSame(doppelstunde.getRaum(), u.getRaum());
            assertSame(doppelstunde.getFach(), u.getFach());
            assertSame(g5a, u.getLerngruppe());
            assertSame(planCaptor.getValue(), u.getStundenplan());
        }
    }

    @Test
    void speichernSetztSchuljahrUndHalbjahrNachAktuellemDatum() {
        when(zeitslotRepository.findAll()).thenReturn(List.of(slot1));

        schreiber.speichern(loesung(List.of(einheit(gruppe("5a"), 1, Wochentag.MONTAG, 1)),
                HardMediumSoftScore.of(0, 0, 0)), 1L, 1);

        ArgumentCaptor<Stundenplan> captor = ArgumentCaptor.forClass(Stundenplan.class);
        verify(stundenplanRepository).save(captor.capture());
        assertEquals(erwartetesSchuljahr(), captor.getValue().getSchuljahr());
        assertEquals(erwartetesHalbjahr(), captor.getValue().getHalbjahr());
    }

    @Test
    void speichernWirftOhnePassendenZeitslot() {
        when(zeitslotRepository.findAll()).thenReturn(List.of(slot1));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                schreiber.speichern(loesung(List.of(einheit(gruppe("5a"), 1, Wochentag.MONTAG, 2)),
                        HardMediumSoftScore.of(0, 0, 0)), 1L, 1));
        assertTrue(ex.getMessage().contains("2"));
        verify(unterrichtsstundeRepository, never()).save(any());
    }

    @Test
    void speichernOhneEinheitenLegtNichtsAn() {
        when(zeitslotRepository.findAll()).thenReturn(List.of(slot1));

        int anzahl = schreiber.speichern(loesung(List.of(), HardMediumSoftScore.of(0, 0, 0)), 1L, 1);

        assertEquals(0, anzahl);
        verifyNoInteractions(stundenplanRepository, unterrichtsstundeRepository);
    }
}
