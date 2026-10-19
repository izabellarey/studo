package de.cau.sp.stundenplanung.solver;

import de.cau.sp.stundenplanung.model.Blockregel;
import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Lerngruppe;
import de.cau.sp.stundenplanung.model.Maximalbelastung;
import de.cau.sp.stundenplanung.model.Raum;
import de.cau.sp.stundenplanung.model.Pflichtfachvorgabe;
import de.cau.sp.stundenplanung.model.Wochentag;
import de.cau.sp.stundenplanung.model.Zeitslot;
import de.cau.sp.stundenplanung.repository.LerngruppeRepository;
import de.cau.sp.stundenplanung.repository.PflichtfachvorgabeRepository;
import de.cau.sp.stundenplanung.repository.ZeitslotRepository;
import de.cau.sp.stundenplanung.solver.model.PlanungsEinheit;
import de.cau.sp.stundenplanung.solver.model.Zeitfenster;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Component
@RequiredArgsConstructor
public class EinheitenGenerator {

    private final PflichtfachvorgabeRepository pflichtfachvorgabeRepository;
    private final LerngruppeRepository lerngruppeRepository;
    private final ZeitslotRepository zeitslotRepository;

    public int stundenProTag() {
        List<Zeitslot> slots = new ArrayList<>();
        zeitslotRepository.findAll().forEach(slots::add);
        slots.removeIf(Zeitslot::isPause);
        return slots.size();
    }

    public List<Zeitfenster> zeitfensterErzeugen() {
        List<Zeitslot> slots = new ArrayList<>();
        zeitslotRepository.findAll().forEach(slots::add);
        slots.removeIf(Zeitslot::isPause);
        slots.sort(Comparator.comparingInt(Zeitslot::getStundennummer));
        int stundenProTag = slots.size();

        List<Zeitfenster> fenster = new ArrayList<>();
        for (Wochentag tag : Wochentag.values()) {
            for (Zeitslot slot : slots) {
                if (freitagnachmittag(tag, slot, stundenProTag)) {
                    continue;
                }
                fenster.add(new Zeitfenster(tag, slot));
            }
        }
        return fenster;
    }

    private boolean freitagnachmittag(Wochentag tag, Zeitslot slot, int stundenProTag) {
        return tag == Wochentag.FREITAG && slot.getStundennummer() > stundenProTag - 2;
    }

    public List<Zeitfenster> blockfensterErzeugen(List<Zeitfenster> alle) {
        List<Integer> nummern = alle.stream()
                .map(z -> z.getZeitslot().getStundennummer()).distinct().sorted().toList();
        return alle.stream()
                .filter(z -> z.getZeitslot().getStundennummer() % 2 == 1)
                .filter(z -> nummern.contains(z.getZeitslot().getStundennummer() + 1))
                .toList();
    }

    public List<PlanungsEinheit> einheitenErzeugen(List<Lehrkraft> lehrkraefte, List<Raum> raeume,
                                                   List<Blockregel> blockregeln,
                                                   List<Maximalbelastung> belastungen,
                                                   List<Zeitfenster> alleFenster,
                                                   List<Zeitfenster> blockFenster,
                                                   int stundenProTag) {
        List<Kontingent> kontingente = kontingenteErmitteln();

        List<PlanungsEinheit> einheiten = new ArrayList<>();
        long naechsteId = 1L;
        for (Kontingent k : kontingente) {
            List<Lehrkraft> passendeLehrkraefte = passendeLehrkraefte(lehrkraefte, k.fach());
            List<Raum> passendeRaeume = passendeRaeume(raeume, k.fach(), k.lerngruppe());
            Integer grenze = tagesgrenze(belastungen, k.lerngruppe());

            int offen = k.wochenstunden();
            if (blockwunsch(blockregeln, k.fach(), k.lerngruppe())) {
                for (int paar = 0; paar < k.wochenstunden() / 2; paar++) {
                    einheiten.add(new PlanungsEinheit(naechsteId++, k.lerngruppe(), k.fach(),
                            2, grenze, stundenProTag, passendeLehrkraefte, passendeRaeume, blockFenster));
                    offen -= 2;
                }
            }
            for (int rest = 0; rest < offen; rest++) {
                einheiten.add(new PlanungsEinheit(naechsteId++, k.lerngruppe(), k.fach(),
                        1, grenze, stundenProTag, passendeLehrkraefte, passendeRaeume, alleFenster));
            }
        }
        return einheiten;
    }

    private record Kontingent(Lerngruppe lerngruppe, Fach fach, int wochenstunden) {
    }

    private List<Kontingent> kontingenteErmitteln() {
        Map<String, Pflichtfachvorgabe> vorgaben = new HashMap<>();
        for (Pflichtfachvorgabe v : pflichtfachvorgabeRepository.findAll()) {
            if (!v.isAktiv() || v.getJahrgangsstufe() == null || v.getFach() == null
                    || v.getStundenanzahl() <= 0) {
                continue;
            }
            String schluessel = v.getJahrgangsstufe().getId() + "-" + v.getFach().getId();
            Pflichtfachvorgabe bisher = vorgaben.get(schluessel);
            if (bisher == null || v.getId() > bisher.getId()) {
                vorgaben.put(schluessel, v);
            }
        }

        List<Kontingent> kontingente = new ArrayList<>();
        for (Lerngruppe gruppe : lerngruppeRepository.findAllByAktivTrue()) {
            if (gruppe.getJahrgangsstufe() == null) {
                continue;
            }
            for (Pflichtfachvorgabe v : vorgaben.values()) {
                if (v.getJahrgangsstufe().getId().equals(gruppe.getJahrgangsstufe().getId())) {
                    kontingente.add(new Kontingent(gruppe, v.getFach(), v.getStundenanzahl()));
                }
            }
        }
        kontingente.sort(Comparator
                .comparing((Kontingent k) -> k.lerngruppe().getId())
                .thenComparing(k -> k.fach().getId()));
        return kontingente;
    }

    private List<Lehrkraft> passendeLehrkraefte(List<Lehrkraft> alle, Fach fach) {
        List<Lehrkraft> passend = alle.stream().filter(l -> l.getFaecher().contains(fach)).toList();
        if (passend.isEmpty()) {
            throw new IllegalStateException(
                    "Keine Lehrkraft unterrichtet " + fach.getKuerzel() + ", der Plan ist so nicht lösbar.");
        }
        return passend;
    }

    private List<Raum> passendeRaeume(List<Raum> alle, Fach fach, Lerngruppe gruppe) {
        List<Raum> passend;
        if (fach.getBenoetigterRaumtyp() != null) {
            passend = alle.stream()
                    .filter(r -> r.getArt() == fach.getBenoetigterRaumtyp())
                    .filter(r -> r.getKapazitaet() >= gruppe.getSchueleranzahl())
                    .toList();
        } else if (gruppe.getKlassenzimmer() != null) {
            passend = List.of(gruppe.getKlassenzimmer());
        } else {
            passend = alle.stream()
                    .filter(r -> r.getKapazitaet() >= gruppe.getSchueleranzahl())
                    .toList();
        }
        if (passend.isEmpty()) {
            throw new IllegalStateException("Kein passender Raum für " + fach.getKuerzel()
                    + " in " + gruppe.getBezeichnung() + ", der Plan ist so nicht lösbar.");
        }
        return passend;
    }

    private boolean blockwunsch(List<Blockregel> blockregeln, Fach fach, Lerngruppe gruppe) {
        if (gruppe.getJahrgangsstufe() == null) {
            return false;
        }
        return blockregeln.stream().anyMatch(r -> r.getFach() != null
                && r.getFach().getId().equals(fach.getId())
                && r.getJahrgangsstufe() != null
                && r.getJahrgangsstufe().getId().equals(gruppe.getJahrgangsstufe().getId()));
    }

    private Integer tagesgrenze(List<Maximalbelastung> belastungen, Lerngruppe gruppe) {
        if (gruppe.getJahrgangsstufe() == null) {
            return null;
        }
        return belastungen.stream()
                .filter(m -> m.getJahrgangsstufe() != null
                        && m.getJahrgangsstufe().getId().equals(gruppe.getJahrgangsstufe().getId()))
                .map(Maximalbelastung::getMaxStundenTag)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }
}