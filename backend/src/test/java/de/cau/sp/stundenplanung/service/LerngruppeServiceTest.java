package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.*;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LerngruppeServiceTest {
    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock JahrgangsstufeRepository jahrgangsstufeRepository;
    @Mock AccountRepository accountRepository;
    @Mock RaumRepository raumRepository;
    @Mock StundenplanRepository stundenplanRepository;
    @Mock AenderungRepository aenderungRepository;
    @Mock UnterrichtsstundeRepository unterrichtsstundeRepository;
    @Mock LehrkraftRepository lehrkraftRepository;
    @Mock LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    @InjectMocks LerngruppeService service;

    @Test
    void alleJahrgangsstufenReturnsNumbersAndClassCounts() {
        Jahrgangsstufe y5 = year(5); Jahrgangsstufe y6 = year(6);
        when(jahrgangsstufeRepository.findAll()).thenReturn(List.of(y5, y6));
        when(lerngruppeRepository.countByJahrgangsstufeAndAktivTrue(y5)).thenReturn(3L);
        when(lerngruppeRepository.countByJahrgangsstufeAndAktivTrue(y6)).thenReturn(0L);

        List<JahrgangsstufeDTO> result = service.alleJahrgangsstufen();

        assertEquals(2, result.size());
        assertEquals(5, result.get(0).getNummer()); assertEquals(3, result.get(0).getAnzahlKlassen());
        assertEquals(6, result.get(1).getNummer()); assertEquals(0, result.get(1).getAnzahlKlassen());
    }

    @Test
    void lerngruppeJgstMapsClassData() {
        Lerngruppe group = group(1L, "7a");
        Lehrkraft head = teacher(2L, "MUST");
        Account account = new Account(); account.setVorname("Max"); account.setName("Mustermann");
        head.setAccount(account); group.setKlassenleitung(head); group.setSchueleranzahl(28);
        group.setKlassenzimmer(room(3L, "A101", Raumart.KLASSENRAUM, 30));
        when(lerngruppeRepository.findByJahrgangsstufeIdAndAktivTrue(7L)).thenReturn(List.of(group));

        List<LerngruppeDTO> result = service.lerngruppeJGST(7L);

        assertEquals(1, result.size());
        assertEquals("7a", result.get(0).getBezeichnung());
        assertEquals("Max", result.get(0).getLehrkraftVorname());
        assertEquals("Mustermann", result.get(0).getLehrkraftNachname());
        assertEquals(28, result.get(0).getSchueleranzahl());
        assertEquals("A101", result.get(0).getKlassenzimmer());
    }

    @Test
    void klassenstundenplanThrowsWhenClassDoesNotExist() {
        when(lerngruppeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.klassenstundenplanAnzeigen(99L));
    }

    @Test
    void klassenstundenplanThrows404WhenNoActivePlanExists() {
        Lerngruppe group = group(1L, "7a");
        when(lerngruppeRepository.findById(1L)).thenReturn(Optional.of(group));
        when(stundenplanRepository.findByLerngruppeAndStatus(group, PlanStatus.AKTIV)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.klassenstundenplanAnzeigen(1L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void klassenstundenplanMapsActiveLesson() {
        Lerngruppe group = group(1L, "7a");
        Zeitslot slot = slot(2L, 2);
        Fach fach = fach("MAT", null);
        Raum room = room(3L, "A101", Raumart.KLASSENRAUM, 30);
        Lehrkraft teacher = teacher(4L, "MUST");
        Unterrichtsstunde lesson = lesson(Wochentag.MONTAG, slot, fach, room, teacher, group);
        Stundenplan plan = new Stundenplan(); plan.setUnterrichtsstunden(List.of(lesson));
        when(lerngruppeRepository.findById(1L)).thenReturn(Optional.of(group));
        when(stundenplanRepository.findByLerngruppeAndStatus(group, PlanStatus.AKTIV)).thenReturn(Optional.of(plan));
        when(aenderungRepository.findByLerngruppeNeuAndDatumBetween(eq(group), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        List<UnterrichtsstundeDTO> result = service.klassenstundenplanAnzeigen(1L);

        assertEquals(1, result.size());
        assertEquals("MAT", result.get(0).getFachName());
        assertFalse(result.get(0).isGeaendert());
    }

    @Test
    void vorschlaegeErmittelnForFreistundeReturnsNoAlternatives() {
        Aenderung change = change(1L, true, EreignisTyp.AUSFALL, Wochentag.MONTAG, slot(1L, 1));
        when(aenderungRepository.findById(1L)).thenReturn(Optional.of(change));

        AenderungsVorschlagDTO result = service.vorschlaegeErmitteln(1L);

        assertTrue(result.getRaumalternativen().isEmpty());
        assertTrue(result.getLehrkraftalternativen().isEmpty());
        verifyNoInteractions(raumRepository, lehrkraftRepository, unterrichtsstundeRepository, lehrerverfuegbarkeitRepository);
    }

    @Test
    void vorschlaegeErmittelnForRoomChangeReturnsFreeRoomsAndMarksSuitableRooms() {
        Zeitslot slot = slot(1L, 1);
        Fach fach = fach("BIO", Raumart.BIOLOGIERAUM);

        Aenderung change = change(
                1L,
                false,
                EreignisTyp.RAUMAENDERUNG,
                Wochentag.MONTAG,
                slot
        );
        change.setFachNeu(fach);

        when(aenderungRepository.findById(1L)).thenReturn(Optional.of(change));

        Raum occupied = room(1L, "B1", Raumart.BIOLOGIERAUM, 30);
        Raum suitable = room(2L, "B2", Raumart.BIOLOGIERAUM, 30);
        Raum wrongType = room(3L, "A1", Raumart.KLASSENRAUM, 30);

        when(unterrichtsstundeRepository.findRaeumeByWochentagAndZeitslot(
                Wochentag.MONTAG, slot, PlanStatus.AKTIV
        )).thenReturn(List.of(occupied));

        when(aenderungRepository.findByWochentagNeuAndZeitslotNeuAndDatum(
                Wochentag.MONTAG, slot, change.getDatum()
        )).thenReturn(List.of());

        when(raumRepository.findAll())
                .thenReturn(List.of(occupied, suitable, wrongType));

        AenderungsVorschlagDTO result = service.vorschlaegeErmitteln(1L);

        assertEquals(2, result.getRaumalternativen().size());

        assertEquals(2L, result.getRaumalternativen().get(0).getRaumId());
        assertTrue(result.getRaumalternativen().get(0).isFachraum());

        assertEquals(3L, result.getRaumalternativen().get(1).getRaumId());
        assertFalse(result.getRaumalternativen().get(1).isFachraum());

        assertTrue(result.getLehrkraftalternativen().isEmpty());
    }

    @Test
    void vorschlaegeErmittelnForTeacherAbsenceReturnsAvailableTeachers() {
        Zeitslot slot = slot(1L, 1);
        Fach fach = fach("MAT", null);

        Aenderung change = change(
                1L,
                false,
                EreignisTyp.AUSFALL,
                Wochentag.MONTAG,
                slot
        );
        change.setDatum(LocalDate.of(2026, 9, 17));
        change.setFachNeu(fach);

        when(aenderungRepository.findById(1L))
                .thenReturn(Optional.of(change));

        Lehrkraft busy = teacher(1L, "BUSY");

        Lehrkraft available = teacher(2L, "FREE");

        Account account = new Account();
        account.setVorname("Max");
        account.setName("Mustermann");
        available.setAccount(account);

        available.setFaecher(List.of(fach));

        when(unterrichtsstundeRepository.findLehrkraefteByWochentagAndZeitslot(
                Wochentag.MONTAG, slot
        , PlanStatus.AKTIV)).thenReturn(List.of(busy));

        when(aenderungRepository.findByWochentagNeuAndZeitslotNeuAndDatum(
                Wochentag.MONTAG, slot, change.getDatum()
        )).thenReturn(List.of());

        when(lehrerverfuegbarkeitRepository
                .findLehrkraftIdsByWochentagAndZeitslotAndDatumAndTyp(
                        Wochentag.MONTAG,
                        slot,
                        change.getDatum(),
                        VerfuegbarkeitsTyp.GESPERRT
                ))
                .thenReturn(List.of());

        when(lehrkraftRepository.findByAccountAktivTrue())
                .thenReturn(List.of(busy, available));

        AenderungsVorschlagDTO result = service.vorschlaegeErmitteln(1L);

        assertEquals(1, result.getLehrkraftalternativen().size());
        assertEquals(
                2L,
                result.getLehrkraftalternativen().get(0).getLehrkraftId()
        );
        assertTrue(
                result.getLehrkraftalternativen().get(0).isFachlehrkraft()
        );
    }

    @Test
    void fachraeumeVerfuegbarFiltersByRoomTypeAndOccupancy() {
        Zeitslot slot = slot(1L, 1); Fach fach = fach("CHEM", Raumart.CHEMIERAUM);
        Raum occupied = room(1L, "C1", Raumart.CHEMIERAUM, 30);
        Raum free = room(2L, "C2", Raumart.CHEMIERAUM, 30);
        Raum wrong = room(3L, "A1", Raumart.KLASSENRAUM, 30);
        when(unterrichtsstundeRepository.findRaeumeByWochentagAndZeitslot(Wochentag.MONTAG, slot, PlanStatus.AKTIV)).thenReturn(List.of(occupied));
        when(aenderungRepository.findBestaetigteByWochentagNeuAndZeitslotNeu(Wochentag.MONTAG, slot)).thenReturn(List.of());
        when(raumRepository.findAll()).thenReturn(List.of(occupied, free, wrong));

        List<Raum> result = service.fachraeumeVerfuegbar(Wochentag.MONTAG, slot, fach);

        assertEquals(List.of(free), result);
    }

    @Test
    void fachraeumeVerfuegbarUsesClassroomWhenNoSpecialRoomTypeIsNeeded() {
        Zeitslot slot = slot(1L, 1); Fach fach = fach("DEU", null);
        Raum classroom = room(1L, "A1", Raumart.KLASSENRAUM, 30);
        Raum lab = room(2L, "B1", Raumart.BIOLOGIERAUM, 30);
        when(unterrichtsstundeRepository.findRaeumeByWochentagAndZeitslot(Wochentag.MONTAG, slot, PlanStatus.AKTIV)).thenReturn(List.of());
        when(aenderungRepository.findBestaetigteByWochentagNeuAndZeitslotNeu(Wochentag.MONTAG, slot)).thenReturn(List.of());
        when(raumRepository.findAll()).thenReturn(List.of(classroom, lab));

        assertEquals(List.of(classroom), service.fachraeumeVerfuegbar(Wochentag.MONTAG, slot, fach));
    }

    @Test
    void fachlehrkraefteVerfuegbarFiltersBusyLockedAndWrongSubject() {
        Zeitslot slot = slot(1L, 1); Fach fach = fach("MAT", null);
        Lehrkraft busy = teacher(1L, "BUSY"); busy.setFaecher(List.of(fach));
        Lehrkraft locked = teacher(2L, "LOCKED"); locked.setFaecher(List.of(fach));
        Lehrkraft free = teacher(3L, "FREE"); free.setFaecher(List.of(fach));
        Lehrkraft wrongSubject = teacher(4L, "WRONG"); wrongSubject.setFaecher(List.of());
        LocalDate date = LocalDate.of(2026, 9, 17);
        when(unterrichtsstundeRepository.findLehrkraefteByWochentagAndZeitslot(Wochentag.MONTAG, slot, PlanStatus.AKTIV)).thenReturn(List.of(busy));
        when(aenderungRepository.findByWochentagNeuAndZeitslotNeuAndDatum(Wochentag.MONTAG, slot, date)).thenReturn(List.of());
        when(lehrerverfuegbarkeitRepository.findLehrkraftIdsByWochentagAndZeitslotAndDatumAndTyp(
                Wochentag.MONTAG, slot, date, VerfuegbarkeitsTyp.GESPERRT)).thenReturn(List.of(2L));
        when(lehrkraftRepository.findByAccountAktivTrue())
                .thenReturn(List.of(busy, locked, free, wrongSubject));
        assertEquals(List.of(free), service.fachlehrkraefteVerfuegbar(Wochentag.MONTAG, slot, date, fach));
    }

    @Test
    void fachlehrkraefteVerfuegbarKeepsPreferredAndNonPreferredTeachersAvailable() {
        Zeitslot slot = slot(1L, 1); Fach fach = fach("MAT", null);
        Lehrkraft teacher = teacher(3L, "FREE"); teacher.setFaecher(List.of(fach));
        LocalDate date = LocalDate.of(2026, 9, 17);
        when(unterrichtsstundeRepository.findLehrkraefteByWochentagAndZeitslot(Wochentag.MONTAG, slot,PlanStatus.AKTIV)).thenReturn(List.of());
        when(aenderungRepository.findByWochentagNeuAndZeitslotNeuAndDatum(Wochentag.MONTAG, slot, date)).thenReturn(List.of());
        when(lehrerverfuegbarkeitRepository.findLehrkraftIdsByWochentagAndZeitslotAndDatumAndTyp(
                Wochentag.MONTAG, slot, date, VerfuegbarkeitsTyp.GESPERRT)).thenReturn(List.of());
        when(lehrkraftRepository.findByAccountAktivTrue())
                .thenReturn(List.of(teacher));
        assertEquals(List.of(teacher), service.fachlehrkraefteVerfuegbar(Wochentag.MONTAG, slot, date, fach));
    }

    @Test
    void fachraeumeAndTeachersReturnEmptyWhenAllAreBlocked() {
        Zeitslot slot = slot(1L, 1); Fach fach = fach("MAT", null);
        Raum room = room(1L, "A1", Raumart.KLASSENRAUM, 30);
        Lehrkraft teacher = teacher(1L, "T"); teacher.setFaecher(List.of(fach));
        LocalDate date = LocalDate.of(2026, 9, 17);
        when(unterrichtsstundeRepository.findRaeumeByWochentagAndZeitslot(Wochentag.MONTAG, slot, PlanStatus.AKTIV)).thenReturn(List.of(room));
        when(unterrichtsstundeRepository.findLehrkraefteByWochentagAndZeitslot(Wochentag.MONTAG, slot, PlanStatus.AKTIV)).thenReturn(List.of(teacher));
        when(aenderungRepository.findBestaetigteByWochentagNeuAndZeitslotNeu(Wochentag.MONTAG, slot)).thenReturn(List.of());
        when(lehrerverfuegbarkeitRepository.findLehrkraftIdsByWochentagAndZeitslotAndDatumAndTyp(any(), any(), any(), any())).thenReturn(List.of());
        when(raumRepository.findAll()).thenReturn(List.of(room));
        when(lehrkraftRepository.findByAccountAktivTrue())
                .thenReturn(List.of(teacher));
        assertTrue(service.fachraeumeVerfuegbar(Wochentag.MONTAG, slot, fach).isEmpty());
        assertTrue(service.fachlehrkraefteVerfuegbar(Wochentag.MONTAG, slot, date, fach).isEmpty());
    }

    private static Jahrgangsstufe year(int number) { Jahrgangsstufe y = new Jahrgangsstufe(); y.setNummer(number); return y; }
    private static Lerngruppe group(Long id, String name) { Lerngruppe g = new Lerngruppe(); g.setId(id); g.setBezeichnung(name); return g; }
    private static Lehrkraft teacher(Long id, String code) { Lehrkraft l = new Lehrkraft(); l.setId(id); l.setKuerzel(code); return l; }
    private static Raum room(Long id, String name, Raumart art, int cap) { Raum r = new Raum(); r.setId(id); r.setBezeichnung(name); r.setArt(art); r.setKapazitaet(cap); return r; }
    private static Fach fach(String code, Raumart needed) { Fach f = new Fach(); f.setKuerzel(code); f.setName(code); f.setBenoetigterRaumtyp(needed); return f; }
    private static Zeitslot slot(Long id, int no) { Zeitslot z = new Zeitslot(); z.setId(id); z.setStundennummer(no); z.setStart(LocalTime.of(8, 0)); z.setEnde(LocalTime.of(8, 45)); return z; }
    private static Unterrichtsstunde lesson(Wochentag day, Zeitslot slot, Fach fach, Raum room, Lehrkraft teacher, Lerngruppe group) {
        Unterrichtsstunde u = new Unterrichtsstunde(); u.setWochentag(day); u.setZeitslot(slot); u.setFach(fach); u.setRaum(room); u.setLehrkraft(teacher); u.setLerngruppe(group); return u;
    }
    private static Aenderung change(Long id, boolean freistunde, EreignisTyp typ, Wochentag day, Zeitslot slot) {
        Aenderung a = new Aenderung(); a.setId(id); a.setFreistunde(freistunde); a.setWochentagNeu(day); a.setZeitslotNeu(slot); a.setAusloeser(event(typ)); return a;
    }
    private static Ereignis event(EreignisTyp typ) { Ereignis e = new Ereignis(); e.setTyp(typ); return e; }
}
