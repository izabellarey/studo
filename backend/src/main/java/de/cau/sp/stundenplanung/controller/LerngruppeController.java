package de.cau.sp.stundenplanung.controller;
import de.cau.sp.stundenplanung.service.LerngruppeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import de.cau.sp.stundenplanung.dto.lerngruppen.*;
import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lerngruppen")
public class LerngruppeController {
    private final LerngruppeService lerngruppeService;

    @GetMapping
    public List<JahrgangsstufeDTO> alleJahrgangsstufen(){
        return lerngruppeService.alleJahrgangsstufen();
    } //zeigt alle Jahrgangsstufen und den Reiter AGs an

    /*@PostMapping("/klassenstufeAnlegen")
    public JahrgangsstufeDTO klassenstufeAnlegen(@RequestBody KlassenstufeDTO klassenstufe){
        return lerngruppeService.klassenstufeAnlegen(klassenstufe);
    } */
    //legt neue Jahrgangsstufe an

    @GetMapping("/{id}")
    public List<LerngruppeDTO> lerngruppeJGST(@PathVariable Long id){
        return lerngruppeService.lerngruppeJGST(id);
    } //listet alle Lerngruppen (Klassen und Kurse) je Jahrgangsstufe auf

    @PostMapping("/{id}/klasseAnlegen")
    public LerngruppeDTO klasseAnlegen(@PathVariable Long id, @RequestBody KlasseDTO klasse){
        return lerngruppeService.klasseAnlegen(id, klasse);
    } //legt Klasse bzw Kurs in entsprechender Jahrgangsstufe an

    @GetMapping("/{klassenstufeId}/{lerngruppeId}")
    public List<UnterrichtsstundeDTO> klassenstundenplanAnzeigen(@PathVariable Long klassenstufeId, @PathVariable Long lerngruppeId){
        return lerngruppeService.klassenstundenplanAnzeigen(lerngruppeId);
    }//zeigt Stundenplan für ausgewählte Klasse an

    @GetMapping("/{klassenstufeId}/{klasseId}/aenderungen/{aenderungId}/vorschlaege")
    public AenderungsVorschlagDTO aenderungsVorschlaege(
            @PathVariable Long klassenstufeId,
            @PathVariable Long klasseId,
            @PathVariable Long aenderungId) {
        return lerngruppeService.vorschlaegeErmitteln(aenderungId);
    } //zeigt alternative Lehrer und Raueme fuer unbestaetigte Aenderungen an

    @PutMapping("/{klassenstufeId}/{klasseId}/aenderungen/{aenderungId}/bestaetigen")
    public UnterrichtsstundeDTO aenderungBestaetigen( //unterrichtsstundeDTO zurück
            @PathVariable Long klassenstufeId,
            @PathVariable Long klasseId,
            @PathVariable Long aenderungId,
            @RequestBody AenderungAuswahlDTO auswahl) {
        return lerngruppeService.aenderungBestaetigen(aenderungId, auswahl);
    } //Bestaetigt ggf angepasste Aenderung im Stundenplan
  //loeschen?
    @PutMapping("/{lerngruppeId}/deaktivieren")
    public void deaktivieren(@PathVariable Long lerngruppeId) {
        lerngruppeService.klasseDeaktivieren(lerngruppeId);
    }
    @PutMapping("/{lerngruppeId}/bearbeiten")
    public LerngruppeDTO bearbeiten(@PathVariable Long lerngruppeId, @RequestBody LerngruppeBearbeitenDTO klasse) {
        return lerngruppeService.klasseBearbeiten(lerngruppeId, klasse);
    }

    /*@GetMapping("/ags")
    public List<AGNameDTO> alleAGs(){
        return lerngruppeService.alleAGs();
    } //listet alle aktuell angebotenen AGs auf

    @PostMapping("/ags/agAnlegen")
    public BestaetigungAGDTO agAnlegen(@RequestBody AGAnlegenDTO ag){
        return lerngruppeService.agAnlegen(ag);
    } //legt neue AG an

    @GetMapping("/ags/{lerngruppeId}")
    public AGDTO agAnzeigen(@PathVariable Long lerngruppeId){
        return lerngruppeService.agInfosAnzeigen(lerngruppeId);
    } //zeigt infos zur ausgewaehlten AG an

    @PostMapping("/ags/{lerngruppeId}/schuelerHinzufuegen")
    public SchuelerHinzufuegenBestaetigungDTO schuelerAgHinzufuegen(@PathVariable Long lerngruppeId, @RequestBody SchuelerAGHinzufuegenDTO schueler){
        return lerngruppeService.schuelerAgHinzufuegen(lerngruppeId, schueler);
    } //fuegt Schueler einer AG hinzu*/



}
