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
class LehrkraftServiceStundenplanTest {

    @Mock LehrkraftRepository lehrkraftRepository;
    @Mock AccountRepository accountRepository;
    @Mock Generieren generieren;
    @Mock LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock FachRepository fachRepository;
    @Mock AccountService accountService;
    @Mock UnterrichtsstundeRepository unterrichtsstundeRepository;
    @Mock AenderungRepository aenderungRepository;

    @InjectMocks LehrkraftService service;

    @Test
    void persoenlicherStundenplanGibtNormaleStundenSortiertZurueck() {
        Account account = account(1L, "mmustermann");
        Lehrkraft lehrkraft = lehrkraft(2L, "MU");

        Unterrichtsstunde dienstag = stunde(2L, Wochentag.DIENSTAG, slot(2L, 2), "DEU", "A102", lehrkraft, "7a");
        Unterrichtsstunde montag = stunde(1L, Wochentag.MONTAG, slot(1L, 1), "MAT", "A101", lehrkraft, "7a");

        when(accountRepository.findByBenutzername("mmustermann")).thenReturn(Optional.of(account));
        when(lehrkraftRepository.findByAccount(account)).thenReturn(Optional.of(lehrkraft));
        when(unterrichtsstundeRepository.findByLehrkraftAndStundenplanStatus(lehrkraft, PlanStatus.AKTIV))
                .thenReturn(List.of(dienstag, montag));
        when(aenderungRepository.findAbgeschlosseneByLehrkraftNeuAndDatumBetween(
                eq(lehrkraft), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        List<UnterrichtsstundeDTO> result = service.persoenlicherStundenplan("mmustermann");

        assertEquals(2, result.size());
        assertEquals(Wochentag.MONTAG, result.get(0).getWochentag());
        assertEquals(1, result.get(0).getStundennummer());
        assertEquals("MAT", result.get(0).getFachName());
        assertFalse(result.get(0).isGeaendert());
        assertEquals(Wochentag.DIENSTAG, result.get(1).getWochentag());
    }

    @Test
    void persoenlicherStundenplanUeberschreibtNormaleStundeMitAbgeschlossenerAenderung() {
        Account account = account(1L, "mmustermann");
        Lehrkraft lehrkraft = lehrkraft(2L, "MU");
        Lerngruppe gruppe = gruppe(3L, "7a");
        Zeitslot slot = slot(1L, 1);
        Unterrichtsstunde original = stunde(10L, Wochentag.MONTAG, slot, "MAT", "A101", lehrkraft, "7a");
        original.setLerngruppe(gruppe);

        Aenderung aenderung = new Aenderung();
        aenderung.setId(50L);
        aenderung.setUnterrichtsstunde(original);
        aenderung.setWochentagNeu(Wochentag.MONTAG);
        aenderung.setZeitslotNeu(slot);
        aenderung.setFachNeu(fach("BIO"));
        aenderung.setRaumNeu(raum(4L, "B201"));
        aenderung.setLehrkraftNeu(lehrkraft(5L, "BI"));
        aenderung.setLerngruppeNeu(gruppe);
        aenderung.setFreistunde(false);
        Ereignis ausloeser = new Ereignis();
        ausloeser.setTyp(EreignisTyp.AUSFALL);
        aenderung.setAusloeser(ausloeser);

        when(accountRepository.findByBenutzername("mmustermann")).thenReturn(Optional.of(account));
        when(lehrkraftRepository.findByAccount(account)).thenReturn(Optional.of(lehrkraft));
        when(unterrichtsstundeRepository.findByLehrkraftAndStundenplanStatus(lehrkraft, PlanStatus.AKTIV))
                .thenReturn(List.of(original));
        when(aenderungRepository.findAbgeschlosseneByLehrkraftNeuAndDatumBetween(
                eq(lehrkraft), any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(aenderung));

        List<UnterrichtsstundeDTO> result = service.persoenlicherStundenplan("mmustermann");

        assertEquals(1, result.size());
        assertEquals(50L, result.get(0).getId());
        assertEquals("BIO", result.get(0).getFachName());
        assertEquals("B201", result.get(0).getRaumBezeichnung());
        assertEquals("BI", result.get(0).getLehrkraftKuerzel());
        assertTrue(result.get(0).isGeaendert());
        assertEquals(EreignisTyp.AUSFALL, result.get(0).getGrund());
    }

    @Test
    void persoenlicherStundenplanWirftExceptionWennAccountNichtExistiert() {
        when(accountRepository.findByBenutzername("unbekannt")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.persoenlicherStundenplan("unbekannt"));
        verifyNoInteractions(lehrkraftRepository, unterrichtsstundeRepository, aenderungRepository);
    }

    @Test
    void persoenlicherStundenplanWirftExceptionWennLehrkraftNichtExistiert() {
        Account account = account(1L, "mmustermann");
        when(accountRepository.findByBenutzername("mmustermann")).thenReturn(Optional.of(account));
        when(lehrkraftRepository.findByAccount(account)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.persoenlicherStundenplan("mmustermann"));
        verifyNoInteractions(unterrichtsstundeRepository, aenderungRepository);
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

    private static Lehrkraft lehrkraft(Long id, String code) {
        Lehrkraft l = new Lehrkraft();
        l.setId(id);
        l.setKuerzel(code);
        l.setFaecher(List.of());
        return l;
    }

    private static Unterrichtsstunde stunde(Long id, Wochentag tag, Zeitslot slot,
                                            String fachCode, String raumName,
                                            Lehrkraft lehrkraft, String gruppeName) {
        Unterrichtsstunde u = new Unterrichtsstunde();
        u.setId(id);
        u.setWochentag(tag);
        u.setZeitslot(slot);
        u.setFach(fach(fachCode));
        u.setRaum(raum(1L, raumName));
        u.setLehrkraft(lehrkraft);
        u.setLerngruppe(gruppe(3L, gruppeName));
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

    private static Lerngruppe gruppe(Long id, String name) {
        Lerngruppe g = new Lerngruppe();
        g.setId(id);
        g.setBezeichnung(name);
        return g;
    }
}
