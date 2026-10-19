package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.accounts.AccountAnlegenDTO;
import de.cau.sp.stundenplanung.dto.accounts.AccountAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.accounts.AccountBearbeitenDTO;
import de.cau.sp.stundenplanung.dto.accounts.ProfilAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.accounts.ProfilBearbeitenDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock Generieren generieren;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks AccountService service;

    //alle(), alleDeaktivierten()
    private Account baueAccount(Long id, String name, boolean aktiv) {
        Account account = new Account();
        account.setId(id);
        account.setName(name);
        account.setVorname("Vorname");
        account.setBenutzername(name.toLowerCase() + "-vorname");
        account.setRolle(Rolle.PLANUNG);
        account.setAktiv(aktiv);
        return account;
    }

    @Test
    void alleGibtNurPlanungsAccountsAlsDTOZurueck() {
        Account account = baueAccount(1L, "Weber", true);

        when(accountRepository.findByRolle(Rolle.PLANUNG)).thenReturn(List.of(account));

        List<AccountAnzeigenDTO> result = service.alle();

        assertEquals(1, result.size());
        assertEquals("Weber", result.get(0).getName());
        verify(accountRepository).findByRolle(Rolle.PLANUNG);
    }

    @Test
    void alleGibtLeereListeZurueckWennKeineAccountsExistieren() {
        when(accountRepository.findByRolle(Rolle.PLANUNG)).thenReturn(List.of());

        List<AccountAnzeigenDTO> result = service.alle();

        assertTrue(result.isEmpty());
    }

    @Test
    void alleDeaktiviertenRuftRepositoryMitRolleUndFalseAuf() {
        Account account = baueAccount(2L, "Klein", false);

        when(accountRepository.findByRolleAndAktiv(Rolle.PLANUNG, false)).thenReturn(List.of(account));

        List<AccountAnzeigenDTO> result = service.alleDeaktivierten();

        assertEquals(1, result.size());
        assertFalse(result.get(0).isAktiv());
        verify(accountRepository, never()).findByRolleAndAktiv(Rolle.PLANUNG, true);
    }

    //anlegen()
    private AccountAnlegenDTO baueAnlegenDTO() {
        AccountAnlegenDTO dto = new AccountAnlegenDTO();
        dto.setName("Weber");
        dto.setVorname("Klaus");
        dto.setPasswort("start1234");
        return dto;
    }

    @Test
    void anlegenSetztRollePlanungAktivTrueUndVerschluesseltPasswort() {
        AccountAnlegenDTO dto = baueAnlegenDTO();

        when(generieren.benutzername("Klaus", "Weber")).thenReturn("klaus-weber");
        when(passwordEncoder.encode("start1234")).thenReturn("verschluesselt");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        service.anlegen(dto);
        verify(accountRepository).save(captor.capture());

        assertEquals(Rolle.PLANUNG, captor.getValue().getRolle());
        assertTrue(captor.getValue().isAktiv());
        assertEquals("verschluesselt", captor.getValue().getPasswort());
    }

    //bearbeiten()
    private AccountBearbeitenDTO baueBearbeitenDTO(boolean aktiv) {
        AccountBearbeitenDTO dto = new AccountBearbeitenDTO();
        dto.setName("Weber");
        dto.setVorname("Klaus");
        dto.setAktiv(aktiv);
        return dto;
    }

    @Test
    void bearbeitenWirftExceptionWennAccountNichtGefunden() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.bearbeiten(1L, baueBearbeitenDTO(true)));
    }

    @Test
    void bearbeitenAktualisiertNameUndAktivStatus() {
        Account account = baueAccount(1L, "Alt", true);
        AccountBearbeitenDTO dto = baueBearbeitenDTO(false);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(generieren.benutzername("Klaus", "Weber")).thenReturn("klaus-weber");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountAnzeigenDTO result = service.bearbeiten(1L, dto);

        assertEquals("Weber", result.getName());
        assertFalse(result.isAktiv());
    }

    @Test
    void bearbeitenBehaeltBenutzernameBeiUnveraendertemNamen() {
        Account account = baueAccount(1L, "Weber", true);
        account.setVorname("Klaus");
        account.setBenutzername("klaus-weber");

        AccountBearbeitenDTO dto = baueBearbeitenDTO(true);
        dto.setName("Weber");
        dto.setVorname("Klaus");

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        service.bearbeiten(1L, dto);

        assertEquals("klaus-weber", account.getBenutzername());
        verify(generieren, never()).benutzername(anyString(), anyString());
    }

    @Test
    void bearbeitenWirftExceptionWennAktivFeldFehlt() {
        Account account = baueAccount(1L, "Weber", true);
        AccountBearbeitenDTO dto = baueBearbeitenDTO(true);
        dto.setAktiv(null);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () -> service.bearbeiten(1L, dto));
    }

    //deaktivieren()
    @Test
    void deaktivierenSetztAktivAufFalse() {
        Account account = baueAccount(1L, "Weber", true);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        service.deaktivieren(1L);

        assertFalse(account.isAktiv());
        verify(accountRepository).save(account);
    }

    @Test
    void deaktivierenWirftExceptionWennAccountNichtGefunden() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.deaktivieren(1L));
    }

    @Test
    void deaktivierenBereitsDeaktivierterAccountSpeichertNicht() {
        Account account = baueAccount(1L, "Weber", false); // bereits inaktiv

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        service.deaktivieren(1L);

        verify(accountRepository, never()).save(any(Account.class));
    }

    //eigeneDatenAnzeigen(), eigenDatenBearbeiten()
    @Test
    void eigeneDatenAnzeigenGibtProfilDatenZurueck() {
        Account account = baueAccount(1L, "Weber", true);
        account.setRolle(Rolle.PLANUNG);

        when(accountRepository.findByBenutzername("klaus-weber")).thenReturn(Optional.of(account));

        ProfilAnzeigenDTO result = service.eigeneDatenAnzeigen("klaus-weber");

        assertEquals("Weber", result.getName());
        assertEquals(Rolle.PLANUNG, result.getRolle());
        assertTrue(result.isAktiv());
    }

    @Test
    void eigeneDatenAnzeigenWirftExceptionWennBenutzerNichtGefunden() {
        when(accountRepository.findByBenutzername("unbekannt")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.eigeneDatenAnzeigen("unbekannt"));
    }

    @Test
    void eigeneDatenBearbeitenAktualisiertNameUndBenutzername() {
        Account account = baueAccount(1L, "Alt", true);
        ProfilBearbeitenDTO dto = new ProfilBearbeitenDTO("Weber", "Klaus");

        when(accountRepository.findByBenutzername("alt-benutzername")).thenReturn(Optional.of(account));
        when(generieren.benutzername("Klaus", "Weber")).thenReturn("klaus-weber");
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        ProfilBearbeitenDTO result = service.eigeneDatenBearbeiten("alt-benutzername", dto);

        assertEquals("Weber", result.getName());
        assertEquals("Klaus", result.getVorname());
        assertEquals("klaus-weber", account.getBenutzername());
    }

    @Test
    void eigeneDatenBearbeitenWirftExceptionWennBenutzerNichtGefunden() {
        when(accountRepository.findByBenutzername("unbekannt")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.eigeneDatenBearbeiten("unbekannt", new ProfilBearbeitenDTO("X", "Y")));
    }

    @Test
    void eigeneDatenBearbeitenBehaeltBenutzernameBeiUnveraendertemNamen() {
        Account account = baueAccount(1L, "Weber", true);
        account.setVorname("Klaus");
        account.setBenutzername("klaus-weber");

        ProfilBearbeitenDTO dto = new ProfilBearbeitenDTO("Weber", "Klaus");

        when(accountRepository.findByBenutzername("klaus-weber")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        service.eigeneDatenBearbeiten("klaus-weber", dto);

        assertEquals("klaus-weber", account.getBenutzername());
        verify(generieren, never()).benutzername(anyString(), anyString());
    }
}
