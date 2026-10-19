package de.cau.sp.stundenplanung.service;
import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.*;

import de.cau.sp.stundenplanung.dto.stundenplan.StundenplanDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
public class LerngruppeService {
    private final LerngruppeRepository lerngruppeRepository;
    private final JahrgangsstufeRepository jahrgangsstufeRepository;
    private final AccountRepository accountRepository;
    private final RaumRepository raumRepository;
    private final StundenplanRepository stundenplanRepository;
    private final AenderungRepository aenderungRepository;
    private final UnterrichtsstundeRepository unterrichtsstundeRepository;
    private final LehrkraftRepository lehrkraftRepository;
    private final LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    private final SchuelerRepository schuelerRepository;
    private final LehrkraftService lehrkraftService;
    private final RaumService raumService;

    public List<JahrgangsstufeDTO> alleJahrgangsstufen(){
        return StreamSupport.stream(jahrgangsstufeRepository.findAll().spliterator(), false)
                .map(jahrgangsstufe -> new JahrgangsstufeDTO(
                        jahrgangsstufe.getId(),
                        jahrgangsstufe.getNummer(),
                        (int) lerngruppeRepository.countByJahrgangsstufeAndAktivTrue(jahrgangsstufe)                ))
                .collect(Collectors.toList());
    }

    /*public JahrgangsstufeDTO klassenstufeAnlegen(KlassenstufeDTO klassenstufe){
        return null;
    }*/

    public List<LerngruppeDTO> lerngruppeJGST(Long id){
        return StreamSupport.stream(lerngruppeRepository.findByJahrgangsstufeIdAndAktivTrue(id).spliterator(), false) //hier auch nach aktiven Klassen suchen
                .map(lerngruppe -> new LerngruppeDTO(
                        lerngruppe.getId(),
                        lerngruppe.getBezeichnung(),
                        lerngruppe.getKlassenleitung().getAccount().getVorname(),
                        lerngruppe.getKlassenleitung().getAccount().getName(),
                        lerngruppe.getSchueleranzahl(),
                        lerngruppe.getKlassenzimmer().getBezeichnung()
                        ))
                .collect(Collectors.toList());
    }
    @Transactional
    public LerngruppeDTO klasseAnlegen(Long id, KlasseDTO klasse){ //hier noch funktion zur automatischen zuordnung von klassenlehrkraft und klassenraum zuordnung
        Jahrgangsstufe jahrgangsstufe = jahrgangsstufeRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Jahrgangsstufe nicht gefunden."));
        Lehrkraft klassenleitung = lehrkraftService.zufaelligeLehrkraftFuerKlassenleitung();
        Raum klassenzimmer = raumService.zufaelligerRaumAlsKlassenzimmer();
        //falls runtime exception geworfen wird, was dann?
        Lerngruppe klasseNeu = new Lerngruppe();
        klasseNeu.setArt(LerngruppenArt.KLASSE);
        klasseNeu.setJahrgangsstufe(jahrgangsstufe);
        klasseNeu.setBezeichnung(klasse.getBezeichnung());
        klasseNeu.setKlassenzimmer(klassenzimmer);
        klasseNeu.setKlassenleitung(klassenleitung);
        klasseNeu.setSchueleranzahl(0);
        klasseNeu.setAktiv(true);
        Lerngruppe gespeichert = lerngruppeRepository.save(klasseNeu);

        return new LerngruppeDTO(gespeichert.getId(),
                gespeichert.getBezeichnung(),
                gespeichert.getKlassenleitung().getAccount().getVorname(),
                gespeichert.getKlassenleitung().getAccount().getName(),
                gespeichert.getSchueleranzahl(),
                gespeichert.getKlassenzimmer().getBezeichnung());
    }

    public void klasseDeaktivieren(Long lerngruppeId){ //hier nur klasse deaktivieren und zugehörigen stundenplan archivieren und klassenleitung = null? und schüleraccounts deaktivieren
            Lerngruppe klasse = lerngruppeRepository.findById(lerngruppeId)
                    .orElseThrow(()-> new RuntimeException("Lerngruppe nicht gefunden."));
            klasse.setAktiv(false);
            List<Schueler> besuchenKlasse = schuelerRepository.findByLerngruppe(klasse);

            for(Schueler s : besuchenKlasse){
                s.setLerngruppe(null);
                s.getAccount().setAktiv(false);
                schuelerRepository.save(s);
            }
            Stundenplan stundenplan = stundenplanRepository.findByLerngruppeAndStatus(klasse, PlanStatus.AKTIV)
                    .orElse(null);
        if(!(stundenplan == null)) {
            stundenplan.setStatus(PlanStatus.ARCHIVIERT);
            stundenplan.setEnddatum(LocalDate.now());
            stundenplanRepository.save(stundenplan);
        }
            klasse.setKlassenzimmer(null);
            klasse.setKlassenleitung(null);
            lerngruppeRepository.save(klasse);
    }

    public LerngruppeDTO klasseBearbeiten(Long lerngruppeId, LerngruppeBearbeitenDTO klasseBearbeitenDTO) {
        Lerngruppe klasse = lerngruppeRepository.findById(lerngruppeId)
                .orElseThrow(() -> new RuntimeException("Klasse nicht gefunden."));
        
        if (!klasse.isAktiv()) {
            throw new RuntimeException("Deaktivierte Klassen koennen nicht bearbeitet werden.");
        }

        klasse.setBezeichnung(klasseBearbeitenDTO.getNeueBezeichnung());
        Lerngruppe gespeichert = lerngruppeRepository.save(klasse);
        return new LerngruppeDTO(gespeichert.getId(),
                gespeichert.getBezeichnung(),
                gespeichert.getKlassenleitung().getAccount().getVorname(),
                gespeichert.getKlassenleitung().getAccount().getName(),
                gespeichert.getSchueleranzahl(),
                gespeichert.getKlassenzimmer().getBezeichnung());

    }

    public List<UnterrichtsstundeDTO> klassenstundenplanAnzeigen(Long id){
        Lerngruppe klasse = lerngruppeRepository.findById(id) //hier auch nach aktiven Klassen suchen?eher nicht, man bekommt nur aktive klassen übergeben
                .orElseThrow(()-> new RuntimeException("Klasse nicht gefunden"));

        Stundenplan aktiverPlan = stundenplanRepository.findByLerngruppeAndStatus(klasse, PlanStatus.AKTIV)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kein aktiver Stundenplan gefunden. Falls diese Klasse kürzlich neu erstellt wurde, müssen alle Stundenpläne initial neu generiert werden, damit diese Klasse einbezogen werden kann."
                ));
        List<Unterrichtsstunde> stunden = aktiverPlan.getUnterrichtsstunden();

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        List<Aenderung> aenderungen = aenderungRepository.findByLerngruppeNeuAndDatumBetween(
                klasse, weekStart, weekEnd
        );

        Map<String, UnterrichtsstundeDTO> eintragMap = new HashMap<>();

        for (Unterrichtsstunde stunde : stunden) {
            String key = stunde.getWochentag().name() + "_" + stunde.getZeitslot().getId();
            eintragMap.put(key, createDTOFromStunde(stunde, false));
        }

        for (Aenderung aenderung : aenderungen) {
            String key;

            if (aenderung.getUnterrichtsstunde() != null) {
                Unterrichtsstunde stunde = aenderung.getUnterrichtsstunde();
                key = stunde.getWochentag().name() + "_" + stunde.getZeitslot().getId();
            } else {
                key = aenderung.getWochentagNeu().name()
                        + "_"
                        + aenderung.getZeitslotNeu().getId();
            }

            eintragMap.put(key, createDTOFromAenderung(aenderung, true));
        }

        return eintragMap.values().stream()
                .sorted(Comparator
                        .comparing(UnterrichtsstundeDTO::getWochentag)
                        .thenComparing(UnterrichtsstundeDTO::getStundennummer))
                .collect(Collectors.toList());
    }

    private UnterrichtsstundeDTO createDTOFromStunde(Unterrichtsstunde stunde, boolean geaendert) {
        return new UnterrichtsstundeDTO(
                stunde.getId(),
                stunde.getWochentag(),
                stunde.getZeitslot().getStundennummer(),
                stunde.getZeitslot().getStart(),
                stunde.getZeitslot().getEnde(),
                stunde.getFach().getKuerzel(),
                stunde.getRaum().getBezeichnung(),
                stunde.getLehrkraft().getKuerzel(),
                stunde.getLerngruppe().getBezeichnung(),
                geaendert,
                false,
                false,
                null
        );
    }

    private UnterrichtsstundeDTO createDTOFromAenderung(Aenderung aenderung, boolean geaendert) {


        return new UnterrichtsstundeDTO(
                aenderung.getId(),
                aenderung.getWochentagNeu(),
                aenderung.getZeitslotNeu().getStundennummer(),
                aenderung.getZeitslotNeu().getStart(),
                aenderung.getZeitslotNeu().getEnde(),
                aenderung.getFachNeu().getKuerzel(),
                aenderung.getRaumNeu().getBezeichnung(),
                aenderung.getLehrkraftNeu().getKuerzel(),
                aenderung.getLerngruppeNeu().getBezeichnung(),
                geaendert,
                aenderung.isFreistunde(),
                aenderung.isOffen(),
                aenderung.getAusloeser().getTyp()
        );
    }
    @Transactional(readOnly = true)
    public AenderungsVorschlagDTO vorschlaegeErmitteln(Long aenderungId) {
        Aenderung aenderung = aenderungRepository.findById(aenderungId)
                .orElseThrow(() -> new EntityNotFoundException("Aenderung mit ID " + aenderungId + " nicht gefunden"));

        List<RaumAlternativeDTO> raumAlternativen = new ArrayList<>();
        List<LehrkraftAlternativeDTO> lehrkraftAlternativen = new ArrayList<>();

        if (aenderung.isFreistunde()) {
            return new AenderungsVorschlagDTO(raumAlternativen, lehrkraftAlternativen);
        }

        EreignisTyp typ = aenderung.getAusloeser().getTyp();
        boolean raumGesucht = typ == EreignisTyp.RAUMAENDERUNG || typ == EreignisTyp.ZUSATZSTUNDE;
        boolean lehrkraftGesucht = typ == EreignisTyp.AUSFALL;

        Wochentag tag = aenderung.getWochentagNeu();
        Zeitslot zeitslot = aenderung.getZeitslotNeu();
        LocalDate datum = aenderung.getDatum();
        Fach fach = aenderung.getFachNeu();

        if (raumGesucht) {
            raumAlternativen = verfuegbareRaeume(tag, zeitslot, datum).stream()
                    .map(raum -> zuRaumAlternativeDTO(raum, fach))
                    .collect(Collectors.toList());
        }
        if (lehrkraftGesucht) {
            lehrkraftAlternativen = verfuegbareLehrkraefte(tag, zeitslot, datum).stream()
                    .map(lehrkraft -> zuLehrkraftAlternativeDTO(lehrkraft, fach))
                    .collect(Collectors.toList());
        }

        return new AenderungsVorschlagDTO(raumAlternativen, lehrkraftAlternativen);
    }

    /** Wiederverwendbar für die Stundenplan-Erstgenerierung: nur die fachlich passenden freien Räume. */
    @Transactional
    public List<Raum> fachraeumeVerfuegbar(Wochentag tag, Zeitslot zeitslot, Fach fach) {
        return verfuegbareRaeumeOhneDatum(tag, zeitslot).stream()
                .filter(raum -> raumPasstZuFach(raum, fach))
                .collect(Collectors.toList());
    }

    /** Wiederverwendbar für die Stundenplan-Erstgenerierung: nur die fachlich passenden freien Lehrkräfte. */
    @Transactional
    public List<Lehrkraft> fachlehrkraefteVerfuegbar(Wochentag tag, Zeitslot zeitslot, LocalDate datum, Fach fach) {
        return verfuegbareLehrkraefte(tag, zeitslot, datum).stream()
                .filter(lehrkraft -> lehrkraftPasstZuFach(lehrkraft, fach))
                .collect(Collectors.toList());
    }
    private List<Raum> verfuegbareRaeumeOhneDatum(Wochentag tag, Zeitslot zeitslot) {
        Set<Long> belegteRaumIds = unterrichtsstundeRepository
                .findRaeumeByWochentagAndZeitslot(tag, zeitslot, PlanStatus.AKTIV).stream()
                .map(Raum::getId)
                .collect(Collectors.toCollection(HashSet::new));

        for (Aenderung bestaetigt : aenderungRepository.findBestaetigteByWochentagNeuAndZeitslotNeu(tag, zeitslot)) {
            Long raumId = bestaetigt.getRaumNeu().getId();
            if (bestaetigt.isFreistunde()) {
                belegteRaumIds.remove(raumId);
            } else {
                belegteRaumIds.add(raumId);
            }
        }

        return StreamSupport.stream(raumRepository.findAll().spliterator(), false)
                .filter(raum -> !belegteRaumIds.contains(raum.getId()))
                .collect(Collectors.toList());
    }

    private List<Raum> verfuegbareRaeume(Wochentag tag, Zeitslot zeitslot, LocalDate datum) {
        Set<Long> belegteRaumIds = unterrichtsstundeRepository
                .findRaeumeByWochentagAndZeitslot(tag, zeitslot, PlanStatus.AKTIV).stream()
                .map(Raum::getId)
                .collect(Collectors.toCollection(HashSet::new));
//jetzt alle Änderungen, es werden nur Räume und Lehrer die GAR NICHT verplant sind vorgeschlagen
        for (Aenderung bestaetigt : aenderungRepository.findByWochentagNeuAndZeitslotNeuAndDatum(tag, zeitslot, datum)) {
            Long raumId = bestaetigt.getRaumNeu().getId();
            if (bestaetigt.isFreistunde()) {
                belegteRaumIds.remove(raumId);
            } else {
                belegteRaumIds.add(raumId);
            }
        }

        return StreamSupport.stream(raumRepository.findAll().spliterator(), false)
                .filter(raum -> !belegteRaumIds.contains(raum.getId()))
                .collect(Collectors.toList());
    }

    private List<Lehrkraft> verfuegbareLehrkraefte(Wochentag tag, Zeitslot zeitslot, LocalDate datum) {
        Set<Long> belegteLehrkraftIds = unterrichtsstundeRepository
                .findLehrkraefteByWochentagAndZeitslot(tag, zeitslot, PlanStatus.AKTIV).stream() //aktivitätsstatus einbauen
                .map(Lehrkraft::getId)
                .collect(Collectors.toCollection(HashSet::new));

        for (Aenderung aenderungen : aenderungRepository.findByWochentagNeuAndZeitslotNeuAndDatum(tag, zeitslot, datum)) {
            Long lehrkraftId = aenderungen.getLehrkraftNeu().getId();
            if (aenderungen.isFreistunde()) {
                belegteLehrkraftIds.remove(lehrkraftId);
            } else {
                belegteLehrkraftIds.add(lehrkraftId);
            }
        }

        // Lehrerverfuegbarkeit: nur GESPERRT schliesst aus. NICHT_BEVORZUGT wird bewusst
        // (noch) nicht beruecksichtigt - hier waere der Punkt, an dem man Lehrkraefte mit
        // NICHT_BEVORZUGT statt komplett auszuschliessen z. B. in eine separate,
        // nachrangige Liste einsortieren oder im DTO markieren koennte.
        belegteLehrkraftIds.addAll(lehrerverfuegbarkeitRepository
                .findLehrkraftIdsByWochentagAndZeitslotAndDatumAndTyp(tag, zeitslot, datum, VerfuegbarkeitsTyp.GESPERRT)); //aktivitätsstatus filtern

        return StreamSupport.stream(lehrkraftRepository.findByAccountAktivTrue().spliterator(), false) //aktivitätsstatus filtern
                .filter(lehrkraft -> !belegteLehrkraftIds.contains(lehrkraft.getId()))
                .collect(Collectors.toList());
    }

    private boolean raumPasstZuFach(Raum raum, Fach fach) {
        Raumart benoetigterTyp = fach.getBenoetigterRaumtyp();
        return benoetigterTyp == null
                ? raum.getArt() == Raumart.KLASSENRAUM
                : raum.getArt() == benoetigterTyp;
    }

    private boolean lehrkraftPasstZuFach(Lehrkraft lehrkraft, Fach fach) {
        return lehrkraft.getFaecher().contains(fach);
    }

    private RaumAlternativeDTO zuRaumAlternativeDTO(Raum raum, Fach fach) {
        RaumAlternativeDTO dto = new RaumAlternativeDTO();
        dto.setRaumId(raum.getId());
        dto.setBezeichnung(raum.getBezeichnung());
        dto.setFachraum(raumPasstZuFach(raum, fach));
        return dto;
    }

    private LehrkraftAlternativeDTO zuLehrkraftAlternativeDTO(Lehrkraft lehrkraft, Fach fach) {
        LehrkraftAlternativeDTO dto = new LehrkraftAlternativeDTO();
        dto.setLehrkraftId(lehrkraft.getId());
        dto.setVorname(lehrkraft.getAccount().getVorname());
        dto.setNachname(lehrkraft.getAccount().getName());
        dto.setFachlehrkraft(lehrkraftPasstZuFach(lehrkraft, fach));
        return dto;
    }

    public UnterrichtsstundeDTO aenderungBestaetigen(Long aenderungId, AenderungAuswahlDTO auswahl){
        Aenderung aenderung = aenderungRepository.findById(aenderungId)
                .orElseThrow(()-> new RuntimeException("Aenderung nicht gefunden"));

        if(auswahl.getGewaehlteLehrkraft() != null){
            Long gewaehlteLehrkraftId = auswahl.getGewaehlteLehrkraft().getLehrkraftId();
            if(aenderung.getLehrkraftNeu().getId() != gewaehlteLehrkraftId){
                Lehrkraft lehrkraftNeu = lehrkraftRepository.findById(gewaehlteLehrkraftId) //hier kann eigentlich nur eine aktive lehrkraft übergeben werden
                        .orElseThrow(()-> new RuntimeException("Lehrkraft nicht gefunden"));
                aenderung.setLehrkraftNeu(lehrkraftNeu);
                aenderungRepository.save(aenderung);
            }
        }

        if(auswahl.getGewaehlterRaum() != null){
            Long gewaehlterRaumId = auswahl.getGewaehlterRaum().getRaumId();

            if(aenderung.getRaumNeu().getId() != gewaehlterRaumId){
                Raum raumNeu = raumRepository.findById(gewaehlterRaumId)
                        .orElseThrow(()-> new RuntimeException("Raum nicht gefunden"));
                aenderung.setRaumNeu(raumNeu);
                aenderungRepository.save(aenderung);
            }

        }
        aenderung.setOffen(false);
        aenderungRepository.save(aenderung);


        return new UnterrichtsstundeDTO(aenderung.getId(),
                aenderung.getWochentagNeu(),
                aenderung.getZeitslotNeu().getStundennummer(),
                aenderung.getZeitslotNeu().getStart(),
                aenderung.getZeitslotNeu().getEnde(),
                aenderung.getFachNeu().getKuerzel(),
                aenderung.getRaumNeu().getBezeichnung(),
                aenderung.getLehrkraftNeu().getKuerzel(),
                aenderung.getLerngruppeNeu().getBezeichnung(),
                true,
                aenderung.isFreistunde(),
                aenderung.isOffen(),
                aenderung.getAusloeser().getTyp()); //hier beim mergen noch anpassen auf ids
    }

    public List<LerngruppeUebersichtDTO> alleKlassen() {
        List<Lerngruppe> lerngruppen = new ArrayList<>();
        lerngruppeRepository.findAllByAktivTrue().forEach(lerngruppen::add);

        List<LerngruppeUebersichtDTO> result = new ArrayList<>();
        for (Lerngruppe lg : lerngruppen) {
            if (lg.getArt() == LerngruppenArt.KLASSE) {
                LerngruppeUebersichtDTO dto = new LerngruppeUebersichtDTO();
                dto.setId(lg.getId());
                dto.setBezeichnung(lg.getBezeichnung());
                if (lg.getJahrgangsstufe() != null) {
                    dto.setJahrgangsnummer(lg.getJahrgangsstufe().getNummer());
                }
                result.add(dto);
            }
        }
        return result;
    }

    public List<StundenplanDTO> archivierteStundenplaene(){
        List<Stundenplan> stundenplaene = stundenplanRepository.findByStatusWithStunden(PlanStatus.ARCHIVIERT);
        List<StundenplanDTO> result = new ArrayList<>();
        for(Stundenplan stundenplan : stundenplaene){
            List<Unterrichtsstunde> stunden = stundenplan.getUnterrichtsstunden();
            List<UnterrichtsstundeDTO> stundenDto = new ArrayList<>();
            for(Unterrichtsstunde s : stunden){
                stundenDto.add(createDTOFromStunde(s, false));
            }

            StundenplanDTO dto = new StundenplanDTO(
                    stundenplan.getSchuljahr(),
                    stundenplan.getHalbjahr(),
                    stundenplan.getLerngruppe().getBezeichnung(),
                    stundenplan.getStartdatum(),
                    stundenplan.getEnddatum(),
                    stundenplan.getErsteller().getVorname(),
                    stundenplan.getErsteller().getName(),
                    stundenDto);
            result.add(dto);

        }

        return result;
        }

    public List<UnterrichtsstundeDTO> klassenstundenplanSchulansichtAnzeigen(Long id){
        Lerngruppe klasse = lerngruppeRepository.findById(id) //hier auch nach aktiven Klassen suchen?eher nicht, man bekommt nur aktive klassen übergeben
                .orElseThrow(()-> new RuntimeException("Klasse nicht gefunden"));

        Stundenplan aktiverPlan = stundenplanRepository.findByLerngruppeAndStatus(klasse, PlanStatus.AKTIV)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kein aktiver Stundenplan gefunden. Falls diese Klasse kürzlich neu erstellt wurde, müssen alle Stundenpläne initial neu generiert werden, damit diese Klasse einbezogen werden kann."
                ));
        List<Unterrichtsstunde> stunden = aktiverPlan.getUnterrichtsstunden();

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        List<Aenderung> aenderungen = aenderungRepository.findAbgeschlosseneByLerngruppeNeuAndDatumBetween(
                klasse, weekStart, weekEnd
        );

        Map<String, UnterrichtsstundeDTO> eintragMap = new HashMap<>();

        for (Unterrichtsstunde stunde : stunden) {
            String key = stunde.getWochentag().name() + "_" + stunde.getZeitslot().getId();
            eintragMap.put(key, createDTOFromStunde(stunde, false));
        }

        for (Aenderung aenderung : aenderungen) {
            String key;

            if (aenderung.getUnterrichtsstunde() != null) {
                Unterrichtsstunde stunde = aenderung.getUnterrichtsstunde();
                key = stunde.getWochentag().name() + "_" + stunde.getZeitslot().getId();
            } else {
                key = aenderung.getWochentagNeu().name()
                        + "_"
                        + aenderung.getZeitslotNeu().getId();
            }

            eintragMap.put(key, createDTOFromAenderung(aenderung, true));
        }

        return eintragMap.values().stream()
                .sorted(Comparator
                        .comparing(UnterrichtsstundeDTO::getWochentag)
                        .thenComparing(UnterrichtsstundeDTO::getStundennummer))
                .collect(Collectors.toList());
    }
    @Transactional
    public String jahreswechsel(){
        List<Stundenplan> aktive = stundenplanRepository.findByStatus(PlanStatus.AKTIV);
        for(Stundenplan sp : aktive){
            sp.setStatus(PlanStatus.ARCHIVIERT);
            sp.setEnddatum(LocalDate.now());
            stundenplanRepository.save(sp);
        }
        List<Lerngruppe> lerngruppen = lerngruppeRepository.findAllByAktivTrue();
        for(Lerngruppe l : lerngruppen){
            Jahrgangsstufe current = l.getJahrgangsstufe();
            if(current.getNummer() != 10){
                Jahrgangsstufe next = jahrgangsstufeRepository
                        .findFirstByNummerGreaterThanOrderByNummerAsc(current.getNummer())
                        .orElseThrow(() -> new NoSuchElementException("Keine höhere Jahrgangsstufe vorhanden"));
                l.setJahrgangsstufe(next);
                String buchstabe = l.getBezeichnung().strip().replaceAll("^\\d+", "");
                l.setBezeichnung(l.getJahrgangsstufe().getNummer() + buchstabe);
                lerngruppeRepository.save(l);
            } else {
                klasseDeaktivieren(l.getId());
            }
                    }
        return "Der Jahreswechsel wurde durchgeführt. Alle Schüler der Jahrgangsstufe 10 letzten Jahres wurden abgemeldet, alle Lerngruppen letzten Jahres wurden die nächsthöhere Jahrgangsstufe versetzt. Sie können nun neue Lerngruppen der Jahrgangsstufe fünf anlegen und anschließend neue Schüler in das System einpflegen.";
    }
    }







