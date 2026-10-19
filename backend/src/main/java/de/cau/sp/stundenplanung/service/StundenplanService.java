package de.cau.sp.stundenplanung.service;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.AenderungsVorschlagDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LehrkraftAlternativeDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.RaumAlternativeDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.GenerierungGestartetDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.GenerierungStatusDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.StundenplanAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.stundenplan.UnterrichtsstundeInitialAendernDTO;


import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StundenplanService {
    private final LerngruppeService lerngruppeService;  
    private final StundenplanRepository stundenplanRepository;
    private final GenerierungAsyncService generierungAsyncService;
    private final GenerierungAnlegenService generierungAnlegenService;
    
    private final LerngruppeRepository lerngruppeRepository;
    private final UnterrichtsstundeRepository unterrichtsstundeRepository;
    private final LehrkraftRepository lehrkraftRepository;
    private final RaumRepository raumRepository;
    private final GenerierungslaufRepository generierungslaufRepository;
    private final AccountRepository accountRepository;


    //Server-Neutstart während Generierungslauf wäre ein Problem
    public GenerierungGestartetDTO generierungStarten() {
        Long neueId = generierungAnlegenService.generierungAnlegen();


        generierungAsyncService.generierungAsyncStarten(neueId);

        GenerierungGestartetDTO dto = new GenerierungGestartetDTO();
        dto.setGenerationId(neueId);
        return dto;
    }

    public GenerierungStatusDTO generierungStatus(Long generationId) {
        Generierungslauf lauf = generierungslaufRepository.findById(generationId)
            .orElseThrow(() -> new RuntimeException("Generierungslauf nicht gefunden"));

        GenerierungStatusDTO dto = new GenerierungStatusDTO();
        dto.setStatus(lauf.getStatus().name());
        return dto;
    }

    //Liefert neuste GenerationId, 
    public Long aktuelleOffeneGenerierung() {
        List<Generierungslauf> laeufe = new ArrayList<>();
        generierungslaufRepository.findAll().forEach(laeufe::add);

        //Zuerst pruefen: laeuft gerade ein Generierungslauf? Der hat Vorrang,
        //damit das Frontend beim Wiederbetreten der Seite den Ladezustand erkennt.
        for (Generierungslauf lauf : laeufe) {
            if (lauf.getStatus() == GenerierungsStatus.LAEUFT) {
                return lauf.getId();
            }
        }

        //Sonst: neuester abgeschlossener Lauf mit noch offenen (nicht veroeffentlichten) Plaenen
        Generierungslauf neuester = null;
        for (Generierungslauf lauf : laeufe) {
            if (lauf.getStatus() != GenerierungsStatus.FERTIG) {
                continue;
            }
            List<Stundenplan> plaene = stundenplanRepository.findByGenerationId(lauf.getId());
            boolean hatNochOffeneEntwuerfe = false;
            for (Stundenplan p : plaene) {
                if (p.getStatus() == PlanStatus.ENTWURF) {
                    hatNochOffeneEntwuerfe = true;
                    break;
                }
            }
            if (hatNochOffeneEntwuerfe) {
                if (neuester == null || lauf.getGestartetAm().isAfter(neuester.getGestartetAm())) {
                    neuester = lauf;
                }
            }
        }

        if (neuester == null) {
            throw new RuntimeException("Keine offene Generierung mit noch nicht veroeffentlichten Plaenen vorhanden");
        }

        return neuester.getId();
    }

    @Transactional
    public StundenplanAnzeigenDTO varianteAnzeigen(Long klassenId, Long generationId, Integer variante) {
        Stundenplan plan = stundenplanRepository
            .findByLerngruppe_IdAndGenerationIdAndVariante(klassenId, generationId, variante)
            .orElseThrow(() -> new RuntimeException("Keine Variante " + variante + " fuer diese Klasse gefunden"));

        return zuAnzeigenDTO(plan);
    }

    @Transactional
    public void finaleStundenplaeneVeroeffentlichen(Long generationId, Integer variante, String benutzername) {
        List<Stundenplan> alle = stundenplanRepository.findByGenerationId(generationId);


        if (alle.isEmpty()) {
            throw new RuntimeException("Keine Stundenplaene fuer diese Generation gefunden");
        }
        
        boolean varianteExistiert = false;
        for (Stundenplan plan : alle) {
            if (plan.getVariante().equals(variante)) {
                varianteExistiert = true;
                break;
            }
        }

        if (!varianteExistiert) {
            throw new RuntimeException("Variante " + variante + " existiert nicht fuer diese Generation");
        }

        Account ersteller = accountRepository.findByBenutzername(benutzername)
                .orElseThrow(() -> new RuntimeException("Account nicht gefunden."));
        //Alle bisher aktiven Pläne archivieren
        List<Stundenplan> bisherAktive = stundenplanRepository.findByStatus(PlanStatus.AKTIV);
        for (Stundenplan alterPlan : bisherAktive) {
            alterPlan.setStatus(PlanStatus.ARCHIVIERT);
            alterPlan.setEnddatum(LocalDate.now());
            stundenplanRepository.save(alterPlan);
        }

        //Neue Variante veröffentlichen
        for (Stundenplan plan : alle) {
            if (plan.getVariante().equals(variante)) {
                plan.setStatus(PlanStatus.AKTIV);
                plan.setStartdatum(LocalDate.now());
                plan.setErsteller(ersteller);
                stundenplanRepository.save(plan);
            } else {
                unterrichtsstundeRepository.deleteAll(plan.getUnterrichtsstunden());
                stundenplanRepository.delete(plan);
            }
        }
    }

    /*
    @Transactional
    public StundenplanAnzeigenDTO besteAlternativeAnzeigen(Long klassenId, Long generationId) {
        List<Stundenplan> alternativen = stundenplanRepository
            .findByLerngruppe_IdAndGenerationId(klassenId, generationId);

        if (alternativen.isEmpty()) {
            throw new RuntimeException("Keine Stundenplan-Alternativen gefunden");
        }

        Stundenplan beste = alternativen.get(0);
        for (Stundenplan plan : alternativen) {
            if (plan.getBewertungsScore() > beste.getBewertungsScore()) {
                beste = plan;
            }
        }

        return zuAnzeigenDTO(beste);
    }

    @Transactional
    public List<StundenplanAnzeigenDTO> weitereAlternativeAnzeigen(Long klassenId, Long generationId) {
        List<Stundenplan> alternativen = stundenplanRepository
            .findByLerngruppe_IdAndGenerationId(klassenId, generationId);

        if (alternativen.isEmpty()) {
            throw new RuntimeException("Keine Stundenplan-Alternativen gefunden");
        }

        // Beste Alternative ermitteln, um sie auszuschließen
        Stundenplan beste = alternativen.get(0);
        for (Stundenplan plan : alternativen) {
            if (plan.getBewertungsScore() > beste.getBewertungsScore()) {
                beste = plan;
            }
        }

        List<StundenplanAnzeigenDTO> result = new ArrayList<>();
        for (Stundenplan plan : alternativen) {
            if (!plan.getId().equals(beste.getId())) {
                result.add(zuAnzeigenDTO(plan));
            }
        }

        return result;
    }

    public void stundenplanWaehlen(Long stundenplanId) {
        Stundenplan plan = stundenplanRepository.findById(stundenplanId)
            .orElseThrow(() -> new RuntimeException("Stundenplan nicht gefunden"));

        plan.setStatus(PlanStatus.GEWAEHLT);
        stundenplanRepository.save(plan);
    }

    public void stundenplanAbwaehlen(Long stundenplanId) {
        Stundenplan plan = stundenplanRepository.findById(stundenplanId)
            .orElseThrow(() -> new RuntimeException("Stundenplan nicht gefunden"));

        plan.setStatus(PlanStatus.ENTWURF);
        stundenplanRepository.save(plan);
    }
    

    @Transactional
    public void finaleStundenplaeneVeroeffentlichen(Long generationId) {
        List<Stundenplan> alleAlternativen = stundenplanRepository.findByGenerationId(generationId);

        if (alleAlternativen.isEmpty()) {
            throw new RuntimeException("Keine Stundenpläne für diese Generation gefunden");
        }

        //Alle betroffenen Lerngruppen sammeln
        List<Long> lerngruppenIds = new ArrayList<>();
        for (Stundenplan plan : alleAlternativen) {
            Long lgId = plan.getLerngruppe().getId();
            if (!lerngruppenIds.contains(lgId)) {
                lerngruppenIds.add(lgId);
            }
        }

        //Prüfen, ob für jede Lerngruppe eine GEWAEHLTE Alternative existiert
        List<String> fehlendeKlassen = new ArrayList<>();
        for (Long lerngruppeId : lerngruppenIds) {
            boolean hatAuswahl = false;
            for (Stundenplan plan : alleAlternativen) {
                if (plan.getLerngruppe().getId().equals(lerngruppeId)
                        && plan.getStatus() == PlanStatus.GEWAEHLT) {
                    hatAuswahl = true;
                    break;
                }
            }
            if (!hatAuswahl) {
                Lerngruppe lg = lerngruppeRepository.findById(lerngruppeId)
                    .orElseThrow(() -> new RuntimeException("Lerngruppe nicht gefunden"));
                fehlendeKlassen.add(lg.getBezeichnung());
            }
        }

        if (!fehlendeKlassen.isEmpty()) {
            throw new RuntimeException(
                "Für folgende Klassen wurde noch kein Stundenplan gewählt: "
                    + String.join(", ", fehlendeKlassen));
        }

        //Veröffentlichen: gewählte Pläne aktivieren, restliche löschen
        for (Stundenplan plan : alleAlternativen) {
            if (plan.getStatus() == PlanStatus.GEWAEHLT) {
                plan.setStatus(PlanStatus.AKTIV);
                stundenplanRepository.save(plan);
            } else {
                stundenplanRepository.delete(plan);
            }
        }
    }
    */

    public AenderungsVorschlagDTO aenderungsVorschlagAnzeigen(Long unterrichtsstundeId) {
        Unterrichtsstunde stunde = unterrichtsstundeRepository.findById(unterrichtsstundeId)
            .orElseThrow(() -> new RuntimeException("Unterrichtsstunde nicht gefunden"));

        pruefeBearbeitbar(stunde.getStundenplan());

        List<Raum> raeume = lerngruppeService.fachraeumeVerfuegbar(
            stunde.getWochentag(), stunde.getZeitslot(), stunde.getFach());

        List<Lehrkraft> lehrkraefte = lerngruppeService.fachlehrkraefteVerfuegbar(
            stunde.getWochentag(), stunde.getZeitslot(), stunde.getStundenplan().getStartdatum(), stunde.getFach());

        List<RaumAlternativeDTO> raumDTOs = new ArrayList<>();
        for (Raum r : raeume) {
            RaumAlternativeDTO dto = new RaumAlternativeDTO();
            dto.setRaumId(r.getId());
            dto.setBezeichnung(r.getBezeichnung());
            dto.setFachraum(true);
            raumDTOs.add(dto);
        }

        List<LehrkraftAlternativeDTO> lehrkraftDTOs = new ArrayList<>();
        for (Lehrkraft l : lehrkraefte) {
            LehrkraftAlternativeDTO dto = new LehrkraftAlternativeDTO();
            dto.setLehrkraftId(l.getId());
            dto.setVorname(l.getAccount().getVorname());
            dto.setNachname(l.getAccount().getName());
            dto.setFachlehrkraft(true); //Hier ohne Bedeutung
            lehrkraftDTOs.add(dto);
        }

        AenderungsVorschlagDTO vorschlag = new AenderungsVorschlagDTO();
        vorschlag.setRaumalternativen(raumDTOs);
        vorschlag.setLehrkraftalternativen(lehrkraftDTOs);
        return vorschlag;
    }
    
    @Transactional
    public void unterrichtsstundeAendern(Long unterrichtsstundeId, UnterrichtsstundeInitialAendernDTO update) {
        Unterrichtsstunde stunde = unterrichtsstundeRepository.findById(unterrichtsstundeId)
            .orElseThrow(() -> new RuntimeException("Unterrichtsstunde nicht gefunden"));

        pruefeBearbeitbar(stunde.getStundenplan());

        if (update.getLehrkraftId() != null) {
            Lehrkraft neueLehrkraft = lehrkraftRepository.findById(update.getLehrkraftId())
                .orElseThrow(() -> new RuntimeException("Lehrkraft nicht gefunden"));
            stunde.setLehrkraft(neueLehrkraft);
        }

        if (update.getRaumId() != null) {
            Raum neuerRaum = raumRepository.findById(update.getRaumId())
                .orElseThrow(() -> new RuntimeException("Raum nicht gefunden"));
            stunde.setRaum(neuerRaum);
        }

        unterrichtsstundeRepository.save(stunde);
    }

    private void pruefeBearbeitbar(Stundenplan plan) {
        if (plan.getStatus() != PlanStatus.ENTWURF) {
            throw new RuntimeException("Nur Stundenplaene im Status ENTWURF koennen bearbeitet werden");
        }
    }

    private StundenplanAnzeigenDTO zuAnzeigenDTO(Stundenplan plan) {
        StundenplanAnzeigenDTO dto = new StundenplanAnzeigenDTO();
        dto.setId(plan.getId());
        dto.setStatus(plan.getStatus());
        dto.setVariante(plan.getVariante());

        List<UnterrichtsstundeDTO> stundenDTOs = new ArrayList<>();

        for (Unterrichtsstunde stunde : plan.getUnterrichtsstunden()) {
            UnterrichtsstundeDTO stundeDTO = new UnterrichtsstundeDTO();

            stundeDTO.setId(stunde.getId());
            stundeDTO.setWochentag(stunde.getWochentag());
            stundeDTO.setStundennummer(stunde.getZeitslot().getStundennummer());
            stundeDTO.setStart(stunde.getZeitslot().getStart());
            stundeDTO.setEnde(stunde.getZeitslot().getEnde());
            stundeDTO.setFachName(stunde.getFach().getName());
            stundeDTO.setRaumBezeichnung(stunde.getRaum().getBezeichnung());
            stundeDTO.setLerngruppeKuerzel(stunde.getLerngruppe().getBezeichnung());

            stundeDTO.setGeaendert(false);
            stundeDTO.setFreistunde(false);
            stundeDTO.setOffeneAenderung(false);
            stundeDTO.setGrund(null);

            stundenDTOs.add(stundeDTO);
        }

        dto.setUnterrichtsstunden(stundenDTOs);
        return dto;
    }

    public String schuljahrZurueckgeben(){
        List<Stundenplan> aktiv = stundenplanRepository.findByStatus(PlanStatus.AKTIV);
        if(aktiv.isEmpty()){
            return "Keine aktiven Stundenpläne in diesem Schuljahr. Schuljahr nicht bestimmbar.";
        }
        Stundenplan s = aktiv.get(0);
        return s.getSchuljahr();
    }
}
