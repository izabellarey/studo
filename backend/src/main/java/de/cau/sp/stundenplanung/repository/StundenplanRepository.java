package de.cau.sp.stundenplanung.repository;

import de.cau.sp.stundenplanung.model.Lerngruppe;
import de.cau.sp.stundenplanung.model.PlanStatus;
import de.cau.sp.stundenplanung.model.Unterrichtsstunde;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.PlanStatus;
import de.cau.sp.stundenplanung.model.Stundenplan;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface StundenplanRepository extends CrudRepository<Stundenplan, Long> {
    List<Stundenplan> findByGenerationId(Long generationId);
    List<Stundenplan> findByLerngruppe_IdAndGenerationId(Long lerngruppeId, Long generationId);

    @EntityGraph(attributePaths = {
            "unterrichtsstunden",
            "unterrichtsstunden.zeitslot",
            "unterrichtsstunden.fach",
            "unterrichtsstunden.raum",
            "unterrichtsstunden.lehrkraft",
            "unterrichtsstunden.lehrkraft.kuerzel",
            "unterrichtsstunden.lerngruppe"
    })
    Optional<Stundenplan> findByLerngruppeAndStatus(Lerngruppe lerngruppe, PlanStatus status);

    List<Stundenplan> findByStatus(PlanStatus status);

    @Query("SELECT DISTINCT s FROM Stundenplan s LEFT JOIN FETCH s.unterrichtsstunden WHERE s.status = :status")
    List<Stundenplan> findByStatusWithStunden(@Param("status") PlanStatus status);

    Optional<Stundenplan> findByLerngruppe_IdAndGenerationIdAndVariante(Long lerngruppeId, Long generationId, Integer variante);
}
