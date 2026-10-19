package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.raeume.RaumDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumplanEintragDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import jakarta.persistence.EntityNotFoundException;
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
class RaumServiceTest {
    @Mock RaumRepository raumRepository;
    @Mock RaumaenderungRepository raumaenderungRepository;
    @Mock UnterrichtsstundeRepository unterrichtsstundeRepository;
    @Mock ZeitslotRepository zeitslotRepository;
    @Mock AenderungRepository aenderungRepository;
    @InjectMocks RaumService service;

    @Test
    void alleRaeumeMapsAllRoomsToDtos() {
        Raum room = room(1L, "A101", Raumart.KLASSENRAUM, 30);
        when(raumRepository.findAll()).thenReturn(List.of(room));

        List<RaumDTO> result = service.alleRaeume();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("A101", result.get(0).getBezeichnung());
        assertEquals(Raumart.KLASSENRAUM, result.get(0).getArt());
        assertEquals(30, result.get(0).getKapazitaet());
    }

    @Test
    void raumHinzufuegenCreatesAndSavesRoom() {
        RaumDTO input = new RaumDTO(null, "B201", Raumart.BIOLOGIERAUM, 25);
        Raum saved = room(7L, "B201", Raumart.BIOLOGIERAUM, 25);
        when(raumRepository.save(any(Raum.class))).thenReturn(saved);

        RaumDTO result = service.raumHinzufuegen(input);

        assertEquals(7L, result.getId());
        assertEquals("B201", result.getBezeichnung());
        assertEquals(Raumart.BIOLOGIERAUM, result.getArt());
        assertEquals(25, result.getKapazitaet());
        verify(raumRepository).save(argThat(r -> r.getBezeichnung().equals("B201")
                && r.getArt() == Raumart.BIOLOGIERAUM && r.getKapazitaet() == 25));
    }

    @Test
    void raumplanThrowsWhenRoomDoesNotExist() {
        when(raumRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.raumplanAnzeigen(99L));
        verifyNoInteractions(unterrichtsstundeRepository, aenderungRepository);
    }

    @Test
    void raumplanUsesNormalLessonsAndCompletedChanges() {
        Raum room = room(1L, "A101", Raumart.KLASSENRAUM, 30);
        Zeitslot slot = slot(2L, 2, LocalTime.of(8, 45), LocalTime.of(9, 30));
        Fach fach = fach("MAT");
        Lehrkraft teacher = teacher("MATH");
        Lerngruppe group = group("7a");
        Unterrichtsstunde lesson = new Unterrichtsstunde();
        lesson.setWochentag(Wochentag.MONTAG); lesson.setZeitslot(slot); lesson.setFach(fach);
        lesson.setLehrkraft(teacher); lesson.setLerngruppe(group); lesson.setRaum(room);
        when(raumRepository.findById(1L)).thenReturn(Optional.of(room));
        when(unterrichtsstundeRepository.findByRaumAndStundenplanStatus(room, PlanStatus.AKTIV)).thenReturn(List.of(lesson));
        when(aenderungRepository.findAbgeschlosseneByRaumAndCurrentWeek(room)).thenReturn(List.of());

        List<RaumplanEintragDTO> result = service.raumplanAnzeigen(1L);

        assertEquals(1, result.size());
        assertEquals(Wochentag.MONTAG, result.get(0).getWochentag());
        assertEquals(2, result.get(0).getStundennummer());
        assertEquals("MAT", result.get(0).getFachName());
        assertEquals("MATH", result.get(0).getLehrkraftKuerzel());
        assertEquals("7a", result.get(0).getLerngruppeKuerzel());
        assertFalse(result.get(0).isGeaendert());
    }

    private static Raum room(Long id, String name, Raumart art, int capacity) {
        Raum r = new Raum(); r.setId(id); r.setBezeichnung(name); r.setArt(art); r.setKapazitaet(capacity); return r;
    }
    private static Zeitslot slot(Long id, int no, LocalTime start, LocalTime end) {
        Zeitslot z = new Zeitslot(); z.setId(id); z.setStundennummer(no); z.setStart(start); z.setEnde(end); return z;
    }
    private static Fach fach(String code) { Fach f = new Fach(); f.setKuerzel(code); f.setName(code); return f; }
    private static Lehrkraft teacher(String code) { Lehrkraft l = new Lehrkraft(); l.setId(3L); l.setKuerzel(code); return l; }
    private static Lerngruppe group(String name) { Lerngruppe g = new Lerngruppe(); g.setId(4L); g.setBezeichnung(name); return g; }
}
