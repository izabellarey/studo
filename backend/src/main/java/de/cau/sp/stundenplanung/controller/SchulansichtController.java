package de.cau.sp.stundenplanung.controller;
import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import de.cau.sp.stundenplanung.dto.ereignis.EreignisDTO;
import de.cau.sp.stundenplanung.dto.ereignis.TagDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftInfoDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LerngruppeDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LerngruppeUebersichtDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.RaumAlternativeDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumplanEintragDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.StundenplanDTO;
import de.cau.sp.stundenplanung.model.EreignisStatus;
import de.cau.sp.stundenplanung.model.EreignisTyp;
import de.cau.sp.stundenplanung.model.Zeitslot;
import de.cau.sp.stundenplanung.service.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schulansicht")
@RequiredArgsConstructor
public class SchulansichtController {

    private final LerngruppeService lerngruppeService;
    private final LehrkraftService lehrkraftService;
    private final RaumService raumService;
    private final StundenplanService stundenplanService;

    @GetMapping
    public String schuljahr(){
        return stundenplanService.schuljahrZurueckgeben();
    }
    @PutMapping("/jahreswechsel")
    public String jahreswechsel(){
        return lerngruppeService.jahreswechsel();
    }

    @GetMapping("/archiv")
    public List<StundenplanDTO> archivierteStundenplaene(){
        return lerngruppeService.archivierteStundenplaene();
    }

    @GetMapping("/lehrkraefte")
    public List<LehrkraftInfoDTO> lehrkraefte(){
        return lehrkraftService.alleInfo();
    }

    @GetMapping("/lehrkraefte/{id}")
    public List<UnterrichtsstundeDTO> lehrkraftEinzeln(@PathVariable Long id){
        return lehrkraftService.lehrkraftEinzeln(id);
    }

    @GetMapping("/raeume")
    public List<RaumDTO> raeume(){
        return raumService.alleRaeume();
    }
    @GetMapping("/raeume/{id}")
    public List<RaumplanEintragDTO> raeumeEintrag(@PathVariable Long id){
        return raumService.raumplanAnzeigen(id);
    }

    @GetMapping("/lerngruppen")
    public List<LerngruppeUebersichtDTO> lerngruppen(){
        return lerngruppeService.alleKlassen();
    }

    @GetMapping("/lerngruppen/{id}")
    public List<UnterrichtsstundeDTO> lerngruppenEinzeln(@PathVariable Long id){
        return lerngruppeService.klassenstundenplanSchulansichtAnzeigen(id);
    }
}
