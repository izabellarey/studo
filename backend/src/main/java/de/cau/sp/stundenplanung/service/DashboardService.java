package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.dashboard.AktuelleAenderungDTO;
import de.cau.sp.stundenplanung.dto.dashboard.DashboardDTO;
import de.cau.sp.stundenplanung.dto.faecher.FachUebersichtDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumanfrageDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.ZeitslotDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LerngruppeRepository lerngruppeRepository;
    private final RaumRepository raumRepository;
    private final LehrkraftRepository lehrkraftRepository;
    private final EreignisRepository ereignisRepository;
    private final ZeitslotRepository zeitslotRepository;
    private final EreignisService ereignisService;
    private final FachRepository fachRepository;
    private final RaumService raumService;
    private final AenderungRepository aenderungRepository;
    private final AenderungsverlaufService aenderungsverlaufService;

    /** TODO: Kennzahlen der drei Kacheln und die aktuellen Änderungen in einem DTO bündeln, damit die Startseite mit einem Aufruf auskommt. */
    public DashboardDTO dashboard() {
        //Aenderungen aus aenderrungsservice abrufen, dann auf dtos mappen, dabei stringFkt abrufen
        // anzahl offene Aenderungen abrufen
        List<AktuelleAenderungDTO> aktuelleAenderungen = aenderungsverlaufService.neuesteAenderungen(5);
        return new DashboardDTO(anzahlBenachrichtigungen(), aktuelleAenderungen);
    }

    /** TODO: Die jüngsten Änderungen für den Block "Aktuelle Änderungen" liefern, höchstens 5 Stück. */
    public List<Aenderung> aktuelleAenderungen(int anzahl) {
        throw new UnsupportedOperationException("noch nicht implementiert");
    }

    /** TODO: Zahl für die Benachrichtigungsglocke liefern. */
    public long anzahlBenachrichtigungen() {
        return aenderungRepository.countDistinctByOffen(true);
    }

    public List<ZeitslotDTO> alleZeitslots(){
        List<Zeitslot> zeitslots = new ArrayList<>();
        zeitslotRepository.findAll().forEach(zeitslots::add);
        List<ZeitslotDTO> zeitslotDTOs = new ArrayList<>();
        for (Zeitslot z : zeitslots) {
            ZeitslotDTO zDto = new ZeitslotDTO();
            zDto.setId(z.getId());
            zDto.setStundennummer(z.getStundennummer());
            zDto.setStart(z.getStart());
            zDto.setEnde(z.getEnde());
            zeitslotDTOs.add(zDto);
        }
        return zeitslotDTOs;
    }

    public List<RaumDTO> verfuegbareRaeume(RaumanfrageDTO dto){
        Zeitslot zeitslot = zeitslotRepository.findById(dto.getZeitslotId())
                .orElseThrow(() -> new IllegalArgumentException("Zeitslot nicht gefunden"));
        Wochentag wochentag = ereignisService.zuWochentag(dto.getStartdatum().getDayOfWeek());
        Fach fach = fachRepository.findById(dto.getFachId())
                .orElseThrow(()-> new RuntimeException("Fach nicht gefunden"));
        return StreamSupport.stream(raumService.verfuegbareRaeume(wochentag, zeitslot, fach).spliterator(), false).map(raum -> new RaumDTO(raum.getId(),
                raum.getBezeichnung(),
                raum.getArt(),
                raum.getKapazitaet())).collect(Collectors.toList());
    }

}

