package de.cau.sp.stundenplanung.repository;

import de.cau.sp.stundenplanung.model.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AenderungRepository extends CrudRepository<Aenderung, Long> {
    @Query("SELECT a FROM Aenderung a " +
            "LEFT JOIN a.unterrichtsstunde u " +
            "WHERE a.offen = false AND (u.raum = :raum OR a.raumNeu = :raum) " +
            "AND a.datum BETWEEN :weekStart AND :weekEnd")
    List<Aenderung> findAbgeschlosseneByRaumAndCurrentWeek(
            @Param("raum") Raum raum,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd
    );

    default List<Aenderung> findAbgeschlosseneByRaumAndCurrentWeek(Raum raum) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY); // Montag dieser Woche
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY); // Sonntag + 1 Tag (für BETWEEN)
        return findAbgeschlosseneByRaumAndCurrentWeek(raum, weekStart, weekEnd);
    }

    @Query("SELECT a FROM Aenderung a WHERE a.offen = false AND a.unterrichtsstunde IN :stunden AND a.datum BETWEEN :start AND :end")
    @EntityGraph(attributePaths = {

            "unterrichtsstunde.zeitslot",
            "unterrichtsstunde.fach",
            "unterrichtsstunde.raum",
            "unterrichtsstunde.lehrkraft",
            "unterrichtsstunde.lehrkraft.kuerzel",
            "unterrichtsstunde.lerngruppe",
            "fachNeu",
            "raumNeu",
            "lehrkraftNeu",
            "lehrkraftNeu.kuerzel",
            "lehrkraftNeu.account",
            "ausloeser"
    })
    List<Aenderung> findAbgeschlosseneByUnterrichtsstundeInAndDatumBetween(
            @Param("stunden") List<Unterrichtsstunde> stunden,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT a FROM Aenderung a WHERE a.unterrichtsstunde IN :stunden AND a.datum BETWEEN :start AND :end")
    @EntityGraph(attributePaths = {

            "unterrichtsstunde.zeitslot",
            "unterrichtsstunde.fach",
            "unterrichtsstunde.raum",
            "unterrichtsstunde.lehrkraft",
            "unterrichtsstunde.lehrkraft.kuerzel",
            "unterrichtsstunde.lerngruppe",
            "fachNeu",
            "raumNeu",
            "lehrkraftNeu",
            "lehrkraftNeu.kuerzel",
            "lehrkraftNeu.account",
            "ausloeser"
    })
    List<Aenderung> findByUnterrichtsstundeInAndDatumBetween(
            @Param("stunden") List<Unterrichtsstunde> stunden,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT a FROM Aenderung a WHERE a.offen = false AND a.lehrkraftNeu = :lehrkraft AND a.datum BETWEEN :start AND :end")
    @EntityGraph(attributePaths = { //a.freistunde = false AND
            "unterrichtsstunde.zeitslot",
            "unterrichtsstunde.fach",
            "unterrichtsstunde.raum",
            "unterrichtsstunde.lehrkraft",
            "unterrichtsstunde.lerngruppe",
            "fachNeu",
            "raumNeu",
            "lehrkraftNeu",
            "lehrkraftNeu.account",
            "lerngruppeNeu",
            "zeitslotNeu",
            "ausloeser"
    })
    List<Aenderung> findAbgeschlosseneByLehrkraftNeuAndDatumBetween(
            @Param("lehrkraft") Lehrkraft lehrkraft,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    @Query("""
    SELECT a
    FROM Aenderung a
    WHERE a.offen = false
      AND a.lerngruppeNeu = :lerngruppeNeu
      AND a.datum BETWEEN :start AND :end
""")
    @EntityGraph(attributePaths = {
            "unterrichtsstunde.zeitslot",
            "unterrichtsstunde.fach",
            "unterrichtsstunde.raum",
            "unterrichtsstunde.lehrkraft",
            "unterrichtsstunde.lerngruppe",

            "fachNeu",
            "raumNeu",
            "lehrkraftNeu",
            "lehrkraftNeu.account",
            "lerngruppeNeu",
            "zeitslotNeu",

            "ausloeser"
    })
    List<Aenderung> findAbgeschlosseneByLerngruppeNeuAndDatumBetween(
            @Param("lerngruppeNeu") Lerngruppe klasse,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT a FROM Aenderung a WHERE a.wochentagNeu = :tag AND a.zeitslotNeu = :zeitslot " +
            "AND a.datum = :datum AND a.offen = false")
    List<Aenderung> findBestaetigteByWochentagNeuAndZeitslotNeuAndDatum(
            @Param("tag") Wochentag tag,
            @Param("zeitslot") Zeitslot zeitslot,
            @Param("datum") LocalDate datum);

    @Query("SELECT a FROM Aenderung a WHERE a.wochentagNeu = :tag AND a.zeitslotNeu = :zeitslot AND a.offen = false")
    List<Aenderung> findBestaetigteByWochentagNeuAndZeitslotNeu(@Param("tag") Wochentag tag, @Param("zeitslot") Zeitslot zeitslot);

    Optional<Aenderung> findByUnterrichtsstundeAndDatum(Unterrichtsstunde unterrichtsstunde, LocalDate datum);

    long countDistinctByOffen(boolean offen);
    @Query("""
    SELECT a
    FROM Aenderung a
    WHERE a.lerngruppeNeu = :lerngruppeNeu
      AND a.datum BETWEEN :start AND :end
""")
    @EntityGraph(attributePaths = {
            "unterrichtsstunde.zeitslot",
            "unterrichtsstunde.fach",
            "unterrichtsstunde.raum",
            "unterrichtsstunde.lehrkraft",
            "unterrichtsstunde.lerngruppe",

            "fachNeu",
            "raumNeu",
            "lehrkraftNeu",
            "lehrkraftNeu.account",
            "lerngruppeNeu",
            "zeitslotNeu",

            "ausloeser"
    })
    List<Aenderung> findByLerngruppeNeuAndDatumBetween(
            @Param("lerngruppeNeu") Lerngruppe klasse,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    @Query("SELECT a FROM Aenderung a WHERE a.wochentagNeu = :tag AND a.zeitslotNeu = :zeitslot " +
            "AND a.datum = :datum")
    List<Aenderung> findByWochentagNeuAndZeitslotNeuAndDatum(
            @Param("tag") Wochentag tag,
            @Param("zeitslot") Zeitslot zeitslot,
            @Param("datum") LocalDate datum);

}
