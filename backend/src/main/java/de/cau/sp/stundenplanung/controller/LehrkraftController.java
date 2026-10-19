package de.cau.sp.stundenplanung.controller;

import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import de.cau.sp.stundenplanung.dto.accounts.PasswortZuruecksetzenDTO;
import de.cau.sp.stundenplanung.dto.dashboard.AktuelleAenderungDTO;
import de.cau.sp.stundenplanung.dto.faecher.FachUebersichtDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftAnlegenDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftBearbeitenDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LerngruppeUebersichtDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumplanEintragDTO;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.service.AccountService;
import de.cau.sp.stundenplanung.service.FachService;
import de.cau.sp.stundenplanung.service.LehrkraftService;
import de.cau.sp.stundenplanung.service.LerngruppeService;
//import org.springframework.security.core.Authentication;
import de.cau.sp.stundenplanung.service.RaumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RequiredArgsConstructor
@RestController
//@RequestMapping("/api/lehrkraftansicht") hier müssen ja noch requests für die Lehreransicht gemappt werden, deshalb erstmal auskommentiert und alle app requests mit eigener url
public class LehrkraftController {

    private final LehrkraftService lehrkraftService;
    private final AccountService accountService;
    private final RaumService raumService;
    private final LerngruppeService lerngruppeService;
    private final FachService fachService;   

    /* 
    @GetMapping("/api/lehrkraefte")
    public List<Lehrkraft> alle() {
        return lehrkraftService.alle();
    }
    */
    @GetMapping("/api/lehrkraefte/klassen")
    public List<LerngruppeUebersichtDTO> alleKlassen() {
        return lerngruppeService.alleKlassen();
    }

    @GetMapping("/api/lehrkraefte/faecher")
    public List<FachUebersichtDTO> alleFaecher() {
        return fachService.alleFaecher();
    }

    @GetMapping("/api/lehrkraefte/{id}")
    public LehrkraftAnzeigenDTO einzeln(@PathVariable Long id) {
         return lehrkraftService.einzeln(id);
    }
   
    @GetMapping("/api/lehrkraefte")
    public List<LehrkraftAnzeigenDTO> alle() {
        return lehrkraftService.alle();
    }

    @GetMapping("/api/lehrkraefte/deaktiviert")
    public List<LehrkraftAnzeigenDTO> alleDeaktivierten() {
        return lehrkraftService.alleDeaktivierten();
    }

    @PostMapping("/api/lehrkraefte")
    public LehrkraftAnzeigenDTO anlegen(@Valid @RequestBody LehrkraftAnlegenDTO neu) {
        return lehrkraftService.anlegen(neu);
    }

    @PutMapping("/api/lehrkraefte/{id}")
    public LehrkraftAnzeigenDTO bearbeiten(@PathVariable Long id, @Valid @RequestBody LehrkraftBearbeitenDTO update) {
         return lehrkraftService.bearbeiten(id, update);
    }

    @PutMapping("/api/lehrkraefte/{id}/deaktivieren")
    public void deaktivieren(@PathVariable Long id) {
         lehrkraftService.deaktivieren(id);
    }

    @PostMapping("/api/lehrkraefte/{id}/passwortAendern")
    public void neuesPasswortSetzen(@PathVariable Long id, @RequestBody PasswortZuruecksetzenDTO dto) {
        lehrkraftService.passwortZuruecksetzen(id, dto);
    }


    //zeigt fuer Lehrkraft aktuellsten Stundenplan inklusive Aenderungen an
    @GetMapping("/api/lehrkraftansicht/stundenplan")
    public List<UnterrichtsstundeDTO> stundenplan(Authentication authentication) {
        String benutzername = authentication.getName();
        return lehrkraftService.persoenlicherStundenplan(benutzername);
    }
    //zeigt aktuelle fuer Lehrkraft relevante Aenderungen an
    @GetMapping("/api/lehrkraftansicht/aenderungen")
    public List<AktuelleAenderungDTO> aktuelleAenderungen(Authentication authentication) {
        String benutzername = authentication.getName();
        return lehrkraftService.aktuelleAenderungenAnzeigen(benutzername);
    }

    @GetMapping("/api/lehrkraftansicht/raeume")
    public List<RaumDTO> alleRaueme(){
        return raumService.alleRaeume();
    }

    @GetMapping("/api/lehrkraftansicht/{raumId}")
    public List<RaumplanEintragDTO> raumplan(@PathVariable Long raumId){
        return  raumService.raumplanAnzeigen(raumId);
    }

}