package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.SeiteDTO;
import de.cau.sp.stundenplanung.dto.aenderungsverlauf.AbgeschlosseneAenderungDTO;
import de.cau.sp.stundenplanung.dto.aenderungsverlauf.OffeneAenderungDTO;
import de.cau.sp.stundenplanung.dto.ereignis.EreignisDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.AccountRepository;
import de.cau.sp.stundenplanung.repository.AenderungRepository;
import de.cau.sp.stundenplanung.repository.EreignisRepository;
import de.cau.sp.stundenplanung.repository.LehrkraftRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AenderungsverlaufServiceTest {

    @Mock AenderungRepository aenderungRepository;
    @Mock EreignisRepository ereignisRepository;
    @Mock AccountRepository accountRepository;
    @Mock LehrkraftRepository lehrkraftRepository;

    AenderungsverlaufService service;

    @BeforeEach
    void setUp() {
        Generieren generieren = new Generieren(accountRepository, lehrkraftRepository);
        service = new AenderungsverlaufService(aenderungRepository, ereignisRepository, generieren);
    }


    @Test
    void offeneListsOnlyOpenAenderungenNewestFirst() {
        Aenderung offenAlt = ausfallAenderung(1L, true, LocalDateTime.of(2026, 1, 1, 8, 0));
        Aenderung offenNeu = ausfallAenderung(2L, true, LocalDateTime.of(2026, 1, 2, 8, 0));
        Aenderung geschlossen = ausfallAenderung(3L, false, LocalDateTime.of(2026, 1, 3, 8, 0));
        when(aenderungRepository.findAll()).thenReturn(List.of(offenAlt, offenNeu, geschlossen));

        List<OffeneAenderungDTO> result = service.offene();

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId(), "neuere zuerst");
        assertEquals(1L, result.get(1).getId());
    }

    @Test
    void anzahlOffeneCountsOnlyOpen() {
        when(aenderungRepository.findAll()).thenReturn(List.of(
                ausfallAenderung(1L, true, LocalDateTime.now()),
                ausfallAenderung(2L, true, LocalDateTime.now()),
                ausfallAenderung(3L, false, LocalDateTime.now())));

        assertEquals(2, service.anzahlOffene());
    }


    @Test
    void offeneEinzelnBuildsAusfallTitelAusUrsprungsstunde() {
        Lehrkraft weber = lehrkraft(5L, "Werner", "Weber");
        Unterrichtsstunde us = stunde(101L, Wochentag.MONTAG, 1, raum(20L, "R101"), weber,
                fach("D", "Deutsch"), lerngruppe(10L, "5a"));
        Ausfall ausfallEvent = ausfall(100L, weber, "Krankheit", account(1L, "Frauke", "Lindner"));
        Aenderung a = new Aenderung();
        a.setId(2L);
        a.setUnterrichtsstunde(us);
        a.setAusloeser(ausfallEvent);
        a.setDatum(LocalDate.of(2026, 9, 22));
        a.setWochentagNeu(Wochentag.MONTAG);
        a.setZeitslotNeu(us.getZeitslot());
        a.setFachNeu(us.getFach());
        a.setLerngruppeNeu(us.getLerngruppe());
        a.setRaumNeu(us.getRaum());
        a.setLehrkraftNeu(weber);
        a.setOffen(true);
        a.setZeitstempel(LocalDateTime.of(2026, 9, 22, 12, 54));
        when(aenderungRepository.findById(2L)).thenReturn(Optional.of(a));

        OffeneAenderungDTO dto = service.offeneEinzeln(2L);

        assertEquals(EreignisTyp.AUSFALL, dto.getTyp());
        assertEquals("AUSFALL: D / 5a / WW – offen", dto.getTitel());
        assertEquals("AUSFALL am 2026-09-22, 1. Stunde (MONTAG, 08:00 Uhr), ursprünglich: D / R101 / WW (5a). "
                + "Neu: D / R101 / WW. Status: noch zu bestätigen. Erstellt: 2026-09-22T12:54.",
                dto.getBeschreibung());
        assertEquals(100L, dto.getEreignisId());
        assertEquals(101L, dto.getUnterrichtsstundeId());
    }

    @Test
    void offeneEinzelnZusatzstundeOhneUnterrichtsstundeNutztNeuFelder() {
        Lehrkraft vogel = lehrkraft(6L, "Anna", "Vogel");
        Lerngruppe klasse5a = lerngruppe(10L, "5a");
        Zusatzstunde zusatzstundeEvent = zusatzstunde(101L, klasse5a, account(1L, "Frauke", "Lindner"));
        Aenderung a = new Aenderung();
        a.setId(3L);
        a.setUnterrichtsstunde(null); // bewusst: keine Ursprungsstunde
        a.setDatum(LocalDate.of(2026, 9, 23));
        a.setLerngruppeNeu(klasse5a);
        a.setFachNeu(fach("INF", "Informatik"));
        a.setWochentagNeu(Wochentag.MITTWOCH);
        a.setZeitslotNeu(zeitslot(31L, 5));
        a.setRaumNeu(raum(21L, "INF1"));
        a.setLehrkraftNeu(vogel);
        a.setAusloeser(zusatzstundeEvent);
        a.setOffen(true);
        a.setZeitstempel(LocalDateTime.of(2026, 9, 23, 10, 0));
        when(aenderungRepository.findById(3L)).thenReturn(Optional.of(a));

        OffeneAenderungDTO dto = service.offeneEinzeln(3L);

        assertEquals("ZUSATZSTUNDE: INF / 5a / AV – offen", dto.getTitel());
        assertEquals("ZUSATZSTUNDE am 2026-09-23, 5. Stunde (MITTWOCH, 11:00 Uhr), "
                + "neu eingerichtete Stunde (keine Ursprungsstunde). "
                + "Neu: INF / INF1 / AV. Status: noch zu bestätigen. Erstellt: 2026-09-23T10:00.",
                dto.getBeschreibung());
        assertNull(dto.getUnterrichtsstundeId());
    }

    @Test
    void offeneEinzelnThrowsWhenNichtGefunden() {
        when(aenderungRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.offeneEinzeln(999L));
    }

    @Test
    void offeneEinzelnThrowsWennBereitsAbgeschlossen() {
        Aenderung geschlossen = ausfallAenderung(1L, false, LocalDateTime.now());
        when(aenderungRepository.findById(1L)).thenReturn(Optional.of(geschlossen));
        assertThrows(RuntimeException.class, () -> service.offeneEinzeln(1L));
    }


    @Test
    void alsErledigtMarkierenZeigtVertretungWennLehrkraftErsetztWurde() {
        Lehrkraft weber = lehrkraft(5L, "Werner", "Weber");
        Lehrkraft seidel = lehrkraft(7L, "Sonja", "Seidel");
        Unterrichtsstunde us = stunde(101L, Wochentag.MONTAG, 1, raum(20L, "R101"), weber,
                fach("D", "Deutsch"), lerngruppe(10L, "5a"));
        Ausfall ausfallEvent = ausfall(100L, weber, "Krankheit", account(1L, "Frauke", "Lindner"));
        Aenderung a = new Aenderung();
        a.setId(2L);
        a.setUnterrichtsstunde(us);
        a.setDatum(LocalDate.of(2026, 1, 5));
        a.setWochentagNeu(Wochentag.MONTAG);
        a.setZeitslotNeu(us.getZeitslot());
        a.setFachNeu(us.getFach());
        a.setLerngruppeNeu(us.getLerngruppe());
        a.setRaumNeu(us.getRaum());
        a.setLehrkraftNeu(seidel);
        a.setFreistunde(false);
        a.setAusloeser(ausfallEvent);
        a.setOffen(true);
        a.setZeitstempel(LocalDateTime.of(2020, 1, 1, 0, 0));
        when(aenderungRepository.findById(2L)).thenReturn(Optional.of(a));

        AbgeschlosseneAenderungDTO dto = service.alsErledigtMarkieren(2L);

        assertFalse(a.isOffen());
        assertEquals("Vertretung", dto.getAnzeigeTyp());
        assertEquals("Krankheit", dto.getGrund());
        assertEquals("Klasse 5a (Deutsch)", dto.getBetroffeneEinheit());
        assertEquals("Frauke Lindner", dto.getEingetragenVon());
        // "Erstellt: ..." haengt vom Zeitpunkt des Testlaufs ab (alsErledigtMarkieren setzt
        // den Zeitstempel neu), deshalb nur der deterministische Teil davor geprueft.
        assertTrue(dto.getAenderung().startsWith(
                "AUSFALL am 2026-01-05, 1. Stunde (MONTAG, 08:00 Uhr), ursprünglich: D / R101 / WW (5a). "
                        + "Neu: D / R101 / SS. Status: bestätigt."),
                "Beschreibung: " + dto.getAenderung());
        assertTrue(a.getZeitstempel().isAfter(LocalDateTime.of(2020, 1, 2, 0, 0)),
                "Zeitstempel muss beim Abschluss aktualisiert werden");
        verify(aenderungRepository).save(a);
    }

    @Test
    void alsErledigtMarkierenZeigtAusfallWennFreistunde() {
        Lehrkraft weber = lehrkraft(5L, "Werner", "Weber");
        Unterrichtsstunde us = stunde(101L, Wochentag.MONTAG, 1, raum(20L, "R101"), weber,
                fach("D", "Deutsch"), lerngruppe(10L, "5a"));
        Ausfall ausfallEvent = ausfall(100L, weber, "Krankheit", account(1L, "Frauke", "Lindner"));
        Aenderung a = new Aenderung();
        a.setId(2L);
        a.setUnterrichtsstunde(us);
        a.setDatum(LocalDate.of(2026, 1, 5));
        a.setWochentagNeu(Wochentag.MONTAG);
        a.setZeitslotNeu(us.getZeitslot());
        a.setFachNeu(us.getFach());
        a.setLerngruppeNeu(us.getLerngruppe());
        a.setRaumNeu(us.getRaum());
        a.setLehrkraftNeu(weber); // keine Vertretung gefunden, Original bleibt eingetragen
        a.setFreistunde(true);
        a.setAusloeser(ausfallEvent);
        a.setOffen(true);
        a.setZeitstempel(LocalDateTime.now());
        when(aenderungRepository.findById(2L)).thenReturn(Optional.of(a));

        AbgeschlosseneAenderungDTO dto = service.alsErledigtMarkieren(2L);

        assertEquals("Ausfall", dto.getAnzeigeTyp());
        assertTrue(dto.getAenderung().contains("Freistunde/Ausfall. Grund: Krankheit."),
                "Beschreibung: " + dto.getAenderung());
    }

    @Test
    void alsErledigtMarkierenThrowsWhenNichtGefunden() {
        when(aenderungRepository.findById(42L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.alsErledigtMarkieren(42L));
        verify(aenderungRepository, never()).save(any());
    }

    @Test
    void abgeschlosseneFiltertNachTyp() {
        Aenderung ausfallGeschlossen = geschlosseneAenderung(1L, EreignisTyp.AUSFALL, LocalDate.of(2026, 9, 1));
        Aenderung raumGeschlossen = geschlosseneAenderung(2L, EreignisTyp.RAUMAENDERUNG, LocalDate.of(2026, 9, 1));
        Aenderung nochOffen = ausfallAenderung(3L, true, LocalDateTime.now());
        when(aenderungRepository.findAll()).thenReturn(List.of(ausfallGeschlossen, raumGeschlossen, nochOffen));

        SeiteDTO<AbgeschlosseneAenderungDTO> result =
                service.abgeschlossene(null, EreignisTyp.AUSFALL, null, null, 0, 20);

        assertEquals(1, result.getInhalt().size());
        assertEquals(1L, result.getInhalt().get(0).getId());
    }

    @Test
    void abgeschlosseneFiltertNachZeitraum() {
        Aenderung imZeitraum = geschlosseneAenderung(1L, EreignisTyp.AUSFALL, LocalDate.of(2026, 9, 15));
        Aenderung davor = geschlosseneAenderung(2L, EreignisTyp.AUSFALL, LocalDate.of(2026, 8, 1));
        when(aenderungRepository.findAll()).thenReturn(List.of(imZeitraum, davor));

        SeiteDTO<AbgeschlosseneAenderungDTO> result = service.abgeschlossene(
                null, null, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), 0, 20);

        assertEquals(1, result.getInhalt().size());
        assertEquals(1L, result.getInhalt().get(0).getId());
    }

    @Test
    void abgeschlosseneFreitextsucheTrifftBetroffeneKlasse() {
        Aenderung a = geschlosseneAenderung(1L, EreignisTyp.AUSFALL, LocalDate.now());
        when(aenderungRepository.findAll()).thenReturn(List.of(a));

        assertEquals(1, service.abgeschlossene("5a", null, null, null, 0, 20).getInhalt().size());
        assertEquals(0, service.abgeschlossene("xyz123", null, null, null, 0, 20).getInhalt().size());
    }

    @Test
    void abgeschlosseneSeitenblaetternLiefertKorrekteSeitengroesse() {
        Aenderung a1 = geschlosseneAenderung(1L, EreignisTyp.AUSFALL, LocalDate.now());
        a1.setZeitstempel(LocalDateTime.of(2026, 1, 1, 0, 0));
        Aenderung a2 = geschlosseneAenderung(2L, EreignisTyp.AUSFALL, LocalDate.now());
        a2.setZeitstempel(LocalDateTime.of(2026, 1, 2, 0, 0));
        Aenderung a3 = geschlosseneAenderung(3L, EreignisTyp.AUSFALL, LocalDate.now());
        a3.setZeitstempel(LocalDateTime.of(2026, 1, 3, 0, 0));
        when(aenderungRepository.findAll()).thenReturn(List.of(a1, a2, a3));

        SeiteDTO<AbgeschlosseneAenderungDTO> seite0 = service.abgeschlossene(null, null, null, null, 0, 1);
        SeiteDTO<AbgeschlosseneAenderungDTO> seite2 = service.abgeschlossene(null, null, null, null, 2, 1);

        assertEquals(3, seite0.getGesamtElemente());
        assertEquals(3, seite0.getGesamtSeiten());
        assertEquals(1, seite0.getInhalt().size());
        assertEquals(3L, seite0.getInhalt().get(0).getId(), "neuester Zeitstempel zuerst");
        assertEquals(1L, seite2.getInhalt().get(0).getId());
    }

    @Test
    void abgeschlosseneEinzelnThrowsWennNochOffen() {
        Aenderung offen = ausfallAenderung(1L, true, LocalDateTime.now());
        when(aenderungRepository.findById(1L)).thenReturn(Optional.of(offen));
        assertThrows(RuntimeException.class, () -> service.abgeschlosseneEinzeln(1L));
    }


    @Test
    void ereignisverlaufMapptJedenTypUndFiltertNachTyp() {
        Account autor = account(1L, "Frauke", "Lindner");
        Ausfall ausfallEvent = ausfall(1L, lehrkraft(5L, "Werner", "Weber"), "Krankheit", autor);
        ausfallEvent.setStartdatum(LocalDate.of(2026, 9, 21));
        ausfallEvent.setErstellungsdatum(LocalDateTime.of(2026, 9, 22, 12, 0));

        Raumaenderung raumEvent = new Raumaenderung();
        raumEvent.setId(2L);
        raumEvent.setTyp(EreignisTyp.RAUMAENDERUNG);
        raumEvent.setRaum(raum(20L, "R101"));
        raumEvent.setAutor(autor);
        raumEvent.setStartdatum(LocalDate.of(2026, 9, 22));
        raumEvent.setErstellungsdatum(LocalDateTime.of(2026, 9, 22, 13, 0));

        Zusatzstunde zusatzEvent = zusatzstunde(3L, lerngruppe(10L, "5a"), autor);
        zusatzEvent.setStartdatum(LocalDate.of(2026, 9, 23));
        zusatzEvent.setErstellungsdatum(LocalDateTime.of(2026, 9, 22, 14, 0));

        when(ereignisRepository.findAll()).thenReturn(List.of(ausfallEvent, raumEvent, zusatzEvent));

        SeiteDTO<EreignisDTO> alle = service.ereignisverlauf(null, null, null, 0, 20);
        assertEquals(3, alle.getInhalt().size());
        assertEquals(3L, alle.getInhalt().get(0).getId(), "neueste Erstellung zuerst");

        EreignisDTO ausfallDto = alle.getInhalt().stream().filter(d -> d.getId().equals(1L)).findFirst().orElseThrow();
        assertEquals(5L, ausfallDto.getLehrkraftId());
        assertEquals("Krankheit", ausfallDto.getGrund());
        assertNull(ausfallDto.getRaumId());
        assertNull(ausfallDto.getLerngruppeId());
        assertEquals(1L, ausfallDto.getAutorId());

        EreignisDTO raumDto = alle.getInhalt().stream().filter(d -> d.getId().equals(2L)).findFirst().orElseThrow();
        assertEquals(20L, raumDto.getRaumId());
        assertNull(raumDto.getLehrkraftId());

        EreignisDTO zusatzDto = alle.getInhalt().stream().filter(d -> d.getId().equals(3L)).findFirst().orElseThrow();
        assertEquals(10L, zusatzDto.getLerngruppeId());

        SeiteDTO<EreignisDTO> nurRaum = service.ereignisverlauf(EreignisTyp.RAUMAENDERUNG, null, null, 0, 20);
        assertEquals(1, nurRaum.getInhalt().size());
        assertEquals(2L, nurRaum.getInhalt().get(0).getId());
    }


    private static Account account(Long id, String vorname, String name) {
        Account a = new Account();
        a.setId(id);
        a.setVorname(vorname);
        a.setName(name);
        return a;
    }

    private static Lehrkraft lehrkraft(Long id, String vorname, String name) {
        Lehrkraft l = new Lehrkraft();
        l.setId(id);
        l.setAccount(account(id + 1000, vorname, name));
        l.setKuerzel(("" + vorname.charAt(0) + name.charAt(0)).toUpperCase());
        return l;
    }

    private static Fach fach(String kuerzel, String name) {
        Fach f = new Fach();
        f.setKuerzel(kuerzel);
        f.setName(name);
        return f;
    }

    private static Lerngruppe lerngruppe(Long id, String bezeichnung) {
        Lerngruppe g = new Lerngruppe();
        g.setId(id);
        g.setBezeichnung(bezeichnung);
        return g;
    }

    private static Raum raum(Long id, String bezeichnung) {
        Raum r = new Raum();
        r.setId(id);
        r.setBezeichnung(bezeichnung);
        return r;
    }

    private static Zeitslot zeitslot(Long id, int nummer) {
        Zeitslot z = new Zeitslot();
        z.setId(id);
        z.setStundennummer(nummer);
        z.setStart(LocalTime.of(8, 0).plusMinutes((nummer - 1) * 45L));
        return z;
    }

    private static Unterrichtsstunde stunde(Long id, Wochentag tag, int stundennummer, Raum raum,
                                             Lehrkraft lehrkraft, Fach fach, Lerngruppe gruppe) {
        Unterrichtsstunde u = new Unterrichtsstunde();
        u.setId(id);
        u.setWochentag(tag);
        u.setZeitslot(zeitslot(id + 500, stundennummer));
        u.setRaum(raum);
        u.setLehrkraft(lehrkraft);
        u.setFach(fach);
        u.setLerngruppe(gruppe);
        return u;
    }

    private static Ausfall ausfall(Long id, Lehrkraft lehrkraft, String grund, Account autor) {
        Ausfall a = new Ausfall();
        a.setId(id);
        a.setTyp(EreignisTyp.AUSFALL);
        a.setLehrkraft(lehrkraft);
        a.setGrund(grund);
        a.setAutor(autor);
        return a;
    }

    private static Zusatzstunde zusatzstunde(Long id, Lerngruppe gruppe, Account autor) {
        Zusatzstunde z = new Zusatzstunde();
        z.setId(id);
        z.setTyp(EreignisTyp.ZUSATZSTUNDE);
        z.setLerngruppe(gruppe);
        z.setAutor(autor);
        return z;
    }

    /** Einfache offene/geschlossene Ausfall-Aenderung ohne Details, nur für Zähl-/Sortiertests. */
    private static Aenderung ausfallAenderung(Long id, boolean offen, LocalDateTime zeitstempel) {
        Lehrkraft weber = lehrkraft(5L, "Werner", "Weber");
        Fach deutsch = fach("D", "Deutsch");
        Lerngruppe klasse5a = lerngruppe(10L, "5a");
        Raum raum101 = raum(20L, "R101");
        Unterrichtsstunde us = stunde(id + 100, Wochentag.MONTAG, 1, raum101, weber, deutsch, klasse5a);
        Aenderung a = new Aenderung();
        a.setId(id);
        a.setUnterrichtsstunde(us);
        a.setAusloeser(ausfall(id + 900, weber, "Krankheit", account(1L, "Frauke", "Lindner")));
        a.setOffen(offen);
        a.setDatum(LocalDate.now());
        a.setZeitstempel(zeitstempel);
        // Generieren.aenderungPlanerTitelGenerieren/-BeschreibungGenerieren lesen diese Felder
        // unbedingt, ohne Rueckgriff auf die Ursprungsstunde.
        a.setWochentagNeu(Wochentag.MONTAG);
        a.setZeitslotNeu(us.getZeitslot());
        a.setFachNeu(deutsch);
        a.setLerngruppeNeu(klasse5a);
        a.setRaumNeu(raum101);
        a.setLehrkraftNeu(weber);
        return a;
    }

    private static Aenderung geschlosseneAenderung(Long id, EreignisTyp typ, LocalDate datum) {
        Lerngruppe klasse5a = lerngruppe(10L, "5a");
        Ereignis ausloeser = switch (typ) {
            case AUSFALL -> ausfall(id + 900, lehrkraft(5L, "Werner", "Weber"), "Krankheit",
                    account(1L, "Frauke", "Lindner"));
            case RAUMAENDERUNG -> {
                Raumaenderung ra = new Raumaenderung();
                ra.setId(id + 900);
                ra.setTyp(EreignisTyp.RAUMAENDERUNG);
                ra.setAutor(account(1L, "Frauke", "Lindner"));
                yield ra;
            }
            case ZUSATZSTUNDE -> zusatzstunde(id + 900, klasse5a, account(1L, "Frauke", "Lindner"));
        };
        Lehrkraft weber = lehrkraft(5L, "Werner", "Weber");
        Fach deutsch = fach("D", "Deutsch");
        Raum raum101 = raum(20L, "R101");
        Unterrichtsstunde us = stunde(id + 100, Wochentag.MONTAG, 1, raum101, weber, deutsch, klasse5a);
        Aenderung a = new Aenderung();
        a.setId(id);
        a.setUnterrichtsstunde(us);
        a.setAusloeser(ausloeser);
        a.setOffen(false);
        a.setDatum(datum);
        a.setZeitstempel(LocalDateTime.now());
        a.setWochentagNeu(Wochentag.MONTAG);
        a.setZeitslotNeu(us.getZeitslot());
        a.setFachNeu(deutsch);
        a.setLerngruppeNeu(klasse5a);
        a.setRaumNeu(raum101);
        a.setLehrkraftNeu(weber);
        return a;
    }

}