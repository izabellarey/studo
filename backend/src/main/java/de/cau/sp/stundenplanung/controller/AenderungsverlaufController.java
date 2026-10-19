package de.cau.sp.stundenplanung.controller;

import de.cau.sp.stundenplanung.dto.SeiteDTO;
import de.cau.sp.stundenplanung.dto.aenderungsverlauf.AbgeschlosseneAenderungDTO;
import de.cau.sp.stundenplanung.dto.aenderungsverlauf.OffeneAenderungDTO;
import de.cau.sp.stundenplanung.dto.ereignis.EreignisDTO;
import de.cau.sp.stundenplanung.model.EreignisTyp;
import de.cau.sp.stundenplanung.service.AenderungsverlaufService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/aenderungsverlauf")
@RequiredArgsConstructor
public class AenderungsverlaufController {

    private final AenderungsverlaufService aenderungsverlaufService;

    /** Liste der offenen Änderungen, neuste zuerst. */
    @GetMapping("/offen")
    public List<OffeneAenderungDTO> offene() {
        return aenderungsverlaufService.offene();
    }

    /** Zähler für offene Änderungen */
    @GetMapping("/offen/anzahl")
    public long anzahlOffene() {
        return aenderungsverlaufService.anzahlOffene();
    }

    /** Knopf Bearbeiten */
    @GetMapping("/offen/{id}")
    public OffeneAenderungDTO offeneEinzeln(@PathVariable Long id) {
        return aenderungsverlaufService.offeneEinzeln(id);
    }

    /** Knopf "Erledigt": verschiebt den Eintrag in die abgeschlossenen Änderungen */
    @PutMapping("/offen/{id}/erledigt")
    public AbgeschlosseneAenderungDTO alsErledigtMarkieren(@PathVariable Long id) {
        return aenderungsverlaufService.alsErledigtMarkieren(id);
    }

    /**
     * Abgeschlossene Änderungen mit den Filtern der Seite.
     *
     * @param suche    Freitext über Klasse, Lehrkraft und Fach; null = kein Filter
     * @param typ      Änderungstyp; null = alle Typen
     * @param von      Beginn des Zeitraums; null = unbegrenzt
     * @param bis      Ende des Zeitraums; null = unbegrenzt
     * @param seite    Seitennummer, beginnend bei 0
     * @param groesse  Einträge pro Seite
     */
    @GetMapping("/abgeschlossen")
    public SeiteDTO<AbgeschlosseneAenderungDTO> abgeschlossene(
            @RequestParam(required = false) String suche,
            @RequestParam(required = false) EreignisTyp typ,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            @RequestParam(defaultValue = "0") int seite,
            @RequestParam(defaultValue = "20") int groesse) {

        return aenderungsverlaufService.abgeschlossene(suche, typ, von, bis, seite, groesse);
    }

    /** Einzelner abgeschlossener Eintrag. */
    @GetMapping("/abgeschlossen/{id}")
    public AbgeschlosseneAenderungDTO abgeschlosseneEinzeln(@PathVariable Long id) {
        return aenderungsverlaufService.abgeschlosseneEinzeln(id);
    }

    /** Alle Änderungstypen */
    @GetMapping("/typen")
    public List<EreignisTyp> typen() {
        return List.of(EreignisTyp.values());
    }

    /**
     * Liste der eingetragenen Ereignisse (Ausfall, Raumänderung, Zusatzstunde), unabhängig
     * davon, ob sie bereits abgeschlossen sind.
     *
     * @param typ    Ereignistyp; null = alle Typen
     * @param von    Beginn des Zeitraums; null = unbegrenzt
     * @param bis    Ende des Zeitraums; null = unbegrenzt
     * @param seite  Seitennummer, beginnend bei 0
     * @param groesse Einträge pro Seite
     */
    @GetMapping("/ereignisse")
    public SeiteDTO<EreignisDTO> ereignisverlauf(
            @RequestParam(required = false) EreignisTyp typ,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate von,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bis,
            @RequestParam(defaultValue = "0") int seite,
            @RequestParam(defaultValue = "20") int groesse) {

        return aenderungsverlaufService.ereignisverlauf(typ, von, bis, seite, groesse);
    }
}