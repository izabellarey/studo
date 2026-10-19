package de.cau.sp.stundenplanung.repository;

import de.cau.sp.stundenplanung.model.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.query.Param;
import de.cau.sp.stundenplanung.model.Lehrerverfuegbarkeit;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Zeitslot;

public interface LehrerverfuegbarkeitRepository extends CrudRepository<Lehrerverfuegbarkeit, Long> {
    List<Lehrerverfuegbarkeit> findByLehrkraft(Lehrkraft lehrkraft);
    List<Lehrerverfuegbarkeit> findByZeitslot(Zeitslot zeitslot);
    @Query("""
        SELECT v.lehrkraft.id FROM Lehrerverfuegbarkeit v
        WHERE v.wochentag = :tag AND v.zeitslot = :zeitslot AND v.typ = :typ
        AND v.startdatum <= :datum AND v.enddatum >= :datum
        AND v.lehrkraft.account.aktiv = true
        """)
    List<Long> findLehrkraftIdsByWochentagAndZeitslotAndDatumAndTyp(
            @Param("tag") Wochentag tag,
            @Param("zeitslot") Zeitslot zeitslot,
            @Param("datum") LocalDate datum,
            @Param("typ") VerfuegbarkeitsTyp typ);
}
