package de.cau.sp.stundenplanung.controller;


import de.cau.sp.stundenplanung.dto.raeume.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import de.cau.sp.stundenplanung.service.RaumService;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/raeume")
public class RaumController {

    private final RaumService raumService;

    //zeigt alle einsehbaren Raueme der Schule an (Dropdown)
    @GetMapping
    public List<RaumDTO> alleRaeume() {
        return raumService.alleRaeume();
    }

    //zeigt zu ausgewaehltem Raum den Raumplan an
    @GetMapping("/{raumId}")
    public List<RaumplanEintragDTO> raumplan(@PathVariable Long raumId) {
        return raumService.raumplanAnzeigen(raumId);
    }

    @PostMapping()
    public RaumDTO raumHinzufuegen(@RequestBody RaumDTO raum) {
        return raumService.raumHinzufuegen(raum);
    }

    @PutMapping("/{raumId}/bearbeiten")
    public RaumDTO raumBearbeiten(@PathVariable Long raumId, @RequestBody RaumDTO raum) {
        return raumService.raumBearbeiten(raumId, raum);
    }
    //fuegt Raumbuchung zu ausgewaehltem Raum hinzu
    /*
    @PostMapping("/{raumId}/buchungHinzufuegen")
    public RaumplanEintragDTO buchungHinzufuegen(@PathVariable Long raumId, @RequestBody RaumbuchungDTO buchung) { //neue DTO Klasse hierfür?
        return raumService.buchungHinzufuegen(raumId, buchung);
    }*/
}
