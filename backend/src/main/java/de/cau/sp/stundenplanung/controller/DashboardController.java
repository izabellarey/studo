package de.cau.sp.stundenplanung.controller;

import de.cau.sp.stundenplanung.dto.dashboard.AktuelleAenderungDTO;
import de.cau.sp.stundenplanung.dto.dashboard.DashboardDTO;
import de.cau.sp.stundenplanung.dto.faecher.FachUebersichtDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumanfrageDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.ZeitslotDTO;
import de.cau.sp.stundenplanung.service.DashboardService;

import de.cau.sp.stundenplanung.service.FachService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final FachService fachService;


    /** Alles zum anzeigen */
    @GetMapping
    public DashboardDTO dashboard() {
        return dashboardService.dashboard();
    }

    @GetMapping("/zeitslots")
    public List<ZeitslotDTO> alleZeitlots(){
        return dashboardService.alleZeitslots();
    }

    @GetMapping("/faecher")
    public List<FachUebersichtDTO> alleFaecher(){
        return fachService.alleFaecher();
    }

    @GetMapping("/verfuegbareRaeume")
    public List<RaumDTO> alleVerfuegbareRaeume(@RequestBody RaumanfrageDTO dto){
        return dashboardService.verfuegbareRaeume(dto);
    }

    /*/**
     * Aktuelle änderungen als einzelpunkt, damit sie nachgeladen werden können
     * @param anzahl höchstanzahl von änderungen

    @GetMapping("/aktuelle-aenderungen")
    public List<AktuelleAenderungDTO> aktuelleAenderungen(
            @RequestParam(defaultValue = "5") int anzahl) {
        return dashboardService.aktuelleAenderungen(anzahl);
    }

    /** Zahl an der Glocke.
    @GetMapping("/benachrichtigungen/anzahl")
    public long anzahlBenachrichtigungen() {
        return dashboardService.anzahlBenachrichtigungen();
    } */
}