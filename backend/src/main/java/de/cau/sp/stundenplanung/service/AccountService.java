package de.cau.sp.stundenplanung.service;

//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
import de.cau.sp.stundenplanung.dto.accounts.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Rolle;
import de.cau.sp.stundenplanung.repository.AccountRepository;

//import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.ArrayList;
@RequiredArgsConstructor
@Service
public class AccountService{
    private final AccountRepository accountRepository;
    private final Generieren generieren;
    private final PasswordEncoder passwordEncoder;

    public List<AccountAnzeigenDTO> alle() {
        List<Account> planungsAccounts = new ArrayList<>();
        accountRepository.findByRolle(Rolle.PLANUNG).forEach(planungsAccounts::add);

        List<AccountAnzeigenDTO> result = new ArrayList<>();

        for (Account account : planungsAccounts) {
            AccountAnzeigenDTO dto = zuAnzeigenDTO(account);
            result.add(dto);
        }

        return result;
    }

    public List<AccountAnzeigenDTO> alleFiltered(Rolle rolle) {
        List<Account> accounts;
        if (rolle == null) {
            accounts = accountRepository.findByAktiv(true);
        } else {
            accounts = accountRepository.findByRolleAndAktiv(rolle, true);
        }

        List<AccountAnzeigenDTO> result = new ArrayList<>();
        for (Account a : accounts) {
            result.add(zuAnzeigenDTO(a));
        }
        return result;
    }

    public List<AccountAnzeigenDTO> alleDeaktivierten() {
        List<Account> accounts = accountRepository.findByRolleAndAktiv(Rolle.PLANUNG, false);

        List<AccountAnzeigenDTO> result = new ArrayList<>();
        for (Account a : accounts) {
            result.add(zuAnzeigenDTO(a));
        }
        return result;
    }

    public AccountAnzeigenDTO einzeln(Long id) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));
        return zuAnzeigenDTO(account);
    }

    public AccountAnzeigenDTO anlegen(AccountAnlegenDTO dto) {
        Account account = new Account();
        account.setName(dto.getName());
        account.setVorname(dto.getVorname());
        account.setBenutzername(generieren.benutzername(dto.getVorname(), dto.getName()));
        account.setRolle(Rolle.PLANUNG);
        account.setPasswort(passwordEncoder.encode(dto.getPasswort()));
        account.setAktiv(true);

        Account gespeicherterAccount = accountRepository.save(account);
        return zuAnzeigenDTO(gespeicherterAccount);
    }

    public AccountAnzeigenDTO bearbeiten(Long id, AccountBearbeitenDTO dto) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));

        if (dto.getAktiv() == null) {
            throw new RuntimeException("Feld 'aktiv' muss angegeben werden");
        }

        boolean nameGeaendert = !account.getName().equals(dto.getName()) || !account.getVorname().equals(dto.getVorname());

        account.setName(dto.getName());
        account.setVorname(dto.getVorname());
        if (nameGeaendert) {
            account.setBenutzername(generieren.benutzername(dto.getVorname(), dto.getName()));
        }
        account.setAktiv(dto.getAktiv());

        Account gespeicherterAccount = accountRepository.save(account);
        return zuAnzeigenDTO(gespeicherterAccount);
    }

    public void deaktivieren(Long id) {
        Account account = accountRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));
        if (account.isAktiv()) {
            account.setAktiv(false);
            accountRepository.save(account);
        }
    }

    private AccountAnzeigenDTO zuAnzeigenDTO(Account account) {
        AccountAnzeigenDTO dto = new AccountAnzeigenDTO();
        dto.setId(account.getId());
        dto.setName(account.getName());
        dto.setVorname(account.getVorname());
        dto.setBenutzername(account.getBenutzername());
        dto.setAktiv(account.isAktiv());
        return dto;
    }

    public ProfilAnzeigenDTO eigeneDatenAnzeigen(String benutzername){
            Account account = accountRepository.findByBenutzername(benutzername)
        .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));
        return new ProfilAnzeigenDTO(
        account.getName(),
        account.getVorname(),
        account.getBenutzername(),
        account.getRolle(),
        account.isAktiv());
    }

    //Profil bearbieten
    public ProfilBearbeitenDTO eigeneDatenBearbeiten(String benutzername, ProfilBearbeitenDTO dto){
        Account account = accountRepository.findByBenutzername(benutzername)
        .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));

        boolean nameGeaendert = !account.getName().equals(dto.getName()) || !account.getVorname().equals(dto.getVorname());
        
        account.setName(dto.getName());
        account.setVorname(dto.getVorname());
        if (nameGeaendert) {
            account.setBenutzername(generieren.benutzername(dto.getVorname(), dto.getName()));
        }
        
        Account gespeichert = accountRepository.save(account);

        return new ProfilBearbeitenDTO(gespeichert.getName(), gespeichert.getVorname());
    }

    public void eigenesPasswortAendern(String benutzername, PasswortAendernDTO dto) {
        Account account = accountRepository.findByBenutzername(benutzername)
                .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));

        if (!passwordEncoder.matches(dto.getAltesPasswort(), account.getPasswort())) {
            throw new RuntimeException("Altes Passwort ist falsch");
        }

        account.setPasswort(passwordEncoder.encode(dto.getNeuesPasswort()));
        accountRepository.save(account);
    }
    //Passwort ändern (Sollte auch Funktion sein, um PW von Lehrkräften und Schüler zu ändern)
    public void passwortZuruecksetzen(Long accountId, PasswortZuruecksetzenDTO dto) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));

        account.setPasswort(passwordEncoder.encode(dto.getNeuesPasswort()));
        //account.setMustChangePassword(true); // Account muss beim naechsten Login ein neues Passwort setzen
        accountRepository.save(account);
    }
}
