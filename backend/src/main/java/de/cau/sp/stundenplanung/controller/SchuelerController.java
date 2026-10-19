package de.cau.sp.stundenplanung.controller;
import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import de.cau.sp.stundenplanung.dto.accounts.PasswortZuruecksetzenDTO;
import de.cau.sp.stundenplanung.dto.dashboard.AktuelleAenderungDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LerngruppeUebersichtDTO;
import de.cau.sp.stundenplanung.dto.schueler.SchuelerAnlegenDTO;
import de.cau.sp.stundenplanung.dto.schueler.SchuelerAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.schueler.SchuelerBearbeitenDTO;
import de.cau.sp.stundenplanung.service.AccountService;
import de.cau.sp.stundenplanung.service.LerngruppeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import de.cau.sp.stundenplanung.service.SchuelerService;


import java.util.List;
@RequiredArgsConstructor
@RestController
//@RequestMapping("/api/schueler")
public class SchuelerController {

    private final SchuelerService schuelerService;
    private final AccountService accountService;
    private final LerngruppeService lerngruppeService;  

    @GetMapping("/api/schueler/klassen")
    public List<LerngruppeUebersichtDTO> alleKlassen() {
        return lerngruppeService.alleKlassen();
    }

    @GetMapping("/api/schueler")
    public List<SchuelerAnzeigenDTO> alle() {
        return schuelerService.alle();
    }

    @GetMapping("/api/schueler/klassenstufe")
    public List<SchuelerAnzeigenDTO> alleNachKlassenstufe(@RequestParam int von, @RequestParam int bis) {
        return schuelerService.alleNachKlassenstufe(von, bis);
    }

    @GetMapping("/api/schueler/deaktiviert")
    public List<SchuelerAnzeigenDTO> alleDeaktivierten() {
        return schuelerService.alleDeaktivierten();
    }

    @GetMapping("/api/schueler/{id}")
    public SchuelerAnzeigenDTO einzeln(@PathVariable Long id) {
        return schuelerService.einzeln(id);
    }

    @PostMapping("/api/schueler")
    public SchuelerAnzeigenDTO anlegen(@RequestBody SchuelerAnlegenDTO neu) {
        return schuelerService.anlegen(neu);
    }

    @PutMapping("/api/schueler/{id}")
    public SchuelerAnzeigenDTO bearbeiten(@PathVariable Long id, @RequestBody SchuelerBearbeitenDTO update) {
        return schuelerService.bearbeiten(id, update);
    }

    @PutMapping("/api/schueler/{id}/deaktivieren")
    public void deaktivieren(@PathVariable Long id) {
        schuelerService.deaktivieren(id);
    }


    //zeigt fuer Schueler aktuellsten Stundenplan inklusive Aenderungen an
    @GetMapping("/api/schueleransicht/stundenplan")
    public List<UnterrichtsstundeDTO> stundenplan(Authentication authentication) {
        String benutzername = authentication.getName();
        return schuelerService.klassenStundenplan(benutzername);
    }
    //zeigt aktuelle fuer Schueler relevante Aenderungen an
    @GetMapping("/api/schueleransicht/aenderungen")
    public List<AktuelleAenderungDTO> aktuelleAenderungen(Authentication authentication) {
        String benutzername = authentication.getName();
        return schuelerService.aktuelleAenderungenAnzeigen(benutzername);
    }

    @PostMapping("/api/schueler/{id}/passwortAendern")
    public void neuesPasswortSetzen(@PathVariable Long id, @RequestBody PasswortZuruecksetzenDTO dto) {
        schuelerService.passwortZuruecksetzen(id, dto);
    }

}
