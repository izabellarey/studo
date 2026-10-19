package de.cau.sp.stundenplanung.solver.model;

import de.cau.sp.stundenplanung.model.Wochentag;
import de.cau.sp.stundenplanung.model.Zeitslot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZeitfensterTest {

    private static Zeitfenster fenster(Wochentag tag, int stunde) {
        Zeitslot slot = new Zeitslot();
        slot.setStundennummer(stunde);
        return new Zeitfenster(tag, slot);
    }

    @Test
    void schluesselBestehtAusWochentagUndStundennummer() {
        assertEquals("DIENSTAG-4", fenster(Wochentag.DIENSTAG, 4).schluessel());
    }

    @Test
    void toStringEntsprichtDemSchluessel() {
        Zeitfenster z = fenster(Wochentag.FREITAG, 1);
        assertEquals(z.schluessel(), z.toString());
    }
}
