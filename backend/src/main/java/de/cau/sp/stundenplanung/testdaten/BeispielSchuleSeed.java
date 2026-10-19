package de.cau.sp.stundenplanung.testdaten;

import de.cau.sp.stundenplanung.dto.ereignis.EreignisDTO;
import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.*;
import de.cau.sp.stundenplanung.service.EreignisService;
import de.cau.sp.stundenplanung.service.Generieren;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.time.LocalDateTime;

/**
 * Legt die Beispielschule an, nur bei app.seed-aktiv=true und leerer Datenbank.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seed-aktiv", havingValue = "true")
public class BeispielSchuleSeed implements CommandLineRunner {

    private final SchuleRepository schuleRepository;
    private final FachRepository fachRepository;
    private final RaumRepository raumRepository;
    private final ZeitslotRepository zeitslotRepository;
    private final StundenrasterRepository stundenrasterRepository;
    private final RasterpauseRepository rasterpauseRepository;
    private final JahrgangsstufeRepository jahrgangsstufeRepository;
    private final LerngruppeRepository lerngruppeRepository;
    private final LehrkraftRepository lehrkraftRepository;
    private final AccountRepository accountRepository;
    private final SchuelerRepository schuelerRepository;
    private final StundenkontingentRepository stundenkontingentRepository;
    private final LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    private final RaumverfuegbarkeitRepository raumverfuegbarkeitRepository;
    private final RahmenbedingungRepository rahmenbedingungRepository;
    private final MaximalbelastungRepository maximalbelastungRepository;
    private final PausenregelRepository pausenregelRepository;
    private final BlockregelRepository blockregelRepository;
    private final PflichtfachvorgabeRepository pflichtfachvorgabeRepository;
    private final EreignisRepository ereignisRepository;

    private final StundenplanRepository stundenplanRepository;
    private final UnterrichtsstundeRepository unterrichtsstundeRepository;
    private final Generieren generieren;
    private final EreignisService ereignisService;
    private final AenderungRepository aenderungRepository;
    private final PasswordEncoder passwordEncoder;

    private Unterrichtsstunde stunde5aMontag2; // M / MUE – fuer die normale Aenderung
    private Unterrichtsstunde stunde5aMittwoch3; // SP / HOF – fuer die Freistunde
    private Ereignis ereignisRaumaenderung;
    private Ereignis ereignisAusfall;
    private Ereignis ereignisZusatzstunde;

    @Value("${app.seed-passwort:$2a$10$3PDTlZnz/apeMIuzuW2Ym.Of2iiSZiBDwPprcctpXcd0ARaLwHoPW}") // jetzt passworthash
                                                                                                // speichern. klartext
                                                                                                // pw: start1234
    private String startpasswort;

    /** Fester Seed, damit jeder Lauf dieselben Namen erzeugt. */
    private final Random zufall = new Random(20260913L);

    private final Map<String, Fach> faecher = new HashMap<>();
    private final Map<String, Raum> raeume = new HashMap<>();
    private final Map<String, Lehrkraft> lehrkraefte = new HashMap<>();
    private final Map<Integer, Jahrgangsstufe> jahrgaenge = new HashMap<>();
    private final Map<String, Lerngruppe> klassen = new HashMap<>();
    private final List<Zeitslot> slots = new ArrayList<>();

    private Schule schule;

    @Override
    @Transactional
    public void run(String... args) {
        if (schuleRepository.count() > 0) {
            log.info("Datenbank enthaelt bereits Daten - Seed wird uebersprungen.");
            return;
        }
        log.info("Lege Beispielschule an...");

        schule();
        zeitraster();
        faecher();
        raeume();
        jahrgangsstufen();
        lehrkraefteAnlegen();
        klassenAnlegen();
        stundenkontingente();
        verfuegbarkeiten();
        regelwerk();
        schueler();
        //testdatenUnterrichtsstunden();
        //testdatenEreignisse();
        // testdatenAntonia();

        log.info("Beispielschule angelegt: {} Lerngruppen, {} Lehrkraefte, {} Raeume",
                klassen.size(), lehrkraefte.size(), raeume.size());
    }

    private void schule() {
        Schule s = new Schule();
        s.setName("Gymnasium am Westring");
        s.setBundesland("Schleswig-Holstein");
        s.setSchulform("Gymnasium (Sek I)");
        schule = schuleRepository.save(s);
    }

    /** Raster und Pausen anlegen, die Zeitslots ergeben sich daraus. */
    private void zeitraster() {
        Stundenraster r = new Stundenraster();
        r.setSchule(schule);
        r.setSchulbeginn(LocalTime.of(8, 0));
        r.setStundendauer(45);
        r.setStundenProTag(8);
        stundenrasterRepository.save(r);

        Map<Integer, Integer> pausen = new HashMap<>();
        pausen.put(2, 20);
        pausen.put(4, 15);
        pausen.put(6, 30);
        for (Map.Entry<Integer, Integer> e : pausen.entrySet()) {
            Rasterpause p = new Rasterpause();
            p.setStundenraster(r);
            p.setNachStunde(e.getKey());
            p.setDauerMinuten(e.getValue());
            rasterpauseRepository.save(p);
        }

        LocalTime start = r.getSchulbeginn();
        for (int nummer = 1; nummer <= r.getStundenProTag(); nummer++) {
            Zeitslot z = new Zeitslot();
            z.setStundennummer(nummer);
            z.setStart(start);
            z.setEnde(start.plusMinutes(r.getStundendauer()));
            z.setPause(false);
            slots.add(zeitslotRepository.save(z));
            start = z.getEnde().plusMinutes(pausen.getOrDefault(nummer, 0));
        }
    }

    private void faecher() {
        // Kürzel, Name, benötigter Raumtyp, Hauptfach
        Object[][] daten = {
                { "D", "Deutsch", null, true },
                { "M", "Mathematik", null, true },
                { "E", "Englisch", null, true },
                { "BIO", "Biologie", Raumart.BIOLOGIERAUM, false },
                { "PH", "Physik", Raumart.PHYSIKRAUM, false },
                { "CH", "Chemie", Raumart.CHEMIERAUM, false },
                { "GE", "Geschichte", null, false },
                { "GEO", "Geographie", null, false },
                { "SP", "Sport", Raumart.SPORTHALLE, false },
                { "KU", "Kunst", Raumart.KUNSTRAUM, false },
                { "MU", "Musik", Raumart.MUSIKRAUM, false },
                { "REL", "Religion", null, false },
                { "FR", "Franzoesisch", null, true },
                { "INF", "Informatik", Raumart.INFORMATIKRAUM, false },
                { "WP", "Wirtschaft/Politik", null, false },
                { "VB", "Verbraucherbildung", null, false },
        };
        for (Object[] d : daten) {
            Fach f = new Fach();
            f.setKuerzel((String) d[0]);
            f.setName((String) d[1]);
            f.setBenoetigterRaumtyp((Raumart) d[2]);
            f.setHauptfach((Boolean) d[3]);
            faecher.put(f.getKuerzel(), fachRepository.save(f));
        }
    }

    private void raeume() {
        for (int etage = 1; etage <= 2; etage++) {
            for (int nr = 1; nr <= 10; nr++) {
                raumAnlegen("R" + etage + "0" + nr, Raumart.KLASSENRAUM, 32);
            }
        }
        raumAnlegen("BIO1", Raumart.BIOLOGIERAUM, 30);
        raumAnlegen("BIO2", Raumart.BIOLOGIERAUM, 30);
        raumAnlegen("PH1", Raumart.PHYSIKRAUM, 30);
        raumAnlegen("PH2", Raumart.PHYSIKRAUM, 30);
        raumAnlegen("CH1", Raumart.CHEMIERAUM, 30);
        raumAnlegen("KU1", Raumart.KUNSTRAUM, 28);
        raumAnlegen("KU2", Raumart.KUNSTRAUM, 28);
        raumAnlegen("MU1", Raumart.MUSIKRAUM, 30);
        raumAnlegen("INF1", Raumart.INFORMATIKRAUM, 28);
        raumAnlegen("INF2", Raumart.INFORMATIKRAUM, 28);
        raumAnlegen("Halle A", Raumart.SPORTHALLE, 60);
        raumAnlegen("Halle B", Raumart.SPORTHALLE, 60);
        raumAnlegen("Aula", Raumart.AULA, 200);
    }

    private void raumAnlegen(String bezeichnung, Raumart art, int kapazitaet) {
        Raum r = new Raum();
        r.setBezeichnung(bezeichnung);
        r.setArt(art);
        r.setKapazitaet(kapazitaet);
        raeume.put(bezeichnung, raumRepository.save(r));
    }

    private void jahrgangsstufen() {
        for (int nummer = 5; nummer <= 10; nummer++) {
            Jahrgangsstufe j = new Jahrgangsstufe();
            j.setNummer(nummer);
            j.setPrio(nummer - 4);
            jahrgaenge.put(nummer, jahrgangsstufeRepository.save(j));
        }
    }

    private void lehrkraefteAnlegen() {
        // Kürzel, Nachname, Vorname, Vollzeit?, Deputat, Fächer
        Object[][] daten = {
                { "MUE", "Mueller", "Anke", true, 25, "M,PH" },
                { "SCH", "Schmidt", "Jonas", true, 25, "M,CH" },
                { "KRA", "Krause", "Petra", true, 25, "M,GEO" },
                { "KOE", "Koehler", "Andreas", true, 25, "PH,M" },
                { "WEB", "Weber", "Thomas", true, 25, "D,GE" },
                { "FIS", "Fischer", "Sabine", true, 25, "D,GEO" },
                { "RIC", "Richter", "Michael", true, 25, "D,GE" },
                { "SEI", "Seidel", "Anja", true, 25, "KU,D" },
                { "KLE", "Klein", "Julia", true, 25, "E,MU" },
                { "BEC", "Becker", "Daniel", true, 25, "E,GE" },
                { "ZIM", "Zimmermann", "Laura", true, 25, "E,GEO" },
                { "HOF", "Hoffmann", "Kerstin", true, 25, "BIO,SP" },
                { "HAR", "Hartmann", "Nadine", true, 25, "BIO,CH" },
                { "WAG", "Wagner", "Stefan", true, 25, "SP,KU" },
                { "BRA", "Braun", "Markus", false, 13, "SP,GEO" },
                { "NEU", "Neumann", "Erik", false, 13, "KU,MU" },
                { "LAN", "Lange", "Christine", true, 25, "FR,REL" },
                { "PET", "Peters", "Sven", true, 25, "FR,GE" },
                { "FLE", "Fleischmann", "Isa", true, 25, "INF,M" },
                { "HUB", "Huber", "Tobias", true, 25, "INF,PH" },
                { "SOM", "Sommer", "Katrin", true, 25, "REL,WP" },
                { "ROT", "Roth", "Daniel", true, 25, "WP,VB" },
                { "BAU", "Bauer", "Lena", false, 13, "REL,VB" },
        };
        for (Object[] d : daten) {
            String kuerzel = (String) d[0];

            Account a = new Account();
            a.setBenutzername(generieren.benutzername((String) d[2], (String) d[1]));
            a.setName((String) d[1]);
            a.setVorname((String) d[2]);
            a.setPasswort(startpasswort); // TODO: hashen, sobald Security steht
            a.setRolle(Rolle.LEHRKRAFT);
            a.setAktiv(true);
            accountRepository.save(a);

            Lehrkraft l = new Lehrkraft();
            l.setKuerzel(kuerzel);
            l.setAccount(a);
            l.setAnstellungsverhaeltnis((boolean) d[3]
                    ? Anstellungsverhaeltnis.VOLLZEIT
                    : Anstellungsverhaeltnis.TEILZEIT);
            l.setDeputatsstunden((int) d[4]);
            l.setFaecher(Arrays.stream(((String) d[5]).split(","))
                    .map(faecher::get).toList());
            lehrkraefte.put(kuerzel, lehrkraftRepository.save(l));
        }
        /* 
        planungskonto("leitung", "Lindner", "Frauke");
        planungskonto("sekretariat", "Otto", "Birgit");
        */
        planungskonto("leitung", "Lindner", "Frauke", "#TeamKommis4life!");
        planungskonto("sekretariat", "Otto", "Birgit", "STudoAufDie1!");
    }

    /* 
    private void planungskonto(String benutzername, String name, String vorname) {
        Account a = new Account();
        a.setBenutzername(generieren.benutzername(vorname, name));
        a.setName(name);
        a.setVorname(vorname);
        a.setPasswort(startpasswort);
        a.setRolle(Rolle.PLANUNG);
        a.setAktiv(true);
        accountRepository.save(a);
    }
    */
    private void planungskonto(String benutzername, String name, String vorname, String passwort) {
        Account a = new Account();
        a.setBenutzername(generieren.benutzername(vorname, name));
        a.setName(name);
        a.setVorname(vorname);
        a.setPasswort(passwordEncoder.encode(passwort));
        a.setRolle(Rolle.PLANUNG);
        a.setAktiv(true);
        accountRepository.save(a);
    }
    private void klassenAnlegen() {
        // Bezeichnung, Jahrgang, Größe, Stammraum, Klassenleitung
        Object[][] daten = {
                { "5a", 5, 26, "R101", "WEB" },
                { "5b", 5, 27, "R102", "KLE" },
                { "6a", 6, 28, "R103", "FIS" },
                { "6b", 6, 26, "R104", "BEC" },
                { "7a", 7, 27, "R105", "MUE" },
                { "7b", 7, 25, "R106", "SCH" },
                { "8a", 8, 26, "R201", "HOF" },
                { "8b", 8, 27, "R202", "WAG" },
                { "9a", 9, 24, "R203", "KRA" },
                { "9b", 9, 25, "R204", "RIC" },
                { "10a", 10, 26, "R205", "ZIM" },
                { "10b", 10, 25, "R206", "HAR" },
        };
        for (Object[] d : daten) {
            Lerngruppe g = new Lerngruppe();
            g.setBezeichnung((String) d[0]);
            g.setJahrgangsstufe(jahrgaenge.get((int) d[1]));
            g.setSchueleranzahl((int) d[2]);
            g.setKlassenzimmer(raeume.get((String) d[3]));
            g.setKlassenleitung(lehrkraefte.get((String) d[4]));
            g.setArt(LerngruppenArt.KLASSE);
            g.setAktiv(true);
            klassen.put(g.getBezeichnung(), lerngruppeRepository.save(g));
        }
    }

    /**
     * Beispielhafte Klassenpräferenzen (max. 3 je Lehrkraft, siehe
     * Lehrkraft.praeferenzen),
     * damit sich der Soft-Constraint lehrkraftPraeferenzErfuellt beim Testen auch
     * auswirkt.
     * Jede Lehrkraft bekommt mindestens eine Klasse, deren Jahrgangsstufe eines
     * ihrer Fächer
     * tatsächlich anbietet (sonst kann der Constraint dort nie greifen, z.B. Chemie
     * erst ab J9).
     */
    private void lehrerpraeferenzen() {
        setzePraeferenzen("MUE", "7a", "9a", "10b");
        setzePraeferenzen("SCH", "9b", "10a");
        setzePraeferenzen("KRA", "5a", "6b");
        setzePraeferenzen("KOE", "8a", "10b");
        setzePraeferenzen("WEB", "5a", "6a", "7a");
        setzePraeferenzen("FIS", "6a", "7b");
        setzePraeferenzen("RIC", "9b", "8b");
        setzePraeferenzen("SEI", "5b", "6b");
        setzePraeferenzen("KLE", "5b", "7a");
        setzePraeferenzen("BEC", "6b", "9a");
        setzePraeferenzen("ZIM", "10a", "8a");
        setzePraeferenzen("HOF", "8a", "5a");
        setzePraeferenzen("HAR", "10b", "9a");
        setzePraeferenzen("WAG", "8b", "6a");
        setzePraeferenzen("BRA", "7a");
        setzePraeferenzen("NEU", "6a");
        setzePraeferenzen("LAN", "7a", "8b");
        setzePraeferenzen("PET", "9a", "10a");
        setzePraeferenzen("VOG", "5b", "9b");
        setzePraeferenzen("HUB", "7b", "8a");
        setzePraeferenzen("SOM", "7a", "9b");
        setzePraeferenzen("ROT", "8b", "10a");
        setzePraeferenzen("BAU", "6b");
    }

    private void setzePraeferenzen(String lehrkraftKuerzel, String... klassenBezeichnungen) {
        Lehrkraft lehrkraft = lehrkraefte.get(lehrkraftKuerzel);
        List<Lerngruppe> praeferenzen = new ArrayList<>();
        for (String bezeichnung : klassenBezeichnungen) {
            praeferenzen.add(klassen.get(bezeichnung));
        }
        lehrkraft.setPraeferenzen(praeferenzen);
        lehrkraftRepository.save(lehrkraft);
    }

    /** Wochenstunden je Jahrgang: {Fach, J5, J6, J7, J8, J9, J10} */
    private static final Object[][] KONTINGENTE = {
            { "D", 4, 4, 4, 4, 3, 3 },
            { "M", 4, 4, 4, 4, 4, 4 },
            { "E", 4, 4, 3, 3, 3, 3 },
            { "BIO", 2, 2, 2, 2, 2, 2 },
            { "PH", 0, 0, 2, 2, 2, 2 },
            { "CH", 0, 0, 0, 2, 2, 2 },
            { "GE", 2, 2, 2, 2, 2, 2 },
            { "GEO", 2, 2, 2, 1, 2, 2 },
            { "SP", 3, 3, 2, 2, 2, 2 },
            { "KU", 2, 2, 2, 1, 1, 1 },
            { "MU", 2, 2, 1, 1, 1, 1 },
            { "REL", 2, 2, 2, 2, 2, 2 },
            { "FR", 0, 0, 4, 4, 4, 4 },
            { "INF", 2, 2, 2, 2, 2, 2 },
            { "WP", 0, 0, 2, 2, 2, 2 },
            { "VB", 1, 1, 1, 1, 1, 1 },
    };

    private void stundenkontingente() {
        for (Lerngruppe g : klassen.values()) {
            int spalte = g.getJahrgangsstufe().getNummer() - 4; // J5 -> Index 1
            for (Object[] zeile : KONTINGENTE) {
                int stunden = (int) zeile[spalte];
                if (stunden == 0) {
                    continue;
                }
                Stundenkontingent k = new Stundenkontingent();
                k.setLerngruppe(g);
                k.setFach(faecher.get((String) zeile[0]));
                k.setWochenstunden(stunden);
                stundenkontingentRepository.save(k);
            }
        }
    }

    private void verfuegbarkeiten() {
        // Teilzeitkräfte: ganze Tage gesperrt
        sperreTage("BRA", Wochentag.FREITAG);
        sperreTage("NEU", Wochentag.MONTAG, Wochentag.DIENSTAG);

        // weiche Wünsche
        for (Wochentag tag : Wochentag.values()) {
            lehrerZeit("WEB", tag, 1, VerfuegbarkeitsTyp.NICHT_BEVORZUGT);
            lehrerZeit("MUE", tag, 7, VerfuegbarkeitsTyp.NICHT_BEVORZUGT);
            lehrerZeit("MUE", tag, 8, VerfuegbarkeitsTyp.NICHT_BEVORZUGT);
        }
        for (int stunde = 5; stunde <= 8; stunde++) {
            lehrerZeit("SEI", Wochentag.MITTWOCH, stunde, VerfuegbarkeitsTyp.NICHT_BEVORZUGT);
        }

        // Raumsperrungen
        raumZeit("Halle A", Wochentag.MITTWOCH, 7);
        raumZeit("Halle A", Wochentag.MITTWOCH, 8);
        for (int stunde = 5; stunde <= 8; stunde++) {
            raumZeit("Aula", Wochentag.FREITAG, stunde);
        }
    }

    private void sperreTage(String kuerzel, Wochentag... tage) {
        for (Wochentag tag : tage) {
            for (int stunde = 1; stunde <= 8; stunde++) {
                lehrerZeit(kuerzel, tag, stunde, VerfuegbarkeitsTyp.GESPERRT);
            }
        }
    }

    private void lehrerZeit(String kuerzel, Wochentag tag, int stunde, VerfuegbarkeitsTyp typ) {
        Lehrerverfuegbarkeit v = new Lehrerverfuegbarkeit();
        v.setLehrkraft(lehrkraefte.get(kuerzel));
        v.setZeitslot(slots.get(stunde - 1));
        v.setWochentag(tag);
        v.setTyp(typ);
        lehrerverfuegbarkeitRepository.save(v);
    }

    private void raumZeit(String bezeichnung, Wochentag tag, int stunde) {
        Raumverfuegbarkeit v = new Raumverfuegbarkeit();
        v.setRaum(raeume.get(bezeichnung));
        v.setZeitslot(slots.get(stunde - 1));
        v.setWochentag(tag);
        v.setTyp(VerfuegbarkeitsTyp.GESPERRT);
        raumverfuegbarkeitRepository.save(v);
    }

    private void regelwerk() {
        Rahmenbedingung rb = new Rahmenbedingung();
        rb.setBezeichnung("Gymnasium Schleswig-Holstein 2026");
        rb.setAktiv(true);
        rahmenbedingungRepository.save(rb);

        for (int nummer = 5; nummer <= 10; nummer++) {
            Jahrgangsstufe j = jahrgaenge.get(nummer);

            Maximalbelastung m = new Maximalbelastung();
            m.setAktiv(true);
            m.setJahrgangsstufe(j);
            m.setMaxStundenTag(nummer <= 6 ? 7 : 8);
            m.setMaxStundenWoche(nummer <= 6 ? 30 : 35);
            maximalbelastungRepository.save(m);

            Pausenregel p = new Pausenregel();
            // p.setRahmenbedingung(rb);
            p.setJahrgangsstufe(j);
            p.setMindestpauseNachStunden(2);
            p.setMindestdauerMinuten(15);
            pausenregelRepository.save(p);
        }

        blockregel(rb, "M", 5, 10);
        blockregel(rb, "D", 5, 10);
        blockregel(rb, "E", 5, 10);
        blockregel(rb, "SP", 5, 10);
        blockregel(rb, "BIO", 5, 10);
        blockregel(rb, "PH", 7, 10);
        blockregel(rb, "CH", 8, 10);
        blockregel(rb, "KU", 5, 7);
        blockregel(rb, "INF", 5, 10);

        // Pflichtfachvorgaben spiegeln die Stundenkontingente je Jahrgang
        for (int nummer = 5; nummer <= 10; nummer++) {
            int spalte = nummer - 4;
            for (Object[] zeile : KONTINGENTE) {
                int stunden = (int) zeile[spalte];
                if (stunden == 0) {
                    continue;
                }
                Pflichtfachvorgabe pf = new Pflichtfachvorgabe();
                pf.setAktiv(true);
                pf.setJahrgangsstufe(jahrgaenge.get(nummer));
                pf.setFach(faecher.get((String) zeile[0]));
                pf.setStundenanzahl(stunden);
                pflichtfachvorgabeRepository.save(pf);
            }
        }
    }

    private void blockregel(Rahmenbedingung rb, String fach, int vonJahrgang, int bisJahrgang) {
        for (int nummer = vonJahrgang; nummer <= bisJahrgang; nummer++) {
            Blockregel b = new Blockregel();
            b.setAktiv(true);
            b.setFach(faecher.get(fach));
            b.setJahrgangsstufe(jahrgaenge.get(nummer));
            blockregelRepository.save(b);
        }
    }

    private static final String[] VORNAMEN = {
            "Lena", "Jonas", "Mia", "Ben", "Emma", "Paul", "Hanna", "Luis",
            "Marie", "Finn", "Lea", "Noah", "Sophie", "Elias", "Clara", "Max",
            "Nele", "Tim", "Ida", "Jan", "Lina", "Erik", "Frieda", "Tom",
    };

    private static final String[] NACHNAMEN = {
            "Bauer", "Schulz", "Koch", "Peters", "Jansen", "Wolf", "Hansen",
            "Meier", "Voss", "Thomsen", "Petersen", "Nielsen", "Boysen",
            "Clausen", "Jessen", "Paulsen", "Ohlsen", "Kruse", "Rohde", "Sievers",
    };

    private void schueler() {
        boolean ehemaligerAngelegt = false;

        for (Lerngruppe g : klassen.values()) {
            int jahrgang = g.getJahrgangsstufe().getNummer();
            LocalDate eintritt = LocalDate.of(2026 - (jahrgang - 5), 8, 1);

            for (int i = 0; i < g.getSchueleranzahl(); i++) {
                String vorname = VORNAMEN[zufall.nextInt(VORNAMEN.length)];
                String nachname = NACHNAMEN[zufall.nextInt(NACHNAMEN.length)];

                Account a = new Account();
                a.setBenutzername(generieren.benutzername(vorname, nachname));
                a.setName(nachname);
                a.setVorname(vorname);
                a.setPasswort(startpasswort);
                a.setRolle(Rolle.SCHUELER);
                a.setAktiv(true);
                accountRepository.save(a);

                Schueler s = new Schueler();
                s.setAccount(a);
                s.setEintrittsdatum(eintritt);

                // genau ein ehemaliger Schüler, um den Zugriffsentzug zu testen
                if (!ehemaligerAngelegt && "10b".equals(g.getBezeichnung()) && i == 0) {
                    s.setAustrittsdatum(LocalDate.now().minusMonths(1));
                    a.setAktiv(false);
                    ehemaligerAngelegt = true;
                }
                s.setLerngruppe(g);
                schuelerRepository.save(s);
            }
        }
    }

    /**
     * Kleine, bewusst überschaubare Stundenplandaten für manuelle Tests.
     * Ereignisse und daraus entstehende Änderungen werden ausschließlich über die
     * produktiven Services erzeugt (siehe testdatenEreignisse()).
     */
    private void testdatenUnterrichtsstunden() {
        Account leitung = accountRepository.findByBenutzername("frauke-lindner")
                .orElseThrow(() -> new IllegalStateException("Planungskonto nicht gefunden"));

        Lerngruppe klasse5a = klassen.get("5a");
        Lerngruppe klasse10a = klassen.get("10a");

        Stundenplan plan5a = neuerStundenplan(klasse5a, leitung);
        Stundenplan plan10a = neuerStundenplan(klasse10a, leitung);

        // 5a: bewusst mehrere belegte Räume/Zeitslots für Ereignis- und
        // Stundenplantests.
        neueStunde(plan5a, klasse5a, Wochentag.MONTAG, 1, "D", "WEB", "R101");
        stunde5aMontag2 = neueStunde(plan5a, klasse5a, Wochentag.MONTAG, 2, "M", "MUE", "R101");
        neueStunde(plan5a, klasse5a, Wochentag.DIENSTAG, 1, "E", "KLE", "R101");
        stunde5aMittwoch3 = neueStunde(plan5a, klasse5a, Wochentag.MITTWOCH, 3, "SP", "HOF", "Halle A");

        // 10a: ein paar zusätzliche Einträge für unabhängige manuelle Tests.
        neueStunde(plan10a, klasse10a, Wochentag.MONTAG, 1, "PH", "KOE", "PH1");
        neueStunde(plan10a, klasse10a, Wochentag.DONNERSTAG, 5, "CH", "SCH", "CH1");
        neueStunde(plan10a, klasse10a, Wochentag.FREITAG, 2, "INF", "VOG", "INF1");
    }

    /**
     * Erzeugt Test-Ereignisse ausschließlich über EreignisService.anlegen().
     * Dadurch entstehen die zugehörigen Aenderungen genau wie im Produktivablauf.
     *
     * Die Szenarien überschneiden sich bewusst nicht auf derselben
     * Unterrichtsstunde
     * und demselben Datum, weil EreignisService bestehende Aenderungen für genau
     * diese
     * Kombination wiederverwendet.
     */
    private void testdatenEreignisse() {
        Account leitung = accountRepository.findByBenutzername("frauke-lindner")
                .orElseThrow(() -> new IllegalStateException("Planungskonto nicht gefunden"));

        Lerngruppe klasse5a = klassen.get("5a");

        // 1) Raumänderung: Dienstag, 1. Stunde in R101 (E/KLE/5a).
        // Daraus erzeugt der Service die passende Aenderung mit einem verfügbaren
        // Ersatzraum.
        EreignisDTO raumaenderung = new EreignisDTO();
        raumaenderung.setTyp(EreignisTyp.RAUMAENDERUNG);
        raumaenderung.setStartdatum(datumFuer(Wochentag.DIENSTAG));
        raumaenderung.setEnddatum(datumFuer(Wochentag.DIENSTAG));
        raumaenderung.setGanztag(false);
        raumaenderung.setStartzeitId(slots.get(0).getId());
        raumaenderung.setEndzeitId(slots.get(0).getId());
        raumaenderung.setAutorId(leitung.getId());
        raumaenderung.setRaumId(raeume.get("R101").getId());
        EreignisDTO raumaenderungAngelegt = ereignisService.anlegen(raumaenderung, leitung.getBenutzername());
        ereignisRaumaenderung = ereignisRepository.findById(raumaenderungAngelegt.getId()).orElseThrow();

        // 2) Ausfall: Montag, 1. Stunde von WEB.
        // Daraus erzeugt der Service eine Vertretung oder - falls keine verfügbar ist -
        // eine Freistunde.
        EreignisDTO ausfall = new EreignisDTO();
        ausfall.setTyp(EreignisTyp.AUSFALL);
        ausfall.setStartdatum(datumFuer(Wochentag.MONTAG));
        ausfall.setEnddatum(datumFuer(Wochentag.MONTAG));
        ausfall.setGanztag(false);
        ausfall.setStartzeitId(slots.get(0).getId());
        ausfall.setEndzeitId(slots.get(0).getId());
        ausfall.setAutorId(leitung.getId());
        ausfall.setLehrkraftId(lehrkraefte.get("WEB").getId());
        ausfall.setGrund("Krankheit");
        EreignisDTO ausfallAngelegt = ereignisService.anlegen(ausfall, leitung.getBenutzername());
        ereignisAusfall = ereignisRepository.findById(ausfallAngelegt.getId()).orElseThrow();

        // 3) Zusatzstunde: Mittwoch, 5. Stunde für 5a.
        // Die Aenderung entsteht ohne Ursprungs-Unterrichtsstunde.
        EreignisDTO zusatzstunde = new EreignisDTO();
        zusatzstunde.setTyp(EreignisTyp.ZUSATZSTUNDE);
        zusatzstunde.setStartdatum(datumFuer(Wochentag.MITTWOCH));
        zusatzstunde.setEnddatum(datumFuer(Wochentag.MITTWOCH));
        zusatzstunde.setGanztag(false);
        zusatzstunde.setStartzeitId(slots.get(4).getId());
        zusatzstunde.setAutorId(leitung.getId());
        zusatzstunde.setLehrkraftId(lehrkraefte.get("VOG").getId());
        zusatzstunde.setRaumId(raeume.get("INF1").getId());
        zusatzstunde.setLerngruppeId(klasse5a.getId());
        zusatzstunde.setFachId(faecher.get("INF").getId());
        EreignisDTO zusatzstundeAngelegt = ereignisService.anlegen(zusatzstunde, leitung.getBenutzername());
        ereignisZusatzstunde = ereignisRepository.findById(zusatzstundeAngelegt.getId()).orElseThrow();

        log.info("Beispiel-Ereignisse und die daraus erzeugten Aenderungen angelegt.");
    }

    private LocalDate datumFuer(Wochentag wochentag) {
        LocalDate montag = LocalDate.now().with(DayOfWeek.MONDAY);
        return montag.plusDays(switch (wochentag) {
            case MONTAG -> 0;
            case DIENSTAG -> 1;
            case MITTWOCH -> 2;
            case DONNERSTAG -> 3;
            case FREITAG -> 4;
        });
    }

    private Stundenplan neuerStundenplan(Lerngruppe lerngruppe, Account ersteller) {
        Stundenplan p = new Stundenplan();
        p.setSchuljahr("2026/2027");
        p.setHalbjahr(1);
        p.setStartdatum(LocalDate.of(2026, 8, 1));
        p.setEnddatum(LocalDate.of(2027, 1, 31));
        p.setStatus(PlanStatus.AKTIV);
        p.setErsteller(ersteller);
        p.setLerngruppe(lerngruppe);
        return stundenplanRepository.save(p);
    }

    private Unterrichtsstunde neueStunde(Stundenplan plan, Lerngruppe gruppe, Wochentag tag,
            int stundennummer, String fachKuerzel, String lehrkraftKuerzel,
            String raumBezeichnung) {
        Unterrichtsstunde u = new Unterrichtsstunde();
        u.setWochentag(tag);
        u.setZeitslot(slots.get(stundennummer - 1));
        u.setRaum(raeume.get(raumBezeichnung));
        u.setLehrkraft(lehrkraefte.get(lehrkraftKuerzel));
        u.setFach(faecher.get(fachKuerzel));
        u.setLerngruppe(gruppe);
        u.setStundenplan(plan);
        return unterrichtsstundeRepository.save(u);
    }

    /**
     * Zwei zusaetzliche, bereits bestaetigte Aenderungen fuer manuelle Tests,
     * jeweils vollstaendig befuellt (bis auf unterrichtsstunde, die hier null
     * bleibt), mit einem der in testdatenEreignisse() erzeugten Ereignisse als
     * Ausloeser-Stub.
     */
    private void testdatenAntonia() {
        Lerngruppe klasse5a = klassen.get("5a");

        // 1) Normale Aenderung: vollstaendiger Wechsel (Raum, Lehrkraft, Fach,
        // Zeitslot, Lerngruppe, Wochentag), bereits bestaetigt.
        Aenderung vertretung = new Aenderung();
        vertretung.setUnterrichtsstunde(stunde5aMontag2);
        vertretung.setDatum(datumFuer(Wochentag.MONTAG));
        vertretung.setZeitstempel(LocalDateTime.now());
        vertretung.setRaumNeu(raeume.get("R102"));
        vertretung.setLehrkraftNeu(lehrkraefte.get("KOE"));
        vertretung.setFachNeu(faecher.get("M"));
        vertretung.setZeitslotNeu(slots.get(1));
        vertretung.setLerngruppeNeu(klasse5a);
        vertretung.setWochentagNeu(Wochentag.MONTAG);
        vertretung.setFreistunde(false);
        vertretung.setAusloeser(ereignisAusfall);
        vertretung.setOffen(false); // bereits bestaetigt
        aenderungRepository.save(vertretung);

        // 2) Freistunde: ebenfalls vollstaendig befuellt, freistunde = true.
        Aenderung freistunde = new Aenderung();
        freistunde.setUnterrichtsstunde(stunde5aMittwoch3);
        freistunde.setDatum(datumFuer(Wochentag.MITTWOCH));
        freistunde.setZeitstempel(LocalDateTime.now());
        freistunde.setRaumNeu(raeume.get("Halle A"));
        freistunde.setLehrkraftNeu(lehrkraefte.get("HOF"));
        freistunde.setFachNeu(faecher.get("SP"));
        freistunde.setZeitslotNeu(slots.get(2));
        freistunde.setLerngruppeNeu(klasse5a);
        freistunde.setWochentagNeu(Wochentag.MITTWOCH);
        freistunde.setFreistunde(true);
        freistunde.setAusloeser(ereignisZusatzstunde);
        freistunde.setOffen(false); // bereits bestaetigt
        aenderungRepository.save(freistunde);

        log.info("Testdaten fuer Antonia (Aenderungen) angelegt.");
    }
}
