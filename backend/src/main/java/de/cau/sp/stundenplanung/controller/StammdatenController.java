package de.cau.sp.stundenplanung.controller;

import de.cau.sp.stundenplanung.dto.stammdaten.RasterpauseDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.PausenregelDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.PflichtfachvorgabeDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.SchulprofilDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.StammdatenDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.StufenvorgabeDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.VertretungsprioritaetDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.StundenrasterDTO;
import de.cau.sp.stundenplanung.dto.faecher.FachAnlegenDTO;
import de.cau.sp.stundenplanung.dto.faecher.FachUebersichtDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.MaximalbelastungDTO;
import de.cau.sp.stundenplanung.service.FachService;
import de.cau.sp.stundenplanung.service.StammdatenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/stammdaten")
@RequiredArgsConstructor
@Validated
public class StammdatenController {

    private final StammdatenService stammdatenService;
    private final FachService fachService;

    /** Lädt die ganze Seite */
    @GetMapping
    public StammdatenDTO alles() {
        return stammdatenService.alles();
    }

    /** "Änderungen speichern". */
    //Siehe Kommentar in StammdatenService
    /* 
    @PutMapping
    public StammdatenDTO allesSpeichern(@Valid @RequestBody StammdatenDTO stammdaten) {
        return stammdatenService.allesSpeichern(stammdaten);
    }
    */

    /** Schulprofil. */
    @GetMapping("/schulprofil")
    public SchulprofilDTO schulprofil() {
        return stammdatenService.schulprofil();
    }

    @PutMapping("/schulprofil")
    public SchulprofilDTO schulprofilSpeichern(@Valid @RequestBody SchulprofilDTO profil) {
        return stammdatenService.schulprofilSpeichern(profil);
    }

    /** Stundenraster. */
    @GetMapping("/stundenraster")
    public StundenrasterDTO stundenraster() {
        return stammdatenService.stundenraster();
    }

    /** Ersetzt ganzes Raster -> keine einzelnen Slots */
    @PutMapping("/stundenraster")
    public StundenrasterDTO stundenrasterSpeichern(@Valid @RequestBody StundenrasterDTO raster) {
        return stammdatenService.stundenrasterSpeichern(raster);
    }

    /** Pausenregelung */
    @GetMapping("/pausenregeln")
    public List<PausenregelDTO> pausenregeln() {
        return stammdatenService.pausenregeln();
    }
    /* 
    @PostMapping("/pausenregeln")
    public PausenregelDTO pausenregelAnlegen(@Valid @RequestBody PausenregelDTO neu) {
        return stammdatenService.pausenregelAnlegen(neu);
    }

    // Entspricht dem Stift-Symbol
    @PutMapping("/pausenregeln/{id}")
    public PausenregelDTO pausenregelBearbeiten(@PathVariable Long id,
                                                @Valid @RequestBody PausenregelDTO update) {
        return stammdatenService.pausenregelBearbeiten(id, update);
    }

    @DeleteMapping("/pausenregeln/{id}")
    public void pausenregelLoeschen(@PathVariable Long id) {
        stammdatenService.pausenregelLoeschen(id);
    }
    */
    /** Maximalbelastung */
    @GetMapping("/maximalbelastungen")
    public List<MaximalbelastungDTO> maximalbelastungen() {
        return stammdatenService.maximalbelastungen();
    }

    @PostMapping("/maximalbelastungen")
    public MaximalbelastungDTO maximalbelastungAnlegen(@Valid @RequestBody MaximalbelastungDTO neu) {
        return stammdatenService.maximalbelastungAnlegen(neu);
    }

    @PutMapping("/maximalbelastungen/{id}")
    public MaximalbelastungDTO maximalbelastungBearbeiten(@PathVariable Long id,
                                                            @Valid @RequestBody MaximalbelastungDTO update) {
        return stammdatenService.maximalbelastungBearbeiten(id, update);
    }

    @DeleteMapping("/maximalbelastungen/{id}")
    public void maximalbelastungLoeschen(@PathVariable Long id) {
        stammdatenService.maximalbelastungLoeschen(id);
    }

    /** Pflichtfächer */
    @GetMapping("/pflichtfaecher")
    public List<PflichtfachvorgabeDTO> pflichtfaecher() {
        return stammdatenService.pflichtfaecher();
    }

    /** Neue Stufenvorgabe zu einem bestehenden Fach anlegen */
    @PostMapping("/pflichtfaecher/{fachId}/stufen")
    public StufenvorgabeDTO pflichtfachStufenvorgabeAnlegen(@PathVariable Long fachId,
                                                @Valid @RequestBody StufenvorgabeDTO neu) {
        return stammdatenService.pflichtfachStufenvorgabeAnlegen(fachId, neu);
    }

    /** Einzelne Stufenvorgabe bearbeiten */
    @PutMapping("/pflichtfaecher/{fachId}/stufen/{stufeId}")
        public StufenvorgabeDTO pflichtfachStufenvorgabeBearbeiten(@PathVariable Long fachId,
                                                        @PathVariable Long stufeId,
                                                        @Valid @RequestBody StufenvorgabeDTO update) {
            return stammdatenService.pflichtfachStufenvorgabeBearbeiten(fachId, stufeId, update);
    }

    /** Einzelne Stufenvorgabe löschen */
    @DeleteMapping("/pflichtfaecher/{fachId}/stufen/{stufeId}")
    public void pflichtfachStufenvorgabeLoeschen(@PathVariable Long fachId, @PathVariable Long stufeId) {
        stammdatenService.pflichtfachStufenvorgabeLoeschen(fachId, stufeId);
    }

    /** Vertretungspriorität */
    @GetMapping("/vertretungsprioritaet")
    public List<VertretungsprioritaetDTO> vertretungsprioritaet() {
        return stammdatenService.vertretungsprioritaet();
    }

    /** Drag and Drop für Reihenfolge */
    @PutMapping("/vertretungsprioritaet")
    public List<VertretungsprioritaetDTO> vertretungsprioritaetSpeichern(
            @RequestBody List<@Valid VertretungsprioritaetDTO> reihenfolge) {
        return stammdatenService.vertretungsprioritaetSpeichern(reihenfolge);
    }

    @GetMapping("/faecher")
    public List<FachUebersichtDTO> alleFaecher() {
        return fachService.alleFaecher();
    }

    @PostMapping("/faecher")
    public FachUebersichtDTO fachAnlegen(@Valid @RequestBody FachAnlegenDTO dto) {
        return fachService.fachAnlegen(dto);
    }

}
    