package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.lerngruppen.AenderungsVorschlagDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.GenerierungGestartetDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.GenerierungStatusDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.UnterrichtsstundeInitialAendernDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StundenplanServiceTest {

        @Mock GenerierungslaufRepository generierungslaufRepository;
        @Mock GenerierungAsyncService generierungAsyncService;
        @Mock StundenplanRepository stundenplanRepository;
        @Mock LerngruppeRepository lerngruppeRepository;
        @Mock LerngruppeService lerngruppeService;
        @Mock UnterrichtsstundeRepository unterrichtsstundeRepository;
        @Mock LehrkraftRepository lehrkraftRepository;
        @Mock RaumRepository raumRepository;
        @Mock AccountRepository accountRepository;
        @Mock GenerierungAnlegenService generierungAnlegenService;
        @InjectMocks StundenplanService service;


    //generieren und Status abfragen
    @Test
    void generierungStartenRuftAnlegenUndAsyncStartenAuf() {
        when(generierungAnlegenService.generierungAnlegen()).thenReturn(5L);

        GenerierungGestartetDTO result = service.generierungStarten();

        assertEquals(5L, result.getGenerationId());
        verify(generierungAsyncService).generierungAsyncStarten(5L);
        }


    @Test
    void generierungStatusGibtStatusDesLaufsZurueck() {
        Generierungslauf lauf = new Generierungslauf();
        lauf.setId(1L);
        lauf.setStatus(GenerierungsStatus.FERTIG);

        when(generierungslaufRepository.findById(1L)).thenReturn(Optional.of(lauf));

        GenerierungStatusDTO result = service.generierungStatus(1L);

        assertEquals("FERTIG", result.getStatus());
    }

    @Test
    void generierungStatusWirftExceptionBeiUnbekannterId() {
        when(generierungslaufRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.generierungStatus(99L));
    }

    @Test
    void aktuelleOffeneGenerierungGibtNeuestenLaufMitOffenenEntwuerfenZurueck() {
        Generierungslauf aelter = new Generierungslauf();
        aelter.setId(1L);
        aelter.setStatus(GenerierungsStatus.FERTIG);
        aelter.setGestartetAm(LocalDateTime.now().minusHours(2));

        Generierungslauf neuer = new Generierungslauf();
        neuer.setId(2L);
        neuer.setStatus(GenerierungsStatus.FERTIG);
        neuer.setGestartetAm(LocalDateTime.now());

        Stundenplan entwurf = new Stundenplan();
        entwurf.setStatus(PlanStatus.ENTWURF);

        when(generierungslaufRepository.findAll()).thenReturn(List.of(aelter, neuer));
        when(stundenplanRepository.findByGenerationId(1L)).thenReturn(List.of(entwurf));
        when(stundenplanRepository.findByGenerationId(2L)).thenReturn(List.of(entwurf));

        Long result = service.aktuelleOffeneGenerierung();

        assertEquals(2L, result);
    }

    @Test
    void aktuelleOffeneGenerierungIgnoriertVeroeffentlichteGenerierung() {
        Generierungslauf lauf = new Generierungslauf();
        lauf.setId(1L);
        lauf.setStatus(GenerierungsStatus.FERTIG);

        Stundenplan aktiv = new Stundenplan();
        aktiv.setStatus(PlanStatus.AKTIV);

        when(generierungslaufRepository.findAll()).thenReturn(List.of(lauf));
        when(stundenplanRepository.findByGenerationId(1L)).thenReturn(List.of(aktiv));

        assertThrows(RuntimeException.class, () -> service.aktuelleOffeneGenerierung());
    }

    //Stundenpläne veröffentlichen
    private Stundenplan baueStundenplanMitLerngruppe(
            Long id,
            PlanStatus status,
            Long lerngruppeId) {

        Lerngruppe lg = new Lerngruppe();
        lg.setId(lerngruppeId);
        lg.setBezeichnung("7a");

        Stundenplan plan = new Stundenplan();
        plan.setId(id);
        plan.setStatus(status);
        plan.setLerngruppe(lg);

        return plan;
    }

    @Test
    void veroeffentlichenWirftExceptionWennKeinePlaeneGefundenWerden() {
        when(stundenplanRepository.findByGenerationId(1L))
                .thenReturn(List.of());

        assertThrows(
                RuntimeException.class,
                () -> service.finaleStundenplaeneVeroeffentlichen(
                        1L, 1, "admin")
        );

        verifyNoInteractions(accountRepository);
    }

    @Test
    void veroeffentlichenAktiviertGewaehlteVarianteUndLoeschtRest() {
        Stundenplan variante1 =
                baueStundenplanMitLerngruppe(1L, PlanStatus.ENTWURF, 1L);
        variante1.setVariante(1);
        variante1.setUnterrichtsstunden(new ArrayList<>());

        Stundenplan variante2 =
                baueStundenplanMitLerngruppe(2L, PlanStatus.ENTWURF, 1L);
        variante2.setVariante(2);
        variante2.setUnterrichtsstunden(new ArrayList<>());

        Account ersteller = new Account();
        ersteller.setId(10L);

        when(stundenplanRepository.findByGenerationId(1L))
                .thenReturn(List.of(variante1, variante2));
        when(accountRepository.findByBenutzername("admin"))
                .thenReturn(Optional.of(ersteller));
        when(stundenplanRepository.findByStatus(PlanStatus.AKTIV))
                .thenReturn(List.of());

        service.finaleStundenplaeneVeroeffentlichen(1L, 1, "admin");

        assertEquals(PlanStatus.AKTIV, variante1.getStatus());
        assertSame(ersteller, variante1.getErsteller());

        verify(stundenplanRepository).save(variante1);
        verify(stundenplanRepository).delete(variante2);
    }

    @Test
    void veroeffentlichenArchiviertBisherAktivePlaene() {
        Stundenplan alterAktiver = new Stundenplan();
        alterAktiver.setId(50L);
        alterAktiver.setStatus(PlanStatus.AKTIV);

        Stundenplan neueVariante =
                baueStundenplanMitLerngruppe(1L, PlanStatus.ENTWURF, 1L);
        neueVariante.setVariante(1);
        neueVariante.setUnterrichtsstunden(new ArrayList<>());

        Account ersteller = new Account();
        ersteller.setId(10L);

        when(stundenplanRepository.findByGenerationId(1L))
                .thenReturn(List.of(neueVariante));
        when(accountRepository.findByBenutzername("admin"))
                .thenReturn(Optional.of(ersteller));
        when(stundenplanRepository.findByStatus(PlanStatus.AKTIV))
                .thenReturn(List.of(alterAktiver));

        service.finaleStundenplaeneVeroeffentlichen(1L, 1, "admin");

        assertEquals(PlanStatus.ARCHIVIERT, alterAktiver.getStatus());
        verify(stundenplanRepository).save(alterAktiver);
    }

    @Test
    void veroeffentlichenLoeschtUnterrichtsstundenDerNichtGewaehltenVariante() {
        Stundenplan gewaehlt =
                baueStundenplanMitLerngruppe(1L, PlanStatus.ENTWURF, 1L);
        gewaehlt.setVariante(1);
        gewaehlt.setUnterrichtsstunden(new ArrayList<>());

        Unterrichtsstunde stundeVonNichtGewaehlter =
                new Unterrichtsstunde();

        Stundenplan nichtGewaehlt =
                baueStundenplanMitLerngruppe(2L, PlanStatus.ENTWURF, 1L);
        nichtGewaehlt.setVariante(2);
        nichtGewaehlt.setUnterrichtsstunden(
                List.of(stundeVonNichtGewaehlter));

        Account ersteller = new Account();
        ersteller.setId(10L);

        when(stundenplanRepository.findByGenerationId(1L))
                .thenReturn(List.of(gewaehlt, nichtGewaehlt));
        when(accountRepository.findByBenutzername("admin"))
                .thenReturn(Optional.of(ersteller));
        when(stundenplanRepository.findByStatus(PlanStatus.AKTIV))
                .thenReturn(List.of());

        service.finaleStundenplaeneVeroeffentlichen(1L, 1, "admin");

        verify(unterrichtsstundeRepository)
                .deleteAll(List.of(stundeVonNichtGewaehlter));
        verify(stundenplanRepository).delete(nichtGewaehlt);
    }

    @Test
    void veroeffentlichenWirftExceptionWennErstellerNichtGefundenWird() {
        Stundenplan plan =
                baueStundenplanMitLerngruppe(1L, PlanStatus.ENTWURF, 1L);
        plan.setVariante(1);
        plan.setUnterrichtsstunden(new ArrayList<>());

        when(stundenplanRepository.findByGenerationId(1L))
                .thenReturn(List.of(plan));
        when(accountRepository.findByBenutzername("unbekannt"))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.finaleStundenplaeneVeroeffentlichen(
                        1L, 1, "unbekannt")
        );

        verify(stundenplanRepository, never()).save(any(Stundenplan.class));
        verify(stundenplanRepository, never()).delete(any(Stundenplan.class));
    }

    @Test
        void veroeffentlichenWirftExceptionBeiUngueltigerVarianteOhneDatenZuZerstoeren() {
        Stundenplan variante1 = baueStundenplanMitLerngruppe(1L, PlanStatus.ENTWURF, 1L);
        variante1.setVariante(1);
        variante1.setUnterrichtsstunden(new ArrayList<>());

        when(stundenplanRepository.findByGenerationId(1L)).thenReturn(List.of(variante1));

        assertThrows(RuntimeException.class,
                () -> service.finaleStundenplaeneVeroeffentlichen(1L, 99, "planer1"));

        verify(stundenplanRepository, never()).save(any(Stundenplan.class));
        verify(stundenplanRepository, never()).delete(any(Stundenplan.class));
        verifyNoInteractions(accountRepository);
        }

    //Änderungsvorschlag anzeigen
    private Unterrichtsstunde baueUnterrichtsstunde(
            Long id,
            PlanStatus planStatus) {

        Stundenplan plan = new Stundenplan();
        plan.setStatus(planStatus);
        plan.setStartdatum(LocalDate.of(2026, 9, 1));

        Fach fach = new Fach();
        fach.setId(1L);

        Zeitslot zeitslot = new Zeitslot();
        zeitslot.setId(1L);

        Unterrichtsstunde stunde = new Unterrichtsstunde();
        stunde.setId(id);
        stunde.setStundenplan(plan);
        stunde.setFach(fach);
        stunde.setZeitslot(zeitslot);
        stunde.setWochentag(Wochentag.MONTAG);

        return stunde;
    }

    @Test
    void vorschlagAnzeigenWirftExceptionBeiAktivemStundenplan() {
        Unterrichtsstunde stunde =
                baueUnterrichtsstunde(1L, PlanStatus.AKTIV);

        when(unterrichtsstundeRepository.findById(1L))
                .thenReturn(Optional.of(stunde));

        assertThrows(
                RuntimeException.class,
                () -> service.aenderungsVorschlagAnzeigen(1L)
        );

        verifyNoInteractions(lerngruppeService);
    }

    @Test
    void vorschlagAnzeigenMapptRaeumeUndLehrkraefteKorrekt() {
        Unterrichtsstunde stunde =
                baueUnterrichtsstunde(1L, PlanStatus.ENTWURF);

        Raum raum = new Raum();
        raum.setId(2L);
        raum.setBezeichnung("R103");

        Account account = new Account();
        account.setVorname("Klaus");
        account.setName("Klein");

        Lehrkraft lehrkraft = new Lehrkraft();
        lehrkraft.setId(3L);
        lehrkraft.setAccount(account);

        when(unterrichtsstundeRepository.findById(1L))
                .thenReturn(Optional.of(stunde));

        when(lerngruppeService.fachraeumeVerfuegbar(any(), any(), any()))
                .thenReturn(List.of(raum));

        when(lerngruppeService.fachlehrkraefteVerfuegbar(
                any(), any(), any(), any()))
                .thenReturn(List.of(lehrkraft));

        AenderungsVorschlagDTO result =
                service.aenderungsVorschlagAnzeigen(1L);

        assertEquals(1, result.getRaumalternativen().size());
        assertEquals(
                "R103",
                result.getRaumalternativen().get(0).getBezeichnung()
        );

        assertEquals(1, result.getLehrkraftalternativen().size());
        assertEquals(
                "Klein",
                result.getLehrkraftalternativen().get(0).getNachname()
        );
    }

    @Test
    void vorschlagAnzeigenWirftExceptionBeiUnbekannterUnterrichtsstunde() {
        when(unterrichtsstundeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.aenderungsVorschlagAnzeigen(99L)
        );
    }

    //Unterrichtsstunden ändern
    @Test
    void aendernWirftExceptionBeiAktivemStundenplan() {
        Unterrichtsstunde stunde =
                baueUnterrichtsstunde(1L, PlanStatus.AKTIV);

        UnterrichtsstundeInitialAendernDTO update =
                new UnterrichtsstundeInitialAendernDTO(2L, null);

        when(unterrichtsstundeRepository.findById(1L))
                .thenReturn(Optional.of(stunde));

        assertThrows(
                RuntimeException.class,
                () -> service.unterrichtsstundeAendern(1L, update)
        );

        verify(unterrichtsstundeRepository, never())
                .save(any(Unterrichtsstunde.class));
    }

    @Test
    void aendernSetztNurLehrkraftWennNurLehrkraftIdGesetztIst() {
        Unterrichtsstunde stunde =
                baueUnterrichtsstunde(1L, PlanStatus.ENTWURF);

        Raum urspruenglicherRaum = new Raum();
        urspruenglicherRaum.setId(5L);
        stunde.setRaum(urspruenglicherRaum);

        Lehrkraft neueLehrkraft = new Lehrkraft();
        neueLehrkraft.setId(2L);

        UnterrichtsstundeInitialAendernDTO update =
                new UnterrichtsstundeInitialAendernDTO(2L, null);

        when(unterrichtsstundeRepository.findById(1L))
                .thenReturn(Optional.of(stunde));

        when(lehrkraftRepository.findById(2L))
                .thenReturn(Optional.of(neueLehrkraft));

        service.unterrichtsstundeAendern(1L, update);

        assertEquals(2L, stunde.getLehrkraft().getId());
        assertEquals(5L, stunde.getRaum().getId());

        verify(raumRepository, never()).findById(anyLong());
    }

    @Test
    void aendernSetztLehrkraftUndRaumWennBeideIdsGesetztSind() {
        Unterrichtsstunde stunde =
                baueUnterrichtsstunde(1L, PlanStatus.ENTWURF);

        Lehrkraft neueLehrkraft = new Lehrkraft();
        neueLehrkraft.setId(2L);

        Raum neuerRaum = new Raum();
        neuerRaum.setId(3L);

        UnterrichtsstundeInitialAendernDTO update =
                new UnterrichtsstundeInitialAendernDTO(2L, 3L);

        when(unterrichtsstundeRepository.findById(1L))
                .thenReturn(Optional.of(stunde));

        when(lehrkraftRepository.findById(2L))
                .thenReturn(Optional.of(neueLehrkraft));

        when(raumRepository.findById(3L))
                .thenReturn(Optional.of(neuerRaum));

        service.unterrichtsstundeAendern(1L, update);

        assertEquals(2L, stunde.getLehrkraft().getId());
        assertEquals(3L, stunde.getRaum().getId());

        verify(unterrichtsstundeRepository).save(stunde);
    }

    @Test
    void aendernWirftExceptionBeiUnbekannterLehrkraftId() {
        Unterrichtsstunde stunde =
                baueUnterrichtsstunde(1L, PlanStatus.ENTWURF);

        UnterrichtsstundeInitialAendernDTO update =
                new UnterrichtsstundeInitialAendernDTO(99L, null);

        when(unterrichtsstundeRepository.findById(1L))
                .thenReturn(Optional.of(stunde));

        when(lehrkraftRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> service.unterrichtsstundeAendern(1L, update)
        );
    }

    @Test
    void schuljahrZurueckgebenGibtSchuljahrDesAktivenStundenplansZurueck() {
        Stundenplan stundenplan = new Stundenplan();
        stundenplan.setSchuljahr("2026/27");
        stundenplan.setStatus(PlanStatus.AKTIV);

        when(stundenplanRepository.findByStatus(PlanStatus.AKTIV))
                .thenReturn(List.of(stundenplan));

        String result = service.schuljahrZurueckgeben();

        assertEquals("2026/27", result);

        verify(stundenplanRepository)
                .findByStatus(PlanStatus.AKTIV);
    }

    @Test
    void schuljahrZurueckgebenGibtHinweisZurueckWennKeineAktivenStundenplaeneExistieren() {
        when(stundenplanRepository.findByStatus(PlanStatus.AKTIV))
                .thenReturn(List.of());

        String result = service.schuljahrZurueckgeben();

        assertEquals(
                "Keine aktiven Stundenpläne in diesem Schuljahr. Schuljahr nicht bestimmbar.",
                result
        );

        verify(stundenplanRepository)
                .findByStatus(PlanStatus.AKTIV);
    }
}