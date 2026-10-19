package de.cau.sp.stundenplanung.controller;

import de.cau.sp.stundenplanung.dto.ereignis.EreignisDTO;
import de.cau.sp.stundenplanung.dto.ereignis.TagDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.RaumAlternativeDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumDTO;
import de.cau.sp.stundenplanung.model.EreignisStatus;
import de.cau.sp.stundenplanung.model.EreignisTyp;
import de.cau.sp.stundenplanung.model.Zeitslot;
import de.cau.sp.stundenplanung.service.EreignisService;

import de.cau.sp.stundenplanung.service.RaumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ereignisse")
@RequiredArgsConstructor
public class EreignisController {

    private final EreignisService ereignisService;
    private final RaumService raumService;

    /**
     * Übersicht nach Status
     *
     * @param status null = alle Ereignisse
     */
    @GetMapping
    public List<EreignisDTO> alle(@RequestParam(required = false) EreignisStatus status) {
        return ereignisService.alle(status);
    }
    /*@GetMapping("/verfuegbareRaeume")
    public List<RaumAlternativeDTO> verfuegbareRaeume(@RequestBody TagDTO tag) {
        Zeitslot zeitslot = zeitslotRepository.findById(tag.getZeitslotId())
                .orElse ...
        return raumService.verfuegbarerRaum(tag.getWochentag(), tag.getZeitslot(), tag.getFach());
    } */

    @GetMapping("/{id}")
    public EreignisDTO einzeln(@PathVariable Long id) {
        return ereignisService.einzeln(id);
    }

    /** Knopf "Ereignis eintragen", Benutzer wird als Autor gesetzt */
    @PostMapping
    public EreignisDTO anlegen(@RequestBody EreignisDTO neu, Authentication authentication) {
        String benutzername = authentication.getName();
        return ereignisService.anlegen(neu, benutzername);
    }

    @PutMapping("/{id}")
    public EreignisDTO bearbeiten(@PathVariable Long id, @Valid @RequestBody EreignisDTO update) {
        return ereignisService.bearbeiten(id, update);
    }

    @DeleteMapping("/{id}")
    public void loeschen(@PathVariable Long id) {
        ereignisService.loeschen(id);
    }

    /** Füllt "Anlass auswählen..." - Ausfall, Raumsperrung, Zusatzstunde */
    @GetMapping("/anlaesse")
    public List<EreignisTyp> anlaesse() {
        return List.of(EreignisTyp.values());
    }
}