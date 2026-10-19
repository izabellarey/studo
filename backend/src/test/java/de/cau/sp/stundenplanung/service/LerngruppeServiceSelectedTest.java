package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.*;
import de.cau.sp.stundenplanung.dto.stundenplan.StundenplanDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LerngruppeServiceSelectedTest {

    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock JahrgangsstufeRepository jahrgangsstufeRepository;
    @Mock AccountRepository accountRepository;
    @Mock RaumRepository raumRepository;
    @Mock StundenplanRepository stundenplanRepository;
    @Mock AenderungRepository aenderungRepository;
    @Mock UnterrichtsstundeRepository unterrichtsstundeRepository;
    @Mock LehrkraftRepository lehrkraftRepository;
    @Mock LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    @Mock SchuelerRepository schuelerRepository;
    @Mock LehrkraftService lehrkraftService;
    @Mock RaumService raumService;

    @InjectMocks LerngruppeService service;

    @Test
    void klasseAnlegenErstelltAktiveKlasseMitJahrgangKlassenleitungUndRaum() {
        Jahrgangsstufe jahrgang = new Jahrgangsstufe();
        jahrgang.setId(5L);
        jahrgang.setNummer(7);
        Lehrkraft klassenleitung = teacher(10L, "MU");
        klassenleitung.setAccount(account("Max", "Mustermann"));
        Raum klassenzimmer = room(20L, "A101", Raumart.KLASSENRAUM, 30);

        when(jahrgangsstufeRepository.findById(5L)).thenReturn(Optional.of(jahrgang));
        when(lehrkraftService.zufaelligeLehrkraftFuerKlassenleitung()).thenReturn(klassenleitung);
        when(raumService.zufaelligerRaumAlsKlassenzimmer()).thenReturn(klassenzimmer);
        when(lerngruppeRepository.save(any(Lerngruppe.class))).thenAnswer(inv -> {
            Lerngruppe g = inv.getArgument(0);
            g.setId(99L);
            return g;
        });

        LerngruppeDTO result = service.klasseAnlegen(5L, new KlasseDTO("7a"));

        ArgumentCaptor<Lerngruppe> captor = ArgumentCaptor.forClass(Lerngruppe.class);
        verify(lerngruppeRepository).save(captor.capture());
        Lerngruppe saved = captor.getValue();

        assertEquals(99L, result.getLerngruppeId());
        assertEquals("7a", result.getBezeichnung());
        assertEquals("Max", result.getLehrkraftVorname());
        assertEquals("Mustermann", result.getLehrkraftNachname());
        assertEquals(0, result.getSchueleranzahl());
        assertEquals("A101", result.getKlassenzimmer());
        assertTrue(saved.isAktiv());
        assertEquals(LerngruppenArt.KLASSE, saved.getArt());
        assertSame(jahrgang, saved.getJahrgangsstufe());
        assertSame(klassenleitung, saved.getKlassenleitung());
        assertSame(klassenzimmer, saved.getKlassenzimmer());
    }

    @Test
    void klasseAnlegenWirftExceptionWennJahrgangNichtExistiert() {
        when(jahrgangsstufeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.klasseAnlegen(99L, new KlasseDTO("7a")));
        verifyNoInteractions(lehrkraftService, raumService, lerngruppeRepository);
    }

    @Test
    void klasseDeaktivierenDeaktiviertKlasseEntferntSchuelerUndArchiviertPlan() {
        Lerngruppe klasse = group(1L, "7a");
        klasse.setAktiv(true);
        klasse.setKlassenzimmer(room(2L, "A101", Raumart.KLASSENRAUM, 30));
        klasse.setKlassenleitung(teacher(3L, "MU"));

        Account a1 = new Account();
        a1.setAktiv(true);
        Schueler s1 = new Schueler();
        s1.setLerngruppe(klasse);
        s1.setAccount(a1);

        Account a2 = new Account();
        a2.setAktiv(true);
        Schueler s2 = new Schueler();
        s2.setLerngruppe(klasse);
        s2.setAccount(a2);

        Stundenplan plan = new Stundenplan();
        plan.setStatus(PlanStatus.AKTIV);

        when(lerngruppeRepository.findById(1L)).thenReturn(Optional.of(klasse));
        when(schuelerRepository.findByLerngruppe(klasse)).thenReturn(List.of(s1, s2));
        when(stundenplanRepository.findByLerngruppeAndStatus(klasse, PlanStatus.AKTIV))
                .thenReturn(Optional.of(plan));

        service.klasseDeaktivieren(1L);

        assertFalse(klasse.isAktiv());
        assertNull(klasse.getKlassenzimmer());
        assertNull(klasse.getKlassenleitung());
        assertNull(s1.getLerngruppe());
        assertNull(s2.getLerngruppe());
        assertFalse(a1.isAktiv());
        assertFalse(a2.isAktiv());
        assertEquals(PlanStatus.ARCHIVIERT, plan.getStatus());
        verify(schuelerRepository).save(s1);
        verify(schuelerRepository).save(s2);
        verify(stundenplanRepository).save(plan);
        verify(lerngruppeRepository).save(klasse);
    }

    @Test
    void klasseDeaktivierenDeaktiviertKlasseAuchOhneAktivenStundenplan() {
        Lerngruppe klasse = group(1L, "7a");

        when(lerngruppeRepository.findById(1L))
                .thenReturn(Optional.of(klasse));

        when(schuelerRepository.findByLerngruppe(klasse))
                .thenReturn(List.of());

        when(stundenplanRepository.findByLerngruppeAndStatus(
                klasse, PlanStatus.AKTIV))
                .thenReturn(Optional.empty());

        service.klasseDeaktivieren(1L);

        assertFalse(klasse.isAktiv());
        assertNull(klasse.getKlassenzimmer());
        assertNull(klasse.getKlassenleitung());

        verify(lerngruppeRepository).save(klasse);
        verify(stundenplanRepository, never()).save(any(Stundenplan.class));
    }

    @Test
    void klasseBearbeitenAendertNurDieBezeichnungUndSpeichert() {
        Lerngruppe klasse = group(1L, "7a");
        Lehrkraft head = teacher(2L, "MU");
        head.setAccount(account("Max", "Mustermann"));
        klasse.setAktiv(true);
        klasse.setKlassenleitung(head);
        klasse.setSchueleranzahl(25);
        klasse.setKlassenzimmer(room(3L, "A101", Raumart.KLASSENRAUM, 30));

        when(lerngruppeRepository.findById(1L)).thenReturn(Optional.of(klasse));
        when(lerngruppeRepository.save(klasse)).thenReturn(klasse);

        LerngruppeDTO result = service.klasseBearbeiten(1L, new LerngruppeBearbeitenDTO("7b"));

        assertEquals("7b", klasse.getBezeichnung());
        assertEquals("7b", result.getBezeichnung());
        assertEquals("Max", result.getLehrkraftVorname());
        assertEquals(25, result.getSchueleranzahl());
        verify(lerngruppeRepository).save(klasse);
    }

    @Test
    void klasseBearbeitenWirftExceptionWennKlasseNichtExistiert() {
        when(lerngruppeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.klasseBearbeiten(99L, new LerngruppeBearbeitenDTO("7b")));
        verify(lerngruppeRepository, never()).save(any(Lerngruppe.class));
    }

    @Test
    void aenderungBestaetigenSetztAusgewaehlteLehrkraftUndRaumUndSchliesstAenderung() {
        Aenderung aenderung = baseChange();
        Lehrkraft alteLehrkraft = aenderung.getLehrkraftNeu();
        Raum alterRaum = aenderung.getRaumNeu();
        Lehrkraft neueLehrkraft = teacher(20L, "NEU");
        Raum neuerRaum = room(21L, "B201", Raumart.KLASSENRAUM, 30);

        when(aenderungRepository.findById(50L)).thenReturn(Optional.of(aenderung));
        when(lehrkraftRepository.findById(20L)).thenReturn(Optional.of(neueLehrkraft));
        when(raumRepository.findById(21L)).thenReturn(Optional.of(neuerRaum));

        AenderungAuswahlDTO auswahl = new AenderungAuswahlDTO(
                new RaumAlternativeDTO("B201", 21L, false),
                new LehrkraftAlternativeDTO("Neu", "Lehrer", 20L, true)
        );

        UnterrichtsstundeDTO result = service.aenderungBestaetigen(50L, auswahl);

        assertSame(neueLehrkraft, aenderung.getLehrkraftNeu());
        assertSame(neuerRaum, aenderung.getRaumNeu());
        assertFalse(aenderung.isOffen());
        assertEquals(50L, result.getId());
        assertEquals("NEU", result.getLehrkraftKuerzel());
        assertEquals("B201", result.getRaumBezeichnung());
        assertFalse(result.isOffeneAenderung());
        assertEquals(EreignisTyp.AUSFALL, result.getGrund());
        assertNotSame(alteLehrkraft, aenderung.getLehrkraftNeu());
        assertNotSame(alterRaum, aenderung.getRaumNeu());
        verify(aenderungRepository, atLeast(1)).save(aenderung);
    }

    @Test
    void aenderungBestaetigenSpeichertNichtUnnoetigNeuWennLehrkraftUndRaumBereitsGewaehlteSind() {
        Aenderung aenderung = baseChange();
        Lehrkraft lehrkraft = aenderung.getLehrkraftNeu();
        Raum raum = aenderung.getRaumNeu();
        when(aenderungRepository.findById(50L)).thenReturn(Optional.of(aenderung));

        AenderungAuswahlDTO auswahl = new AenderungAuswahlDTO(
                new RaumAlternativeDTO("A101", raum.getId(), false),
                new LehrkraftAlternativeDTO("Max", "Mustermann", lehrkraft.getId(), true)
        );

        service.aenderungBestaetigen(50L, auswahl);

        assertFalse(aenderung.isOffen());
        verify(lehrkraftRepository, never()).findById(anyLong());
        verify(raumRepository, never()).findById(anyLong());
        verify(aenderungRepository).save(aenderung);
    }

    @Test
    void aenderungBestaetigenWirftExceptionWennAenderungNichtExistiert() {
        when(aenderungRepository.findById(99L)).thenReturn(Optional.empty());

        AenderungAuswahlDTO auswahl = new AenderungAuswahlDTO(
                new RaumAlternativeDTO("A101", 1L, false),
                new LehrkraftAlternativeDTO("Max", "Mustermann", 2L, true)
        );

        assertThrows(RuntimeException.class, () -> service.aenderungBestaetigen(99L, auswahl));
        verify(aenderungRepository, never()).save(any(Aenderung.class));
    }

    @Test
    void alleKlassenFiltertNurLerngruppenMitArtKlasse() {
        Jahrgangsstufe y7 = new Jahrgangsstufe();
        y7.setNummer(7);
        Jahrgangsstufe y8 = new Jahrgangsstufe();
        y8.setNummer(8);

        Lerngruppe klasse = group(1L, "7a");
        klasse.setArt(LerngruppenArt.KLASSE);
        klasse.setJahrgangsstufe(y7);

        Lerngruppe ag = group(2L, "AG Robotik");
        ag.setArt(LerngruppenArt.AG);
        ag.setJahrgangsstufe(y8);

        when(lerngruppeRepository.findAllByAktivTrue()).thenReturn(List.of(klasse, ag));

        List<LerngruppeUebersichtDTO> result = service.alleKlassen();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("7a", result.get(0).getBezeichnung());
        assertEquals(7, result.get(0).getJahrgangsnummer());
    }

    @Test
    void alleKlassenGibtLeereListeZurueckWennKeineKlasseVorhandenIst() {
        Lerngruppe ag = group(2L, "AG Robotik");
        ag.setArt(LerngruppenArt.AG);
        Jahrgangsstufe y8 = new Jahrgangsstufe();
        y8.setNummer(8);
        ag.setJahrgangsstufe(y8);
        when(lerngruppeRepository.findAllByAktivTrue()).thenReturn(List.of(ag));

        assertTrue(service.alleKlassen().isEmpty());
    }

    private static Aenderung baseChange() {
        Aenderung a = new Aenderung();
        a.setId(50L);
        a.setWochentagNeu(Wochentag.MONTAG);
        a.setZeitslotNeu(slot(1L, 1));
        a.setFachNeu(fach("MAT"));
        a.setLehrkraftNeu(teacher(10L, "ALT"));
        a.setRaumNeu(room(11L, "A101", Raumart.KLASSENRAUM, 30));
        a.setLerngruppeNeu(group(12L, "7a"));
        a.setFreistunde(false);
        a.setOffen(true);
        Ereignis e = new Ereignis();
        e.setTyp(EreignisTyp.AUSFALL);
        a.setAusloeser(e);
        return a;
    }

    private static Account account(String vorname, String name) {
        Account a = new Account();
        a.setVorname(vorname);
        a.setName(name);
        return a;
    }

    private static Lerngruppe group(Long id, String name) {
        Lerngruppe g = new Lerngruppe();
        g.setId(id);
        g.setBezeichnung(name);
        return g;
    }

    private static Lehrkraft teacher(Long id, String code) {
        Lehrkraft l = new Lehrkraft();
        l.setId(id);
        l.setKuerzel(code);
        return l;
    }

    private static Raum room(Long id, String name, Raumart art, int cap) {
        Raum r = new Raum();
        r.setId(id);
        r.setBezeichnung(name);
        r.setArt(art);
        r.setKapazitaet(cap);
        return r;
    }

    private static Fach fach(String code) {
        Fach f = new Fach();
        f.setId((long) code.hashCode());
        f.setKuerzel(code);
        f.setName(code);
        return f;
    }

    private static Zeitslot slot(Long id, int number) {
        Zeitslot z = new Zeitslot();
        z.setId(id);
        z.setStundennummer(number);
        z.setStart(LocalTime.of(8, 0));
        z.setEnde(LocalTime.of(8, 45));
        return z;
    }
    @Test
    void archivierteStundenplaeneGibtArchivierteStundenplaeneAlsDTOsZurueck() {
        Lerngruppe lerngruppe = new Lerngruppe();
        lerngruppe.setId(1L);
        lerngruppe.setBezeichnung("7a");

        Account ersteller = new Account();
        ersteller.setVorname("Max");
        ersteller.setName("Mustermann");

        Stundenplan stundenplan = new Stundenplan();
        stundenplan.setId(1L);
        stundenplan.setSchuljahr("2025/26");
        stundenplan.setHalbjahr(1);
        stundenplan.setLerngruppe(lerngruppe);
        stundenplan.setStartdatum(LocalDate.of(2025, 9, 1));
        stundenplan.setEnddatum(LocalDate.of(2026, 1, 31));
        stundenplan.setErsteller(ersteller);
        stundenplan.setStatus(PlanStatus.ARCHIVIERT);

        Fach fach = new Fach();
        fach.setId(1L);
        fach.setKuerzel("MAT");

        Zeitslot zeitslot = new Zeitslot();
        zeitslot.setId(1L);
        zeitslot.setStundennummer(1);
        zeitslot.setStart(LocalTime.of(8, 0));
        zeitslot.setEnde(LocalTime.of(8, 45));

        Raum raum = new Raum();
        raum.setId(1L);
        raum.setBezeichnung("R101");

        Account lehrkraftAccount = new Account();
        lehrkraftAccount.setVorname("Anna");
        lehrkraftAccount.setName("Lehrerin");

        Lehrkraft lehrkraft = new Lehrkraft();
        lehrkraft.setId(1L);
        lehrkraft.setKuerzel("AL");
        lehrkraft.setAccount(lehrkraftAccount);

        Unterrichtsstunde stunde = new Unterrichtsstunde();
        stunde.setId(1L);
        stunde.setStundenplan(stundenplan);
        stunde.setLerngruppe(lerngruppe);
        stunde.setFach(fach);
        stunde.setZeitslot(zeitslot);
        stunde.setRaum(raum);
        stunde.setLehrkraft(lehrkraft);
        stunde.setWochentag(Wochentag.MONTAG);

        stundenplan.setUnterrichtsstunden(List.of(stunde));

        when(stundenplanRepository.findByStatusWithStunden(PlanStatus.ARCHIVIERT))
                .thenReturn(List.of(stundenplan));

        List<StundenplanDTO> result = service.archivierteStundenplaene();

        assertEquals(1, result.size());
        assertEquals("7a", result.get(0).getLerngruppeBezeichnung());
        assertEquals("2025/26", result.get(0).getSchuljahr());
        assertEquals(1, result.get(0).getHalbjahr());

        verify(stundenplanRepository)
                .findByStatusWithStunden(PlanStatus.ARCHIVIERT);
    }

    @Test
    void archivierteStundenplaeneGibtLeereListeZurueckWennKeineVorhandenSind() {
        when(stundenplanRepository.findByStatusWithStunden(PlanStatus.ARCHIVIERT))
                .thenReturn(List.of());

        List<StundenplanDTO> result = service.archivierteStundenplaene();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(stundenplanRepository)
                .findByStatusWithStunden(PlanStatus.ARCHIVIERT);
    }
    @Test
    void jahreswechselWirftExceptionWennKeineHoehereJahrgangsstufeExistiert() {
        Jahrgangsstufe jahrgang6 = new Jahrgangsstufe();
        jahrgang6.setNummer(6);

        Lerngruppe klasse = new Lerngruppe();
        klasse.setId(1L);
        klasse.setBezeichnung("6a");
        klasse.setAktiv(true);
        klasse.setJahrgangsstufe(jahrgang6);

        when(stundenplanRepository.findByStatus(PlanStatus.AKTIV))
                .thenReturn(List.of());

        when(lerngruppeRepository.findAllByAktivTrue())
                .thenReturn(List.of(klasse));

        when(jahrgangsstufeRepository
                .findFirstByNummerGreaterThanOrderByNummerAsc(6))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> service.jahreswechsel()
        );

        verify(lerngruppeRepository, never()).save(klasse);
    }
    @Test
    void jahreswechselDeaktiviertZehnteKlasse() {
        Jahrgangsstufe jahrgang10 = new Jahrgangsstufe();
        jahrgang10.setNummer(10);

        Lerngruppe klasse = new Lerngruppe();
        klasse.setId(1L);
        klasse.setBezeichnung("10a");
        klasse.setAktiv(true);
        klasse.setJahrgangsstufe(jahrgang10);

        when(stundenplanRepository.findByStatus(PlanStatus.AKTIV))
                .thenReturn(List.of());

        when(lerngruppeRepository.findAllByAktivTrue())
                .thenReturn(List.of(klasse));

        // wird von jahreswechsel() indirekt über klasseDeaktivieren() benötigt
        when(lerngruppeRepository.findById(1L))
                .thenReturn(Optional.of(klasse));

        when(schuelerRepository.findByLerngruppe(klasse))
                .thenReturn(List.of());

        when(stundenplanRepository.findByLerngruppeAndStatus(
                klasse, PlanStatus.AKTIV))
                .thenReturn(Optional.empty());

        service.jahreswechsel();

        assertFalse(klasse.isAktiv());
        assertNull(klasse.getKlassenzimmer());
        assertNull(klasse.getKlassenleitung());

        verify(lerngruppeRepository).save(klasse);
    }
    @Test
    void jahreswechselArchiviertAktiveStundenplaeneUndStuftenLerngruppenHoch() {
        Stundenplan stundenplan = new Stundenplan();
        stundenplan.setStatus(PlanStatus.AKTIV);

        Jahrgangsstufe jahrgang6 = new Jahrgangsstufe();
        jahrgang6.setNummer(6);

        Jahrgangsstufe jahrgang7 = new Jahrgangsstufe();
        jahrgang7.setNummer(7);

        Lerngruppe klasse = new Lerngruppe();
        klasse.setId(1L);
        klasse.setBezeichnung("6a");
        klasse.setAktiv(true);
        klasse.setJahrgangsstufe(jahrgang6);

        when(stundenplanRepository.findByStatus(PlanStatus.AKTIV))
                .thenReturn(List.of(stundenplan));

        when(lerngruppeRepository.findAllByAktivTrue())
                .thenReturn(List.of(klasse));

        when(jahrgangsstufeRepository
                .findFirstByNummerGreaterThanOrderByNummerAsc(6))
                .thenReturn(Optional.of(jahrgang7));

        service.jahreswechsel();

        assertEquals(PlanStatus.ARCHIVIERT, stundenplan.getStatus());
        assertEquals(LocalDate.now(), stundenplan.getEnddatum());

        assertEquals(jahrgang7, klasse.getJahrgangsstufe());
        assertEquals("7a", klasse.getBezeichnung());

        verify(stundenplanRepository).save(stundenplan);
        verify(lerngruppeRepository).save(klasse);
    }
}
