package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.raeume.RaumDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaumServiceBearbeitenTest {

    @Mock RaumRepository raumRepository;
    @Mock RaumaenderungRepository raumaenderungRepository;
    @Mock UnterrichtsstundeRepository unterrichtsstundeRepository;
    @Mock ZeitslotRepository zeitslotRepository;
    @Mock AenderungRepository aenderungRepository;
    @Mock LerngruppeRepository lerngruppeRepository;

    @InjectMocks RaumService service;

    @Test
    void raumBearbeitenAendertBezeichnungUndKapazitaetAberNichtDieRaumart() {
        Raum raum = room(1L, "A101", Raumart.KLASSENRAUM, 30);
        RaumDTO update = new RaumDTO(99L, "A201", Raumart.BIOLOGIERAUM, 28);
        when(raumRepository.findById(1L)).thenReturn(Optional.of(raum));
        when(raumRepository.save(raum)).thenReturn(raum);

        RaumDTO result = service.raumBearbeiten(1L, update);

        assertEquals(1L, result.getId());
        assertEquals("A201", result.getBezeichnung());
        assertEquals(28, result.getKapazitaet());
        assertEquals(Raumart.KLASSENRAUM, result.getArt());
        assertEquals(Raumart.KLASSENRAUM, raum.getArt());
        verify(raumRepository).save(raum);
    }

    @Test
    void raumBearbeitenWirftExceptionWennRaumNichtExistiert() {
        when(raumRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.raumBearbeiten(99L, new RaumDTO(99L, "A201", Raumart.KLASSENRAUM, 30)));
        verify(raumRepository, never()).save(any(Raum.class));
    }

    private static Raum room(Long id, String name, Raumart art, int capacity) {
        Raum r = new Raum();
        r.setId(id);
        r.setBezeichnung(name);
        r.setArt(art);
        r.setKapazitaet(capacity);
        return r;
    }
    @Test
    void verfuegbareRaeumeGibtFachraeumeZurueckWennVorhanden() {
        Wochentag tag = Wochentag.MONTAG;

        Zeitslot zeitslot = new Zeitslot();
        zeitslot.setId(1L);

        Fach fach = new Fach();
        fach.setBenoetigterRaumtyp(Raumart.BIOLOGIERAUM);

        Raum fachraum1 = room(1L, "Bio 1", Raumart.BIOLOGIERAUM, 30);
        Raum fachraum2 = room(2L, "Bio 2", Raumart.BIOLOGIERAUM, 25);

        when(unterrichtsstundeRepository.findVerfuegbareFachraeume(
                tag,
                zeitslot,
                Raumart.BIOLOGIERAUM,
                PlanStatus.AKTIV
        )).thenReturn(List.of(fachraum1, fachraum2));

        List<Raum> result = service.verfuegbareRaeume(tag, zeitslot, fach);

        assertEquals(2, result.size());
        assertSame(fachraum1, result.get(0));
        assertSame(fachraum2, result.get(1));

        verify(unterrichtsstundeRepository)
                .findVerfuegbareFachraeume(
                        tag,
                        zeitslot,
                        Raumart.BIOLOGIERAUM,
                        PlanStatus.AKTIV
                );

        // Wenn passende Fachräume existieren, werden allgemeine Räume
        // nicht zusätzlich abgefragt.
        verify(unterrichtsstundeRepository, never())
                .findVerfuegbareRaeume(
                        any(Wochentag.class),
                        any(Zeitslot.class),
                        any(PlanStatus.class)
                );
    }

    @Test
    void verfuegbareRaeumeGibtAllgemeineRaeumeZurueckWennKeineFachraeumeVorhandenSind() {
        Wochentag tag = Wochentag.MONTAG;

        Zeitslot zeitslot = new Zeitslot();
        zeitslot.setId(1L);

        Fach fach = new Fach();
        fach.setBenoetigterRaumtyp(Raumart.BIOLOGIERAUM);

        Raum raum1 = room(1L, "A101", Raumart.KLASSENRAUM, 30);
        Raum raum2 = room(2L, "A102", Raumart.KLASSENRAUM, 30);

        when(unterrichtsstundeRepository.findVerfuegbareFachraeume(
                tag,
                zeitslot,
                Raumart.BIOLOGIERAUM,
                PlanStatus.AKTIV
        )).thenReturn(List.of());

        when(unterrichtsstundeRepository.findVerfuegbareRaeume(
                tag,
                zeitslot,
                PlanStatus.AKTIV
        )).thenReturn(List.of(raum1, raum2));

        List<Raum> result = service.verfuegbareRaeume(tag, zeitslot, fach);

        assertEquals(2, result.size());
        assertSame(raum1, result.get(0));
        assertSame(raum2, result.get(1));

        verify(unterrichtsstundeRepository)
                .findVerfuegbareFachraeume(
                        tag,
                        zeitslot,
                        Raumart.BIOLOGIERAUM,
                        PlanStatus.AKTIV
                );

        verify(unterrichtsstundeRepository)
                .findVerfuegbareRaeume(
                        tag,
                        zeitslot,
                        PlanStatus.AKTIV
                );
    }

    @Test
    void verfuegbareRaeumeGibtLeereListeZurueckWennKeineRaeumeVerfuegbarSind() {
        Wochentag tag = Wochentag.MONTAG;

        Zeitslot zeitslot = new Zeitslot();
        zeitslot.setId(1L);

        Fach fach = new Fach();
        fach.setBenoetigterRaumtyp(Raumart.BIOLOGIERAUM);

        when(unterrichtsstundeRepository.findVerfuegbareFachraeume(
                tag,
                zeitslot,
                Raumart.BIOLOGIERAUM,
                PlanStatus.AKTIV
        )).thenReturn(List.of());

        when(unterrichtsstundeRepository.findVerfuegbareRaeume(
                tag,
                zeitslot,
                PlanStatus.AKTIV
        )).thenReturn(List.of());

        List<Raum> result = service.verfuegbareRaeume(tag, zeitslot, fach);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(unterrichtsstundeRepository)
                .findVerfuegbareFachraeume(
                        tag,
                        zeitslot,
                        Raumart.BIOLOGIERAUM,
                        PlanStatus.AKTIV
                );

        verify(unterrichtsstundeRepository)
                .findVerfuegbareRaeume(
                        tag,
                        zeitslot,
                        PlanStatus.AKTIV
                );
    }
}
