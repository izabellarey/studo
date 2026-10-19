package de.cau.sp.stundenplanung.service;


import de.cau.sp.stundenplanung.dto.accounts.PasswortZuruecksetzenDTO;
import de.cau.sp.stundenplanung.dto.dashboard.AktuelleAenderungDTO;
import de.cau.sp.stundenplanung.dto.faecher.FachUebersichtDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftInfoDTO;
import de.cau.sp.stundenplanung.dto.raeume.RaumDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftAnlegenDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.lehrkraefte.LehrkraftBearbeitenDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LerngruppeDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LerngruppeFuerLehrkraftDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LerngruppeUebersichtDTO;
import de.cau.sp.stundenplanung.dto.verfuegbarkeiten.VerfuegbarkeitEingabeDTO;
import de.cau.sp.stundenplanung.dto.verfuegbarkeiten.VerfuegbarkeitFuerLehrkraftDTO;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
public class LehrkraftService {
    private final LehrkraftRepository lehrkraftRepository;
    private final AccountRepository accountRepository;
    private final Generieren generieren;
    private final LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    private final LerngruppeRepository lerngruppeRepository;
    private final FachRepository fachRepository;
    private final PasswordEncoder passwordEncoder;

    private final AccountService accountService;
    private final UnterrichtsstundeRepository unterrichtsstundeRepository;
    private final AenderungRepository aenderungRepository;
    private final ZeitslotRepository zeitslotRepository;

    public List<UnterrichtsstundeDTO> persoenlicherStundenplan(String benutzername){
        Account nutzer = accountRepository.findByBenutzername(benutzername)
                .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));
        Lehrkraft lehrkraft = lehrkraftRepository.findByAccount(nutzer)
                .orElseThrow(() -> new RuntimeException("Lehrer nicht gefunden"));

        List<Unterrichtsstunde> stunden = unterrichtsstundeRepository.findByLehrkraftAndStundenplanStatus(lehrkraft, PlanStatus.AKTIV);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        List<Aenderung> aenderungen = aenderungRepository.findAbgeschlosseneByLehrkraftNeuAndDatumBetween(
                lehrkraft, weekStart, weekEnd
        );

        Map<String, UnterrichtsstundeDTO> eintragMap = new HashMap<>();

        for (Unterrichtsstunde stunde : stunden) {
            String key = stunde.getWochentag().name() + "_" + stunde.getZeitslot().getId();
            eintragMap.put(key, createDTOFromStunde(stunde, false));
        }

        for (Aenderung aenderung : aenderungen) {
            String key = aenderung.getWochentagNeu().name()
                    + "_"
                    + aenderung.getZeitslotNeu().getId();

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
                false,
                aenderung.getAusloeser().getTyp()
        );
    }

    public List<AktuelleAenderungDTO> aktuelleAenderungenAnzeigen(String benutzername){
        Account nutzer = accountRepository.findByBenutzername(benutzername)
                .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));
        Lehrkraft lehrkraft = lehrkraftRepository.findByAccount(nutzer)
                .orElseThrow(() -> new RuntimeException("Lehrer nicht gefunden"));

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        return StreamSupport.stream(aenderungRepository.findAbgeschlosseneByLehrkraftNeuAndDatumBetween( //hier auf aktivitätsstatus prüfen? es können sich ja nur aktive lehrer einloggen und auf die funktion zugreifen
                        lehrkraft, weekStart, weekEnd
                ).spliterator(), false)
                .map(aenderung -> new AktuelleAenderungDTO(aenderung.getId(),
                        aenderung.getAusloeser().getTyp(),
                        generieren.aenderungLehrkraftTitelGenerieren(aenderung),
                        generieren.aenderungLehrkraftBeschreibungGenerieren(aenderung),
                        false
                ))
                .collect(Collectors.toList());

    }

    /* 
    public List<Lehrkraft> alle() {
        List<Lehrkraft> liste = new ArrayList<>();
        lehrkraftRepository.findAll().forEach(liste::add);
        return liste;
    }
    */
    @Transactional
    public List<LehrkraftAnzeigenDTO> alle() {
        List<Lehrkraft> lehrkraefte = lehrkraftRepository.findByAccount_Aktiv(true);

        List<LehrkraftAnzeigenDTO> result = new ArrayList<>();
        for (Lehrkraft l : lehrkraefte) {
            result.add(zuAnzeigenDTO(l));
        }
        return result;
    }

    @Transactional
    public List<LehrkraftAnzeigenDTO> alleDeaktivierten() {
        List<Lehrkraft> lehrkraefte = lehrkraftRepository.findByAccount_Aktiv(false);

        List<LehrkraftAnzeigenDTO> result = new ArrayList<>();
        for (Lehrkraft l : lehrkraefte) {
            result.add(zuAnzeigenDTO(l));
        }
        return result;
    }

    @Transactional
    public LehrkraftAnzeigenDTO einzeln(Long id) {
        Lehrkraft l = lehrkraftRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Lehrkraft nicht gefunden"));
        return zuAnzeigenDTO(l);
    }
    

    @Transactional
    public LehrkraftAnzeigenDTO anlegen(LehrkraftAnlegenDTO dto) {
        // 1. Account erzeugen
        Account account = new Account();
        account.setName(dto.getName());
        account.setVorname(dto.getVorname());
        account.setBenutzername(generieren.benutzername(dto.getVorname(), dto.getName()));
        account.setRolle(Rolle.LEHRKRAFT);
        account.setPasswort(passwordEncoder.encode(dto.getPasswort())); 
        account.setAktiv(true);
        Account gespeicherterAccount = accountRepository.save(account);

        // 2. Lehrkraft erzeugen, mit Account verknüpfen
        Lehrkraft lehrkraft = new Lehrkraft();
        lehrkraft.setAccount(gespeicherterAccount);
        lehrkraft.setKuerzel(generieren.kuerzel(dto.getVorname(), dto.getName()));
        lehrkraft.setAnstellungsverhaeltnis(dto.getAnstellungsverhaeltnis());
        lehrkraft.setDeputatsstunden(dto.getDeputatsstunden());

        //Fächer hinzufügen
        List<Fach> faecher = new ArrayList<>();
        if (dto.getFachIds() != null) {
            fachRepository.findAllById(dto.getFachIds()).forEach(faecher::add);
        }
        lehrkraft.setFaecher(faecher);

        //Präferenz von Klassen hinzufügen
        List<Lerngruppe> praeferenzen = new ArrayList<>();
        if (dto.getPraeferenzIds() != null) {
            lerngruppeRepository.findAllById(dto.getPraeferenzIds()).forEach(praeferenzen::add);
        }
        lehrkraft.setPraeferenzen(praeferenzen);

        Lehrkraft gespeicherteLehrkraft = lehrkraftRepository.save(lehrkraft);

        //Lerngruppe verknüpfen
        if (dto.getLerngruppeId() != null){
            Lerngruppe gruppe = lerngruppeRepository.findById(dto.getLerngruppeId())
            .orElseThrow(() -> new RuntimeException("Lerngruppe nicht gefunden"));

            gruppe.setKlassenleitung(gespeicherteLehrkraft);
            lerngruppeRepository.save(gruppe);
        }

        //Verfügbarkeiten anlegen
        if (dto.getVerfuegbarkeiten() != null) {
            for (VerfuegbarkeitEingabeDTO  verfDTO : dto.getVerfuegbarkeiten()) {
                
            Zeitslot zeitslot = null;
            Wochentag wochentag = null;

            if (Boolean.TRUE.equals(verfDTO.getGanztaegig())) {
                // Ganztägig: kein Zeitslot, kein Wochentag - nur Start-/Enddatum zaehlen
                if (verfDTO.getStartdatum() == null || verfDTO.getEnddatum() == null) {
                    throw new RuntimeException("Bei ganztaegiger Verfuegbarkeit muessen Start- und Enddatum angegeben werden");
                }
            } else {
                // Einzelner Zeitslot: Zeitslot und Wochentag muessen angegeben sein
                if (verfDTO.getZeitslotId() == null) {
                    throw new RuntimeException("Zeitslot muss angegeben werden, wenn nicht ganztaegig");
                }
                zeitslot = zeitslotRepository.findById(verfDTO.getZeitslotId())
                    .orElseThrow(() -> new RuntimeException("Zeitslot nicht gefunden"));
                wochentag = verfDTO.getWochentag();
            }

            Lehrerverfuegbarkeit verf = new Lehrerverfuegbarkeit();
            verf.setLehrkraft(gespeicherteLehrkraft);
            verf.setWochentag(wochentag);   // null bei ganztaegig
            verf.setZeitslot(zeitslot);     // null bei ganztaegig
            verf.setTyp(verfDTO.getTyp());
            verf.setStartdatum(verfDTO.getStartdatum());
            verf.setEnddatum(verfDTO.getEnddatum());

            lehrerverfuegbarkeitRepository.save(verf);
            }
        }

        return zuAnzeigenDTO(gespeicherteLehrkraft);
    }

    @Transactional
    public LehrkraftAnzeigenDTO bearbeiten(Long id, LehrkraftBearbeitenDTO dto) {
       Lehrkraft lehrkraft = lehrkraftRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lehrkraft nicht gefunden"));
        
        if (dto.getAktiv() == null) {
            throw new RuntimeException("Feld 'aktiv' muss angegeben werden");
        }

        Account account = lehrkraft.getAccount();

        boolean warAktiv = account.isAktiv();
        boolean nameGeaendert = !account.getName().equals(dto.getName()) || !account.getVorname().equals(dto.getVorname());

        account.setName(dto.getName());
        account.setVorname(dto.getVorname());
        if (nameGeaendert) {
            account.setBenutzername(generieren.benutzername(dto.getVorname(), dto.getName()));
        }
        account.setAktiv(dto.getAktiv());
        accountRepository.save(account);
        
        if (nameGeaendert) {
            lehrkraft.setKuerzel(generieren.kuerzel(dto.getVorname(), dto.getName()));
        }
        lehrkraft.setAnstellungsverhaeltnis(dto.getAnstellungsverhaeltnis());
        lehrkraft.setDeputatsstunden(dto.getDeputatsstunden());

        boolean wirdJetztDeaktiviert = warAktiv && !dto.getAktiv();
        if (wirdJetztDeaktiviert) {
            Lerngruppe klassenleitung = lerngruppeRepository.findByKlassenleitung(lehrkraft);
            if (klassenleitung != null) {
                Lehrkraft ersatz = zufaelligeLehrkraftFuerKlassenleitung();
                klassenleitung.setKlassenleitung(ersatz);
                lerngruppeRepository.save(klassenleitung);
            }
        }

        //Fächer aktualisieren
        List<Fach> faecher = new ArrayList<>();
        if (dto.getFachIds() != null) {
            fachRepository.findAllById(dto.getFachIds()).forEach(faecher::add);
        }
        lehrkraft.setFaecher(faecher);

        //Präferenzen aktualisieren
        List<Lerngruppe> praeferenzen = new ArrayList<>();
        if (dto.getPraeferenzIds() != null) {
            lerngruppeRepository.findAllById(dto.getPraeferenzIds()).forEach(praeferenzen::add);
        }
        lehrkraft.setPraeferenzen(praeferenzen);

        //Alte Lerngruppen-Verknüpfungen entfernen
        // WICHTIG: lerngruppeId == null bedeutet explizit "keine Klassenleitung mehr",
        // NICHT "unverändert lassen". Der Client muss beim Bearbeiten immer die
        // aktuelle lerngruppeId erneut mitschicken, falls die Zuordnung bestehen
        // bleiben soll - sonst geht sie verloren (Ersatz wird automatisch gesetzt,
        // damit eine Klasse nie ohne Klassenleitung ist).
        Lerngruppe alteGruppe = lerngruppeRepository.findByKlassenleitung(lehrkraft);
        if (alteGruppe != null) {
            Lehrkraft ersatz = zufaelligeLehrkraftFuerKlassenleitung();
            alteGruppe.setKlassenleitung(ersatz);
            lerngruppeRepository.save(alteGruppe);
        }

        //Neue Lerngruppe verknüpfen - nur wenn eine ID mitgeschickt wurde
        if (dto.getLerngruppeId() != null) {
            Lerngruppe gruppe = lerngruppeRepository.findById(dto.getLerngruppeId())
                    .orElseThrow(() -> new RuntimeException("Lerngruppe nicht gefunden"));

            gruppe.setKlassenleitung(lehrkraft);
            lerngruppeRepository.save(gruppe);
        }

        //Alte Verfügbarkeiten löschen
        List<Lehrerverfuegbarkeit> alteVerfuegbarkeiten =
                lehrerverfuegbarkeitRepository.findByLehrkraft(lehrkraft);

        lehrerverfuegbarkeitRepository.deleteAll(alteVerfuegbarkeiten);

        //Neue Verfügbarkeiten anlegen
        if (dto.getVerfuegbarkeiten() != null) {

            for (VerfuegbarkeitEingabeDTO verfDTO : dto.getVerfuegbarkeiten()) {
                Zeitslot zeitslot = null;
                Wochentag wochentag = null;

                if (Boolean.TRUE.equals(verfDTO.getGanztaegig())) {
                    if (verfDTO.getStartdatum() == null || verfDTO.getEnddatum() == null) {
                        throw new RuntimeException("Bei ganztaegiger Verfuegbarkeit muessen Start- und Enddatum angegeben werden");
                    }
                } else {
                    if (verfDTO.getZeitslotId() == null) {
                        throw new RuntimeException("Zeitslot muss angegeben werden, wenn nicht ganztaegig");
                    }
                    zeitslot = zeitslotRepository.findById(verfDTO.getZeitslotId())
                        .orElseThrow(() -> new RuntimeException("Zeitslot nicht gefunden"));
                    wochentag = verfDTO.getWochentag();
                }

                Lehrerverfuegbarkeit verf = new Lehrerverfuegbarkeit();

                verf.setLehrkraft(lehrkraft);
                verf.setWochentag(wochentag);
                verf.setZeitslot(zeitslot);
                verf.setTyp(verfDTO.getTyp());
                verf.setStartdatum(verfDTO.getStartdatum());
                verf.setEnddatum(verfDTO.getEnddatum());

                lehrerverfuegbarkeitRepository.save(verf);
            }
        }

        Lehrkraft gespeicherteLehrkraft = lehrkraftRepository.save(lehrkraft);
        return zuAnzeigenDTO(gespeicherteLehrkraft);
    }

    public void deaktivieren(Long id) {
        Lehrkraft lehrkraft = lehrkraftRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lehrkraft nicht gefunden"));
        Account account = lehrkraft.getAccount();
        
        if (account.isAktiv()) {
            account.setAktiv(false);
            accountRepository.save(account);

            Lerngruppe klassenleitung = lerngruppeRepository.findByKlassenleitung(lehrkraft);
            if (klassenleitung != null) {
                Lehrkraft ersatz = zufaelligeLehrkraftFuerKlassenleitung();
                klassenleitung.setKlassenleitung(ersatz);
                lerngruppeRepository.save(klassenleitung);
            }
        }

    }

    public Lehrkraft zufaelligeLehrkraftFuerKlassenleitung() {
        List<Lehrkraft> alleLehrkraefte = new ArrayList<>();
        lehrkraftRepository.findAll().forEach(alleLehrkraefte::add);

        List<Lehrkraft> verfuegbar = new ArrayList<>();
        for (Lehrkraft l : alleLehrkraefte) {
            Lerngruppe leitet = lerngruppeRepository.findByKlassenleitung(l);
            if (leitet == null && l.getAccount().isAktiv()) {
                verfuegbar.add(l);
            }
        }

        if (verfuegbar.isEmpty()) {
            throw new RuntimeException("Keine verfuegbare Lehrkraft ohne Klassenleitung gefunden");
        }

        int zufallsIndex = new Random().nextInt(verfuegbar.size());
        return verfuegbar.get(zufallsIndex);
    }

    public Optional<Lehrkraft> verfuegbareLehrkraft(Wochentag wochentag, Zeitslot zeitslot, Fach fach) {
        List<Lehrkraft> fachlehrkraefte = unterrichtsstundeRepository
                .findVerfuegbareFachlehrkraefte(wochentag, zeitslot, fach, PlanStatus.AKTIV);
        if (!fachlehrkraefte.isEmpty()) {
            return Optional.of(fachlehrkraefte.get(new Random().nextInt(fachlehrkraefte.size())));
        }

        List<Lehrkraft> alleVerfuegbaren = unterrichtsstundeRepository
                .findVerfuegbareLehrkraefte(wochentag, zeitslot, PlanStatus.AKTIV);
        if (alleVerfuegbaren.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(alleVerfuegbaren.get(new Random().nextInt(alleVerfuegbaren.size())));
    }
    

    private LehrkraftAnzeigenDTO zuAnzeigenDTO(Lehrkraft l) {
        LehrkraftAnzeigenDTO dto = new LehrkraftAnzeigenDTO();

        dto.setId(l.getId());
        dto.setName(l.getAccount().getName());
        dto.setVorname(l.getAccount().getVorname());
        dto.setBenutzername(l.getAccount().getBenutzername()); 
        dto.setAktiv(l.getAccount().isAktiv());
        dto.setKuerzel(l.getKuerzel());
        dto.setAnstellungsverhaeltnis(l.getAnstellungsverhaeltnis());
        dto.setDeputatsstunden(l.getDeputatsstunden());
        List<FachUebersichtDTO> fachDTOs = new ArrayList<>();
        for (Fach f : l.getFaecher()) {
            FachUebersichtDTO fDto = new FachUebersichtDTO();
            fDto.setId(f.getId());
            fDto.setKuerzel(f.getKuerzel());
            fDto.setName(f.getName());
            fDto.setBenoetigterRaumtyp(f.getBenoetigterRaumtyp());
            fachDTOs.add(fDto);
        }
        dto.setFaecher(fachDTOs);

        List<LerngruppeUebersichtDTO> praeferenzDTOs = new ArrayList<>();
        for (Lerngruppe lg : l.getPraeferenzen()) {
            LerngruppeUebersichtDTO lgDto = new LerngruppeUebersichtDTO();
            lgDto.setId(lg.getId());
            lgDto.setBezeichnung(lg.getBezeichnung());
            lgDto.setJahrgangsnummer(lg.getJahrgangsstufe().getNummer());
            praeferenzDTOs.add(lgDto);
        }
        dto.setPraeferenzen(praeferenzDTOs);

        Lerngruppe lerngruppe = lerngruppeRepository.findByKlassenleitung(l); //nach aktivitätsstatus filtern? eher nicht, es wird klassenleitung null gesetzt bei deaktivierung einer klasse
        if (lerngruppe != null) {
            LerngruppeFuerLehrkraftDTO lgDTO = new LerngruppeFuerLehrkraftDTO();
            lgDTO.setBezeichnung(lerngruppe.getBezeichnung());
            
            if (lerngruppe.getJahrgangsstufe() != null) {
                lgDTO.setJahrgangsstufe(lerngruppe.getJahrgangsstufe().getNummer());
            }

            if (lerngruppe.getKlassenzimmer() != null) {
                lgDTO.setKlassenzimmer(lerngruppe.getKlassenzimmer().getBezeichnung());
            }
            dto.setLerngruppe(lgDTO);
        }

        List<Lehrerverfuegbarkeit> verfuegbarkeiten = lehrerverfuegbarkeitRepository.findByLehrkraft(l);
        List<VerfuegbarkeitFuerLehrkraftDTO> verfDTOs = new ArrayList<>();

        for (Lehrerverfuegbarkeit verf : verfuegbarkeiten) {
            VerfuegbarkeitFuerLehrkraftDTO verfDTO = new VerfuegbarkeitFuerLehrkraftDTO();

            boolean istGanztaegig = verf.getZeitslot() == null;
            verfDTO.setGanztaegig(istGanztaegig);

            if (!istGanztaegig) {
                verfDTO.setZeitslotId(verf.getZeitslot().getId());
                verfDTO.setStundennummer(verf.getZeitslot().getStundennummer());
                verfDTO.setWochentag(verf.getWochentag());
            }

            verfDTO.setTyp(verf.getTyp());
            verfDTO.setStartdatum(verf.getStartdatum());
            verfDTO.setEnddatum(verf.getEnddatum());
            verfDTOs.add(verfDTO);
        }

        dto.setVerfuegbarkeiten(verfDTOs);

        return dto;

    }

    public void passwortZuruecksetzen(Long lehrkraftId, PasswortZuruecksetzenDTO dto) {
        Lehrkraft lehrkraft = lehrkraftRepository.findById(lehrkraftId)
                .orElseThrow(() -> new RuntimeException("Lehrkraft nicht gefunden"));

        Long accountId = lehrkraft.getAccount().getId();
        accountService.passwortZuruecksetzen(accountId, dto);
    }

    public List<LehrkraftInfoDTO> alleInfo(){
        return StreamSupport.stream(lehrkraftRepository.findByAccount_Aktiv(true).spliterator(), false)
                .map(lehrkraft -> new LehrkraftInfoDTO(
                        lehrkraft.getId(),
                        lehrkraft.getAccount().getVorname(),
                        lehrkraft.getAccount().getName(),
                        lehrkraft.getKuerzel()
                )).collect(Collectors.toList());
    }

    public List<UnterrichtsstundeDTO> lehrkraftEinzeln(Long id){
        Lehrkraft lehrkraft = lehrkraftRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lehrkraft nicht gefunden"));
        List<Unterrichtsstunde> stunden = unterrichtsstundeRepository.findByLehrkraftAndStundenplanStatus(lehrkraft, PlanStatus.AKTIV);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        List<Aenderung> aenderungen = aenderungRepository.findAbgeschlosseneByLehrkraftNeuAndDatumBetween(
                lehrkraft, weekStart, weekEnd
        );

        Map<String, UnterrichtsstundeDTO> eintragMap = new HashMap<>();

        for (Unterrichtsstunde stunde : stunden) {
            String key = stunde.getWochentag().name() + "_" + stunde.getZeitslot().getId();
            eintragMap.put(key, createDTOFromStunde(stunde, false));
        }

        for (Aenderung aenderung : aenderungen) {
            String key = aenderung.getWochentagNeu().name()
                    + "_"
                    + aenderung.getZeitslotNeu().getId();

            eintragMap.put(key, createDTOFromAenderung(aenderung, true));
        }

        return eintragMap.values().stream()
                .sorted(Comparator
                        .comparing(UnterrichtsstundeDTO::getWochentag)
                        .thenComparing(UnterrichtsstundeDTO::getStundennummer))
                .collect(Collectors.toList());

    }

}
