package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.raeume.*;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class RaumService {
    private final RaumRepository raumRepository;
    private final RaumaenderungRepository raumaenderungRepository;
    private final UnterrichtsstundeRepository unterrichtsstundeRepository;
    private final ZeitslotRepository zeitslotRepository;
    private final AenderungRepository aenderungRepository;
    private final LerngruppeRepository lerngruppeRepository;


    public List<RaumDTO> alleRaeume() {
        return StreamSupport.stream(raumRepository.findAll().spliterator(), false)
                .map(raum -> new RaumDTO(raum.getId(),
                        raum.getBezeichnung(),
                        raum.getArt(),
                        raum.getKapazitaet()
                ))
                .collect(Collectors.toList());
    }

    public List<RaumplanEintragDTO> raumplanAnzeigen(Long raumId) {
        Raum raum = raumRepository.findById(raumId)
                .orElseThrow(() -> new EntityNotFoundException("Raum nicht gefunden: " + raumId));

        List<Unterrichtsstunde> stunden = unterrichtsstundeRepository.findByRaumAndStundenplanStatus(raum, PlanStatus.AKTIV); //hier studenplanaktivitätsstatus filtern
        List<Aenderung> aenderungen = aenderungRepository.findAbgeschlosseneByRaumAndCurrentWeek(raum);  //annahme, dass deaktivierung von klassen noch auswirkungen auf restliche woche haben darf

        Map<String, RaumplanEintragDTO> eintragMap = new HashMap<>();

        for (Unterrichtsstunde stunde : stunden) {
            String key = stunde.getWochentag().name() + "_" + stunde.getZeitslot().getId();
            eintragMap.put(key, createDTOFromStunde(stunde, raum, false));
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

            eintragMap.put(key, createDTOFromAenderung(aenderung, raum, true));
        }

        return eintragMap.values().stream()
                .sorted(Comparator
                        .comparing(RaumplanEintragDTO::getWochentag)
                        .thenComparing(RaumplanEintragDTO::getStundennummer))
                .collect(Collectors.toList());
    }

    private RaumplanEintragDTO createDTOFromStunde(Unterrichtsstunde stunde, Raum raum, boolean geaendert) {
        return new RaumplanEintragDTO(
                stunde.getWochentag(),
                stunde.getZeitslot().getStundennummer(),
                stunde.getZeitslot().getStart(),
                stunde.getZeitslot().getEnde(),
                stunde.getFach().getKuerzel(),
                stunde.getLehrkraft().getKuerzel(),
                stunde.getLerngruppe().getBezeichnung(),
                geaendert,
                false
        );
    }

    private RaumplanEintragDTO createDTOFromAenderung(Aenderung aenderung, Raum raum, boolean geaendert) {
        return new RaumplanEintragDTO(
                aenderung.getWochentagNeu(),
                aenderung.getZeitslotNeu().getStundennummer(),
                aenderung.getZeitslotNeu().getStart(),
                aenderung.getZeitslotNeu().getEnde(),
                aenderung.getFachNeu().getKuerzel(),
                aenderung.getLehrkraftNeu().getKuerzel(),
                aenderung.getLerngruppeNeu().getBezeichnung(),
                geaendert,
                aenderung.isFreistunde()
        );
    }

    public RaumDTO raumHinzufuegen(RaumDTO raum) {
        Raum raumNeu = new Raum();
        raumNeu.setBezeichnung(raum.getBezeichnung());
        raumNeu.setArt(raum.getArt());
        raumNeu.setKapazitaet(raum.getKapazitaet());
        Raum gespeichert = raumRepository.save(raumNeu);

        return new RaumDTO(gespeichert.getId(), gespeichert.getBezeichnung(), gespeichert.getArt(), gespeichert.getKapazitaet());
    }

    public RaumDTO raumBearbeiten(Long raumId, RaumDTO raumNeu) {
        Raum raum = raumRepository.findById(raumId)
                .orElseThrow(() -> new RuntimeException("Raum nicht gefunden"));
        raum.setBezeichnung(raumNeu.getBezeichnung());
        raum.setKapazitaet(raumNeu.getKapazitaet());
        Raum gespeichert = raumRepository.save(raum);

        return new RaumDTO(gespeichert.getId(),
                gespeichert.getBezeichnung(),
                gespeichert.getArt(),
                gespeichert.getKapazitaet());
    }

    public Raum zufaelligerRaumAlsKlassenzimmer() {
        List<Raum> alleRaeume = new ArrayList<>();
        raumRepository.findByArt(Raumart.KLASSENRAUM).forEach(alleRaeume::add);

        List<Raum> verfuegbar = new ArrayList<>();
        for (Raum r : alleRaeume) {
            Lerngruppe belegt = lerngruppeRepository.findByKlassenzimmer(r);
            if (belegt == null) {
                verfuegbar.add(r);
            }
        }

        if (verfuegbar.isEmpty()) {
            throw new RuntimeException("Kein Raum als Klassenzimmer verfügbar.");
        }

        int zufallsIndex = new Random().nextInt(verfuegbar.size());
        return verfuegbar.get(zufallsIndex);
    }

    public Optional<Raum> verfuegbarerRaum(Wochentag wochentag, Zeitslot zeitslot, Fach fach) {
        List<Raum> fachraeume = unterrichtsstundeRepository
                .findVerfuegbareFachraeume(wochentag, zeitslot, fach.getBenoetigterRaumtyp(), PlanStatus.AKTIV);
        if (!fachraeume.isEmpty()) {
            return Optional.of(fachraeume.get(new Random().nextInt(fachraeume.size())));
        }

        List<Raum> alleVerfuegbaren = unterrichtsstundeRepository
                .findVerfuegbareRaeume(wochentag, zeitslot, PlanStatus.AKTIV);
        if (alleVerfuegbaren.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(alleVerfuegbaren.get(new Random().nextInt(alleVerfuegbaren.size())));
    }

    public List<Raum> verfuegbareRaeume(Wochentag wochentag, Zeitslot zeitslot, Fach fach) {
        List<Raum> fachraeume = unterrichtsstundeRepository
                .findVerfuegbareFachraeume(wochentag, zeitslot, fach.getBenoetigterRaumtyp(), PlanStatus.AKTIV);
        if (!fachraeume.isEmpty()) {
            return fachraeume;
        }
        List<Raum> alleVerfuegbaren = new ArrayList<>();
        alleVerfuegbaren = unterrichtsstundeRepository
                .findVerfuegbareRaeume(wochentag, zeitslot, PlanStatus.AKTIV);

        return alleVerfuegbaren;
    }

}
