package de.cau.sp.stundenplanung.controller;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import de.cau.sp.stundenplanung.dto.lerngruppen.AenderungsVorschlagDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LerngruppeUebersichtDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.StundenplanAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.UnterrichtsstundeInitialAendernDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.GenerierungGestartetDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.GenerierungStatusDTO;
import de.cau.sp.stundenplanung.service.LerngruppeService;
import de.cau.sp.stundenplanung.service.StundenplanService;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/stundenplaene")
public class StundenplanController {

    private final StundenplanService stundenplanService;
    private final LerngruppeService lerngruppeService;

    @GetMapping("/klassen")
    public List<LerngruppeUebersichtDTO> alleKlassen() {
        return lerngruppeService.alleKlassen();
    }
    
    @PostMapping("/generieren")
    public GenerierungGestartetDTO generierungStarten() {
        return stundenplanService.generierungStarten();
    }

    @GetMapping("/generierung-status/{generationId}")
    public GenerierungStatusDTO generierungStatus(@PathVariable Long generationId) {
        return stundenplanService.generierungStatus(generationId);
    }

    //Zuletzt abgeschlossen, aber noch nicht veröffentlicht
    @GetMapping("/aktuelle-generation")
    public Long aktuelleGeneration() {
        return stundenplanService.aktuelleOffeneGenerierung();
    }

    @GetMapping("/variante/{klassenId}")
    public StundenplanAnzeigenDTO varianteAnzeigen(@PathVariable Long klassenId,
                                                    @RequestParam Long generationId,
                                                    @RequestParam Integer variante) {
        return stundenplanService.varianteAnzeigen(klassenId, generationId, variante);
    }

    /*
    @GetMapping("/beste/{klassenId}")
    public StundenplanAnzeigenDTO besteAlternativeAnzeigen(@PathVariable Long klassenId, @RequestParam Long generationId) {
        return stundenplanService.besteAlternativeAnzeigen(klassenId, generationId);
    }

    @GetMapping("/alternativen/{klassenId}")
    public List<StundenplanAnzeigenDTO> weitereAlternativeAnzeigen(@PathVariable Long klassenId, @RequestParam Long generationId) {
        return stundenplanService.weitereAlternativeAnzeigen(klassenId, generationId);
    }
    
    @PutMapping("/waehlen/{stundenplanId}")
    public void stundenplanWaehlen(@PathVariable Long stundenplanId) {
        stundenplanService.stundenplanWaehlen(stundenplanId);
    }

    @PutMapping("/abwaehlen/{stundenplanId}")
    public void stundenplanAbwaehlen(@PathVariable Long stundenplanId) {
        stundenplanService.stundenplanAbwaehlen(stundenplanId);
    }
    */

    @PutMapping("/veroeffentlichen")
    public void veroeffentlichen(@RequestParam Long generationId, @RequestParam Integer variante, Authentication authentication) {
        String benutzername = authentication.getName();
        stundenplanService.finaleStundenplaeneVeroeffentlichen(generationId, variante, benutzername);
    }

    @GetMapping("/unterrichtsstunde/{unterrichtsstundeId}/aenderung")
    public AenderungsVorschlagDTO aenderungsVorschlagAnzeigen(@PathVariable Long unterrichtsstundeId) {
        return stundenplanService.aenderungsVorschlagAnzeigen(unterrichtsstundeId);
    }

    @PutMapping("/unterrichtsstunde/{unterrichtsstundeId}/aenderung")
    public void unterrichtsstundeAendern(@PathVariable Long unterrichtsstundeId, @RequestBody UnterrichtsstundeInitialAendernDTO update) {
        stundenplanService.unterrichtsstundeAendern(unterrichtsstundeId, update);
    }

    /* 
    @GetMapping("/initial")
    public List<StundenplanAnzeigenDTO> initialeStundenplaene() {
        return null;//stundenplanService.initialeStundenplaene();
    }
    // Alle Stundenpläne des aktuellen Halbjahres, ohne Änderungen

    @GetMapping("/{id}")
    public StundenplanAnzeigenDTO einzelnerPlan(@PathVariable Long id) {
        return null;//stundenplanService.einzelnerPlan(id);
    }
    // Detailansicht eines konkreten Stundenplans

    @GetMapping("/{id}/alternativen")
    public List<StundenplanAnzeigenDTO> alternativen(@PathVariable Long id) {
        return null;//stundenplanService.alternativen(id);
    }
    // Andere Lösungsvorschläge aus demselben Generierungslauf
    */
}
