package de.cau.sp.stundenplanung.solver.model;

import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.model.Jahrgangsstufe;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Lerngruppe;
import de.cau.sp.stundenplanung.model.Raum;
import de.cau.sp.stundenplanung.model.Wochentag;
import de.cau.sp.stundenplanung.model.Zeitslot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlanungsEinheitTest {

    private static Zeitfenster fenster(Wochentag tag, int stunde) {
        Zeitslot slot = new Zeitslot();
        slot.setStundennummer(stunde);
        return new Zeitfenster(tag, slot);
    }

    private static Lerngruppe gruppe(Integer prio) {
        Lerngruppe g = new Lerngruppe();
        g.setBezeichnung("5a");
        if (prio != null) {
            Jahrgangsstufe j = new Jahrgangsstufe();
            j.setPrio(prio);
            g.setJahrgangsstufe(j);
        }
        return g;
    }

    private static PlanungsEinheit einheit(Lerngruppe gruppe, int dauer, int stundenProTag, Zeitfenster fenster) {
        Fach fach = new Fach();
        fach.setKuerzel("M");
        PlanungsEinheit e = new PlanungsEinheit(1L, gruppe, fach, dauer, 6, stundenProTag,
                List.of(), List.of(), List.of());
        e.setZeitfenster(fenster);
        return e;
    }

    @Test
    void konstruktorUebernimmtAlleWerte() {
        Lerngruppe g = gruppe(null);
        Fach fach = new Fach();
        List<Lehrkraft> lehrkraefte = List.of(new Lehrkraft());
        List<Raum> raeume = List.of(new Raum());
        List<Zeitfenster> fensterListe = List.of(fenster(Wochentag.MONTAG, 1));

        PlanungsEinheit e = new PlanungsEinheit(7L, g, fach, 2, 6, 8, lehrkraefte, raeume, fensterListe);

        assertEquals(7L, e.getId());
        assertSame(g, e.getLerngruppe());
        assertSame(fach, e.getFach());
        assertEquals(2, e.getDauer());
        assertEquals(6, e.getMaxStundenTag());
        assertEquals(8, e.getStundenProTag());
        assertSame(lehrkraefte, e.getMoeglicheLehrkraefte());
        assertSame(raeume, e.getMoeglicheRaeume());
        assertSame(fensterListe, e.getMoeglicheZeitfenster());
        /* Planungsvariablen setzt erst der Solver */
        assertNull(e.getLehrkraft());
        assertNull(e.getRaum());
        assertNull(e.getZeitfenster());
    }

    @Test
    void wochentagUndStundeKommenAusDemZeitfenster() {
        PlanungsEinheit e = einheit(gruppe(null), 1, 8, fenster(Wochentag.MITTWOCH, 3));

        assertEquals(Wochentag.MITTWOCH, e.wochentag());
        assertEquals(3, e.stunde());
    }

    @Test
    void letzteStundeIstBeiEinzelstundeDieStundeSelbst() {
        assertEquals(3, einheit(gruppe(null), 1, 8, fenster(Wochentag.MONTAG, 3)).letzteStunde());
    }

    @Test
    void letzteStundeUmfasstBeiDoppelstundeZweiStunden() {
        assertEquals(4, einheit(gruppe(null), 2, 8, fenster(Wochentag.MONTAG, 3)).letzteStunde());
    }

    @Test
    void belegtGiltNurInnerhalbDerEinheit() {
        PlanungsEinheit doppelstunde = einheit(gruppe(null), 2, 8, fenster(Wochentag.MONTAG, 3));

        assertFalse(doppelstunde.belegt(2));
        assertTrue(doppelstunde.belegt(3));
        assertTrue(doppelstunde.belegt(4));
        assertFalse(doppelstunde.belegt(5));
    }

    @Test
    void letzteVormittagsstundeIstDieObereHaelfteDesTages() {
        assertEquals(4, einheit(gruppe(null), 1, 8, null).letzteVormittagsstunde());
        assertEquals(4, einheit(gruppe(null), 1, 7, null).letzteVormittagsstunde());
    }

    @Test
    void ersteRandstundeIstDieVorletzteStunde() {
        assertEquals(7, einheit(gruppe(null), 1, 8, null).ersteRandstunde());
    }

    @Test
    void prioIstEinsOhneJahrgangsstufe() {
        assertEquals(1, einheit(gruppe(null), 1, 8, null).prio());
    }

    @Test
    void prioSinktMitHoehererGespeicherterPrio() {
        /* legt die aktuelle Formel 20 - prio fest, niedrige gespeicherte Zahl = höhere Gewichtung */
        assertEquals(19, einheit(gruppe(1), 1, 8, null).prio());
        assertEquals(14, einheit(gruppe(6), 1, 8, null).prio());
    }

    @Test
    void prioIstMindestensEins() {
        assertEquals(1, einheit(gruppe(30), 1, 8, null).prio());
    }

    @Test
    void toStringZeigtDoppelstundeUndZeitfenster() {
        PlanungsEinheit e = einheit(gruppe(null), 2, 8, fenster(Wochentag.MONTAG, 3));
        assertEquals("5a M (Doppelstunde) @ MONTAG-3", e.toString());
    }

    @Test
    void toStringZeigtStrichOhneZeitfenster() {
        assertEquals("5a M @ -", einheit(gruppe(null), 1, 8, null).toString());
    }
}
