package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.stammdaten.MaximalbelastungDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.PausenregelDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.PflichtfachvorgabeDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.RasterpauseDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.SchulprofilDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.StufenvorgabeDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.StundenrasterDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.VertretungsprioritaetDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StammdatenServiceTest {

    @Mock SchuleRepository schuleRepository;
    @Mock ZeitslotRepository zeitslotRepository;
    @Mock StundenrasterRepository stundenrasterRepository;
    @Mock RasterpauseRepository rasterpauseRepository;
    @Mock PausenregelRepository pausenregelRepository;
    @Mock BlockregelRepository blockregelRepository;
    @Mock PflichtfachvorgabeRepository pflichtfachvorgabeRepository;
    @Mock MaximalbelastungRepository maximalbelastungRepository;
    @Mock JahrgangsstufeRepository jahrgangsstufeRepository;
    @Mock StundenplanRepository stundenplanRepository;
    @Mock FachRepository fachRepository;
    @Mock LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    @Mock RaumverfuegbarkeitRepository raumverfuegbarkeitRepository;
    @InjectMocks StammdatenService service;


    //schulprofil(), schulprofilSpeichern()
    @Test
    void schulprofilWirftExceptionWennKeineSchuleExistiert() {
        when(schuleRepository.findAll()).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> service.schulprofil());
    }

    @Test
    void schulprofilGibtDatenDerErstenSchuleZurueck() {
        Schule schule = new Schule();
        schule.setName("Gymnasium am Westring");
        schule.setBundesland("Schleswig-Holstein");
        schule.setSchulform("Gymnasium");

        when(schuleRepository.findAll()).thenReturn(List.of(schule));

        SchulprofilDTO result = service.schulprofil();

        assertEquals("Gymnasium am Westring", result.getName());
    }

    @Test
    void schulprofilSpeichernLegtNeueSchuleAnWennNochKeineExistiert() {
        SchulprofilDTO dto = new SchulprofilDTO();
        dto.setName("Neue Schule");
        dto.setBundesland("SH");
        dto.setSchulform("Gymnasium");

        when(schuleRepository.findAll()).thenReturn(List.of());
        when(schuleRepository.save(any(Schule.class))).thenAnswer(inv -> inv.getArgument(0));

        SchulprofilDTO result = service.schulprofilSpeichern(dto);

        assertEquals("Neue Schule", result.getName());
    }

    @Test
    void schulprofilSpeichernAktualisiertBestehendeSchule() {
        Schule bestehend = new Schule();
        bestehend.setName("Alt");

        SchulprofilDTO dto = new SchulprofilDTO();
        dto.setName("Neu");
        dto.setBundesland("SH");
        dto.setSchulform("Gymnasium");

        when(schuleRepository.findAll()).thenReturn(List.of(bestehend));
        when(schuleRepository.save(any(Schule.class))).thenAnswer(inv -> inv.getArgument(0));

        service.schulprofilSpeichern(dto);

        assertEquals("Neu", bestehend.getName());
        verify(schuleRepository, times(1)).save(bestehend);
    }

    //stundenraster(), stundenrasterSpeichern()
    @Test
    void stundenrasterWirftExceptionWennNichtKonfiguriert() {
        when(stundenrasterRepository.findAll()).thenReturn(List.of());

        assertThrows(RuntimeException.class, () -> service.stundenraster());
    }

    @Test
    void stundenrasterMapptRasterPausenUndZeitslotsKorrekt() {
        Stundenraster raster = new Stundenraster();
        raster.setId(1L);
        raster.setStundenProTag(6);
        raster.setStundendauer(45);

        Rasterpause pause = new Rasterpause();
        pause.setId(1L);
        pause.setNachStunde(2);
        pause.setDauerMinuten(20);

        Zeitslot zeitslot = new Zeitslot();
        zeitslot.setId(1L);
        zeitslot.setStundennummer(1);

        when(stundenrasterRepository.findAll()).thenReturn(List.of(raster));
        when(rasterpauseRepository.findByStundenraster(raster)).thenReturn(List.of(pause));
        when(zeitslotRepository.findAll()).thenReturn(List.of(zeitslot));

        StundenrasterDTO result = service.stundenraster();

        assertEquals(1, result.getPausen().size());
        assertEquals(1, result.getZeitslots().size());
        assertEquals(20, result.getPausen().get(0).getDauerMinuten());
    }

    @Test
    void stundenrasterSpeichernWirftExceptionWennStundenplanExistiert() {
        when(stundenplanRepository.findAll()).thenReturn(List.of(new Stundenplan()));

        assertThrows(RuntimeException.class, () -> service.stundenrasterSpeichern(new StundenrasterDTO()));
    }

    @Test
    void stundenrasterSpeichernBerechnetZeitslotsAusRasterUndPausen() {
        StundenrasterDTO update = new StundenrasterDTO();
        update.setSchulbeginn(LocalTime.of(8, 0));
        update.setStundenProTag(2);
        update.setStundendauer(45);
        RasterpauseDTO pauseDto = new RasterpauseDTO();
        pauseDto.setNachStunde(1);
        pauseDto.setDauerMinuten(20);
        update.setPausen(List.of(pauseDto));

        when(stundenplanRepository.findAll()).thenReturn(List.of());
        when(stundenrasterRepository.findAll()).thenReturn(List.of());
        when(stundenrasterRepository.save(any(Stundenraster.class))).thenAnswer(inv -> inv.getArgument(0));
        when(rasterpauseRepository.findByStundenraster(any())).thenReturn(List.of());
        when(rasterpauseRepository.save(any(Rasterpause.class))).thenAnswer(inv -> inv.getArgument(0));
        when(zeitslotRepository.findAll()).thenReturn(List.of());
        when(zeitslotRepository.save(any(Zeitslot.class))).thenAnswer(inv -> inv.getArgument(0));

        StundenrasterDTO result = service.stundenrasterSpeichern(update);

        assertEquals(2, result.getZeitslots().size());
        assertEquals(LocalTime.of(8, 0), result.getZeitslots().get(0).getStart());
        assertEquals(LocalTime.of(8, 45), result.getZeitslots().get(0).getEnde());
        // 2. Stunde beginnt erst nach der 20-Min-Pause nach Stunde 1
        assertEquals(LocalTime.of(9, 5), result.getZeitslots().get(1).getStart());
    }

    @Test
    void stundenrasterSpeichernLoeschtVerfuegbarkeitenDerAltenZeitslotsVorDemLoeschen() {
        StundenrasterDTO update = new StundenrasterDTO();
        update.setSchulbeginn(LocalTime.of(8, 0));
        update.setStundenProTag(1);
        update.setStundendauer(45);

        Zeitslot alterSlot = new Zeitslot();
        alterSlot.setId(1L);
        Lehrerverfuegbarkeit verf = new Lehrerverfuegbarkeit();

        when(stundenplanRepository.findAll()).thenReturn(List.of());
        when(stundenrasterRepository.findAll()).thenReturn(List.of());
        when(stundenrasterRepository.save(any(Stundenraster.class))).thenAnswer(inv -> inv.getArgument(0));
        when(rasterpauseRepository.findByStundenraster(any())).thenReturn(List.of());
        when(zeitslotRepository.findAll()).thenReturn(List.of(alterSlot));
        when(lehrerverfuegbarkeitRepository.findByZeitslot(alterSlot)).thenReturn(List.of(verf));
        when(raumverfuegbarkeitRepository.findByZeitslot(alterSlot)).thenReturn(List.of());
        when(zeitslotRepository.save(any(Zeitslot.class))).thenAnswer(inv -> inv.getArgument(0));

        service.stundenrasterSpeichern(update);

        verify(lehrerverfuegbarkeitRepository).deleteAll(List.of(verf));
        verify(zeitslotRepository).deleteAll(List.of(alterSlot));
    }

    //pausenregeln()
    @Test
    void pausenregelnMapptAlleFelderKorrekt() {
        Jahrgangsstufe stufe = new Jahrgangsstufe();
        stufe.setId(1L);

        Pausenregel regel = new Pausenregel();
        regel.setId(1L);
        regel.setMindestpauseNachStunden(2);
        regel.setMindestdauerMinuten(15);
        regel.setJahrgangsstufe(stufe);
        regel.setAktiv(true);

        when(pausenregelRepository.findAll()).thenReturn(List.of(regel));

        List<PausenregelDTO> result = service.pausenregeln();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getJahrgangsstufeId());
        assertEquals(15, result.get(0).getMindestdauerMinuten());
    }

    //maximalbelastungen(), und anlegen, bearbeiten, löschen
    @Test
    void maximalbelastungenMapptListeKorrekt() {
        Jahrgangsstufe stufe = new Jahrgangsstufe();
        stufe.setId(1L);
        Maximalbelastung regel = new Maximalbelastung();
        regel.setId(1L);
        regel.setJahrgangsstufe(stufe);
        regel.setMaxStundenTag(6);
        regel.setMaxStundenWoche(30);

        when(maximalbelastungRepository.findAll()).thenReturn(List.of(regel));

        List<MaximalbelastungDTO> result = service.maximalbelastungen();

        assertEquals(1, result.size());
        assertEquals(30, result.get(0).getMaxStundenWoche());
    }

    @Test
    void maximalbelastungAnlegenSetztHaerteUndGewichtAlsDefault() {
        Jahrgangsstufe stufe = new Jahrgangsstufe();
        stufe.setId(1L);
        MaximalbelastungDTO dto = new MaximalbelastungDTO();
        dto.setJahrgangsstufeId(1L);
        dto.setMaxStundenTag(6);
        dto.setMaxStundenWoche(30);
        dto.setAktiv(true);

        when(jahrgangsstufeRepository.findById(1L)).thenReturn(Optional.of(stufe));
        when(maximalbelastungRepository.save(any(Maximalbelastung.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Maximalbelastung> captor = ArgumentCaptor.forClass(Maximalbelastung.class);
        service.maximalbelastungAnlegen(dto);
        verify(maximalbelastungRepository).save(captor.capture());

        assertEquals(Haerte.WEICH, captor.getValue().getHaerte());
        assertEquals(30, captor.getValue().getMaxStundenWoche());
    }

    @Test
    void maximalbelastungAnlegenWirftExceptionBeiUnbekannterJahrgangsstufe() {
        MaximalbelastungDTO dto = new MaximalbelastungDTO();
        dto.setJahrgangsstufeId(99L);

        when(jahrgangsstufeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.maximalbelastungAnlegen(dto));
    }

    @Test
    void maximalbelastungBearbeitenAktualisiertWerteOhneHaerteZuAendern() {
        Jahrgangsstufe stufe = new Jahrgangsstufe();
        stufe.setId(1L);
        Maximalbelastung bestehend = new Maximalbelastung();
        bestehend.setId(1L);
        bestehend.setJahrgangsstufe(stufe);
        bestehend.setHaerte(Haerte.WEICH);

        MaximalbelastungDTO update = new MaximalbelastungDTO();
        update.setMaxStundenTag(7);
        update.setMaxStundenWoche(32);
        update.setAktiv(false);

        when(maximalbelastungRepository.findById(1L)).thenReturn(Optional.of(bestehend));
        when(maximalbelastungRepository.save(any(Maximalbelastung.class))).thenAnswer(inv -> inv.getArgument(0));

        MaximalbelastungDTO result = service.maximalbelastungBearbeiten(1L, update);

        assertEquals(32, result.getMaxStundenWoche());
        assertEquals(Haerte.WEICH, bestehend.getHaerte()); // unveraendert
    }

    @Test
    void maximalbelastungLoeschenRuftDeleteByIdAuf() {
        service.maximalbelastungLoeschen(1L);

        verify(maximalbelastungRepository).deleteById(1L);
    }

    //pflichtfaecher(), und anlegen, bearbeiten, löschen
    private Fach baueFach(Long id, boolean hauptfach) {
        Fach fach = new Fach();
        fach.setId(id);
        fach.setKuerzel("MA");
        fach.setHauptfach(hauptfach);
        return fach;
    }

    private Jahrgangsstufe baueJahrgangsstufe(Long id, int nummer) {
        Jahrgangsstufe stufe = new Jahrgangsstufe();
        stufe.setId(id);
        stufe.setNummer(nummer);
        return stufe;
    }

    @Test
    void pflichtfaecherGruppiertMehrereStufenUnterDemselbenFach() {
        Fach fach = baueFach(1L, false);
        Jahrgangsstufe stufe5 = baueJahrgangsstufe(1L, 5);
        Jahrgangsstufe stufe6 = baueJahrgangsstufe(2L, 6);

        Pflichtfachvorgabe vorgabe1 = new Pflichtfachvorgabe();
        vorgabe1.setFach(fach);
        vorgabe1.setJahrgangsstufe(stufe5);
        Pflichtfachvorgabe vorgabe2 = new Pflichtfachvorgabe();
        vorgabe2.setFach(fach);
        vorgabe2.setJahrgangsstufe(stufe6);

        when(pflichtfachvorgabeRepository.findAll()).thenReturn(List.of(vorgabe1, vorgabe2));
        when(blockregelRepository.findByFachAndJahrgangsstufe(any(), any())).thenReturn(Optional.empty());

        List<PflichtfachvorgabeDTO> result = service.pflichtfaecher();

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getStufen().size());
        assertFalse(result.get(0).isHauptfach());
    }

    @Test
    void anlegenBeiHauptfachWirftException() {
        Fach hauptfach = baueFach(1L, true);
        when(fachRepository.findById(1L)).thenReturn(Optional.of(hauptfach));

        StufenvorgabeDTO neu = new StufenvorgabeDTO();
        neu.setJahrgangsstufeId(1L);

        assertThrows(RuntimeException.class, () -> service.pflichtfachStufenvorgabeAnlegen(1L, neu));
    }

    @Test
    void anlegenBeiNichtHauptfachErstelltBlockregelWennDoppelstundeGewuenscht() {
        Fach fach = baueFach(1L, false);
        Jahrgangsstufe stufe = baueJahrgangsstufe(1L, 5);
        StufenvorgabeDTO neu = new StufenvorgabeDTO();
        neu.setJahrgangsstufeId(1L);
        neu.setStundenanzahl(4);
        neu.setDoppelstundenBevorzugt(true);

        when(fachRepository.findById(1L)).thenReturn(Optional.of(fach));
        when(jahrgangsstufeRepository.findById(1L)).thenReturn(Optional.of(stufe));
        when(pflichtfachvorgabeRepository.save(any(Pflichtfachvorgabe.class))).thenAnswer(inv -> inv.getArgument(0));
        when(blockregelRepository.findByFachAndJahrgangsstufe(fach, stufe)).thenReturn(Optional.empty());

        service.pflichtfachStufenvorgabeAnlegen(1L, neu);

        verify(blockregelRepository).save(any(Blockregel.class));
    }

    @Test
    void bearbeitenBeiHauptfachAendertNurDoppelstundeNichtStundenanzahl() {
        Fach hauptfach = baueFach(1L, true);
        Jahrgangsstufe stufe = baueJahrgangsstufe(1L, 5);
        Pflichtfachvorgabe bestehend = new Pflichtfachvorgabe();
        bestehend.setId(10L);
        bestehend.setFach(hauptfach);
        bestehend.setJahrgangsstufe(stufe);
        bestehend.setStundenanzahl(4);

        StufenvorgabeDTO update = new StufenvorgabeDTO();
        update.setStundenanzahl(999); // sollte ignoriert werden
        update.setDoppelstundenBevorzugt(true);

        when(pflichtfachvorgabeRepository.findById(10L)).thenReturn(Optional.of(bestehend));
        when(blockregelRepository.findByFachAndJahrgangsstufe(hauptfach, stufe)).thenReturn(Optional.empty());

        service.pflichtfachStufenvorgabeBearbeiten(1L, 10L, update);

        assertEquals(4, bestehend.getStundenanzahl()); // unveraendert
        verify(pflichtfachvorgabeRepository, never()).save(any(Pflichtfachvorgabe.class));
        verify(blockregelRepository).save(any(Blockregel.class));
    }

    @Test
    void loeschenBeiHauptfachWirftException() {
        Fach hauptfach = baueFach(1L, true);
        Pflichtfachvorgabe vorgabe = new Pflichtfachvorgabe();
        vorgabe.setId(10L);
        vorgabe.setFach(hauptfach);

        when(pflichtfachvorgabeRepository.findById(10L)).thenReturn(Optional.of(vorgabe));

        assertThrows(RuntimeException.class, () -> service.pflichtfachStufenvorgabeLoeschen(1L, 10L));
    }

    @Test
    void loeschenBeiNichtHauptfachEntferntBlockregelUndVorgabe() {
        Fach fach = baueFach(1L, false);
        Jahrgangsstufe stufe = baueJahrgangsstufe(1L, 5);
        Pflichtfachvorgabe vorgabe = new Pflichtfachvorgabe();
        vorgabe.setId(10L);
        vorgabe.setFach(fach);
        vorgabe.setJahrgangsstufe(stufe);

        Blockregel bestehendeRegel = new Blockregel();
        bestehendeRegel.setId(5L);

        when(pflichtfachvorgabeRepository.findById(10L)).thenReturn(Optional.of(vorgabe));
        when(blockregelRepository.findByFachAndJahrgangsstufe(fach, stufe)).thenReturn(Optional.of(bestehendeRegel));

        service.pflichtfachStufenvorgabeLoeschen(1L, 10L);

        verify(blockregelRepository).deleteById(5L);
        verify(pflichtfachvorgabeRepository).deleteById(10L);
    }

    //vertretungsprioritaet(), vertretungsprioritaetSpeichern()
    @Test
    void vertretungsprioritaetSortiertNachPrioAufsteigend() {
        Jahrgangsstufe stufe1 = baueJahrgangsstufe(1L, 5);
        stufe1.setPrio(3);
        Jahrgangsstufe stufe2 = baueJahrgangsstufe(2L, 6);
        stufe2.setPrio(1);
        Jahrgangsstufe stufe3 = baueJahrgangsstufe(3L, 7);
        stufe3.setPrio(2);

        when(jahrgangsstufeRepository.findAll()).thenReturn(List.of(stufe1, stufe2, stufe3));

        List<VertretungsprioritaetDTO> result = service.vertretungsprioritaet();

        assertEquals(2L, result.get(0).getJahrgangsstufeId()); // prio 1
        assertEquals(3L, result.get(1).getJahrgangsstufeId()); // prio 2
        assertEquals(1L, result.get(2).getJahrgangsstufeId()); // prio 3
    }

    @Test
    void vertretungsprioritaetSpeichernVergibtPrioNachListenposition() {
        VertretungsprioritaetDTO eintrag1 = new VertretungsprioritaetDTO();
        eintrag1.setJahrgangsstufeId(2L);
        VertretungsprioritaetDTO eintrag2 = new VertretungsprioritaetDTO();
        eintrag2.setJahrgangsstufeId(1L);

        Jahrgangsstufe stufe2 = baueJahrgangsstufe(2L, 6);
        Jahrgangsstufe stufe1 = baueJahrgangsstufe(1L, 5);

        when(jahrgangsstufeRepository.findById(2L)).thenReturn(Optional.of(stufe2));
        when(jahrgangsstufeRepository.findById(1L)).thenReturn(Optional.of(stufe1));
        when(jahrgangsstufeRepository.save(any(Jahrgangsstufe.class))).thenAnswer(inv -> inv.getArgument(0));

        List<VertretungsprioritaetDTO> result = service.vertretungsprioritaetSpeichern(List.of(eintrag1, eintrag2));

        assertEquals(1, result.get(0).getPrio()); // erste Position -> prio 1
        assertEquals(2, result.get(1).getPrio()); // zweite Position -> prio 2
    }

    @Test
    void vertretungsprioritaetSpeichernWirftExceptionBeiUnbekannterJahrgangsstufe() {
        VertretungsprioritaetDTO eintrag = new VertretungsprioritaetDTO();
        eintrag.setJahrgangsstufeId(99L);

        when(jahrgangsstufeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.vertretungsprioritaetSpeichern(List.of(eintrag)));
    }
}
