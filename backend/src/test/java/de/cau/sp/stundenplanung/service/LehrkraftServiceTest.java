package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftAnlegenDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftBearbeitenDTO;
import de.cau.sp.stundenplanung.dto.verfuegbarkeiten.VerfuegbarkeitEingabeDTO;
import de.cau.sp.stundenplanung.dto.verfuegbarkeiten.VerfuegbarkeitFuerLehrkraftDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LehrkraftServiceTest {

    @Mock LehrkraftRepository lehrkraftRepository;
    @Mock AccountRepository accountRepository;
    @Mock Generieren generieren;
    @Mock LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock FachRepository fachRepository;
    @Mock ZeitslotRepository zeitslotRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks LehrkraftService service;

    //alle()/alleDeaktivierte()
    private Lehrkraft baueLehrkraft(Long id, String name, boolean aktiv) {
        Account account = new Account();
        account.setName(name);
        account.setVorname("Vorname");
        account.setAktiv(aktiv);

        Lehrkraft lehrkraft = new Lehrkraft();
        lehrkraft.setId(id);
        lehrkraft.setAccount(account);
        lehrkraft.setKuerzel("AB");
        lehrkraft.setAnstellungsverhaeltnis(Anstellungsverhaeltnis.VOLLZEIT);
        lehrkraft.setDeputatsstunden(25);
        lehrkraft.setFaecher(new ArrayList<>());
        lehrkraft.setPraeferenzen(new ArrayList<>()); 
        return lehrkraft;
    }

    @Test
    void alleGibtNurAktiveLehrkraefteAlsDTOZurueck() {
        Lehrkraft lehrkraft = baueLehrkraft(1L, "Meyer", true);

        when(lehrkraftRepository.findByAccount_Aktiv(true)).thenReturn(List.of(lehrkraft));
        when(lerngruppeRepository.findByKlassenleitung(lehrkraft)).thenReturn(null);
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(lehrkraft)).thenReturn(List.of());

        List<LehrkraftAnzeigenDTO> result = service.alle();

        assertEquals(1, result.size());
        assertEquals("Meyer", result.get(0).getName());
        assertTrue(result.get(0).isAktiv());
        verify(lehrkraftRepository).findByAccount_Aktiv(true);
    }

    @Test
    void alleGibtLeereListeZurueckWennKeineAktivenLehrkraefteExistieren() {
        when(lehrkraftRepository.findByAccount_Aktiv(true)).thenReturn(List.of());

        List<LehrkraftAnzeigenDTO> result = service.alle();

        assertTrue(result.isEmpty());
    }

    @Test
    void alleDeaktiviertenRuftRepositoryMitFalseAuf() {
        Lehrkraft lehrkraft = baueLehrkraft(2L, "Schulz", false);

        when(lehrkraftRepository.findByAccount_Aktiv(false)).thenReturn(List.of(lehrkraft));
        when(lerngruppeRepository.findByKlassenleitung(lehrkraft)).thenReturn(null);
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(lehrkraft)).thenReturn(List.of());

        List<LehrkraftAnzeigenDTO> result = service.alleDeaktivierten();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isAktiv());
        verify(lehrkraftRepository).findByAccount_Aktiv(false);
        verify(lehrkraftRepository, never()).findByAccount_Aktiv(true);
    }

    @Test
    void alleMappedFaecherKorrektZuFachUebersichtDTO() {
        Fach mathe = new Fach();
        mathe.setId(10L);
        mathe.setKuerzel("MA");
        mathe.setName("Mathematik");

        Lehrkraft lehrkraft = baueLehrkraft(3L, "Klein", true);
        lehrkraft.setFaecher(List.of(mathe));

        when(lehrkraftRepository.findByAccount_Aktiv(true)).thenReturn(List.of(lehrkraft));
        when(lerngruppeRepository.findByKlassenleitung(lehrkraft)).thenReturn(null);
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(lehrkraft)).thenReturn(List.of());

        List<LehrkraftAnzeigenDTO> result = service.alle();

        assertEquals(1, result.get(0).getFaecher().size());
        assertEquals(10L, result.get(0).getFaecher().get(0).getId());
        assertEquals("MA", result.get(0).getFaecher().get(0).getKuerzel());
        assertEquals("Mathematik", result.get(0).getFaecher().get(0).getName());
    }

    //anlegen()
    private LehrkraftAnlegenDTO baueAnlegenDTO() {
        LehrkraftAnlegenDTO dto = new LehrkraftAnlegenDTO();
        dto.setName("Meyer");
        dto.setVorname("Petra");
        dto.setPasswort("start1234");
        dto.setAnstellungsverhaeltnis(Anstellungsverhaeltnis.VOLLZEIT);
        dto.setDeputatsstunden(25);
        return dto;
    }

    @Test
    void anlegenErstelltAccountMitRolleLehrkraftUndAktivTrue() {
        LehrkraftAnlegenDTO dto = baueAnlegenDTO();

        when(generieren.benutzername("Petra", "Meyer")).thenReturn("petra-meyer");
        when(generieren.kuerzel("Petra", "Meyer")).thenReturn("PM");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lerngruppeRepository.findByKlassenleitung(any(Lehrkraft.class))).thenReturn(null);
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(any(Lehrkraft.class))).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("verschluesselt");

        service.anlegen(dto);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(Rolle.LEHRKRAFT, captor.getValue().getRolle());
        assertTrue(captor.getValue().isAktiv());
        assertEquals("petra-meyer", captor.getValue().getBenutzername());
    }

    @Test
    void anlegenMitFachIdsLaedtFaecherAusRepository() {
        LehrkraftAnlegenDTO dto = baueAnlegenDTO();
        dto.setFachIds(List.of(10L, 11L));

        Fach mathe = new Fach();
        mathe.setId(10L);
        Fach deutsch = new Fach();
        deutsch.setId(11L);

        when(generieren.benutzername(anyString(), anyString())).thenReturn("petra-meyer");
        when(generieren.kuerzel(anyString(), anyString())).thenReturn("PM");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(fachRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(mathe, deutsch));
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lerngruppeRepository.findByKlassenleitung(any(Lehrkraft.class))).thenReturn(null);
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(any(Lehrkraft.class))).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("verschluesselt");

        LehrkraftAnzeigenDTO result = service.anlegen(dto);

        assertEquals(2, result.getFaecher().size());
    }

    @Test
    void anlegenMitLerngruppeIdSetztKlassenleitung() {
        LehrkraftAnlegenDTO dto = baueAnlegenDTO();
        dto.setLerngruppeId(5L);

        Lerngruppe gruppe = new Lerngruppe();
        gruppe.setId(5L);

        when(generieren.benutzername(anyString(), anyString())).thenReturn("petra-meyer");
        when(generieren.kuerzel(anyString(), anyString())).thenReturn("PM");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lerngruppeRepository.findById(5L)).thenReturn(Optional.of(gruppe));
        when(lerngruppeRepository.findByKlassenleitung(any(Lehrkraft.class))).thenReturn(null);
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(any(Lehrkraft.class))).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("verschluesselt");

        service.anlegen(dto);

        ArgumentCaptor<Lerngruppe> captor = ArgumentCaptor.forClass(Lerngruppe.class);
        verify(lerngruppeRepository).save(captor.capture());
        assertNotNull(captor.getValue().getKlassenleitung());
    }

    @Test
    void anlegenMitUnbekannterLerngruppeIdWirftException() {
        LehrkraftAnlegenDTO dto = baueAnlegenDTO();
        dto.setLerngruppeId(99L);

        when(generieren.benutzername(anyString(), anyString())).thenReturn("petra-meyer");
        when(generieren.kuerzel(anyString(), anyString())).thenReturn("PM");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lerngruppeRepository.findById(99L)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("verschluesselt");
        
        assertThrows(RuntimeException.class, () -> service.anlegen(dto));
    }

    @Test
    void anlegenMitVerfuegbarkeitLaedtZeitslotKorrekt() {
        LehrkraftAnlegenDTO dto = baueAnlegenDTO();
        VerfuegbarkeitEingabeDTO verf = new VerfuegbarkeitEingabeDTO();
        verf.setZeitslotId(1L);
        verf.setWochentag(Wochentag.MONTAG);
        verf.setTyp(VerfuegbarkeitsTyp.GESPERRT);
        verf.setGanztaegig(false);
        dto.setVerfuegbarkeiten(List.of(verf));

        Zeitslot zeitslot = new Zeitslot();
        zeitslot.setId(1L);
        zeitslot.setStundennummer(1);

        when(generieren.benutzername(anyString(), anyString())).thenReturn("petra-meyer");
        when(generieren.kuerzel(anyString(), anyString())).thenReturn("PM");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lerngruppeRepository.findByKlassenleitung(any(Lehrkraft.class))).thenReturn(null);
        when(zeitslotRepository.findById(1L)).thenReturn(Optional.of(zeitslot));
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(any(Lehrkraft.class))).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("verschluesselt");

        service.anlegen(dto);

        verify(lehrerverfuegbarkeitRepository).save(any(Lehrerverfuegbarkeit.class));
    }

    @Test
    void anlegenMitGanztaegigerVerfuegbarkeitBrauchtKeinenZeitslot() {
        LehrkraftAnlegenDTO dto = baueAnlegenDTO();
        VerfuegbarkeitEingabeDTO ganztaegig = new VerfuegbarkeitEingabeDTO();
        ganztaegig.setGanztaegig(true);
        ganztaegig.setTyp(VerfuegbarkeitsTyp.GESPERRT);
        ganztaegig.setStartdatum(LocalDate.of(2026, 10, 1));
        ganztaegig.setEnddatum(LocalDate.of(2026, 10, 5));
        dto.setVerfuegbarkeiten(List.of(ganztaegig));

        when(generieren.benutzername(anyString(), anyString())).thenReturn("petra-meyer");
        when(generieren.kuerzel(anyString(), anyString())).thenReturn("PM");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lerngruppeRepository.findByKlassenleitung(any(Lehrkraft.class))).thenReturn(null);
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(any(Lehrkraft.class))).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("verschluesselt");

        service.anlegen(dto);

        ArgumentCaptor<Lehrerverfuegbarkeit> captor = ArgumentCaptor.forClass(Lehrerverfuegbarkeit.class);
        verify(lehrerverfuegbarkeitRepository).save(captor.capture());
        assertNull(captor.getValue().getZeitslot());
        verifyNoInteractions(zeitslotRepository);
    }

    //bearbeiten()
    private LehrkraftBearbeitenDTO baueBearbeitenDTO(boolean aktiv, Long lerngruppeId) {
        LehrkraftBearbeitenDTO dto = new LehrkraftBearbeitenDTO();
        dto.setName("Meyer");
        dto.setVorname("Petra");
        dto.setAktiv(aktiv);
        dto.setAnstellungsverhaeltnis(Anstellungsverhaeltnis.VOLLZEIT);
        dto.setDeputatsstunden(20);
        dto.setLerngruppeId(lerngruppeId);
        return dto;
    }

    @Test
    void bearbeitenWirftExceptionWennLehrkraftNichtGefunden() {
        when(lehrkraftRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.bearbeiten(1L, baueBearbeitenDTO(true, null)));
    }

    @Test
    void bearbeitenAktualisiertAccountUndLehrkraftFelder() {
        Lehrkraft lehrkraft = baueLehrkraft(1L, "Alt", true);
        LehrkraftBearbeitenDTO dto = baueBearbeitenDTO(true, null);

        when(lehrkraftRepository.findById(1L)).thenReturn(Optional.of(lehrkraft));
        when(generieren.benutzername("Petra", "Meyer")).thenReturn("petra-meyer");
        when(generieren.kuerzel("Petra", "Meyer")).thenReturn("PM");
        when(lerngruppeRepository.findByKlassenleitung(lehrkraft)).thenReturn(null);
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(lehrkraft)).thenReturn(List.of());

        LehrkraftAnzeigenDTO result = service.bearbeiten(1L, dto);

        assertEquals("Meyer", result.getName());
        assertEquals("PM", result.getKuerzel());
    }

    @Test
    void bearbeitenBeiDeaktivierungSetztAutomatischErsatzAlsKlassenleitung() {
        Lehrkraft lehrkraft = baueLehrkraft(1L, "Meyer", true);
        LehrkraftBearbeitenDTO dto = baueBearbeitenDTO(false, null); // war aktiv, wird jetzt deaktiviert

        Lerngruppe klassenleitungGroup = new Lerngruppe();
        klassenleitungGroup.setId(50L);

        Lehrkraft ersatzKandidat = baueLehrkraft(99L, "Klein", true);

        when(lehrkraftRepository.findById(1L)).thenReturn(Optional.of(lehrkraft));
        when(generieren.benutzername(anyString(), anyString())).thenReturn("petra-meyer");
        when(generieren.kuerzel(anyString(), anyString())).thenReturn("PM");
        when(lerngruppeRepository.findByKlassenleitung(lehrkraft)).thenReturn(klassenleitungGroup, (Lerngruppe) null);
        when(lehrkraftRepository.findAll()).thenReturn(List.of(ersatzKandidat));
        when(lerngruppeRepository.findByKlassenleitung(ersatzKandidat)).thenReturn(null);
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(lehrkraft)).thenReturn(List.of());

        service.bearbeiten(1L, dto);

        assertNotNull(klassenleitungGroup.getKlassenleitung());
        assertEquals(99L, klassenleitungGroup.getKlassenleitung().getId());
    }

    @Test
    void bearbeitenErsetztVerfuegbarkeitenKomplett() {
        Lehrkraft lehrkraft = baueLehrkraft(1L, "Meyer", true);
        LehrkraftBearbeitenDTO dto = baueBearbeitenDTO(true, null);
        VerfuegbarkeitEingabeDTO  neu = new VerfuegbarkeitEingabeDTO ();
        neu.setZeitslotId(1L);
        neu.setWochentag(Wochentag.MONTAG);
        neu.setTyp(VerfuegbarkeitsTyp.GESPERRT);
        neu.setGanztaegig(false);
        dto.setVerfuegbarkeiten(List.of(neu));

        Zeitslot zeitslot = new Zeitslot();
        zeitslot.setId(1L);
        zeitslot.setStundennummer(1);

        Lehrerverfuegbarkeit alte = new Lehrerverfuegbarkeit();
        alte.setZeitslot(zeitslot);
        
        when(lehrkraftRepository.findById(1L)).thenReturn(Optional.of(lehrkraft));
        when(generieren.benutzername(anyString(), anyString())).thenReturn("petra-meyer");
        when(generieren.kuerzel(anyString(), anyString())).thenReturn("PM");
        when(lerngruppeRepository.findByKlassenleitung(lehrkraft)).thenReturn(null);
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(lehrkraft)).thenReturn(List.of(alte));
        when(zeitslotRepository.findById(1L)).thenReturn(Optional.of(zeitslot));

        service.bearbeiten(1L, dto);

        verify(lehrerverfuegbarkeitRepository).deleteAll(List.of(alte));
        verify(lehrerverfuegbarkeitRepository).save(any(Lehrerverfuegbarkeit.class));
    }

    @Test
    void bearbeitenMitGanztaegigerVerfuegbarkeitBrauchtKeinenZeitslot() {
        Lehrkraft lehrkraft = baueLehrkraft(1L, "Meyer", true);
        LehrkraftBearbeitenDTO dto = baueBearbeitenDTO(true, null);

        VerfuegbarkeitEingabeDTO ganztaegig = new VerfuegbarkeitEingabeDTO();
        ganztaegig.setGanztaegig(true);
        ganztaegig.setTyp(VerfuegbarkeitsTyp.GESPERRT);
        ganztaegig.setStartdatum(LocalDate.of(2026, 10, 1));
        ganztaegig.setEnddatum(LocalDate.of(2026, 10, 5));
        dto.setVerfuegbarkeiten(List.of(ganztaegig));

        when(lehrkraftRepository.findById(1L)).thenReturn(Optional.of(lehrkraft));
        when(generieren.benutzername(anyString(), anyString())).thenReturn("petra-meyer");
        when(generieren.kuerzel(anyString(), anyString())).thenReturn("PM");
        when(lerngruppeRepository.findByKlassenleitung(lehrkraft)).thenReturn(null);
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(lehrkraft)).thenReturn(List.of());

        service.bearbeiten(1L, dto);

        ArgumentCaptor<Lehrerverfuegbarkeit> captor = ArgumentCaptor.forClass(Lehrerverfuegbarkeit.class);
        verify(lehrerverfuegbarkeitRepository).save(captor.capture());
        assertNull(captor.getValue().getZeitslot());
        assertNull(captor.getValue().getWochentag());
        verifyNoInteractions(zeitslotRepository);
    }

    @Test
    void bearbeitenBehaeltBenutzernameUndKuerzelBeiUnveraendertemNamen() {
        Lehrkraft lehrkraft = baueLehrkraft(1L, "Meyer", true);
        lehrkraft.getAccount().setVorname("Petra");   // <- anpassen, damit es zum DTO passt
        lehrkraft.getAccount().setBenutzername("petra-meyer");
        lehrkraft.setKuerzel("PM");

        LehrkraftBearbeitenDTO dto = baueBearbeitenDTO(true, null);

        when(lehrkraftRepository.findById(1L)).thenReturn(Optional.of(lehrkraft));
        when(lerngruppeRepository.findByKlassenleitung(lehrkraft)).thenReturn(null);
        when(lehrkraftRepository.save(any(Lehrkraft.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lehrerverfuegbarkeitRepository.findByLehrkraft(lehrkraft)).thenReturn(List.of());

        service.bearbeiten(1L, dto);

        assertEquals("petra-meyer", lehrkraft.getAccount().getBenutzername());
        assertEquals("PM", lehrkraft.getKuerzel());
        verify(generieren, never()).benutzername(anyString(), anyString());
        verify(generieren, never()).kuerzel(anyString(), anyString());
    }

    @Test
    void bearbeitenWirftExceptionWennAktivFeldFehlt() {
        Lehrkraft lehrkraft = baueLehrkraft(1L, "Meyer", true);
        LehrkraftBearbeitenDTO dto = baueBearbeitenDTO(true, null);
        dto.setAktiv(null);

        when(lehrkraftRepository.findById(1L)).thenReturn(Optional.of(lehrkraft));

        assertThrows(RuntimeException.class, () -> service.bearbeiten(1L, dto));
    }

    //deaktivieren()
    @Test
    void deaktivierenSetztAccountAufInaktivUndErsatzAlsKlassenleitung() {
        Lehrkraft lehrkraft = baueLehrkraft(1L, "Meyer", true);
        Lerngruppe klassenleitungGroup = new Lerngruppe();
        klassenleitungGroup.setId(50L);
        Lehrkraft ersatzKandidat = baueLehrkraft(99L, "Klein", true);

        when(lehrkraftRepository.findById(1L)).thenReturn(Optional.of(lehrkraft));
        when(lerngruppeRepository.findByKlassenleitung(lehrkraft)).thenReturn(klassenleitungGroup);
        when(lehrkraftRepository.findAll()).thenReturn(List.of(ersatzKandidat));
        when(lerngruppeRepository.findByKlassenleitung(ersatzKandidat)).thenReturn(null);

        service.deaktivieren(1L);

        assertFalse(lehrkraft.getAccount().isAktiv());
        assertEquals(99L, klassenleitungGroup.getKlassenleitung().getId());
        verify(accountRepository).save(lehrkraft.getAccount());
    }

    @Test
    void deaktivierenBereitsDeaktivierteLehrkraftMachtNichts() {
        Lehrkraft lehrkraft = baueLehrkraft(1L, "Meyer", false); // bereits inaktiv

        when(lehrkraftRepository.findById(1L)).thenReturn(Optional.of(lehrkraft));

        service.deaktivieren(1L);

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(lerngruppeRepository);
    }

    @Test
    void deaktivierenOhneKlassenleitungSpeichertKeineLerngruppe() {
        Lehrkraft lehrkraft = baueLehrkraft(1L, "Meyer", true);

        when(lehrkraftRepository.findById(1L)).thenReturn(Optional.of(lehrkraft));
        when(lerngruppeRepository.findByKlassenleitung(lehrkraft)).thenReturn(null);

        service.deaktivieren(1L);

        verify(lerngruppeRepository, never()).save(any(Lerngruppe.class));
    }

    //zufaelligeLehrkraftFuerKlassenleitung()
    @Test
    void zufaelligeLehrkraftWirftExceptionWennKeineVerfuegbar() {
        Lehrkraft belegt = baueLehrkraft(1L, "Meyer", true);
        Lehrkraft inaktiv = baueLehrkraft(2L, "Schulz", false);

        when(lehrkraftRepository.findAll()).thenReturn(List.of(belegt, inaktiv));
        when(lerngruppeRepository.findByKlassenleitung(belegt)).thenReturn(new Lerngruppe());

        assertThrows(RuntimeException.class, () -> service.zufaelligeLehrkraftFuerKlassenleitung());
    }

    @Test
    void zufaelligeLehrkraftGibtEinzigeVerfuegbareZurueck() {
        Lehrkraft verfuegbar = baueLehrkraft(1L, "Klein", true);

        when(lehrkraftRepository.findAll()).thenReturn(List.of(verfuegbar));
        when(lerngruppeRepository.findByKlassenleitung(verfuegbar)).thenReturn(null);

        Lehrkraft result = service.zufaelligeLehrkraftFuerKlassenleitung();

        assertEquals(1L, result.getId());
    }

    @Test
    void zufaelligeLehrkraftSchliesstBelegteUndInaktiveAus() {
        Lehrkraft belegt = baueLehrkraft(1L, "Meyer", true);
        Lehrkraft inaktiv = baueLehrkraft(2L, "Schulz", false);
        Lehrkraft verfuegbar = baueLehrkraft(3L, "Klein", true);

        when(lehrkraftRepository.findAll()).thenReturn(List.of(belegt, inaktiv, verfuegbar));
        when(lerngruppeRepository.findByKlassenleitung(belegt)).thenReturn(new Lerngruppe());
        when(lerngruppeRepository.findByKlassenleitung(inaktiv)).thenReturn(null);
        when(lerngruppeRepository.findByKlassenleitung(verfuegbar)).thenReturn(null);

        Lehrkraft result = service.zufaelligeLehrkraftFuerKlassenleitung();

        assertEquals(3L, result.getId());
    }
}