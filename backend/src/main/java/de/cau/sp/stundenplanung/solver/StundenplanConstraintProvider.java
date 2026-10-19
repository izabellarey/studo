package de.cau.sp.stundenplanung.solver;

import ai.timefold.solver.core.api.score.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import de.cau.sp.stundenplanung.model.Lehrerverfuegbarkeit;
import de.cau.sp.stundenplanung.model.Raumverfuegbarkeit;
import de.cau.sp.stundenplanung.model.VerfuegbarkeitsTyp;
import de.cau.sp.stundenplanung.model.Wochentag;
import de.cau.sp.stundenplanung.solver.model.PlanungsEinheit;

import java.util.List;
import java.util.stream.Collectors;


public class StundenplanConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory f) {
        return new Constraint[] {
                // harte Regeln: physikalisch unmöglich, durch die Wertebereiche ohnehin ausgeschlossen, oder in jedem getesteten Szenario erfüllbar geblieben
                eineLehrkraftJeFach(f),
                raumwechselInFolgestunden(f),
                klassenleitungUnterrichtet(f),
                zuVieleFachstundenAmTag(f),
                lehrkraftDoppelbelegung(f),
                lerngruppeDoppelbelegung(f),
                raumDoppelbelegung(f),
                lehrkraftGesperrt(f),
                raumGesperrt(f),
                fachqualifikation(f),
                raumeignung(f),
                klassenzimmerPflicht(f),
                raumKapazitaet(f),
                tagesgrenzeUeberschritten(f),

                // organisatorische Regeln: nur bei ueberlasteten Schulen rechnerisch erzwingbar, deshalb Kompromiss statt Scheitern
                deputatUeberschritten(f),

                // weiche Regeln
                freistundeLerngruppe(f),
                fachstundenNichtBenachbart(f),
                spaeterTagesbeginn(f),
                ungleicheTageslaenge(f),
                freistundeLehrkraft(f),
                randstunde(f),
                hauptfachAmNachmittag(f),
                bloeckeAnFolgetagen(f),
                wunschzeitVerfehlt(f),
                wunschzeitGetroffen(f),
                lehrkraftPraeferenzErfuellt(f),
                raumwechselAmTag(f),
        };
    }

    /** Eine Lehrkraft kann nicht zeitgleich in zwei Gruppen unterrichten. */
    Constraint lehrkraftDoppelbelegung(ConstraintFactory f) {
        return f.forEachUniquePair(PlanungsEinheit.class,
                        Joiners.equal(PlanungsEinheit::wochentag),
                        Joiners.equal(PlanungsEinheit::getLehrkraft))
                .filter(StundenplanConstraintProvider::ueberschneidetSich)
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Lehrkraft doppelt belegt");
    }

    /** Eine Lerngruppe kann nicht zeitgleich zwei Stunden haben. */
    Constraint lerngruppeDoppelbelegung(ConstraintFactory f) {
        return f.forEachUniquePair(PlanungsEinheit.class,
                        Joiners.equal(PlanungsEinheit::wochentag),
                        Joiners.equal(PlanungsEinheit::getLerngruppe))
                .filter(StundenplanConstraintProvider::ueberschneidetSich)
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Lerngruppe doppelt belegt");
    }

    /** Ein Raum kann nicht zeitgleich zwei Stunden aufnehmen. */
    Constraint raumDoppelbelegung(ConstraintFactory f) {
        return f.forEachUniquePair(PlanungsEinheit.class,
                        Joiners.equal(PlanungsEinheit::wochentag),
                        Joiners.equal(PlanungsEinheit::getRaum))
                .filter(StundenplanConstraintProvider::ueberschneidetSich)
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Raum doppelt belegt");
    }

    /** Keine Einplanung in einer gesperrten Zeit der Lehrkraft. */
    Constraint lehrkraftGesperrt(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .join(Lehrerverfuegbarkeit.class,
                        Joiners.equal(PlanungsEinheit::getLehrkraft, Lehrerverfuegbarkeit::getLehrkraft),
                        Joiners.equal(PlanungsEinheit::wochentag, Lehrerverfuegbarkeit::getWochentag))
                .filter((einheit, v) -> v.getTyp() == VerfuegbarkeitsTyp.GESPERRT
                        && einheit.belegt(v.getZeitslot().getStundennummer()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Lehrkraft zu dieser Zeit gesperrt");
    }

    /** Keine Einplanung in einer Sperrzeit des Raums. */
    Constraint raumGesperrt(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .join(Raumverfuegbarkeit.class,
                        Joiners.equal(PlanungsEinheit::getRaum, Raumverfuegbarkeit::getRaum),
                        Joiners.equal(PlanungsEinheit::wochentag, Raumverfuegbarkeit::getWochentag))
                .filter((einheit, v) -> v.getTyp() == VerfuegbarkeitsTyp.GESPERRT
                        && einheit.belegt(v.getZeitslot().getStundennummer()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Raum zu dieser Zeit gesperrt");
    }

    /** Eine Lehrkraft unterrichtet nur Fächer, für die sie ausgebildet ist. */
    Constraint fachqualifikation(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .filter(e -> !e.getLehrkraft().getFaecher().contains(e.getFach()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Lehrkraft unterrichtet fachfremd");
    }

    /** Der Raumtyp muss zum Fach passen (z.B. Sport in der Sporthalle). */
    Constraint raumeignung(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .filter(e -> e.getFach().getBenoetigterRaumtyp() != null
                        && e.getFach().getBenoetigterRaumtyp() != e.getRaum().getArt())
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Raumtyp passt nicht zum Fach");
    }

    /** Fächer ohne Fachraumbedarf finden im Klassenzimmer der Lerngruppe statt. */
    Constraint klassenzimmerPflicht(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .filter(e -> e.getFach().getBenoetigterRaumtyp() == null
                        && e.getLerngruppe().getKlassenzimmer() != null
                        && !e.getLerngruppe().getKlassenzimmer().equals(e.getRaum()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Nicht im Klassenzimmer der Lerngruppe");
    }

    /** Der Raum muss gross genug für die Lerngruppe sein. */
    Constraint raumKapazitaet(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .filter(e -> e.getRaum().getKapazitaet() < e.getLerngruppe().getSchueleranzahl())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        e -> e.getLerngruppe().getSchueleranzahl() - e.getRaum().getKapazitaet())
                .asConstraint("Raum zu klein");
    }

    /** Ein Fach wird in einer Lerngruppe die ganze Woche von derselben Lehrkraft unterrichtet. */
    Constraint eineLehrkraftJeFach(ConstraintFactory f) {
        return f.forEachUniquePair(PlanungsEinheit.class,
                        Joiners.equal(PlanungsEinheit::getLerngruppe),
                        Joiners.equal(PlanungsEinheit::getFach))
                .filter((a, b) -> !a.getLehrkraft().equals(b.getLehrkraft()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Wechselnde Lehrkraft im selben Fach");
    }

    /** Zwei Stunden desselben Fachs direkt hintereinander bleiben im selben Raum. */
    Constraint raumwechselInFolgestunden(ConstraintFactory f) {
        return f.forEachUniquePair(PlanungsEinheit.class,
                        Joiners.equal(PlanungsEinheit::getLerngruppe),
                        Joiners.equal(PlanungsEinheit::getFach),
                        Joiners.equal(PlanungsEinheit::wochentag))
                .filter((a, b) -> direktBenachbart(a, b) && !a.getRaum().equals(b.getRaum()))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Raumwechsel innerhalb einer Doppelstunde");
    }

    /** Die Tagesobergrenze der Jahrgangsstufe darf nicht überschritten werden. */
    Constraint tagesgrenzeUeberschritten(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .filter(e -> e.getMaxStundenTag() != null)
                .groupBy(PlanungsEinheit::getLerngruppe, PlanungsEinheit::wochentag,
                        ConstraintCollectors.toList())
                .filter((gruppe, tag, amTag) -> stundenzahl(amTag) > amTag.get(0).getMaxStundenTag())
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (gruppe, tag, amTag) -> stundenzahl(amTag) - amTag.get(0).getMaxStundenTag())
                .asConstraint("Zu viele Stunden an einem Tag");
    }

    /** Keine Lehrkraft bekommt mehr Stunden als ihr Deputat. */
    Constraint deputatUeberschritten(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .groupBy(PlanungsEinheit::getLehrkraft, ConstraintCollectors.toList())
                .filter((lehrkraft, stunden) -> stundenzahl(stunden) > lehrkraft.getDeputatsstunden())
                .penalize(HardMediumSoftScore.ONE_MEDIUM,
                        (lehrkraft, stunden) -> stundenzahl(stunden) - lehrkraft.getDeputatsstunden())
                .asConstraint("Deputat ueberschritten");
    }

    /** Die Klassenleitung muss ihre eigene Klasse mindestens eine Stunde unterrichten. */
    Constraint klassenleitungUnterrichtet(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .groupBy(PlanungsEinheit::getLerngruppe, ConstraintCollectors.toList())
                .filter((gruppe, stunden) -> gruppe.getKlassenleitung() != null
                        && stunden.stream().noneMatch(e ->
                                gruppe.getKlassenleitung().equals(e.getLehrkraft())))
                .penalize(HardMediumSoftScore.ONE_HARD)
                .asConstraint("Klassenleitung unterrichtet die eigene Klasse nicht");
    }

    /** Höchstens zwei Stunden desselben Fachs an einem Tag. */
    Constraint zuVieleFachstundenAmTag(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .groupBy(PlanungsEinheit::getLerngruppe, PlanungsEinheit::getFach,
                        ConstraintCollectors.toList())
                .filter((gruppe, fach, stunden) -> ueberzaehligeFachstunden(stunden) > 0)
                .penalize(HardMediumSoftScore.ONE_HARD,
                        (gruppe, fach, stunden) -> ueberzaehligeFachstunden(stunden))
                .asConstraint("Mehr als zwei Stunden eines Fachs am Tag");
    }

    /** Zwei Einheiten eines Fachs am selben Tag gehören direkt nebeneinander. */
    Constraint fachstundenNichtBenachbart(ConstraintFactory f) {
        return f.forEachUniquePair(PlanungsEinheit.class,
                        Joiners.equal(PlanungsEinheit::getLerngruppe),
                        Joiners.equal(PlanungsEinheit::getFach),
                        Joiners.equal(PlanungsEinheit::wochentag))
                .filter((a, b) -> !bildetBlock(a, b))
                .penalize(HardMediumSoftScore.ofSoft(100))
                .asConstraint("Fachstunden am selben Tag nicht als Block");
    }

    /** Der Unterrichtstag soll in der ersten Stunde beginnen, je später desto teurer. */
    Constraint spaeterTagesbeginn(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .groupBy(PlanungsEinheit::getLerngruppe, PlanungsEinheit::wochentag,
                        ConstraintCollectors.toList())
                .filter((gruppe, tag, amTag) -> ersteStunde(amTag) > 1)
                .penalize(HardMediumSoftScore.ONE_SOFT, (gruppe, tag, amTag) ->
                        120L * (ersteStunde(amTag) - 1) * (10 + amTag.get(0).prio()) / 10)
                .asConstraint("Tag beginnt nicht in der ersten Stunde");
    }

    /** Eine Klasse soll keine Freistunden im Plan haben. */
    Constraint freistundeLerngruppe(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .groupBy(PlanungsEinheit::getLerngruppe, PlanungsEinheit::wochentag,
                        ConstraintCollectors.toList())
                .filter((gruppe, tag, amTag) -> luecken(amTag) > 0)
                .penalize(HardMediumSoftScore.ONE_SOFT, (gruppe, tag, amTag) -> {
                    long l = luecken(amTag);
                    return 800 * l * l * (10 + amTag.get(0).prio()) / 10;
                })
                .asConstraint("Freistunde einer Lerngruppe");
    }

    /** Die Tage einer Lerngruppe sollen etwa gleich lang sein. */
    Constraint ungleicheTageslaenge(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .groupBy(PlanungsEinheit::getLerngruppe, ConstraintCollectors.toList())
                .filter((gruppe, alle) -> tagesabweichung(alle) > 0)
                .penalize(HardMediumSoftScore.ofSoft(25), (gruppe, alle) -> tagesabweichung(alle))
                .asConstraint("Ungleiche Tageslaengen");
    }

    /** Löcher im Tagesplan einer Lehrkraft. */
    Constraint freistundeLehrkraft(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .groupBy(PlanungsEinheit::getLehrkraft, PlanungsEinheit::wochentag,
                        ConstraintCollectors.toList())
                .filter((lehrkraft, tag, amTag) -> luecken(amTag) > 0)
                .penalize(HardMediumSoftScore.ofSoft(25), (lehrkraft, tag, amTag) -> luecken(amTag))
                .asConstraint("Freistunde einer Lehrkraft");
    }

    /** Die letzten beiden Stunden des Tages möglichst freihalten, nach Priorität gewichtet. */
    Constraint randstunde(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .filter(e -> e.letzteStunde() >= e.ersteRandstunde())
                .penalize(HardMediumSoftScore.ONE_SOFT,
                        e -> stundenAb(e, e.ersteRandstunde()) * 10L * (10 + e.prio()) / 10)
                .asConstraint("Unterricht in einer Randstunde");
    }

    /** Hauptfächer gehören in den Vormittag, danach wird es zunehmend teurer. */
    Constraint hauptfachAmNachmittag(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .filter(e -> e.getFach().isHauptfach() && e.letzteStunde() > e.letzteVormittagsstunde())
                .penalize(HardMediumSoftScore.ONE_SOFT, StundenplanConstraintProvider::nachmittagsstrafe)
                .asConstraint("Hauptfach am Nachmittag");
    }

    /** Die Einheiten eines Fachs sollen über die Woche verteilt liegen, nicht an Folgetagen. */
    Constraint bloeckeAnFolgetagen(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .filter(e -> e.getDauer() > 1)
                .groupBy(PlanungsEinheit::getLerngruppe, PlanungsEinheit::getFach,
                        ConstraintCollectors.toList())
                .filter((gruppe, fach, stunden) -> folgetagePaare(stunden) > 0)
                .penalize(HardMediumSoftScore.ofSoft(15),
                        (gruppe, fach, stunden) -> folgetagePaare(stunden))
                .asConstraint("Fach an aufeinanderfolgenden Tagen");
    }

    /** Bevorzugte Zeiten der Lehrkraft sollen möglichst getroffen werden. */
    Constraint wunschzeitVerfehlt(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .join(Lehrerverfuegbarkeit.class,
                        Joiners.equal(PlanungsEinheit::getLehrkraft, Lehrerverfuegbarkeit::getLehrkraft),
                        Joiners.equal(PlanungsEinheit::wochentag, Lehrerverfuegbarkeit::getWochentag))
                .filter((einheit, v) -> v.getTyp() == VerfuegbarkeitsTyp.NICHT_BEVORZUGT
                        && einheit.belegt(v.getZeitslot().getStundennummer()))
                .penalize(HardMediumSoftScore.ofSoft(20))
                .asConstraint("Unerwuenschte Zeit der Lehrkraft");
    }

    /** Als bevorzugt gemeldete Zeiten der Lehrkraft werden belohnt, spiegelbildlich zur Strafe. */
    Constraint wunschzeitGetroffen(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .join(Lehrerverfuegbarkeit.class,
                        Joiners.equal(PlanungsEinheit::getLehrkraft, Lehrerverfuegbarkeit::getLehrkraft),
                        Joiners.equal(PlanungsEinheit::wochentag, Lehrerverfuegbarkeit::getWochentag))
                .filter((einheit, v) -> v.getTyp() == VerfuegbarkeitsTyp.BEVORZUGT
                        && einheit.belegt(v.getZeitslot().getStundennummer()))
                .reward(HardMediumSoftScore.ofSoft(20))
                .asConstraint("Bevorzugte Zeit der Lehrkraft getroffen");
    }

    /** Belohnung: Eine Lehrkraft soll ihre bevorzugte Klasse unterrichten. */
    Constraint lehrkraftPraeferenzErfuellt(ConstraintFactory f) {
        return f.forEach(PlanungsEinheit.class)
                .filter(e -> e.getLehrkraft().getPraeferenzen() != null
                        && e.getLehrkraft().getPraeferenzen().contains(e.getLerngruppe()))
                .reward(HardMediumSoftScore.ofSoft(20))
                .asConstraint("Lehrkraft unterrichtet bevorzugte Klasse");
    }

    /** Raumwechsel zwischen unmittelbar aufeinanderfolgenden Einheiten einer Lerngruppe. */
    Constraint raumwechselAmTag(ConstraintFactory f) {
        return f.forEachUniquePair(PlanungsEinheit.class,
                        Joiners.equal(PlanungsEinheit::getLerngruppe),
                        Joiners.equal(PlanungsEinheit::wochentag))
                .filter((a, b) -> direktBenachbart(a, b) && !a.getRaum().equals(b.getRaum()))
                .penalize(HardMediumSoftScore.ofSoft(3))
                .asConstraint("Raumwechsel zwischen Folgestunden");
    }

    /** Belegen zwei Einheiten am selben Tag dieselbe Stunde? */
    private static boolean ueberschneidetSich(PlanungsEinheit a, PlanungsEinheit b) {
        return a.stunde() <= b.letzteStunde() && b.stunde() <= a.letzteStunde();
    }

    /** Schliesst die spätere Einheit unmittelbar an die frühere an? */
    private static boolean direktBenachbart(PlanungsEinheit a, PlanungsEinheit b) {
        PlanungsEinheit frueh = a.stunde() <= b.stunde() ? a : b;
        PlanungsEinheit spaet = frueh == a ? b : a;
        return frueh.letzteStunde() + 1 == spaet.stunde();
    }

    /** Benachbart und auf einer ungeraden Nummer beginnend, also ohne Pause dazwischen. */
    private static boolean bildetBlock(PlanungsEinheit a, PlanungsEinheit b) {
        return direktBenachbart(a, b) && Math.min(a.stunde(), b.stunde()) % 2 == 1;
    }

    /** Wie viele Unterrichtsstunden diese Einheiten zusammen belegen. */
    private static long stundenzahl(List<PlanungsEinheit> einheiten) {
        return einheiten.stream().mapToLong(PlanungsEinheit::getDauer).sum();
    }

    /** Wie viele belegte Stunden einer Einheit ab der angegebenen Nummer liegen. */
    private static long stundenAb(PlanungsEinheit e, int ab) {
        return Math.max(0, e.letzteStunde() - Math.max(e.stunde(), ab) + 1);
    }

    /** Sechs Punkte je Stunde nach dem Vormittag, mit dem Abstand dazu steigend. */
    private static long nachmittagsstrafe(PlanungsEinheit e) {
        int vormittag = e.letzteVormittagsstunde();
        long punkte = 0;
        for (int h = Math.max(e.stunde(), vormittag + 1); h <= e.letzteStunde(); h++) {
            punkte += 6L * (h - vormittag);
        }
        return punkte;
    }

    /** Die Stundennummer, in der der Tag beginnt. */
    private static int ersteStunde(List<PlanungsEinheit> amTag) {
        return amTag.stream().mapToInt(PlanungsEinheit::stunde).min().orElseThrow();
    }

    /** Leere Stunden zwischen der ersten und der letzten belegten Stunde eines Tages. */
    private static long luecken(List<PlanungsEinheit> amTag) {
        int erste = ersteStunde(amTag);
        int letzte = amTag.stream().mapToInt(PlanungsEinheit::letzteStunde).max().orElseThrow();
        return (letzte - erste + 1L) - stundenzahl(amTag);
    }

    /** Summe der Stunden, die an einem Tag über der zweiten desselben Fachs liegen. */
    private static long ueberzaehligeFachstunden(List<PlanungsEinheit> stunden) {
        return stunden.stream()
                .collect(Collectors.groupingBy(PlanungsEinheit::wochentag))
                .values().stream()
                .mapToLong(amTag -> Math.max(0, stundenzahl(amTag) - 2))
                .sum();
    }

    /** Summe der Abweichungen vom gleichmäßig verteilten Tagespensum. */
    private static long tagesabweichung(List<PlanungsEinheit> alle) {
        long ideal = Math.round(stundenzahl(alle) / (double) Wochentag.values().length);
        var jeTag = alle.stream().collect(Collectors.groupingBy(PlanungsEinheit::wochentag));
        long abweichung = 0;
        for (Wochentag tag : Wochentag.values()) {
            long amTag = jeTag.containsKey(tag) ? stundenzahl(jeTag.get(tag)) : 0;
            abweichung += Math.abs(amTag - ideal);
        }
        return abweichung;
    }

    /** Paare benachbarter Wochentage, an denen dasselbe Fach liegt. */
    private static long folgetagePaare(List<PlanungsEinheit> stunden) {
        List<Integer> tage = stunden.stream().map(e -> e.wochentag().ordinal())
                .distinct().sorted().toList();
        long paare = 0;
        for (int i = 1; i < tage.size(); i++) {
            if (tage.get(i) - tage.get(i - 1) == 1) {
                paare++;
            }
        }
        return paare;
    }
}