package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.schueler.SchuelerAnlegenDTO;
import de.cau.sp.stundenplanung.dto.schueler.SchuelerAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.schueler.SchuelerBearbeitenDTO;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchuelerServiceTest {

    @Mock SchuelerRepository schuelerRepository;
    @Mock AccountRepository accountRepository;
    @Mock LerngruppeRepository lerngruppeRepository;
    @Mock Generieren generieren;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks SchuelerService service;

    //alle(), alleNachKlassenstufe(), alleDeaktivierten()
    private Schueler baueSchueler(Long id, String name, boolean aktiv, Lerngruppe lerngruppe) {
        Account account = new Account();
        account.setName(name);
        account.setVorname("Vorname");
        account.setBenutzername(name.toLowerCase() + "-vorname");
        account.setAktiv(aktiv);

        Schueler schueler = new Schueler();
        schueler.setId(id);
        schueler.setAccount(account);
        schueler.setLerngruppe(lerngruppe);
        return schueler;
    }

    private Lerngruppe baueLerngruppe(Long id, String bezeichnung) {
        Raum raum = new Raum();
        raum.setBezeichnung("R102");

        Lerngruppe lerngruppe = new Lerngruppe();
        lerngruppe.setId(id);
        lerngruppe.setBezeichnung(bezeichnung);
        lerngruppe.setKlassenzimmer(raum);
        return lerngruppe;
    }

    @Test
    void alleGibtNurAktiveSchuelerMitKlasseZurueck() {
        Lerngruppe klasse = baueLerngruppe(1L, "7a");
        Schueler schueler = baueSchueler(1L, "Schmidt", true, klasse);

        when(schuelerRepository.findByAccount_Aktiv(true)).thenReturn(List.of(schueler));

        List<SchuelerAnzeigenDTO> result = service.alle();

        assertEquals(1, result.size());
        assertEquals("Schmidt", result.get(0).getName());
        assertEquals("7a", result.get(0).getKlasse());
        assertEquals("R102", result.get(0).getKlassenraum());
    }

    @Test
    void alleOhneLerngruppeLaesstKlassenfelderLeer() {
        Schueler schueler = baueSchueler(2L, "Ohne", true, null);

        when(schuelerRepository.findByAccount_Aktiv(true)).thenReturn(List.of(schueler));

        List<SchuelerAnzeigenDTO> result = service.alle();

        assertNull(result.get(0).getKlasse());
        assertNull(result.get(0).getKlasseId());
    }

    @Test
    void alleNachKlassenstufeRuftRepositoryMitKorrektenGrenzenAuf() {
        when(schuelerRepository.findByLerngruppe_Jahrgangsstufe_NummerBetweenAndAccount_Aktiv(5, 6, true))
            .thenReturn(List.of());

        service.alleNachKlassenstufe(5, 6);

        verify(schuelerRepository).findByLerngruppe_Jahrgangsstufe_NummerBetweenAndAccount_Aktiv(5, 6, true);
    }

    @Test
    void alleDeaktiviertenRuftRepositoryMitFalseAuf() {
        Schueler schueler = baueSchueler(3L, "Deaktiv", false, null);

        when(schuelerRepository.findByAccount_Aktiv(false)).thenReturn(List.of(schueler));

        List<SchuelerAnzeigenDTO> result = service.alleDeaktivierten();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isAktiv());
        verify(schuelerRepository, never()).findByAccount_Aktiv(true);
    }

    //anlegen()
    private SchuelerAnlegenDTO baueAnlegenDTO(Long klassenId) {
        SchuelerAnlegenDTO dto = new SchuelerAnlegenDTO();
        dto.setName("Schmidt");
        dto.setVorname("Lena");
        dto.setPasswort("start1234");
        dto.setEintrittsdatum(LocalDate.of(2026, 8, 1));
        dto.setKlassenId(klassenId);
        return dto;
    }

    @Test
    void anlegenErstelltAccountMitRolleSchuelerUndAktivTrue() {
        SchuelerAnlegenDTO dto = baueAnlegenDTO(null);

        when(generieren.benutzername("Lena", "Schmidt")).thenReturn("lena-schmidt");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(schuelerRepository.save(any(Schueler.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("verschluesselt");

        service.anlegen(dto);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertEquals(Rolle.SCHUELER, captor.getValue().getRolle());
        assertTrue(captor.getValue().isAktiv());
        assertEquals("lena-schmidt", captor.getValue().getBenutzername());
    }

    @Test
    void anlegenMitKlasseErhoehtSchueleranzahl() {
        SchuelerAnlegenDTO dto = baueAnlegenDTO(1L);
        Lerngruppe klasse = baueLerngruppe(1L, "7a");
        klasse.setSchueleranzahl(24);

        when(generieren.benutzername(anyString(), anyString())).thenReturn("lena-schmidt");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lerngruppeRepository.findById(1L)).thenReturn(Optional.of(klasse));
        when(schuelerRepository.save(any(Schueler.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("verschluesselt");

        service.anlegen(dto);

        assertEquals(25, klasse.getSchueleranzahl());
        verify(lerngruppeRepository).save(klasse);
    }

    @Test
    void anlegenMitUnbekannterKlassenIdWirftException() {
        SchuelerAnlegenDTO dto = baueAnlegenDTO(99L);

        when(generieren.benutzername(anyString(), anyString())).thenReturn("lena-schmidt");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lerngruppeRepository.findById(99L)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("verschluesselt");
        
        assertThrows(RuntimeException.class, () -> service.anlegen(dto));
    }

    //bearbeiten()
    private SchuelerBearbeitenDTO baueBearbeitenDTO(boolean aktiv, Long klassenId) {
        SchuelerBearbeitenDTO dto = new SchuelerBearbeitenDTO();
        dto.setName("Schmidt");
        dto.setVorname("Lena");
        dto.setAktiv(aktiv);
        dto.setKlassenId(klassenId);
        return dto;
    }

    @Test
    void bearbeitenBeiKlassenwechselVerringertAlteUndErhoehtNeueKlasse() {
        Lerngruppe alteKlasse = baueLerngruppe(1L, "7a");
        alteKlasse.setSchueleranzahl(25);
        Lerngruppe neueKlasse = baueLerngruppe(2L, "7b");
        neueKlasse.setSchueleranzahl(20);

        Schueler schueler = baueSchueler(1L, "Schmidt", true, alteKlasse);
        SchuelerBearbeitenDTO dto = baueBearbeitenDTO(true, 2L);

        when(schuelerRepository.findById(1L)).thenReturn(Optional.of(schueler));
        when(generieren.benutzername(anyString(), anyString())).thenReturn("lena-schmidt");
        when(lerngruppeRepository.findById(2L)).thenReturn(Optional.of(neueKlasse));
        when(schuelerRepository.save(any(Schueler.class))).thenAnswer(inv -> inv.getArgument(0));

        service.bearbeiten(1L, dto);

        assertEquals(24, alteKlasse.getSchueleranzahl());
        assertEquals(21, neueKlasse.getSchueleranzahl());
    }

    @Test
    void bearbeitenBeiDeaktivierungVerringertNurAlteKlasseOhneNeueErhoehung() {
        Lerngruppe alteKlasse = baueLerngruppe(1L, "7a");
        alteKlasse.setSchueleranzahl(25);

        Schueler schueler = baueSchueler(1L, "Schmidt", true, alteKlasse); // war aktiv
        SchuelerBearbeitenDTO dto = baueBearbeitenDTO(false, null); // wird jetzt deaktiviert

        when(schuelerRepository.findById(1L)).thenReturn(Optional.of(schueler));
        when(generieren.benutzername(anyString(), anyString())).thenReturn("lena-schmidt");
        when(schuelerRepository.save(any(Schueler.class))).thenAnswer(inv -> inv.getArgument(0));

        service.bearbeiten(1L, dto);

        assertEquals(24, alteKlasse.getSchueleranzahl());
        verify(lerngruppeRepository, never()).findById(anyLong());
    }

    @Test
    void bearbeitenMitGleicherKlasseAendertSchueleranzahlNichtUndAktualisiertAccount() {
        Lerngruppe klasse = baueLerngruppe(1L, "7a");
        klasse.setSchueleranzahl(25);

        Schueler schueler = baueSchueler(1L, "Alt", true, klasse);
        SchuelerBearbeitenDTO dto = baueBearbeitenDTO(true, 1L); // dieselbe Klasse erneut

        when(schuelerRepository.findById(1L)).thenReturn(Optional.of(schueler));
        when(generieren.benutzername("Lena", "Schmidt")).thenReturn("lena-schmidt");
        when(lerngruppeRepository.findById(1L)).thenReturn(Optional.of(klasse));
        when(schuelerRepository.save(any(Schueler.class))).thenAnswer(inv -> inv.getArgument(0));

        service.bearbeiten(1L, dto);

        assertEquals(25, klasse.getSchueleranzahl());
        verify(lerngruppeRepository, never()).save(any(Lerngruppe.class));
        assertEquals("Schmidt", schueler.getAccount().getName());
        assertEquals("Lena", schueler.getAccount().getVorname());
        assertEquals("lena-schmidt", schueler.getAccount().getBenutzername());
    }

    @Test
    void bearbeitenBehaeltBenutzernameBeiUnveraendertemNamen() {
        Lerngruppe klasse = baueLerngruppe(1L, "7a");
        Schueler schueler = baueSchueler(1L, "Schmidt", true, klasse);
        schueler.getAccount().setVorname("Lena");
        schueler.getAccount().setBenutzername("lena-schmidt");

        SchuelerBearbeitenDTO dto = baueBearbeitenDTO(true, 1L);

        when(schuelerRepository.findById(1L)).thenReturn(Optional.of(schueler));
        when(lerngruppeRepository.findById(1L)).thenReturn(Optional.of(klasse));
        when(schuelerRepository.save(any(Schueler.class))).thenAnswer(inv -> inv.getArgument(0));

        service.bearbeiten(1L, dto);

        assertEquals("lena-schmidt", schueler.getAccount().getBenutzername());
        verify(generieren, never()).benutzername(anyString(), anyString());
    }

    @Test
    void bearbeitenWirftExceptionWennAktivFeldFehlt() {
        Lerngruppe klasse = baueLerngruppe(1L, "7a");
        Schueler schueler = baueSchueler(1L, "Schmidt", true, klasse);
        SchuelerBearbeitenDTO dto = baueBearbeitenDTO(true, 1L);
        dto.setAktiv(null);

        when(schuelerRepository.findById(1L)).thenReturn(Optional.of(schueler));

        assertThrows(RuntimeException.class, () -> service.bearbeiten(1L, dto));
    }

    @Test
    void bearbeitenBeiGleichzeitigerDeaktivierungUndKlassenwechselAendertReferenzNicht() {
        Lerngruppe alteKlasse = baueLerngruppe(1L, "7a");
        alteKlasse.setSchueleranzahl(25);

        Schueler schueler = baueSchueler(1L, "Schmidt", true, alteKlasse);
        SchuelerBearbeitenDTO dto = baueBearbeitenDTO(false, 2L); // deaktivieren UND Klasse 2 angeben

        when(schuelerRepository.findById(1L)).thenReturn(Optional.of(schueler));
        when(generieren.benutzername(anyString(), anyString())).thenReturn("lena-schmidt");
        when(schuelerRepository.save(any(Schueler.class))).thenAnswer(inv -> inv.getArgument(0));

        service.bearbeiten(1L, dto);

        assertEquals(alteKlasse, schueler.getLerngruppe());   // Referenz bleibt bei der ALTEN Klasse
        assertEquals(24, alteKlasse.getSchueleranzahl());
        verify(lerngruppeRepository, never()).findById(2L);   // neue Klasse wird gar nicht erst geladen
    }

    //deaktivieren()
    @Test
    void deaktivierenSetztAccountInaktivUndVerringertSchueleranzahl() {
        Lerngruppe klasse = baueLerngruppe(1L, "7a");
        klasse.setSchueleranzahl(25);
        Schueler schueler = baueSchueler(1L, "Schmidt", true, klasse);

        when(schuelerRepository.findById(1L)).thenReturn(Optional.of(schueler));

        service.deaktivieren(1L);

        assertFalse(schueler.getAccount().isAktiv());
        assertEquals(24, klasse.getSchueleranzahl());
        verify(accountRepository).save(schueler.getAccount());
    }

    @Test
    void deaktivierenBereitsDeaktivierterSchuelerMachtNichts() {
        Schueler schueler = baueSchueler(1L, "Schmidt", false, null); // bereits inaktiv

        when(schuelerRepository.findById(1L)).thenReturn(Optional.of(schueler));

        service.deaktivieren(1L);

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(lerngruppeRepository);
    }

    @Test
    void deaktivierenOhneKlasseSpeichertKeineLerngruppe() {
        Schueler schueler = baueSchueler(1L, "Schmidt", true, null);

        when(schuelerRepository.findById(1L)).thenReturn(Optional.of(schueler));

        service.deaktivieren(1L);

        verify(lerngruppeRepository, never()).save(any(Lerngruppe.class));
    }
}
