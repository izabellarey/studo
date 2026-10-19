package de.cau.sp.stundenplanung.controller;

import de.cau.sp.stundenplanung.dto.accounts.AccountAnlegenDTO;
import de.cau.sp.stundenplanung.dto.accounts.AccountAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.accounts.ProfilAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.accounts.ProfilBearbeitenDTO;
import de.cau.sp.stundenplanung.dto.accounts.AccountBearbeitenDTO;
import de.cau.sp.stundenplanung.dto.accounts.PasswortAendernDTO;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
import de.cau.sp.stundenplanung.model.Rolle;
import de.cau.sp.stundenplanung.service.AccountService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//import java.util.ArrayList;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;


    @GetMapping
    public List<AccountAnzeigenDTO> alle() {
        return accountService.alle();
    }
    
    @GetMapping("/filter")
    public List<AccountAnzeigenDTO> alleFiltered(@RequestParam(required = false) Rolle rolle) {
        // Falls rolle als Parameter mitgegeben wird (z.B. ?rolle=PLANUNG), 
        // filtert der Service danach; sonst werden alle Accounts zurückgegeben
        return accountService.alleFiltered(rolle);
    }

    @GetMapping("/deaktiviert")
    public List<AccountAnzeigenDTO> alleDeaktivierten() {
        return accountService.alleDeaktivierten();
    }

    @GetMapping("/{id}")
    public AccountAnzeigenDTO einzeln(@PathVariable Long id) {
         return accountService.einzeln(id);
    }

    @PostMapping
    public AccountAnzeigenDTO anlegen(@RequestBody AccountAnlegenDTO neu) {
        return accountService.anlegen(neu);
    }

    @PutMapping("/{id}")
    public AccountAnzeigenDTO bearbeiten(@PathVariable Long id, @RequestBody AccountBearbeitenDTO update) {
        return accountService.bearbeiten(id, update);
    }

    @PutMapping("/{id}/deaktivieren")
    public void deaktivieren(@PathVariable Long id) {
        accountService.deaktivieren(id);
    }

    //später: Auth-Context
    @GetMapping("/profil")
    public ProfilAnzeigenDTO eigeneDatenAnzeigen(Authentication authentication) {
        String benutzername = authentication.getName();
        return accountService.eigeneDatenAnzeigen(benutzername);
    }

    // Name, Vorname, Benutzername
    @PutMapping("/profil")
    public ProfilBearbeitenDTO eigeneDatenBearbeiten(Authentication authentication, @RequestBody ProfilBearbeitenDTO update) {
        String benutzername = authentication.getName();
        return accountService.eigeneDatenBearbeiten(benutzername, update);
    }

    // Passwort, hier Spring Security! @AuthenticationPrincipal, Token
    // Filter vor allen Controller Funktionen bauen "public class JwtAuthFilter extends OncePerRequestFilter"
    @PutMapping("/profil/passwort")
    public void eigenesPasswortAendern(Authentication authentication, @RequestBody PasswortAendernDTO dto) {
        String benutzername = authentication.getName();
        accountService.eigenesPasswortAendern(benutzername, dto);
    }
    
}





