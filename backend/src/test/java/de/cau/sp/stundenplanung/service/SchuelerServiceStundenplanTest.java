package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchuelerServiceStundenplanTest {

    @Mock SchuelerRepository schuelerRepository;
    @Mock AccountRepository accountRepository;
    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock Generieren generieren;
    @Mock AccountService accountService;
    @Mock StundenplanRepository stundenplanRepository;
    @Mock AenderungRepository aenderungRepository;

    @InjectMocks SchuelerService service;

    @Test
    void klassenStundenplanGibtNormaleStundenSortiertZurueck() {
        Account account = account(1L, "schueler");
        Lerngruppe klasse = gruppe(2L, "7a");
        Schueler schueler = new Schueler();
        schueler.setAccount(account);
        schueler.setLerngruppe(klasse);

        Unterrichtsstunde montag = stunde(1L, Wochentag.MONTAG, slot(1L, 1), "MAT", "A101", "MU", "7a");
        Unterrichtsstunde dienstag = stunde(2L, Wochentag.DIENSTAG, slot(2L, 2), "DEU", "A102", "ME", "7a");
        Stundenplan plan = new Stundenplan();
        plan.setUnterrichtsstunden(List.of(dienstag, montag));

        when(accountRepository.findByBenutzername("schueler")).thenReturn(Optional.of(account));
        when(schuelerRepository.findByAccount(account)).thenReturn(Optional.of(schueler));
        when(stundenplanRepository.findByLerngruppeAndStatus(klasse, PlanStatus.AKTIV)).thenReturn(Optional.of(plan));
        when(aenderungRepository.findAbgeschlosseneByLerngruppeNeuAndDatumBetween(
                eq(klasse), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        List<UnterrichtsstundeDTO> result = service.klassenStundenplan("schueler");

        assertEquals(2, result.size());
        assertEquals(Wochentag.MONTAG, result.get(0).getWochentag());
        assertEquals(1, result.get(0).getStundennummer());
        assertEquals("MAT", result.get(0).getFachName());
        assertFalse(result.get(0).isGeaendert());
        assertFalse(result.get(0).isFreistunde());
        assertEquals(Wochentag.DIENSTAG, result.get(1).getWochentag());
        assertEquals(2, result.get(1).getStundennummer());
    }

    @Test
    void klassenStundenplanErsetztNormaleStundeDurchAbgeschlosseneAenderung() {
        Account account = account(1L, "schueler");
        Lerngruppe klasse = gruppe(2L, "7a");
        Schueler schueler = new Schueler();
        schueler.setAccount(account);
        schueler.setLerngruppe(klasse);

        Zeitslot slot = slot(1L, 1);
        Unterrichtsstunde original = stunde(10L, Wochentag.MONTAG, slot, "MAT", "A101", "MU", "7a");
        Stundenplan plan = new Stundenplan();
        plan.setUnterrichtsstunden(List.of(original));

        Aenderung aenderung = new Aenderung();
        aenderung.setId(50L);
        aenderung.setUnterrichtsstunde(original);
        aenderung.setWochentagNeu(Wochentag.MONTAG);
        aenderung.setZeitslotNeu(slot);
        aenderung.setFachNeu(fach("BIO"));
        aenderung.setRaumNeu(raum(3L, "B201"));
        aenderung.setLehrkraftNeu(lehrkraft(4L, "BIO"));
        aenderung.setLerngruppeNeu(klasse);
        aenderung.setFreistunde(false);
        aenderung.setOffen(false);
        Ereignis ausloeser = new Ereignis();
        ausloeser.setTyp(EreignisTyp.RAUMAENDERUNG);
        aenderung.setAusloeser(ausloeser);

        when(accountRepository.findByBenutzername("schueler")).thenReturn(Optional.of(account));
        when(schuelerRepository.findByAccount(account)).thenReturn(Optional.of(schueler));
        when(stundenplanRepository.findByLerngruppeAndStatus(klasse, PlanStatus.AKTIV)).thenReturn(Optional.of(plan));
        when(aenderungRepository.findAbgeschlosseneByLerngruppeNeuAndDatumBetween(
                eq(klasse), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(aenderung));

        List<UnterrichtsstundeDTO> result = service.klassenStundenplan("schueler");

        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).getId());
        assertEquals("BIO", result.get(0).getFachName());
        assertEquals("B201", result.get(0).getRaumBezeichnung());
        assertEquals("BIO", result.get(0).getLehrkraftKuerzel());
        assertTrue(result.get(0).isGeaendert());
        assertFalse(result.get(0).isFreistunde());
        assertFalse(result.get(0).isOffeneAenderung());
        assertEquals(EreignisTyp.RAUMAENDERUNG, result.get(0).getGrund());
    }

    @Test
    void klassenStundenplanWirftExceptionWennAccountNichtExistiert() {
        when(accountRepository.findByBenutzername("unbekannt")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.klassenStundenplan("unbekannt"));
        verifyNoInteractions(schuelerRepository, stundenplanRepository, aenderungRepository);
    }

    @Test
    void klassenStundenplanWirftExceptionWennKeinAktiverPlanExistiert() {
        Account account = account(1L, "schueler");
        Lerngruppe klasse = gruppe(2L, "7a");
        Schueler schueler = new Schueler();
        schueler.setAccount(account);
        schueler.setLerngruppe(klasse);

        when(accountRepository.findByBenutzername("schueler")).thenReturn(Optional.of(account));
        when(schuelerRepository.findByAccount(account)).thenReturn(Optional.of(schueler));
        when(stundenplanRepository.findByLerngruppeAndStatus(klasse, PlanStatus.AKTIV)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.klassenStundenplan("schueler"));
        verifyNoInteractions(aenderungRepository);
    }

    private static Account account(Long id, String username) {
        Account a = new Account();
        a.setId(id);
        a.setBenutzername(username);
        a.setName("Mustermann");
        a.setVorname("Max");
        a.setAktiv(true);
        return a;
    }

    private static Lerngruppe gruppe(Long id, String name) {
        Lerngruppe g = new Lerngruppe();
        g.setId(id);
        g.setBezeichnung(name);
        return g;
    }

    private static Unterrichtsstunde stunde(Long id, Wochentag tag, Zeitslot slot,
                                            String fachCode, String raumName,
                                            String lehrkraftCode, String gruppeName) {
        Unterrichtsstunde u = new Unterrichtsstunde();
        u.setId(id);
        u.setWochentag(tag);
        u.setZeitslot(slot);
        u.setFach(fach(fachCode));
        u.setRaum(raum(1L, raumName));
        u.setLehrkraft(lehrkraft(1L, lehrkraftCode));
        u.setLerngruppe(gruppe(2L, gruppeName));
        return u;
    }

    private static Zeitslot slot(Long id, int number) {
        Zeitslot z = new Zeitslot();
        z.setId(id);
        z.setStundennummer(number);
        z.setStart(LocalTime.of(8, 0));
        z.setEnde(LocalTime.of(8, 45));
        return z;
    }

    private static Fach fach(String code) {
        Fach f = new Fach();
        f.setId((long) code.hashCode());
        f.setKuerzel(code);
        f.setName(code);
        return f;
    }

    private static Raum raum(Long id, String name) {
        Raum r = new Raum();
        r.setId(id);
        r.setBezeichnung(name);
        return r;
    }

    private static Lehrkraft lehrkraft(Long id, String code) {
        Lehrkraft l = new Lehrkraft();
        l.setId(id);
        l.setKuerzel(code);
        return l;
    }
}
