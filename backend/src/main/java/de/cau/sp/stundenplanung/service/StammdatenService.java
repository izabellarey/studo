package de.cau.sp.stundenplanung.service;

//import de.cau.sp.stundenplanung.dto.stammdaten.BlockungsvorgabeDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.RasterpauseDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.MaximalbelastungDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.PausenregelDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.PflichtfachvorgabeDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.SchulprofilDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.StammdatenDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.StufenvorgabeDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.StundenrasterDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.VertretungsprioritaetDTO;
import de.cau.sp.stundenplanung.dto.stammdaten.ZeitslotDTO;
import de.cau.sp.stundenplanung.model.Blockregel;
import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.model.Haerte;
import de.cau.sp.stundenplanung.model.Jahrgangsstufe;
import de.cau.sp.stundenplanung.model.Lehrerverfuegbarkeit;
import de.cau.sp.stundenplanung.model.Maximalbelastung;
import de.cau.sp.stundenplanung.model.Pausenregel;
import de.cau.sp.stundenplanung.model.Pflichtfachvorgabe;
import de.cau.sp.stundenplanung.model.PlanStatus;
import de.cau.sp.stundenplanung.model.Rasterpause;
import de.cau.sp.stundenplanung.model.Raumverfuegbarkeit;
import de.cau.sp.stundenplanung.model.Schule;
import de.cau.sp.stundenplanung.model.Stundenplan;
import de.cau.sp.stundenplanung.model.Stundenraster;
import de.cau.sp.stundenplanung.model.Zeitslot;
import de.cau.sp.stundenplanung.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StammdatenService {

    private final SchuleRepository schuleRepository;
    private final ZeitslotRepository zeitslotRepository;
    private final StundenrasterRepository stundenrasterRepository;
    private final RasterpauseRepository rasterpauseRepository;
    private final PausenregelRepository pausenregelRepository;
    private final BlockregelRepository blockregelRepository;
    private final PflichtfachvorgabeRepository pflichtfachvorgabeRepository;
    private final MaximalbelastungRepository maximalbelastungRepository;
    private final JahrgangsstufeRepository jahrgangsstufeRepository;
    //private final RahmenbedingungRepository rahmenbedingungRepository;
    private final StundenplanRepository stundenplanRepository;
    private final FachRepository fachRepository;
    private final LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    private final RaumverfuegbarkeitRepository raumverfuegbarkeitRepository;

   
    @Transactional
    public StammdatenDTO alles() {
        StammdatenDTO dto = new StammdatenDTO();

        dto.setSchulprofil(schulprofil());
        dto.setStundenraster(stundenraster().getZeitslots());
        dto.setPausenregeln(pausenregeln());
        dto.setPflichtfaecher(pflichtfaecher());
        dto.setVertretungsprioritaet(vertretungsprioritaet());

        return dto;
    }

    //Kompliziert, da viele Vergleiche mit IDs vorher gemacht werden.
    //Außerdem unnötig wenn jede "Sache" einzelnt gespeichert wird (so wie es gerade im Controller passiert)
    /*
    public StammdatenDTO allesSpeichern(StammdatenDTO stammdaten) {
        throw new UnsupportedOperationException("noch nicht implementiert");
    }
    */

    public SchulprofilDTO schulprofil() {
        //Liste mit nur einer Schule
        List<Schule> schulen = new ArrayList<>();
        schuleRepository.findAll().forEach(schulen::add);

        if (schulen.isEmpty()) {
            throw new RuntimeException("Schule nicht konfiguriert");
        }
        Schule schule = schulen.get(0);

        SchulprofilDTO dto = new SchulprofilDTO();

        dto.setName(schule.getName());
        dto.setBundesland(schule.getBundesland());
        dto.setSchulform(schule.getSchulform());

        return dto;
    }

    public SchulprofilDTO schulprofilSpeichern(SchulprofilDTO profil) {
        List<Schule> schulen = new ArrayList<>();
        schuleRepository.findAll().forEach(schulen::add);

        Schule schule;
        if (schulen.isEmpty()) {
            schule = new Schule();
        } else {
            schule = schulen.get(0);
        }

        schule.setName(profil.getName());
        schule.setBundesland(profil.getBundesland());
        schule.setSchulform(profil.getSchulform());

        Schule gespeicherteSchule = schuleRepository.save(schule);

        SchulprofilDTO dto = new SchulprofilDTO();
        dto.setName(gespeicherteSchule.getName());
        dto.setBundesland(gespeicherteSchule.getBundesland());
        dto.setSchulform(gespeicherteSchule.getSchulform());

        return dto;
    }

    public StundenrasterDTO stundenraster() {
        List<Stundenraster> raster = new ArrayList<>();
        stundenrasterRepository.findAll().forEach(raster::add);

        if (raster.isEmpty()) {
            throw new RuntimeException("Stundenraster nicht konfiguriert");
        }
        Stundenraster r = raster.get(0);

        //Grunddaten füllen
        StundenrasterDTO dto = new StundenrasterDTO();
        dto.setId(r.getId());
        dto.setSchulbeginn(r.getSchulbeginn());
        dto.setStundenProTag(r.getStundenProTag());
        dto.setStundendauer(r.getStundendauer());

        //Pausen-Liste füllen
        List<Rasterpause> pausen = rasterpauseRepository.findByStundenraster(r);
        List<RasterpauseDTO> pausenDTOs = new ArrayList<>();
        for (Rasterpause p : pausen) {
            RasterpauseDTO pDto = new RasterpauseDTO();
            pDto.setId(p.getId());
            pDto.setNachStunde(p.getNachStunde());
            pDto.setDauerMinuten(p.getDauerMinuten());
            pausenDTOs.add(pDto);
        }
        dto.setPausen(pausenDTOs);

        //Zeitslot-Liste füllen
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
        dto.setZeitslots(zeitslotDTOs);

        return dto;
    }

    @Transactional
    public StundenrasterDTO stundenrasterSpeichern(StundenrasterDTO update) {

        //Änderung nur erlauben, solange noch kein Stundenplan existiert
        pruefeKeinStundenplanExistiert();

        //Bestehendes Raster laden, oder neues anlegen, falls noch keins existiert
        List<Stundenraster> rasterListe = new ArrayList<>();
        stundenrasterRepository.findAll().forEach(rasterListe::add);

        Stundenraster raster;
        if (rasterListe.isEmpty()) {
            raster = new Stundenraster();
        } else {
            raster = rasterListe.get(0);
        }

        //Grunddaten des Rasters aktualisieren und speichern
        raster.setSchulbeginn(update.getSchulbeginn());
        raster.setStundenProTag(update.getStundenProTag());
        raster.setStundendauer(update.getStundendauer());
        Stundenraster gespeichertesRaster = stundenrasterRepository.save(raster);

        //Alte Pausen entfernen, neue anlegen
        List<Rasterpause> altePausen = rasterpauseRepository.findByStundenraster(gespeichertesRaster);
        rasterpauseRepository.deleteAll(altePausen);

        List<Rasterpause> neuePausen = new ArrayList<>();
        if (update.getPausen() != null) {
            for (RasterpauseDTO pDto : update.getPausen()) {
                Rasterpause pause = new Rasterpause();
                pause.setStundenraster(gespeichertesRaster);
                pause.setNachStunde(pDto.getNachStunde());
                pause.setDauerMinuten(pDto.getDauerMinuten());
                neuePausen.add(rasterpauseRepository.save(pause));
            }
        }

        //Alte Zeitslots verwerfen, aus den neuen Rasterdaten und Pausen neu berechnen (private Methode)
        List<Zeitslot> alteZeitslots = new ArrayList<>();
        zeitslotRepository.findAll().forEach(alteZeitslots::add);

        //Vor dem Löschen: alle Referenzen auf die alten Zeitslots entfernen,
        //sonst schlägt das Löschen wegen Fremdschlüssel-Constraints fehl
        for (Zeitslot alterSlot : alteZeitslots) {
            List<Lehrerverfuegbarkeit> verfuegbarkeiten = lehrerverfuegbarkeitRepository.findByZeitslot(alterSlot);
            lehrerverfuegbarkeitRepository.deleteAll(verfuegbarkeiten);

            List<Raumverfuegbarkeit> raumverfuegbarkeiten = raumverfuegbarkeitRepository.findByZeitslot(alterSlot);
            raumverfuegbarkeitRepository.deleteAll(raumverfuegbarkeiten);
        }

        zeitslotRepository.deleteAll(alteZeitslots);

        List<Zeitslot> neueZeitslots = berechneZeitslots(gespeichertesRaster, neuePausen);
        List<Zeitslot> gespeicherteZeitslots = new ArrayList<>();
        for (Zeitslot z : neueZeitslots) {
            gespeicherteZeitslots.add(zeitslotRepository.save(z));
        }

        //Rückgabe-DTO aus den gespeicherten, aktuellen Werten zusammenbauen
        StundenrasterDTO dto = new StundenrasterDTO();
        dto.setId(gespeichertesRaster.getId());
        dto.setSchulbeginn(gespeichertesRaster.getSchulbeginn());
        dto.setStundenProTag(gespeichertesRaster.getStundenProTag());
        dto.setStundendauer(gespeichertesRaster.getStundendauer());

        List<RasterpauseDTO> pausenDTOs = new ArrayList<>();
        for (Rasterpause p : neuePausen) {
            RasterpauseDTO pDto = new RasterpauseDTO();
            pDto.setId(p.getId());
            pDto.setNachStunde(p.getNachStunde());
            pDto.setDauerMinuten(p.getDauerMinuten());
            pausenDTOs.add(pDto);
        }
        dto.setPausen(pausenDTOs);

        List<ZeitslotDTO> zeitslotDTOs = new ArrayList<>();
        for (Zeitslot z : gespeicherteZeitslots) {
            ZeitslotDTO zDto = new ZeitslotDTO();
            zDto.setId(z.getId());
            zDto.setStundennummer(z.getStundennummer());
            zDto.setStart(z.getStart());
            zDto.setEnde(z.getEnde());
            zeitslotDTOs.add(zDto);
        }
        dto.setZeitslots(zeitslotDTOs);

        return dto;
    }
    
    //Wird in stundenrasterSpeichern verwendet
    private List<Zeitslot> berechneZeitslots(Stundenraster raster, List<Rasterpause> pausen) {
        List<Zeitslot> zeitslots = new ArrayList<>();

        LocalTime aktuelleZeit = raster.getSchulbeginn();

        for (int stundennummer = 1; stundennummer <= raster.getStundenProTag(); stundennummer++) {
            Zeitslot stunde = new Zeitslot();
            stunde.setStundennummer(stundennummer);
            stunde.setStart(aktuelleZeit);
            stunde.setEnde(aktuelleZeit.plusMinutes(raster.getStundendauer()));
            zeitslots.add(stunde);

            aktuelleZeit = stunde.getEnde();

            for (Rasterpause p : pausen) {
                if (p.getNachStunde() == stundennummer) {
                    aktuelleZeit = aktuelleZeit.plusMinutes(p.getDauerMinuten());
                }
            }
        }

        return zeitslots;
    }

    //Wird in stundenrasterSpeichern verwendet
    private void pruefeKeinStundenplanExistiert() {
        List<Stundenplan> alle = new ArrayList<>();
        stundenplanRepository.findAll().forEach(alle::add);

        if (!alle.isEmpty()) {
            throw new RuntimeException("Stundenraster kann nicht geaendert werden, sobald bereits Stundenplaene existieren");
        }
    }


    public List<PausenregelDTO> pausenregeln() {
        List<Pausenregel> alleRegeln = new ArrayList<>();
        pausenregelRepository.findAll().forEach(alleRegeln::add);

        List<PausenregelDTO> result = new ArrayList<>();
        for (Pausenregel regel : alleRegeln) {
            result.add(zuPausenregelDTO(regel));
        }
        return result;
    }
/* 
    public PausenregelDTO pausenregelAnlegen(PausenregelDTO neu) {
        Jahrgangsstufe stufe = jahrgangsstufeRepository.findById(neu.getJahrgangsstufeId())
            .orElseThrow(() -> new RuntimeException("Jahrgangsstufe nicht gefunden"));

        Pausenregel regel = new Pausenregel();
        regel.setJahrgangsstufe(stufe);
        regel.setMindestpauseNachStunden(neu.getMindestpauseNachStunden());
        regel.setMindestdauerMinuten(neu.getMindestdauerMinuten());
        regel.setAktiv(neu.isAktiv());

        // TODO: Default-Werte
        regel.setHaerte(Haerte.WEICH);
        regel.setGewicht(0);

        Pausenregel gespeichert = pausenregelRepository.save(regel);
        return zuPausenregelDTO(gespeichert);
    }

    public PausenregelDTO pausenregelBearbeiten(Long id, PausenregelDTO update) {
        Pausenregel regel = pausenregelRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pausenregel nicht gefunden"));

        regel.setMindestpauseNachStunden(update.getMindestpauseNachStunden());
        regel.setMindestdauerMinuten(update.getMindestdauerMinuten());
        regel.setAktiv(update.isAktiv());

        Pausenregel gespeichert = pausenregelRepository.save(regel);
        return zuPausenregelDTO(gespeichert);
    }

    public void pausenregelLoeschen(Long id) {
        pausenregelRepository.deleteById(id);
    }
*/
    private PausenregelDTO zuPausenregelDTO(Pausenregel regel) {
        PausenregelDTO dto = new PausenregelDTO();
        dto.setId(regel.getId());
        dto.setMindestpauseNachStunden(regel.getMindestpauseNachStunden());
        dto.setMindestdauerMinuten(regel.getMindestdauerMinuten());
        dto.setJahrgangsstufeId(regel.getJahrgangsstufe().getId());
        dto.setAktiv(regel.isAktiv());
        return dto;
    }

    /** Maximalbelastung */
    public List<MaximalbelastungDTO> maximalbelastungen() {
         List<Maximalbelastung> alleRegeln = new ArrayList<>();
        maximalbelastungRepository.findAll().forEach(alleRegeln::add);

        List<MaximalbelastungDTO> result = new ArrayList<>();
        for (Maximalbelastung regel : alleRegeln) {
            result.add(zuMaximalbelastungDTO(regel));
        }
        return result;
    }

    public MaximalbelastungDTO maximalbelastungAnlegen(MaximalbelastungDTO neu) {
        Jahrgangsstufe stufe = jahrgangsstufeRepository.findById(neu.getJahrgangsstufeId())
            .orElseThrow(() -> new RuntimeException("Jahrgangsstufe nicht gefunden"));

        Maximalbelastung regel = new Maximalbelastung();
        regel.setJahrgangsstufe(stufe);
        regel.setMaxStundenTag(neu.getMaxStundenTag());
        regel.setMaxStundenWoche(neu.getMaxStundenWoche());
        regel.setAktiv(neu.isAktiv());

        //Dummy-Default-Werte, werden nicht benutzt
        regel.setHaerte(Haerte.WEICH);
        regel.setGewicht(0);

        Maximalbelastung gespeichert = maximalbelastungRepository.save(regel);
        return zuMaximalbelastungDTO(gespeichert);
    }

    public MaximalbelastungDTO maximalbelastungBearbeiten(Long id, MaximalbelastungDTO update) {
        Maximalbelastung regel = maximalbelastungRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Maximalbelastung nicht gefunden"));

        regel.setMaxStundenTag(update.getMaxStundenTag());
        regel.setMaxStundenWoche(update.getMaxStundenWoche());
        regel.setAktiv(update.isAktiv());

        Maximalbelastung gespeichert = maximalbelastungRepository.save(regel);
        return zuMaximalbelastungDTO(gespeichert);
    }

    public void maximalbelastungLoeschen(Long id) {
        maximalbelastungRepository.deleteById(id);
    }

    private MaximalbelastungDTO zuMaximalbelastungDTO(Maximalbelastung regel) {
        MaximalbelastungDTO dto = new MaximalbelastungDTO();
        dto.setId(regel.getId());
        dto.setMaxStundenTag(regel.getMaxStundenTag());
        dto.setMaxStundenWoche(regel.getMaxStundenWoche());
        dto.setJahrgangsstufeId(regel.getJahrgangsstufe().getId());
        dto.setAktiv(regel.isAktiv());
        return dto;
    }

    public List<PflichtfachvorgabeDTO> pflichtfaecher() {
        //Alle Pflichtfachvorgaben aus der Datenbank laden
        List<Pflichtfachvorgabe> alleVorgaben = new ArrayList<>();
        pflichtfachvorgabeRepository.findAll().forEach(alleVorgaben::add);

        //Zwei zusammengehoerige Listen: fachIds merkt sich, welche Fächer schon
        //gesehen wurden; an derselben Position in gruppen liegt die zugehörige
        //Liste ihrer Stufenvorgaben.
        List<Long> fachIds = new ArrayList<>();
        List<List<Pflichtfachvorgabe>> gruppen = new ArrayList<>();

        for (Pflichtfachvorgabe v : alleVorgaben) {
            Long fachId = v.getFach().getId();

            int index = fachIds.indexOf(fachId);
            if (index == -1) {
                //Fach ist neu: eigene Gruppe dafür anlegen
                fachIds.add(fachId);
                List<Pflichtfachvorgabe> neueGruppe = new ArrayList<>();
                neueGruppe.add(v);
                gruppen.add(neueGruppe);
            } else {
                //Fach ist schon bekannt: Vorgabe an bestehende Gruppe anhängen
                gruppen.get(index).add(v);
            }
        }

        //Aus jeder Fach-Gruppe ein fertiges PflichtfachvorgabeDTO bauen
        List<PflichtfachvorgabeDTO> result = new ArrayList<>();
        for (List<Pflichtfachvorgabe> gruppe : gruppen) {
            result.add(zuFachDTO(gruppe));
        }

        return result;
    }

    public StufenvorgabeDTO pflichtfachStufenvorgabeAnlegen(Long fachId, StufenvorgabeDTO neu) {
        Fach fach = fachRepository.findById(fachId)
            .orElseThrow(() -> new RuntimeException("Fach nicht gefunden"));

        if (fach.isHauptfach()) {
            throw new RuntimeException("Für Hauptfächer können keine neuen Stufenvorgaben angelegt werden");
        }

        Jahrgangsstufe stufe = jahrgangsstufeRepository.findById(neu.getJahrgangsstufeId())
            .orElseThrow(() -> new RuntimeException("Jahrgangsstufe nicht gefunden"));

        Pflichtfachvorgabe vorgabe = new Pflichtfachvorgabe();
        vorgabe.setFach(fach);
        vorgabe.setJahrgangsstufe(stufe);
        vorgabe.setStundenanzahl(neu.getStundenanzahl());
        vorgabe.setAktiv(neu.isAktiv());

        //Dummy-Default-Werte, werden nicht benutzt
        vorgabe.setHaerte(Haerte.HART);
        vorgabe.setGewicht(0);

        Pflichtfachvorgabe gespeichert = pflichtfachvorgabeRepository.save(vorgabe);

        //Blockregel passend zum gewünschten Zustand anlegen oder entfernen
        blockregelVerwalten(fach, stufe, neu.isDoppelstundenBevorzugt());

        return zuStufenvorgabeDTO(gespeichert);
    }

    public StufenvorgabeDTO pflichtfachStufenvorgabeBearbeiten(Long fachId, Long stufeId, StufenvorgabeDTO update) {
        Pflichtfachvorgabe vorgabe = pflichtfachvorgabeRepository.findById(stufeId)
            .orElseThrow(() -> new RuntimeException("Stufenvorgabe nicht gefunden"));

        if (!vorgabe.getFach().getId().equals(fachId)) {
            throw new RuntimeException("Stufenvorgabe gehoert nicht zu diesem Fach");
        }

        if (!vorgabe.getFach().isHauptfach()) {
            //Nicht-Hauptfach: alles übernehmen
            vorgabe.setStundenanzahl(update.getStundenanzahl());
            vorgabe.setAktiv(update.isAktiv());
            pflichtfachvorgabeRepository.save(vorgabe);
        }
        //Bei Hauptfach: stundenanzahl/aktiv bleiben unangetastet, egal was im DTO steht

        //In beiden Fällen: nur doppelstundenBevorzugt wird ausgewertet
        blockregelVerwalten(vorgabe.getFach(), vorgabe.getJahrgangsstufe(), update.isDoppelstundenBevorzugt());

        return zuStufenvorgabeDTO(vorgabe);
    }

    public void pflichtfachStufenvorgabeLoeschen(Long fachId, Long stufeId) {
        Pflichtfachvorgabe vorgabe = pflichtfachvorgabeRepository.findById(stufeId)
            .orElseThrow(() -> new RuntimeException("Stufenvorgabe nicht gefunden"));

        if (!vorgabe.getFach().getId().equals(fachId)) {
            throw new RuntimeException("Stufenvorgabe gehoert nicht zu diesem Fach");
        }
        
        if (vorgabe.getFach().isHauptfach()) {
            throw new RuntimeException("Stufenvorgaben von Hauptfaechern koennen nicht geloescht werden");
        }

        //Zugehörige Blockregel entfernen, falls vorhanden
        blockregelVerwalten(vorgabe.getFach(), vorgabe.getJahrgangsstufe(), false);

        pflichtfachvorgabeRepository.deleteById(stufeId);
    }

    private StufenvorgabeDTO zuStufenvorgabeDTO(Pflichtfachvorgabe vorgabe) {
        StufenvorgabeDTO dto = new StufenvorgabeDTO();
        dto.setId(vorgabe.getId());
        dto.setJahrgangsstufeId(vorgabe.getJahrgangsstufe().getId());
        dto.setJahrgangsnummer(vorgabe.getJahrgangsstufe().getNummer());
        dto.setStundenanzahl(vorgabe.getStundenanzahl());
        dto.setAktiv(vorgabe.isAktiv());
        //Prüfen, ob für diese Fach/Stufe-Kombination eine Blockregel existiert
        Optional<Blockregel> blockregel = blockregelRepository
            .findByFachAndJahrgangsstufe(vorgabe.getFach(), vorgabe.getJahrgangsstufe());
        dto.setDoppelstundenBevorzugt(blockregel.isPresent());

        return dto;
    }

    //Wird in pflichfaecher verwendet
    private PflichtfachvorgabeDTO zuFachDTO(List<Pflichtfachvorgabe> vorgabenEinesFachs) {
        //Alle Eintraege in dieser Gruppe gehören zum selben Fach
        Fach fach = vorgabenEinesFachs.get(0).getFach();

        PflichtfachvorgabeDTO dto = new PflichtfachvorgabeDTO();
        dto.setFachId(fach.getId());
        dto.setFachkuerzel(fach.getKuerzel());
        dto.setFachname(fach.getName());
        dto.setHauptfach(fach.isHauptfach());

        //Jede einzelne Vorgabe der Gruppe in ein StufenvorgabeDTO umwandeln
        List<StufenvorgabeDTO> stufen = new ArrayList<>();
        for (Pflichtfachvorgabe v : vorgabenEinesFachs) {
            stufen.add(zuStufenvorgabeDTO(v));
        }
        dto.setStufen(stufen);

        return dto;
    }

    private void blockregelVerwalten(Fach fach, Jahrgangsstufe stufe, boolean gewuenscht) {
        Optional<Blockregel> bestehende = blockregelRepository.findByFachAndJahrgangsstufe(fach, stufe);

        if (gewuenscht && bestehende.isEmpty()) {
            //Soll existieren, gibt es aber noch nicht -> anlegen
            Blockregel neu = new Blockregel();
            neu.setFach(fach);
            neu.setJahrgangsstufe(stufe);
            neu.setAktiv(true);
            
            //Dummy-Default-Werte, werden nicht benutzt
            neu.setHaerte(Haerte.WEICH);
            neu.setGewicht(0);
            blockregelRepository.save(neu);

        } else if (!gewuenscht && bestehende.isPresent()) {
            //Soll NICHT existieren, gibt es aber -> loeschen
            blockregelRepository.deleteById(bestehende.get().getId());
        }
    }

    public List<VertretungsprioritaetDTO> vertretungsprioritaet() {
        List<Jahrgangsstufe> stufen = new ArrayList<>();
        jahrgangsstufeRepository.findAll().forEach(stufen::add);

        // Nach prio aufsteigend sortieren
        for (int i = 0; i < stufen.size() - 1; i++) {
            for (int j = 0; j < stufen.size() - 1 - i; j++) {
                if (stufen.get(j).getPrio() > stufen.get(j + 1).getPrio()) {
                    Jahrgangsstufe temp = stufen.get(j);
                    stufen.set(j, stufen.get(j + 1));
                    stufen.set(j + 1, temp);
                }
            }
        }

        List<VertretungsprioritaetDTO> result = new ArrayList<>();
        for (Jahrgangsstufe stufe : stufen) {
            VertretungsprioritaetDTO dto = new VertretungsprioritaetDTO();
            dto.setJahrgangsstufeId(stufe.getId());
            dto.setNummer(stufe.getNummer());
            dto.setPrio(stufe.getPrio());
            result.add(dto);
        }
        return result;
    }

    //Die per Drag-and-drop festgelegte Reihenfolge übernehmen.
    //Aktuell: prio = 1 -> Höchste Priorität, oben/vorne in der Liste
    public List<VertretungsprioritaetDTO> vertretungsprioritaetSpeichern(List<VertretungsprioritaetDTO> reihenfolge) {
        List<VertretungsprioritaetDTO> result = new ArrayList<>();

        int neuePrio = 1;
        for (VertretungsprioritaetDTO eintrag : reihenfolge) {
            Jahrgangsstufe stufe = jahrgangsstufeRepository.findById(eintrag.getJahrgangsstufeId())
                .orElseThrow(() -> new RuntimeException("Jahrgangsstufe nicht gefunden"));

            stufe.setPrio(neuePrio);
            Jahrgangsstufe gespeichert = jahrgangsstufeRepository.save(stufe);

            VertretungsprioritaetDTO dto = new VertretungsprioritaetDTO();
            dto.setJahrgangsstufeId(gespeichert.getId());
            dto.setNummer(gespeichert.getNummer());
            dto.setPrio(gespeichert.getPrio());
            result.add(dto);

            neuePrio++;
        }

        return result;
    }
}