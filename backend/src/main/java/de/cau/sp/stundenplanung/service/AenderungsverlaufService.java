package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.SeiteDTO;
import de.cau.sp.stundenplanung.dto.aenderungsverlauf.AbgeschlosseneAenderungDTO;
import de.cau.sp.stundenplanung.dto.aenderungsverlauf.OffeneAenderungDTO;
import de.cau.sp.stundenplanung.dto.dashboard.AktuelleAenderungDTO;
import de.cau.sp.stundenplanung.dto.ereignis.EreignisDTO;
import de.cau.sp.stundenplanung.model.Aenderung;
import de.cau.sp.stundenplanung.model.Ausfall;
import de.cau.sp.stundenplanung.model.Ereignis;
import de.cau.sp.stundenplanung.model.EreignisTyp;
import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.model.Lerngruppe;
import de.cau.sp.stundenplanung.model.Raum;
import de.cau.sp.stundenplanung.model.Raumaenderung;
import de.cau.sp.stundenplanung.model.Unterrichtsstunde;
import de.cau.sp.stundenplanung.model.Zusatzstunde;
import de.cau.sp.stundenplanung.model.Jahrgangsstufe;
import de.cau.sp.stundenplanung.repository.AenderungRepository;
import de.cau.sp.stundenplanung.repository.EreignisRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AenderungsverlaufService {

    private final AenderungRepository aenderungRepository;
    private final EreignisRepository ereignisRepository;
    private final Generieren generieren;

    @Transactional(readOnly = true)
    public List<OffeneAenderungDTO> offene() {
        List<Aenderung> alle = new ArrayList<>();
        aenderungRepository.findAll().forEach(alle::add);
        return alle.stream()
                .filter(Aenderung::isOffen)
                .sorted(Comparator.comparing(Aenderung::getZeitstempel).reversed())
                .map(this::zuOffenerAenderung)
                .toList();
    }

    @Transactional(readOnly = true)
    public long anzahlOffene() {
        long anzahl = 0;
        for (Aenderung a : aenderungRepository.findAll()) {
            if (a.isOffen()) {
                anzahl++;
            }
        }
        return anzahl;
    }

    @Transactional(readOnly = true)
    public List<AktuelleAenderungDTO> neuesteAenderungen(int anzahl) {
        List<Aenderung> alle = new ArrayList<>();
        aenderungRepository.findAll().forEach(alle::add);

        return alle.stream()
                .sorted(Comparator.comparing(Aenderung::getZeitstempel).reversed())
                .limit(Math.max(0, anzahl))
                .map(this::zuAktuellerAenderung)
                .toList();
    }

    private AktuelleAenderungDTO zuAktuellerAenderung(Aenderung a) {
        EreignisTyp typ = a.getAusloeser() != null ? a.getAusloeser().getTyp() : null;
        if (a.isOffen()) {
            OffeneAenderungDTO offen = zuOffenerAenderung(a);
            return new AktuelleAenderungDTO(a.getId(), typ, offen.getTitel(), offen.getBeschreibung(), true,
                    offen.getKlassenstufeId(), offen.getKlasseId());
        }
        AbgeschlosseneAenderungDTO abgeschlossen = zuAbgeschlossenerAenderung(a);
        String titel = "%s: %s".formatted(abgeschlossen.getAnzeigeTyp(), abgeschlossen.getBetroffeneEinheit());
        return new AktuelleAenderungDTO(a.getId(), typ, titel, abgeschlossen.getAenderung(), false);
    }

    @Transactional(readOnly = true)
    public OffeneAenderungDTO offeneEinzeln(Long id) {
        Aenderung aenderung = findOffen(id);
        return zuOffenerAenderung(aenderung);
    }

    @Transactional
    public AbgeschlosseneAenderungDTO alsErledigtMarkieren(Long id) {
        Aenderung aenderung = findOffen(id);
        aenderung.setOffen(false);
        aenderung.setZeitstempel(LocalDateTime.now());
        aenderungRepository.save(aenderung);
        return zuAbgeschlossenerAenderung(aenderung);
    }

    @Transactional(readOnly = true)
    public SeiteDTO<AbgeschlosseneAenderungDTO> abgeschlossene(String suche, EreignisTyp typ, LocalDate von,
                                                                 LocalDate bis, int seite, int groesse) {
        List<Aenderung> alle = new ArrayList<>();
        aenderungRepository.findAll().forEach(alle::add);

        List<AbgeschlosseneAenderungDTO> gefiltert = alle.stream()
                .filter(a -> !a.isOffen())
                .filter(a -> typ == null || (a.getAusloeser() != null && a.getAusloeser().getTyp() == typ))
                .filter(a -> von == null || !a.getDatum().isBefore(von))
                .filter(a -> bis == null || !a.getDatum().isAfter(bis))
                .sorted(Comparator.comparing(Aenderung::getZeitstempel).reversed())
                .map(this::zuAbgeschlossenerAenderung)
                .filter(dto -> passtZurSuche(dto, suche))
                .toList();
        return seite(gefiltert, seite, groesse);
    }

    @Transactional(readOnly = true)
    public AbgeschlosseneAenderungDTO abgeschlosseneEinzeln(Long id) {
        Aenderung aenderung = aenderungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Änderung nicht gefunden"));
        if (aenderung.isOffen()) {
            throw new RuntimeException("Änderung ist noch nicht abgeschlossen");
        }
        return zuAbgeschlossenerAenderung(aenderung);
    }

    @Transactional(readOnly = true)
    public SeiteDTO<EreignisDTO> ereignisverlauf(EreignisTyp typ, LocalDate von, LocalDate bis,
                                                  int seite, int groesse) {
        List<Ereignis> alle = new ArrayList<>();
        ereignisRepository.findAll().forEach(alle::add);

        List<EreignisDTO> gefiltert = alle.stream()
                .filter(e -> typ == null || e.getTyp() == typ)
                .filter(e -> von == null || !e.getStartdatum().isBefore(von))
                .filter(e -> bis == null || !e.getStartdatum().isAfter(bis))
                .sorted(Comparator.comparing(Ereignis::getErstellungsdatum).reversed())
                .map(this::zuEreignisDTO)
                .toList();
        return seite(gefiltert, seite, groesse);
    }

    private Aenderung findOffen(Long id) {
        Aenderung aenderung = aenderungRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Änderung nicht gefunden"));
        if (!aenderung.isOffen()) {
            throw new RuntimeException("Änderung ist bereits abgeschlossen");
        }
        return aenderung;
    }

    private OffeneAenderungDTO zuOffenerAenderung(Aenderung a) {
        Ereignis ausloeser = a.getAusloeser();
        EreignisTyp typ = ausloeser != null ? ausloeser.getTyp() : null;
        Unterrichtsstunde us = a.getUnterrichtsstunde();
        Lerngruppe lerngruppe = a.getLerngruppeNeu();
        Jahrgangsstufe jahrgangsstufe = lerngruppe != null ? lerngruppe.getJahrgangsstufe() : null;

        return new OffeneAenderungDTO(
                a.getId(), typ,
                generieren.aenderungPlanerTitelGenerieren(a),
                generieren.aenderungPlanerBeschreibungGenerieren(a),
                ausloeser != null ? ausloeser.getId() : null,
                us != null ? us.getId() : null,
                jahrgangsstufe != null ? jahrgangsstufe.getId() : null,
                lerngruppe != null ? lerngruppe.getId() : null,
                a.getZeitstempel());
    }

    private AbgeschlosseneAenderungDTO zuAbgeschlossenerAenderung(Aenderung a) {
        Ereignis ausloeser = a.getAusloeser();
        EreignisTyp typ = ausloeser != null ? ausloeser.getTyp() : null;
        Unterrichtsstunde us = a.getUnterrichtsstunde();

        Lerngruppe lerngruppe = a.getLerngruppeNeu() != null ? a.getLerngruppeNeu()
                : (us != null ? us.getLerngruppe() : null);
        Fach fach = a.getFachNeu() != null ? a.getFachNeu() : (us != null ? us.getFach() : null);

        String anzeigeTyp;
        String grund = null;

        if (typ == EreignisTyp.AUSFALL) {
            anzeigeTyp = a.isFreistunde() ? "Ausfall" : "Vertretung";
            if (ausloeser instanceof Ausfall ausfall) {
                grund = ausfall.getGrund();
            }
        } else if (typ == EreignisTyp.RAUMAENDERUNG) {
            anzeigeTyp = "Raumänderung";
        } else if (typ == EreignisTyp.ZUSATZSTUNDE) {
            anzeigeTyp = "Zusatzstunde";
        } else {
            anzeigeTyp = "";
        }

        return new AbgeschlosseneAenderungDTO(
            a.getId(), typ, anzeigeTyp,
            "Klasse %s (%s)".formatted(bezeichnung(lerngruppe), bezeichnung(fach)),
            generieren.aenderungPlanerBeschreibungGenerieren(a), grund,
            ausloeser != null ? vollerName(ausloeser.getAutor()) : "",
            a.getZeitstempel(), a.isFreistunde());
    }

    private EreignisDTO zuEreignisDTO(Ereignis e) {
        Long lehrkraftId = null;
        Long raumId = null;
        Long lerngruppeId = null;
        Long fachId = null;
        String grund = null;

        if (e instanceof Ausfall ausfall) {
            lehrkraftId = ausfall.getLehrkraft() != null ? ausfall.getLehrkraft().getId() : null;
            grund = ausfall.getGrund();
        } else if (e instanceof Raumaenderung raumaenderung) {
            raumId = raumaenderung.getRaum() != null ? raumaenderung.getRaum().getId() : null;
        } else if (e instanceof Zusatzstunde zusatzstunde) {
            lerngruppeId = zusatzstunde.getLerngruppe() != null ? zusatzstunde.getLerngruppe().getId() : null;
        }

        return new EreignisDTO(
                e.getId(), e.getTyp(), e.getStartdatum(), e.getEnddatum(),
                false,
                null, null,
                e.getErstellungsdatum(),
                e.getAutor() != null ? e.getAutor().getId() : null,
                lehrkraftId, raumId, lerngruppeId, fachId, grund);
    }

    private <T> SeiteDTO<T> seite(List<T> alle, int seite, int groesse) {
        int von = Math.min(seite * groesse, alle.size());
        int bis = Math.min(von + groesse, alle.size());
        int gesamtSeiten = groesse > 0 ? (int) Math.ceil((double) alle.size() / groesse) : 0;
        return new SeiteDTO<>(alle.subList(von, bis), seite, groesse, alle.size(), gesamtSeiten);
    }

    private boolean passtZurSuche(AbgeschlosseneAenderungDTO dto, String suche) {
        if (suche == null || suche.isBlank()) {
            return true;
        }
        String text = (dto.getBetroffeneEinheit() + " " + dto.getAenderung() + " " + dto.getEingetragenVon())
                .toLowerCase();
        return text.contains(suche.toLowerCase());
    }

    private String vollerName(de.cau.sp.stundenplanung.model.Account account) {
        if (account == null) {
            return "";
        }
        return account.getVorname() + " " + account.getName();
    }

    private String bezeichnung(Raum raum) {
        return raum != null ? raum.getBezeichnung() : "unbekannt";
    }

    private String bezeichnung(Lerngruppe lerngruppe) {
        return lerngruppe != null ? lerngruppe.getBezeichnung() : "unbekannt";
    }

    private String bezeichnung(Fach fach) {
        return fach != null ? fach.getName() : "unbekannt";
    }

}