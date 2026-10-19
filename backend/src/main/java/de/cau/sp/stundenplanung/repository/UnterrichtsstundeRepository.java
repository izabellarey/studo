package de.cau.sp.stundenplanung.repository;

import de.cau.sp.stundenplanung.model.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UnterrichtsstundeRepository extends CrudRepository<Unterrichtsstunde, Long> {
    @EntityGraph(attributePaths = {
            "zeitslot",
            "fach",
            "lerngruppe",
            "lehrkraft",
            "lehrkraft.account"
    })
    List<Unterrichtsstunde> findByRaumAndStundenplanStatus(Raum raum, PlanStatus status);
    List<Unterrichtsstunde> findByLehrkraftAndStundenplan_Status(Lehrkraft lehrkraft, PlanStatus status);
    List<Unterrichtsstunde> findByLehrkraftAndStundenplanStatus(Lehrkraft lehrkraft, PlanStatus status);
    @Query("SELECT DISTINCT u.raum FROM Unterrichtsstunde u WHERE u.wochentag = :tag AND u.zeitslot = :zeitslot AND u.stundenplan.status = :status")
    List<Raum> findRaeumeByWochentagAndZeitslot(@Param("tag") Wochentag tag, @Param("zeitslot") Zeitslot zeitslot, @Param("status") PlanStatus status);

    @Query("SELECT DISTINCT u.lehrkraft FROM Unterrichtsstunde u " +
            "WHERE u.wochentag = :tag AND u.zeitslot = :zeitslot " +
            "AND u.lehrkraft.account.aktiv = true " +
            "AND u.stundenplan.status = :status")
    List<Lehrkraft> findLehrkraefteByWochentagAndZeitslot(
            @Param("tag") Wochentag tag,
            @Param("zeitslot") Zeitslot zeitslot,
            @Param("status") PlanStatus status); //nur aktive Lehrkräfte und unterrichtsstunden, deren zugehöriger stundenplan aktiv ist

    Optional<Unterrichtsstunde> findByWochentagAndZeitslotAndLerngruppe(
            Wochentag wochentag, Zeitslot zeitslot, Lerngruppe lerngruppe);
    Optional<Unterrichtsstunde> findByWochentagAndZeitslotAndLehrkraftAndStundenplan_Status(
            Wochentag wochentag, Zeitslot zeitslot, Lehrkraft lehrkraft, PlanStatus status);
    @Query("SELECT DISTINCT l FROM Lehrkraft l " +
            "WHERE :fach MEMBER OF l.faecher " +
            "AND l.account.aktiv = true " +
            "AND l NOT IN (" +
            "  SELECT u.lehrkraft FROM Unterrichtsstunde u " +
            "  WHERE u.wochentag = :wochentag AND u.zeitslot = :zeitslot " +
            "  AND u.stundenplan.status = :status" +
            ")")
    List<Lehrkraft> findVerfuegbareFachlehrkraefte(@Param("wochentag") Wochentag wochentag,
                                                   @Param("zeitslot") Zeitslot zeitslot,
                                                   @Param("fach") Fach fach,
                                                   @Param("status") PlanStatus status);

    @Query("SELECT DISTINCT l FROM Lehrkraft l " +
            "WHERE l.account.aktiv = true " +
            "AND l NOT IN (" +
            "  SELECT u.lehrkraft FROM Unterrichtsstunde u " +
            "  WHERE u.wochentag = :wochentag AND u.zeitslot = :zeitslot " +
            "  AND u.stundenplan.status = :status" +
            ")")
    List<Lehrkraft> findVerfuegbareLehrkraefte(@Param("wochentag") Wochentag wochentag,
                                               @Param("zeitslot") Zeitslot zeitslot,
                                               @Param("status") PlanStatus status);
    Optional<Unterrichtsstunde> findByWochentagAndZeitslotAndRaumAndStundenplan_Status(Wochentag wochentag, Zeitslot zeitslot, Raum raum, PlanStatus status);
    @Query("SELECT DISTINCT r FROM Raum r " +
            "WHERE r.art = :raumtyp " +
            "AND r NOT IN (" +
            "  SELECT u.raum FROM Unterrichtsstunde u " +
            "  WHERE u.wochentag = :wochentag AND u.zeitslot = :zeitslot " +
            "  AND u.stundenplan.status = :status" +
            ")")
    List<Raum> findVerfuegbareFachraeume(@Param("wochentag") Wochentag wochentag,
                                         @Param("zeitslot") Zeitslot zeitslot,
                                         @Param("raumtyp") Raumart raumtyp,
                                         @Param("status") PlanStatus status);

    @Query("SELECT DISTINCT r FROM Raum r " +
            "WHERE r NOT IN (" +
            "  SELECT u.raum FROM Unterrichtsstunde u " +
            "  WHERE u.wochentag = :wochentag AND u.zeitslot = :zeitslot " +
            "  AND u.stundenplan.status = :status" +
            ")")
    List<Raum> findVerfuegbareRaeume(@Param("wochentag") Wochentag wochentag,
                                     @Param("zeitslot") Zeitslot zeitslot,
                                     @Param("status") PlanStatus status);

}
