package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.ereignis.EreignisDTO;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EreignisServiceAnlegenTest {

    @Mock EreignisRepository ereignisRepository;
    @Mock LehrkraftRepository lehrkraftRepository;
    @Mock RaumRepository raumRepository;
    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock AccountRepository accountRepository;
    @Mock ZeitslotRepository zeitslotRepository;
    @Mock FachRepository fachRepository;
    @Mock ZusatzstundeRepository zusatzstundeRepository;
    @Mock AenderungRepository aenderungRepository;
    @Mock UnterrichtsstundeRepository unterrichtsstundeRepository;
    @Mock AusfallRepository ausfallRepository;
    @Mock RaumaenderungRepository raumaenderungRepository;
    @Mock LehrkraftService lehrkraftService;
    @Mock RaumService raumService;

    @InjectMocks EreignisService service;

    @Test
    void anlegenZusatzstundeErstelltEreignisUndOffeneAenderung() {
        LocalDate date = LocalDate.of(2026, 9, 22);
        Account autor = account(1L);
        autor.setBenutzername("planer1");
        Lerngruppe gruppe = group(2L, "7a");
        Lehrkraft lehrkraft = teacher(3L, "MU");
        Zeitslot slot = slot(4L, 3);
        Fach fach = fach(5L, "BIO");
        Raum raum = room(6L, "B201");

        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(autor));
        when(lerngruppeRepository.findById(2L)).thenReturn(Optional.of(gruppe));
        when(lehrkraftRepository.findById(3L)).thenReturn(Optional.of(lehrkraft));
        when(zeitslotRepository.findById(4L)).thenReturn(Optional.of(slot));
        when(fachRepository.findById(5L)).thenReturn(Optional.of(fach));
        when(raumRepository.findById(6L)).thenReturn(Optional.of(raum));
        when(zusatzstundeRepository.save(any(Zusatzstunde.class))).thenAnswer(inv -> {
            Zusatzstunde z = inv.getArgument(0);
            z.setId(100L);
            return z;
        });
        when(aenderungRepository.save(any(Aenderung.class))).thenAnswer(inv -> inv.getArgument(0));

        EreignisDTO input = dto(EreignisTyp.ZUSATZSTUNDE, date, date);
        input.setLerngruppeId(2L);
        input.setLehrkraftId(3L);
        input.setStartzeitId(4L);
        input.setFachId(5L);
        input.setRaumId(6L);

        EreignisDTO result = service.anlegen(input, autor.getBenutzername());

        assertEquals(100L, result.getId());
        assertEquals(EreignisTyp.ZUSATZSTUNDE, result.getTyp());
        assertEquals(date, result.getStartdatum());
        assertEquals(date, result.getEnddatum());
        assertEquals(4L, result.getStartzeitId());
        assertEquals(3L, result.getLehrkraftId());
        assertEquals(6L, result.getRaumId());
        assertEquals(2L, result.getLerngruppeId());
        assertEquals(5L, result.getFachId());

        ArgumentCaptor<Aenderung> captor = ArgumentCaptor.forClass(Aenderung.class);
        verify(aenderungRepository).save(captor.capture());
        Aenderung aenderung = captor.getValue();
        assertNull(aenderung.getUnterrichtsstunde());
        assertTrue(aenderung.isOffen());
        assertFalse(aenderung.isFreistunde());
        assertEquals(date, aenderung.getDatum());
        assertEquals(Wochentag.DIENSTAG, aenderung.getWochentagNeu());
        assertSame(gruppe, aenderung.getLerngruppeNeu());
        assertSame(lehrkraft, aenderung.getLehrkraftNeu());
        assertSame(fach, aenderung.getFachNeu());
        assertSame(raum, aenderung.getRaumNeu());
        assertSame(slot, aenderung.getZeitslotNeu());
        assertEquals(EreignisTyp.ZUSATZSTUNDE, aenderung.getAusloeser().getTyp());
    }

    @Test
    void anlegenZusatzstundeAmWochenendeWirftException() {
        LocalDate saturday = LocalDate.of(2026, 9, 26);
        Account autor = account(1L);
        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(autor));
        when(lerngruppeRepository.findById(2L)).thenReturn(Optional.of(group(2L, "7a")));
        when(lehrkraftRepository.findById(3L)).thenReturn(Optional.of(teacher(3L, "MU")));
        when(zeitslotRepository.findById(4L)).thenReturn(Optional.of(slot(4L, 3)));
        when(fachRepository.findById(5L)).thenReturn(Optional.of(fach(5L, "BIO")));
        when(raumRepository.findById(6L)).thenReturn(Optional.of(room(6L, "B201")));

        EreignisDTO input = dto(EreignisTyp.ZUSATZSTUNDE, saturday, saturday);
        input.setLerngruppeId(2L);
        input.setLehrkraftId(3L);
        input.setStartzeitId(4L);
        input.setFachId(5L);
        input.setRaumId(6L);

        assertThrows(RuntimeException.class, () -> service.anlegen(input, "planer1"));
        verifyNoInteractions(zusatzstundeRepository, aenderungRepository);
    }

    @Test
    void anlegenAusfallErstelltVertretungUndUebernimmtStammdatenDerOriginalstunde() {
        LocalDate date = LocalDate.of(2026, 9, 22);
        Account autor = account(1L);
        Lehrkraft ausgefalleneLehrkraft = teacher(3L, "MU");
        ausgefalleneLehrkraft.setAccount(account(30L));
        ausgefalleneLehrkraft.getAccount().setAktiv(true);
        Lehrkraft ersatz = teacher(4L, "ER");
        Zeitslot start = slot(10L, 2);
        Zeitslot end = slot(11L, 2);
        Fach fach = fach(5L, "MAT");
        Raum raum = room(6L, "A101");
        Lerngruppe gruppe = group(7L, "7a");
        Unterrichtsstunde stunde = lesson(20L, Wochentag.DIENSTAG, start, fach, raum, ausgefalleneLehrkraft, gruppe);

        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(autor));
        when(lehrkraftRepository.findById(3L)).thenReturn(Optional.of(ausgefalleneLehrkraft));
        when(zeitslotRepository.findById(10L)).thenReturn(Optional.of(start));
        when(zeitslotRepository.findById(11L)).thenReturn(Optional.of(end));
        when(zeitslotRepository.findByStundennummerBetweenOrderByStundennummerAsc(2, 2))
                .thenReturn(List.of(start));
        when(unterrichtsstundeRepository.findByWochentagAndZeitslotAndLehrkraftAndStundenplan_Status(
                Wochentag.DIENSTAG, start, ausgefalleneLehrkraft, PlanStatus.AKTIV))
                .thenReturn(Optional.of(stunde));
        when(aenderungRepository.findByUnterrichtsstundeAndDatum(stunde, date)).thenReturn(Optional.empty());
        when(lehrkraftService.verfuegbareLehrkraft(Wochentag.DIENSTAG, start, fach))
                .thenReturn(Optional.of(ersatz));
        when(aenderungRepository.save(any(Aenderung.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ausfallRepository.save(any(Ausfall.class))).thenAnswer(inv -> {
            Ausfall a = inv.getArgument(0);
            a.setId(100L);
            return a;
        });

        EreignisDTO input = dto(EreignisTyp.AUSFALL, date, date);
        input.setLehrkraftId(3L);
        input.setStartzeitId(10L);
        input.setEndzeitId(11L);
        input.setGrund("krank");

        EreignisDTO result = service.anlegen(input, "planer1");

        assertEquals(100L, result.getId());
        assertEquals(EreignisTyp.AUSFALL, result.getTyp());
        assertEquals("krank", result.getGrund());

        ArgumentCaptor<Aenderung> captor = ArgumentCaptor.forClass(Aenderung.class);
        verify(aenderungRepository).save(captor.capture());
        Aenderung aenderung = captor.getValue();
        assertSame(stunde, aenderung.getUnterrichtsstunde());
        assertSame(ersatz, aenderung.getLehrkraftNeu());
        assertSame(raum, aenderung.getRaumNeu());
        assertSame(fach, aenderung.getFachNeu());
        assertSame(start, aenderung.getZeitslotNeu());
        assertSame(gruppe, aenderung.getLerngruppeNeu());
        assertFalse(aenderung.isFreistunde());
        assertTrue(aenderung.isOffen());
        assertEquals(date, aenderung.getDatum());
    }

    @Test
    void anlegenAusfallOhneVertretungslehrkraftMarkiertAenderungAlsFreistunde() {
        LocalDate date = LocalDate.of(2026, 9, 22);
        Account autor = account(1L);
        Lehrkraft lehrkraft = teacher(3L, "MU");
        lehrkraft.setAccount(account(30L));
        lehrkraft.getAccount().setAktiv(true);
        Zeitslot slot = slot(10L, 2);
        Fach fach = fach(5L, "MAT");
        Raum raum = room(6L, "A101");
        Lerngruppe gruppe = group(7L, "7a");
        Unterrichtsstunde stunde = lesson(20L, Wochentag.DIENSTAG, slot, fach, raum, lehrkraft, gruppe);

        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(autor));
        when(lehrkraftRepository.findById(3L)).thenReturn(Optional.of(lehrkraft));
        when(zeitslotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(zeitslotRepository.findById(11L)).thenReturn(Optional.of(slot));
        when(zeitslotRepository.findByStundennummerBetweenOrderByStundennummerAsc(2, 2))
                .thenReturn(List.of(slot));
        when(unterrichtsstundeRepository.findByWochentagAndZeitslotAndLehrkraftAndStundenplan_Status(
                Wochentag.DIENSTAG, slot, lehrkraft, PlanStatus.AKTIV))
                .thenReturn(Optional.of(stunde));
        when(aenderungRepository.findByUnterrichtsstundeAndDatum(stunde, date)).thenReturn(Optional.empty());
        when(lehrkraftService.verfuegbareLehrkraft(Wochentag.DIENSTAG, slot, fach))
                .thenReturn(Optional.empty());
        when(aenderungRepository.save(any(Aenderung.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ausfallRepository.save(any(Ausfall.class))).thenAnswer(inv -> inv.getArgument(0));

        EreignisDTO input = dto(EreignisTyp.AUSFALL, date, date);
        input.setLehrkraftId(3L);
        input.setStartzeitId(10L);
        input.setEndzeitId(11L);

        service.anlegen(input, "planer1");

        ArgumentCaptor<Aenderung> captor = ArgumentCaptor.forClass(Aenderung.class);
        verify(aenderungRepository).save(captor.capture());
        Aenderung aenderung = captor.getValue();
        assertTrue(aenderung.isFreistunde());
        assertSame(lehrkraft, aenderung.getLehrkraftNeu());
        assertSame(raum, aenderung.getRaumNeu());
    }

    @Test
    void anlegenAusfallLehntInaktiveLehrkraftAb() {
        Account autor = account(1L);
        Lehrkraft lehrkraft = teacher(3L, "MU");
        lehrkraft.setAccount(account(30L));
        lehrkraft.getAccount().setAktiv(false);

        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(autor));
        when(lehrkraftRepository.findById(3L)).thenReturn(Optional.of(lehrkraft));

        EreignisDTO input = dto(EreignisTyp.AUSFALL, LocalDate.of(2026, 9, 22), LocalDate.of(2026, 9, 22));
        input.setLehrkraftId(3L);

        assertThrows(RuntimeException.class, () -> service.anlegen(input, "planer1"));
        verifyNoInteractions(aenderungRepository);
    }

    @Test
    void anlegenRaumaenderungErstelltRaumwechselMitVerfuegbaremErsatzraum() {
        LocalDate date = LocalDate.of(2026, 9, 22);
        Account autor = account(1L);
        Raum gesperrterRaum = room(6L, "A101");
        Raum ersatzraum = room(7L, "B201");
        Lehrkraft lehrkraft = teacher(3L, "MU");
        Fach fach = fach(5L, "MAT");
        Lerngruppe gruppe = group(8L, "7a");
        Zeitslot slot = slot(10L, 2);
        Unterrichtsstunde stunde = lesson(20L, Wochentag.DIENSTAG, slot, fach, gesperrterRaum, lehrkraft, gruppe);

        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(autor));
        when(raumRepository.findById(6L)).thenReturn(Optional.of(gesperrterRaum));
        when(zeitslotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(zeitslotRepository.findById(11L)).thenReturn(Optional.of(slot));
        when(zeitslotRepository.findByStundennummerBetweenOrderByStundennummerAsc(2, 2))
                .thenReturn(List.of(slot));
        when(unterrichtsstundeRepository.findByWochentagAndZeitslotAndRaumAndStundenplan_Status(
                Wochentag.DIENSTAG, slot, gesperrterRaum, PlanStatus.AKTIV))
                .thenReturn(Optional.of(stunde));
        when(aenderungRepository.findByUnterrichtsstundeAndDatum(stunde, date)).thenReturn(Optional.empty());
        when(raumService.verfuegbarerRaum(Wochentag.DIENSTAG, slot, fach)).thenReturn(Optional.of(ersatzraum));
        when(aenderungRepository.save(any(Aenderung.class))).thenAnswer(inv -> inv.getArgument(0));
        when(raumaenderungRepository.save(any(Raumaenderung.class))).thenAnswer(inv -> {
            Raumaenderung r = inv.getArgument(0);
            r.setId(101L);
            return r;
        });

        EreignisDTO input = dto(EreignisTyp.RAUMAENDERUNG, date, date);
        input.setRaumId(6L);
        input.setStartzeitId(10L);
        input.setEndzeitId(11L);

        EreignisDTO result = service.anlegen(input, "planer1");

        assertEquals(101L, result.getId());
        assertEquals(EreignisTyp.RAUMAENDERUNG, result.getTyp());
        assertEquals(6L, result.getRaumId());

        ArgumentCaptor<Aenderung> captor = ArgumentCaptor.forClass(Aenderung.class);
        verify(aenderungRepository).save(captor.capture());
        Aenderung aenderung = captor.getValue();
        assertSame(ersatzraum, aenderung.getRaumNeu());
        assertSame(lehrkraft, aenderung.getLehrkraftNeu());
        assertSame(fach, aenderung.getFachNeu());
        assertSame(slot, aenderung.getZeitslotNeu());
        assertSame(gruppe, aenderung.getLerngruppeNeu());
        assertFalse(aenderung.isFreistunde());
        assertTrue(aenderung.isOffen());
    }

    @Test
    void anlegenRaumaenderungOhneErsatzraumMarkiertAenderungAlsFreistunde() {
        LocalDate date = LocalDate.of(2026, 9, 22);
        Account autor = account(1L);
        Raum raum = room(6L, "A101");
        Lehrkraft lehrkraft = teacher(3L, "MU");
        Fach fach = fach(5L, "MAT");
        Lerngruppe gruppe = group(8L, "7a");
        Zeitslot slot = slot(10L, 2);
        Unterrichtsstunde stunde = lesson(20L, Wochentag.DIENSTAG, slot, fach, raum, lehrkraft, gruppe);

        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(autor));
        when(raumRepository.findById(6L)).thenReturn(Optional.of(raum));
        when(zeitslotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(zeitslotRepository.findById(11L)).thenReturn(Optional.of(slot));
        when(zeitslotRepository.findByStundennummerBetweenOrderByStundennummerAsc(2, 2)).thenReturn(List.of(slot));
        when(unterrichtsstundeRepository.findByWochentagAndZeitslotAndRaumAndStundenplan_Status(
                Wochentag.DIENSTAG, slot, raum, PlanStatus.AKTIV)).thenReturn(Optional.of(stunde));
        when(aenderungRepository.findByUnterrichtsstundeAndDatum(stunde, date)).thenReturn(Optional.empty());
        when(raumService.verfuegbarerRaum(Wochentag.DIENSTAG, slot, fach)).thenReturn(Optional.empty());
        when(aenderungRepository.save(any(Aenderung.class))).thenAnswer(inv -> inv.getArgument(0));
        when(raumaenderungRepository.save(any(Raumaenderung.class))).thenAnswer(inv -> inv.getArgument(0));

        EreignisDTO input = dto(EreignisTyp.RAUMAENDERUNG, date, date);
        input.setRaumId(6L);
        input.setStartzeitId(10L);
        input.setEndzeitId(11L);

        service.anlegen(input, "planer1");

        ArgumentCaptor<Aenderung> captor = ArgumentCaptor.forClass(Aenderung.class);
        verify(aenderungRepository).save(captor.capture());
        Aenderung aenderung = captor.getValue();
        assertTrue(aenderung.isFreistunde());
        assertSame(raum, aenderung.getRaumNeu());
    }

    @Test
    void anlegenRaumaenderungErhaeltBereitsVorhandeneLehrkraftBeiKumulierterAenderung() {
        LocalDate date = LocalDate.of(2026, 9, 22);
        Account autor = account(1L);
        Raum alterRaum = room(6L, "A101");
        Raum neuerRaum = room(7L, "B201");
        Lehrkraft bereitsGeaendert = teacher(30L, "VERT");
        Lehrkraft original = teacher(3L, "MU");
        Fach fach = fach(5L, "MAT");
        Lerngruppe gruppe = group(8L, "7a");
        Zeitslot slot = slot(10L, 2);
        Unterrichtsstunde stunde = lesson(20L, Wochentag.DIENSTAG, slot, fach, alterRaum, original, gruppe);

        Aenderung existing = new Aenderung();
        existing.setUnterrichtsstunde(stunde);
        existing.setLehrkraftNeu(bereitsGeaendert);
        existing.setRaumNeu(alterRaum);
        existing.setFachNeu(fach);
        existing.setZeitslotNeu(slot);
        existing.setLerngruppeNeu(gruppe);
        existing.setOffen(false);

        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(autor));
        when(raumRepository.findById(6L)).thenReturn(Optional.of(alterRaum));
        when(zeitslotRepository.findById(10L)).thenReturn(Optional.of(slot));
        when(zeitslotRepository.findById(11L)).thenReturn(Optional.of(slot));
        when(zeitslotRepository.findByStundennummerBetweenOrderByStundennummerAsc(2, 2)).thenReturn(List.of(slot));
        when(unterrichtsstundeRepository.findByWochentagAndZeitslotAndRaumAndStundenplan_Status(
                Wochentag.DIENSTAG, slot, alterRaum, PlanStatus.AKTIV)).thenReturn(Optional.of(stunde));
        when(aenderungRepository.findByUnterrichtsstundeAndDatum(stunde, date)).thenReturn(Optional.of(existing));
        when(raumService.verfuegbarerRaum(Wochentag.DIENSTAG, slot, fach)).thenReturn(Optional.of(neuerRaum));
        when(aenderungRepository.save(existing)).thenReturn(existing);
        when(raumaenderungRepository.save(any(Raumaenderung.class))).thenAnswer(inv -> inv.getArgument(0));

        EreignisDTO input = dto(EreignisTyp.RAUMAENDERUNG, date, date);
        input.setRaumId(6L);
        input.setStartzeitId(10L);
        input.setEndzeitId(11L);

        service.anlegen(input, "planer1");

        assertSame(bereitsGeaendert, existing.getLehrkraftNeu());
        assertSame(neuerRaum, existing.getRaumNeu());
        assertTrue(existing.isOffen());
        verify(aenderungRepository).save(existing);
    }

    @Test
    void anlegenRaumaenderungUeberspringtWochenende() {
        LocalDate saturday = LocalDate.of(2026, 9, 26);
        Account autor = account(1L);
        Raum raum = room(6L, "A101");

        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(autor));
        when(raumRepository.findById(6L)).thenReturn(Optional.of(raum));
        when(zeitslotRepository.findById(10L)).thenReturn(Optional.of(slot(10L, 2)));
        when(zeitslotRepository.findById(11L)).thenReturn(Optional.of(slot(11L, 2)));
        when(zeitslotRepository.findByStundennummerBetweenOrderByStundennummerAsc(2, 2))
                .thenReturn(List.of(slot(10L, 2)));
        when(raumaenderungRepository.save(any(Raumaenderung.class))).thenAnswer(inv -> inv.getArgument(0));

        EreignisDTO input = dto(EreignisTyp.RAUMAENDERUNG, saturday, saturday);
        input.setRaumId(6L);
        input.setStartzeitId(10L);
        input.setEndzeitId(11L);

        service.anlegen(input, "planer1");

        verify(unterrichtsstundeRepository, never())
                .findByWochentagAndZeitslotAndRaumAndStundenplan_Status(any(), any(), any(), any());
        verifyNoInteractions(aenderungRepository, raumService);
    }

    @Test
    void anlegenWirftExceptionWennEnddatumVorStartdatumLiegt() {
        EreignisDTO dto = new EreignisDTO();
        dto.setTyp(EreignisTyp.AUSFALL);
        dto.setStartdatum(LocalDate.of(2026, 9, 25));
        dto.setEnddatum(LocalDate.of(2026, 9, 20));   // vor dem Start!

        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(new Account()));

        assertThrows(RuntimeException.class, () -> service.anlegen(dto, "planer1"));
    }

    @Test
    void anlegenWirftExceptionWennStartdatumFehlt() {
        EreignisDTO dto = new EreignisDTO();
        dto.setTyp(EreignisTyp.AUSFALL);
        dto.setStartdatum(null);

        when(accountRepository.findByBenutzername("planer1")).thenReturn(Optional.of(new Account()));

        assertThrows(RuntimeException.class, () -> service.anlegen(dto, "planer1"));
    }

    private static EreignisDTO dto(EreignisTyp typ, LocalDate start, LocalDate end) {
        EreignisDTO dto = new EreignisDTO();
        dto.setTyp(typ);
        dto.setStartdatum(start);
        dto.setEnddatum(end);
        dto.setGanztag(false);
        return dto;
    }

    private static Account account(Long id) {
        Account a = new Account();
        a.setId(id);
        a.setName("Mustermann");
        a.setVorname("Max");
        a.setAktiv(true);
        return a;
    }

    private static Lehrkraft teacher(Long id, String code) {
        Lehrkraft l = new Lehrkraft();
        l.setId(id);
        l.setKuerzel(code);
        return l;
    }

    private static Lerngruppe group(Long id, String name) {
        Lerngruppe g = new Lerngruppe();
        g.setId(id);
        g.setBezeichnung(name);
        return g;
    }

    private static Raum room(Long id, String name) {
        Raum r = new Raum();
        r.setId(id);
        r.setBezeichnung(name);
        r.setArt(Raumart.KLASSENRAUM);
        r.setKapazitaet(30);
        return r;
    }

    private static Fach fach(Long id, String code) {
        Fach f = new Fach();
        f.setId(id);
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

    private static Unterrichtsstunde lesson(Long id, Wochentag day, Zeitslot slot,
                                            Fach fach, Raum raum, Lehrkraft lehrkraft,
                                            Lerngruppe gruppe) {
        Unterrichtsstunde u = new Unterrichtsstunde();
        u.setId(id);
        u.setWochentag(day);
        u.setZeitslot(slot);
        u.setFach(fach);
        u.setRaum(raum);
        u.setLehrkraft(lehrkraft);
        u.setLerngruppe(gruppe);
        return u;
    } 
}
