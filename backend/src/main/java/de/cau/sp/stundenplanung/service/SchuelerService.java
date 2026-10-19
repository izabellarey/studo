package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.accounts.PasswortZuruecksetzenDTO;
import de.cau.sp.stundenplanung.dto.dashboard.AktuelleAenderungDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import de.cau.sp.stundenplanung.dto.schueler.SchuelerAnlegenDTO;
import de.cau.sp.stundenplanung.dto.schueler.SchuelerAnzeigenDTO;
import de.cau.sp.stundenplanung.dto.schueler.SchuelerBearbeitenDTO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SchuelerService {


    private final SchuelerRepository schuelerRepository;
    private final AccountRepository accountRepository;
    private final Generieren generieren;
    private final LerngruppeRepository lerngruppeRepository;
    private final PasswordEncoder passwordEncoder;

    private final AccountService accountService;
    private final StundenplanRepository stundenplanRepository;
    private final AenderungRepository aenderungRepository;

    public List<UnterrichtsstundeDTO> klassenStundenplan(String benutzername) {
        Account nutzer = accountRepository.findByBenutzername(benutzername)
                .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));
        Schueler schueler = schuelerRepository.findByAccount(nutzer)
                .orElseThrow(() -> new RuntimeException("Schüler nicht gefunden"));
        Lerngruppe klasse = schueler.getLerngruppe(); //kann nur aktive lerngruppe sein

        Stundenplan aktiverPlan = stundenplanRepository.findByLerngruppeAndStatus(klasse, PlanStatus.AKTIV)
                .orElseThrow(() -> new RuntimeException("Kein aktiver Stundenplan gefunden"));

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
    public List<AktuelleAenderungDTO> aktuelleAenderungenAnzeigen(String benutzername){//String benutzername){

        Account nutzer = accountRepository.findByBenutzername(benutzername)
                .orElseThrow(() -> new RuntimeException("Account nicht gefunden"));
        Schueler schueler = schuelerRepository.findByAccount(nutzer)
                .orElseThrow(() -> new RuntimeException("Schüler nicht gefunden"));
        Lerngruppe klasse = schueler.getLerngruppe();

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        return StreamSupport.stream(aenderungRepository.findAbgeschlosseneByLerngruppeNeuAndDatumBetween( //nur aktive klassen überhaupt mitgegeben
                        klasse, weekStart, weekEnd
                ).spliterator(), false)
                .map(aenderung -> new AktuelleAenderungDTO(aenderung.getId(),
                        aenderung.getAusloeser().getTyp(),
                        generieren.aenderungSchuelerTitelGenerieren(aenderung),
                        generieren.aenderungSchuelerBeschreibungGenerieren(aenderung),
                        false
                ))
                .collect(Collectors.toList());
    }

    public List<SchuelerAnzeigenDTO> alle() {
        List<Schueler> schuelerListe = schuelerRepository.findByAccount_Aktiv(true);

        List<SchuelerAnzeigenDTO> result = new ArrayList<>();

        for (Schueler s : schuelerListe) {
            SchuelerAnzeigenDTO dto = zuAnzeigenDTO(s);
            result.add(dto);
        }

        return result;
    }

    public List<SchuelerAnzeigenDTO> alleNachKlassenstufe(int von, int bis) {
        List<Schueler> schuelerListe = schuelerRepository.findByLerngruppe_Jahrgangsstufe_NummerBetweenAndAccount_Aktiv(von, bis, true);
        

        List<SchuelerAnzeigenDTO> result = new ArrayList<>();

        for (Schueler s : schuelerListe) {
            SchuelerAnzeigenDTO dto = zuAnzeigenDTO(s);
            result.add(dto);
        }

        return result;
    }

    public List<SchuelerAnzeigenDTO> alleDeaktivierten() {
        List<Schueler> schuelerListe = schuelerRepository.findByAccount_Aktiv(false);

        List<SchuelerAnzeigenDTO> result = new ArrayList<>();
        for (Schueler s : schuelerListe) {
            result.add(zuAnzeigenDTO(s));
        }
        return result;
    }

    public SchuelerAnzeigenDTO einzeln(Long id) {
        Schueler schueler = schuelerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Schueler nicht gefunden"));
        return zuAnzeigenDTO(schueler);
    }

    @Transactional
    public SchuelerAnzeigenDTO anlegen(SchuelerAnlegenDTO dto) {
        // 1. Account erzeugen
        Account account = new Account();
        account.setName(dto.getName());
        account.setVorname(dto.getVorname());
        account.setBenutzername(generieren.benutzername(dto.getVorname(), dto.getName()));
        account.setRolle(Rolle.SCHUELER);
        account.setPasswort(passwordEncoder.encode(dto.getPasswort()));
        account.setAktiv(true);
        Account gespeicherterAccount = accountRepository.save(account);

        // 2. Schueler erzeugen, mit Account verknüpfen
        Schueler schueler = new Schueler();
        schueler.setAccount(gespeicherterAccount);
        schueler.setEintrittsdatum(dto.getEintrittsdatum());

        if (dto.getKlassenId() != null) {
            Lerngruppe gruppe = lerngruppeRepository.findById(dto.getKlassenId())
                .orElseThrow(() -> new RuntimeException("Lerngruppe nicht gefunden"));
            schueler.setLerngruppe(gruppe);
            
            // Schueleranzahl der neuen Klasse erhoehen
            gruppe.setSchueleranzahl(gruppe.getSchueleranzahl() + 1);
            lerngruppeRepository.save(gruppe);
        }

        Schueler gespeicherterSchueler = schuelerRepository.save(schueler);

        return zuAnzeigenDTO(gespeicherterSchueler);
    }

    @Transactional
    public SchuelerAnzeigenDTO bearbeiten(Long id, SchuelerBearbeitenDTO dto) {
        Schueler schueler = schuelerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Schueler nicht gefunden"));

        if (dto.getAktiv() == null) {
            throw new RuntimeException("Feld 'aktiv' muss angegeben werden");
        }

        // Alten Zustand VOR den Aenderungen merken
        Lerngruppe alteGruppe = schueler.getLerngruppe();
        boolean warAktiv = schueler.getAccount().isAktiv();

        Account account = schueler.getAccount();
        boolean nameGeaendert = !account.getName().equals(dto.getName()) || !account.getVorname().equals(dto.getVorname());

        account.setName(dto.getName());
        account.setVorname(dto.getVorname());
        if (nameGeaendert) {
            account.setBenutzername(generieren.benutzername(dto.getVorname(), dto.getName()));
        }
        account.setAktiv(dto.getAktiv());
        accountRepository.save(account);

        schueler.setAustrittsdatum(dto.getAustrittsdatum());

        boolean wirdJetztDeaktiviert = warAktiv && !dto.getAktiv();

        Lerngruppe neueGruppe = alteGruppe;
        if (!wirdJetztDeaktiviert && dto.getKlassenId() != null) {
            neueGruppe = lerngruppeRepository.findById(dto.getKlassenId())
                .orElseThrow(() -> new RuntimeException("Lerngruppe nicht gefunden"));
            schueler.setLerngruppe(neueGruppe);
        }

        boolean klasseHatSichGeaendert = false;

        if (alteGruppe == null && neueGruppe != null) {
            klasseHatSichGeaendert = true;
        } else if (alteGruppe != null && neueGruppe != null) {
            if (!alteGruppe.getId().equals(neueGruppe.getId())) {
                klasseHatSichGeaendert = true;
            }
        }

        if (wirdJetztDeaktiviert) {
            if (alteGruppe != null) {
                alteGruppe.setSchueleranzahl(alteGruppe.getSchueleranzahl() - 1);
                lerngruppeRepository.save(alteGruppe);
            }
        } else if (klasseHatSichGeaendert) {
            if (alteGruppe != null) {
                alteGruppe.setSchueleranzahl(alteGruppe.getSchueleranzahl() - 1);
                lerngruppeRepository.save(alteGruppe);
            }
            if (neueGruppe != null) {
                neueGruppe.setSchueleranzahl(neueGruppe.getSchueleranzahl() + 1);
                lerngruppeRepository.save(neueGruppe);
            }
        }

        Schueler gespeicherterSchueler = schuelerRepository.save(schueler);
        return zuAnzeigenDTO(gespeicherterSchueler);
    }

    public void deaktivieren(Long id) {
        Schueler schueler = schuelerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Schueler nicht gefunden"));
        Account account = schueler.getAccount();
        if (account.isAktiv()) {
            account.setAktiv(false);
            accountRepository.save(account);

            Lerngruppe gruppe = schueler.getLerngruppe();
            if (gruppe != null) {
                gruppe.setSchueleranzahl(gruppe.getSchueleranzahl() - 1);
                lerngruppeRepository.save(gruppe);
            }
        }
    }

    private SchuelerAnzeigenDTO zuAnzeigenDTO(Schueler s) {
        SchuelerAnzeigenDTO dto = new SchuelerAnzeigenDTO();

        dto.setId(s.getId());
        dto.setName(s.getAccount().getName());
        dto.setVorname(s.getAccount().getVorname());
        dto.setBenutzername(s.getAccount().getBenutzername());
        dto.setAktiv(s.getAccount().isAktiv());

        if (s.getLerngruppe() != null) {
            dto.setKlasseId(s.getLerngruppe().getId());
            dto.setKlasse(s.getLerngruppe().getBezeichnung());
            if (s.getLerngruppe().getKlassenzimmer() != null) {
                dto.setKlassenraum(s.getLerngruppe().getKlassenzimmer().getBezeichnung());
            }
        }

        return dto;
    }

    public void passwortZuruecksetzen(Long schuelerId, PasswortZuruecksetzenDTO dto) {
        Schueler schueler = schuelerRepository.findById(schuelerId)
                .orElseThrow(() -> new RuntimeException("Lehrkraft nicht gefunden"));

        Long accountId = schueler.getAccount().getId();
        accountService.passwortZuruecksetzen(accountId, dto);
    }
}
