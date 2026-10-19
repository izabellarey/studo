package de.cau.sp.stundenplanung.solver;

import de.cau.sp.stundenplanung.model.Blockregel;
import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.model.Jahrgangsstufe;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Lerngruppe;
import de.cau.sp.stundenplanung.model.Maximalbelastung;
import de.cau.sp.stundenplanung.model.Raum;
import de.cau.sp.stundenplanung.model.Raumart;
import de.cau.sp.stundenplanung.model.Pflichtfachvorgabe;
import de.cau.sp.stundenplanung.model.Wochentag;
import de.cau.sp.stundenplanung.model.Zeitslot;
import de.cau.sp.stundenplanung.repository.LerngruppeRepository;
import de.cau.sp.stundenplanung.repository.PflichtfachvorgabeRepository;
import de.cau.sp.stundenplanung.repository.ZeitslotRepository;
import de.cau.sp.stundenplanung.solver.model.PlanungsEinheit;
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
class EinheitenGeneratorTest {

    @Mock PflichtfachvorgabeRepository pflichtfachvorgabeRepository;
    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock ZeitslotRepository zeitslotRepository;
    @InjectMocks EinheitenGenerator generator;

    /* getrennte Listen, damit prüfbar ist, welche Fensterliste eine Einheit bekommt */
    private final List<Zeitfenster> alleFenster = List.of(new Zeitfenster(Wochentag.MONTAG, slot(1, false)));
    private final List<Zeitfenster> blockFenster = List.of(new Zeitfenster(Wochentag.DIENSTAG, slot(1, false)));

    private static Zeitslot slot(int nummer, boolean pause) {
        Zeitslot z = new Zeitslot();
        z.setStundennummer(nummer);
        z.setPause(pause);
        return z;
    }

    private static Jahrgangsstufe jahrgang(long id) {
        Jahrgangsstufe j = new Jahrgangsstufe();
        j.setId(id);
        return j;
    }

    private static Fach fach(long id, String kuerzel, Raumart raumtyp) {
        Fach f = new Fach();
        f.setId(id);
        f.setKuerzel(kuerzel);
        f.setBenoetigterRaumtyp(raumtyp);
        return f;
    }

    private static Raum raum(long id, Raumart art, int kapazitaet) {
        Raum r = new Raum();
        r.setId(id);
        r.setArt(art);
        r.setKapazitaet(kapazitaet);
        return r;
    }

    private static Lehrkraft lehrkraft(Fach... faecher) {
        Lehrkraft l = new Lehrkraft();
        l.setFaecher(List.of(faecher));
        return l;
    }

    private static Lerngruppe gruppe(long id, Jahrgangsstufe jahrgang, int schueler, boolean aktiv, Raum klassenzimmer) {
        Lerngruppe g = new Lerngruppe();
        g.setId(id);
        g.setBezeichnung("G" + id);
        g.setJahrgangsstufe(jahrgang);
        g.setSchueleranzahl(schueler);
        g.setAktiv(aktiv);
        g.setKlassenzimmer(klassenzimmer);
        return g;
    }

    private long naechsteVorgabeId = 1L;

    private Pflichtfachvorgabe vorgabe(Jahrgangsstufe jahrgang, Fach fach, int stunden) {
        Pflichtfachvorgabe v = new Pflichtfachvorgabe();
        v.setId(naechsteVorgabeId++);
        v.setJahrgangsstufe(jahrgang);
        v.setFach(fach);
        v.setStundenanzahl(stunden);
        v.setAktiv(true);
        return v;
    }

    /* Die Lerngruppen-Query liefert nur aktive Klassen, das bildet der Stub nach. */
    private void stubben(List<Lerngruppe> gruppen, Pflichtfachvorgabe... vorgaben) {
        when(pflichtfachvorgabeRepository.findAll()).thenReturn(List.of(vorgaben));
        when(lerngruppeRepository.findAllByAktivTrue())
                .thenReturn(gruppen.stream().filter(Lerngruppe::isAktiv).toList());
    }

    private static Blockregel blockregel(Fach fach, Jahrgangsstufe jahrgang) {
        Blockregel b = new Blockregel();
        b.setFach(fach);
        b.setJahrgangsstufe(jahrgang);
        return b;
    }

    private static Maximalbelastung belastung(Jahrgangsstufe jahrgang, Integer maxStundenTag) {
        Maximalbelastung m = new Maximalbelastung();
        m.setJahrgangsstufe(jahrgang);
        m.setMaxStundenTag(maxStundenTag);
        return m;
    }

    private List<PlanungsEinheit> erzeugen(List<Lehrkraft> lehrkraefte, List<Raum> raeume,
                                           List<Blockregel> blockregeln, List<Maximalbelastung> belastungen) {
        return generator.einheitenErzeugen(lehrkraefte, raeume, blockregeln, belastungen,
                alleFenster, blockFenster, 8);
    }

    @Test
    void stundenProTagZaehltNurUnterrichtsslots() {
        when(zeitslotRepository.findAll()).thenReturn(List.of(
                slot(1, false), slot(2, false), slot(0, true), slot(3, false)));

        assertEquals(3, generator.stundenProTag());
    }

    @Test
    void zeitfensterErzeugenLiefertAlleTageOhneFreitagnachmittag() {
        /* unsortiert und mit Pause, um Sortierung und Pausenfilter mitzuprüfen */
        when(zeitslotRepository.findAll()).thenReturn(List.of(
                slot(3, false), slot(1, false), slot(0, true), slot(6, false),
                slot(2, false), slot(5, false), slot(4, false)));

        List<Zeitfenster> fenster = generator.zeitfensterErzeugen();

        /* Montag bis Donnerstag je 6 Stunden, Freitag ohne die letzten beiden */
        assertEquals(4 * 6 + 4, fenster.size());
        assertEquals("MONTAG-1", fenster.get(0).schluessel());
        assertTrue(fenster.stream().noneMatch(z -> z.getZeitslot().isPause()));
        assertTrue(fenster.stream()
                .filter(z -> z.getWochentag() == Wochentag.FREITAG)
                .allMatch(z -> z.getZeitslot().getStundennummer() <= 4));
    }

    @Test
    void blockfensterErzeugenNimmtUngeradeStundenMitNachfolger() {
        List<Zeitfenster> alle = List.of(1, 2, 3, 4, 5).stream()
                .map(n -> new Zeitfenster(Wochentag.MONTAG, slot(n, false)))
                .toList();

        List<Integer> start = generator.blockfensterErzeugen(alle).stream()
                .map(z -> z.getZeitslot().getStundennummer())
                .toList();

        /* 5 hat keine 6. Stunde als Nachfolger */
        assertEquals(List.of(1, 3), start);
    }

    @Test
    void einheitenErzeugenIgnoriertInaktiveLerngruppen() {
        Fach m = fach(1L, "M", null);
        Lerngruppe inaktiv = gruppe(1L, jahrgang(1L), 25, false, raum(1L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(inaktiv), vorgabe(inaktiv.getJahrgangsstufe(), m, 4));

        assertTrue(erzeugen(List.of(lehrkraft(m)), List.of(), List.of(), List.of()).isEmpty());
    }

    @Test
    void einheitenErzeugenBildetDoppelstundenBeiBlockregel() {
        Jahrgangsstufe j5 = jahrgang(1L);
        Fach m = fach(1L, "M", null);
        Lerngruppe g = gruppe(1L, j5, 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(g), vorgabe(g.getJahrgangsstufe(), m, 5));

        List<PlanungsEinheit> einheiten = erzeugen(List.of(lehrkraft(m)), List.of(),
                List.of(blockregel(m, j5)), List.of());

        /* 5 Stunden = 2 Doppelstunden + 1 Einzelstunde */
        assertEquals(List.of(2, 2, 1), einheiten.stream().map(PlanungsEinheit::getDauer).toList());
        assertSame(blockFenster, einheiten.get(0).getMoeglicheZeitfenster());
        assertSame(blockFenster, einheiten.get(1).getMoeglicheZeitfenster());
        assertSame(alleFenster, einheiten.get(2).getMoeglicheZeitfenster());
        assertEquals(List.of(1L, 2L, 3L), einheiten.stream().map(PlanungsEinheit::getId).toList());
    }

    @Test
    void einheitenErzeugenOhneBlockregelNurEinzelstunden() {
        Fach m = fach(1L, "M", null);
        Lerngruppe g = gruppe(1L, jahrgang(1L), 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(g), vorgabe(g.getJahrgangsstufe(), m, 3));

        List<PlanungsEinheit> einheiten = erzeugen(List.of(lehrkraft(m)), List.of(), List.of(), List.of());

        assertEquals(3, einheiten.size());
        assertTrue(einheiten.stream().allMatch(e -> e.getDauer() == 1));
        assertTrue(einheiten.stream().allMatch(e -> e.getStundenProTag() == 8));
    }

    @Test
    void einheitenErzeugenSortiertNachLerngruppeUndFach() {
        Fach m = fach(1L, "M", null);
        Fach d = fach(2L, "D", null);
        Jahrgangsstufe stufe = jahrgang(1L);
        Lerngruppe g1 = gruppe(1L, stufe, 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        Lerngruppe g2 = gruppe(2L, stufe, 25, true, raum(2L, Raumart.KLASSENRAUM, 30));
        Jahrgangsstufe j = g1.getJahrgangsstufe();
        stubben(List.of(g2, g1), vorgabe(j, m, 1), vorgabe(j, d, 1));

        List<PlanungsEinheit> einheiten = erzeugen(List.of(lehrkraft(m, d)), List.of(), List.of(), List.of());

        /* erst nach Lerngruppe, dann nach Fach-ID (M hat 1, D hat 2) */
        assertEquals(4, einheiten.size());
        assertSame(g1, einheiten.get(0).getLerngruppe());
        assertSame(m, einheiten.get(0).getFach());
        assertSame(g1, einheiten.get(1).getLerngruppe());
        assertSame(d, einheiten.get(1).getFach());
        assertSame(g2, einheiten.get(2).getLerngruppe());
        assertSame(m, einheiten.get(2).getFach());
    }

    @Test
    void einheitenErzeugenUebernimmtTagesgrenzeDerEigenenJahrgangsstufe() {
        Jahrgangsstufe j5 = jahrgang(1L);
        Fach m = fach(1L, "M", null);
        Lerngruppe g = gruppe(1L, j5, 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(g), vorgabe(g.getJahrgangsstufe(), m, 1));

        List<PlanungsEinheit> einheiten = erzeugen(List.of(lehrkraft(m)), List.of(), List.of(),
                List.of(belastung(jahrgang(2L), 4), belastung(j5, 6)));

        assertEquals(6, einheiten.get(0).getMaxStundenTag());
    }

    @Test
    void einheitenErzeugenUeberspringtLerngruppenOhneJahrgangsstufe() {
        Fach m = fach(1L, "M", null);
        Lerngruppe g = gruppe(1L, null, 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(g), vorgabe(jahrgang(1L), m, 2));

        assertTrue(erzeugen(List.of(lehrkraft(m)), List.of(), List.of(), List.of()).isEmpty());
    }

    @Test
    void einheitenErzeugenBietetNurLehrkraefteMitPassendemFachAn() {
        Fach m = fach(1L, "M", null);
        Fach d = fach(2L, "D", null);
        Lehrkraft mathe = lehrkraft(m);
        Lehrkraft deutsch = lehrkraft(d);
        Lerngruppe g = gruppe(1L, jahrgang(1L), 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(g), vorgabe(g.getJahrgangsstufe(), m, 1));

        List<PlanungsEinheit> einheiten = erzeugen(List.of(mathe, deutsch), List.of(), List.of(), List.of());

        assertEquals(List.of(mathe), einheiten.get(0).getMoeglicheLehrkraefte());
    }

    @Test
    void einheitenErzeugenFiltertFachraeumeNachTypUndKapazitaet() {
        Fach ph = fach(1L, "PH", Raumart.PHYSIKRAUM);
        Raum physikGross = raum(1L, Raumart.PHYSIKRAUM, 30);
        Raum physikKlein = raum(2L, Raumart.PHYSIKRAUM, 20);
        Raum klassenraum = raum(3L, Raumart.KLASSENRAUM, 30);
        Lerngruppe g = gruppe(1L, jahrgang(1L), 28, true, klassenraum);
        stubben(List.of(g), vorgabe(g.getJahrgangsstufe(), ph, 1));

        List<PlanungsEinheit> einheiten = erzeugen(List.of(lehrkraft(ph)),
                List.of(physikGross, physikKlein, klassenraum), List.of(), List.of());

        /* Fachraumbedarf geht vor Klassenzimmer */
        assertEquals(List.of(physikGross), einheiten.get(0).getMoeglicheRaeume());
    }

    @Test
    void einheitenErzeugenOhneRaumtypNutztNurDasKlassenzimmer() {
        Fach m = fach(1L, "M", null);
        Raum klassenzimmer = raum(1L, Raumart.KLASSENRAUM, 30);
        Lerngruppe g = gruppe(1L, jahrgang(1L), 25, true, klassenzimmer);
        stubben(List.of(g), vorgabe(g.getJahrgangsstufe(), m, 1));

        List<PlanungsEinheit> einheiten = erzeugen(List.of(lehrkraft(m)),
                List.of(raum(2L, Raumart.KLASSENRAUM, 30)), List.of(), List.of());

        assertEquals(List.of(klassenzimmer), einheiten.get(0).getMoeglicheRaeume());
    }

    @Test
    void einheitenErzeugenOhneKlassenzimmerNimmtAlleRaeumeMitGenugPlatz() {
        Fach m = fach(1L, "M", null);
        Raum gross = raum(1L, Raumart.KLASSENRAUM, 30);
        Raum klein = raum(2L, Raumart.KLASSENRAUM, 20);
        Raum aula = raum(3L, Raumart.AULA, 200);
        Lerngruppe g = gruppe(1L, jahrgang(1L), 25, true, null);
        stubben(List.of(g), vorgabe(g.getJahrgangsstufe(), m, 1));

        List<PlanungsEinheit> einheiten = erzeugen(List.of(lehrkraft(m)),
                List.of(gross, klein, aula), List.of(), List.of());

        assertEquals(List.of(gross, aula), einheiten.get(0).getMoeglicheRaeume());
    }

    @Test
    void einheitenErzeugenNutztVorgabenAllerKlassenDerJahrgangsstufe() {
        Jahrgangsstufe j5 = jahrgang(1L);
        Fach neuesFach = fach(9L, "NEU", null);
        Lerngruppe g5a = gruppe(1L, j5, 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        Lerngruppe g5b = gruppe(2L, j5, 25, true, raum(2L, Raumart.KLASSENRAUM, 30));
        Lerngruppe g6a = gruppe(3L, jahrgang(2L), 25, true, raum(3L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(g5a, g5b, g6a), vorgabe(j5, neuesFach, 2));

        List<PlanungsEinheit> einheiten = erzeugen(List.of(lehrkraft(neuesFach)), List.of(), List.of(), List.of());

        /* 2 Stunden für beide 5er-Klassen, nichts für die 6a */
        assertEquals(4, einheiten.size());
        assertTrue(einheiten.stream().allMatch(e -> e.getFach() == neuesFach));
        assertTrue(einheiten.stream().noneMatch(e -> e.getLerngruppe() == g6a));
    }

    @Test
    void einheitenErzeugenIgnoriertInaktiveVorgaben() {
        Jahrgangsstufe j5 = jahrgang(1L);
        Fach m = fach(1L, "M", null);
        Lerngruppe g = gruppe(1L, j5, 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        Pflichtfachvorgabe inaktiv = vorgabe(j5, m, 4);
        inaktiv.setAktiv(false);
        stubben(List.of(g), inaktiv);

        assertTrue(erzeugen(List.of(lehrkraft(m)), List.of(), List.of(), List.of()).isEmpty());
    }

    @Test
    void einheitenErzeugenNimmtBeiDoppelterVorgabeDieNeuere() {
        Jahrgangsstufe j5 = jahrgang(1L);
        Fach m = fach(1L, "M", null);
        Lerngruppe g = gruppe(1L, j5, 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(g), vorgabe(j5, m, 4), vorgabe(j5, m, 3));

        assertEquals(3, erzeugen(List.of(lehrkraft(m)), List.of(), List.of(), List.of()).size());
    }

    @Test
    void einheitenErzeugenIgnoriertVorgabenMitNullStunden() {
        Jahrgangsstufe j5 = jahrgang(1L);
        Fach m = fach(1L, "M", null);
        Lerngruppe g = gruppe(1L, j5, 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(g), vorgabe(j5, m, 0));

        assertTrue(erzeugen(List.of(lehrkraft(m)), List.of(), List.of(), List.of()).isEmpty());
    }

    @Test
    void einheitenErzeugenWirftOhnePassendeLehrkraft() {
        Fach m = fach(1L, "M", null);
        Lerngruppe g = gruppe(1L, jahrgang(1L), 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(g), vorgabe(g.getJahrgangsstufe(), m, 1));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> erzeugen(List.of(lehrkraft(fach(2L, "D", null))), List.of(), List.of(), List.of()));
        assertTrue(ex.getMessage().contains("M"));
    }

    @Test
    void einheitenErzeugenWirftOhnePassendenRaum() {
        Fach ch = fach(1L, "CH", Raumart.CHEMIERAUM);
        Lerngruppe g = gruppe(1L, jahrgang(1L), 25, true, raum(1L, Raumart.KLASSENRAUM, 30));
        stubben(List.of(g), vorgabe(g.getJahrgangsstufe(), ch, 1));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> erzeugen(List.of(lehrkraft(ch)), List.of(raum(2L, Raumart.KLASSENRAUM, 30)),
                        List.of(), List.of()));
        assertTrue(ex.getMessage().contains("CH"));
    }
}