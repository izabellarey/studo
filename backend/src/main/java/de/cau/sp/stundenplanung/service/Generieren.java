package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.model.*;
import org.springframework.stereotype.Service;

import de.cau.sp.stundenplanung.repository.AccountRepository;
import de.cau.sp.stundenplanung.repository.LehrkraftRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class Generieren {

    private final AccountRepository accountRepository;
    private final LehrkraftRepository lehrkraftRepository;

    public String benutzername(String vorname, String name) {
        String basis = vorname.toLowerCase() + "-" + name.toLowerCase();
        String kandidat = basis;
        int zaehler = 2;

        while (accountRepository.findByBenutzername(kandidat).isPresent()) {
            kandidat = basis + zaehler;
            zaehler++;
        }

        return kandidat;
    }

    public String kuerzel(String vorname, String name) {
        String basis = "" + Character.toUpperCase(vorname.charAt(0))
                          + Character.toUpperCase(name.charAt(0));
        String kandidat = basis;
        int zaehler = 2;

        while (lehrkraftRepository.findByKuerzel(kandidat).isPresent()) {
            kandidat = basis + zaehler;
            zaehler++;
        }

        return kandidat;
    }

    private String stundeninfo(Aenderung aenderung) {
        return aenderung.getZeitslotNeu().getStundennummer() + ". Stunde"
                + " (" + aenderung.getWochentagNeu() + ", " + aenderung.getZeitslotNeu().getStart() + " Uhr)";
    }

    private boolean istVertretung(Aenderung aenderung) {
        Unterrichtsstunde stunde = aenderung.getUnterrichtsstunde();
        if (stunde == null) {
            return false; // keine Ursprungsstunde -> keine Vertretung moeglich (das ist der Zusatzstunden-Fall)
        }
        Lehrkraft original = stunde.getLehrkraft();
        Lehrkraft neu = aenderung.getLehrkraftNeu();
        return neu != null && !neu.equals(original) && !aenderung.isFreistunde();
    }

// ---------- LEHRKRAFT ----------

    public String aenderungLehrkraftTitelGenerieren(Aenderung aenderung) {
        EreignisTyp typ = aenderung.getAusloeser().getTyp();

        if (aenderung.isFreistunde()) {
            return "Stunde fällt aus: " + aenderung.getFachNeu().getKuerzel()
                    + ", " + aenderung.getZeitslotNeu().getStundennummer() + ". Stunde ("
                    + aenderung.getLerngruppeNeu().getBezeichnung() + ")";
        }
        if (istVertretung(aenderung)) {
            return "Vertretung: " + aenderung.getFachNeu().getKuerzel() + ", "
                    + aenderung.getZeitslotNeu().getStundennummer() + ". Stunde ("
                    + aenderung.getLerngruppeNeu().getBezeichnung() + ")";
        }
        return switch (typ) {
            case RAUMAENDERUNG -> "Raumänderung: " + aenderung.getFachNeu().getKuerzel()
                    + " jetzt in " + aenderung.getRaumNeu().getBezeichnung();
            case ZUSATZSTUNDE -> "Zusätzliche Stunde: " + aenderung.getFachNeu().getKuerzel()
                    + " (" + aenderung.getLerngruppeNeu().getBezeichnung() + ")";
            case AUSFALL -> "Stunde entfällt"; // sollte durch Freistunde-/Vertretungs-Zweig oben bereits abgedeckt sein
        };
    }

    public String aenderungLehrkraftBeschreibungGenerieren(Aenderung aenderung) {
        Unterrichtsstunde stunde = aenderung.getUnterrichtsstunde();
        StringBuilder sb = new StringBuilder();
        sb.append("Am ").append(aenderung.getDatum()).append(", ").append(stundeninfo(aenderung)).append(": ");

        if (aenderung.isFreistunde()) {
            sb.append("Der ursprünglich geplante Unterricht (")
                    .append(aenderung.getFachNeu().getKuerzel()).append(", ")
                    .append(aenderung.getLerngruppeNeu().getBezeichnung())
                    .append(") entfällt ersatzlos.");
            if (aenderung.getAusloeser() instanceof Ausfall ausfall && ausfall.getGrund() != null) {
                sb.append(" Grund: ").append(ausfall.getGrund()).append(".");
            }
        } else if (istVertretung(aenderung)) {
            sb.append("Sie übernehmen die Vertretung für ")
                    .append(stunde.getLehrkraft().getKuerzel()).append(" in ")
                    .append(aenderung.getFachNeu().getKuerzel())
                    .append(" bei ").append(aenderung.getLerngruppeNeu().getBezeichnung())
                    .append(", Raum ").append(aenderung.getRaumNeu().getBezeichnung())
                    .append(".");
        } else if (stunde == null) {
            sb.append("Neu eingerichtete Stunde: ")
                    .append(aenderung.getFachNeu().getKuerzel())
                    .append(" bei ").append(aenderung.getLerngruppeNeu().getBezeichnung())
                    .append(" mit ").append(aenderung.getLehrkraftNeu().getKuerzel())
                    .append(", Raum ").append(aenderung.getRaumNeu().getBezeichnung())
                    .append(".");
        } else {
            sb.append("Raum geändert von ").append(stunde.getRaum().getBezeichnung())
                    .append(" zu ").append(aenderung.getRaumNeu().getBezeichnung())
                    .append(" (").append(aenderung.getFachNeu().getKuerzel()).append(", ")
                    .append(aenderung.getLerngruppeNeu().getBezeichnung()).append(").");
        }

        sb.append(aenderung.isOffen() ? " (Noch nicht bestätigt)" : " (Bestätigt)");
        return sb.toString();
    }

// ---------- SCHUELER ----------

    public String aenderungSchuelerTitelGenerieren(Aenderung aenderung) {
        EreignisTyp typ = aenderung.getAusloeser().getTyp();

        if (aenderung.isFreistunde()) {
            return aenderung.getFachNeu().getKuerzel() + " fällt aus ("
                    + aenderung.getZeitslotNeu().getStundennummer() + ". Stunde)";
        }
        return switch (typ) {
            case RAUMAENDERUNG -> "Raumwechsel: " + aenderung.getFachNeu().getKuerzel()
                    + " jetzt in " + aenderung.getRaumNeu().getBezeichnung();
            case AUSFALL -> istVertretung(aenderung)
                    ? "Vertretung in " + aenderung.getFachNeu().getKuerzel()
                    : aenderung.getFachNeu().getKuerzel() + " fällt aus";
            case ZUSATZSTUNDE -> "Zusätzliche Stunde: " + aenderung.getFachNeu().getKuerzel();
        };
    }

    public String aenderungSchuelerBeschreibungGenerieren(Aenderung aenderung) {
        Unterrichtsstunde stunde = aenderung.getUnterrichtsstunde();
        StringBuilder sb = new StringBuilder();
        sb.append("Am ").append(aenderung.getDatum()).append(", ").append(stundeninfo(aenderung)).append(": ");

        if (aenderung.isFreistunde()) {
            sb.append(aenderung.getFachNeu().getKuerzel()).append(" bei ")
                    .append(stunde.getLehrkraft().getKuerzel()).append(" entfällt.");
        } else if (istVertretung(aenderung)) {
            sb.append(aenderung.getFachNeu().getKuerzel())
                    .append(" wird vertretungsweise von ")
                    .append(aenderung.getLehrkraftNeu().getKuerzel())
                    .append(" unterrichtet, Raum ")
                    .append(aenderung.getRaumNeu().getBezeichnung())
                    .append(".");
        } else if (stunde == null) {
            sb.append(aenderung.getFachNeu().getKuerzel())
                    .append(" (neu, mit ").append(aenderung.getLehrkraftNeu().getKuerzel())
                    .append(") findet statt in ").append(aenderung.getRaumNeu().getBezeichnung())
                    .append(".");
        } else {
            sb.append(aenderung.getFachNeu().getKuerzel()).append(" findet jetzt statt in ")
                    .append(aenderung.getRaumNeu().getBezeichnung())
                    .append(" (statt in ").append(stunde.getRaum().getBezeichnung()).append(").");
        }
        return sb.toString();
    }

// ---------- PLANUNG ----------

    public String aenderungPlanerTitelGenerieren(Aenderung aenderung) {
        Unterrichtsstunde stunde = aenderung.getUnterrichtsstunde();
        EreignisTyp typ = aenderung.getAusloeser().getTyp();
        String lehrkraftKuerzel = stunde != null
                ? stunde.getLehrkraft().getKuerzel()
                : aenderung.getLehrkraftNeu().getKuerzel();

        return typ + ": " + aenderung.getFachNeu().getKuerzel() + " / "
                + aenderung.getLerngruppeNeu().getBezeichnung() + " / "
                + lehrkraftKuerzel
                + (aenderung.isOffen() ? " – offen" : " – bestätigt");
    }

    public String aenderungPlanerBeschreibungGenerieren(Aenderung aenderung) {
        Unterrichtsstunde stunde = aenderung.getUnterrichtsstunde();
        StringBuilder sb = new StringBuilder();
        sb.append(aenderung.getAusloeser().getTyp()).append(" am ").append(aenderung.getDatum())
                .append(", ").append(stundeninfo(aenderung));

        if (stunde != null) {
            sb.append(", ursprünglich: ").append(stunde.getFach().getKuerzel())
                    .append(" / ").append(stunde.getRaum().getBezeichnung())
                    .append(" / ").append(stunde.getLehrkraft().getKuerzel())
                    .append(" (").append(stunde.getLerngruppe().getBezeichnung()).append("). ");
        } else {
            sb.append(", neu eingerichtete Stunde (keine Ursprungsstunde). ");
        }

        sb.append("Neu: ").append(aenderung.getFachNeu().getKuerzel()).append(" / ")
                .append(aenderung.getRaumNeu().getBezeichnung()).append(" / ")
                .append(aenderung.getLehrkraftNeu().getKuerzel());

        if (aenderung.isFreistunde()) {
            sb.append(", Freistunde/Ausfall.");
            if (aenderung.getAusloeser() instanceof Ausfall ausfall && ausfall.getGrund() != null) {
                sb.append(" Grund: ").append(ausfall.getGrund()).append(".");
            }
        } else {
            sb.append(".");
        }

        sb.append(" Status: ").append(aenderung.isOffen() ? "noch zu bestätigen." : "bestätigt.");
        sb.append(" Erstellt: ").append(aenderung.getZeitstempel()).append(".");
        return sb.toString();
    }

}