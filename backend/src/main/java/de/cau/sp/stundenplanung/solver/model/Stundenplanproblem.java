package de.cau.sp.stundenplanung.solver.model;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.score.HardMediumSoftScore;

import de.cau.sp.stundenplanung.model.Lehrerverfuegbarkeit;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Raum;
import de.cau.sp.stundenplanung.model.Raumverfuegbarkeit;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
public class Stundenplanproblem {

    @ProblemFactCollectionProperty
    private List<Lehrkraft> lehrkraefte;

    @ProblemFactCollectionProperty
    private List<Raum> raeume;

    @ProblemFactCollectionProperty
    private List<Zeitfenster> zeitfenster;

    @ProblemFactCollectionProperty
    private List<Lehrerverfuegbarkeit> lehrerverfuegbarkeiten;

    @ProblemFactCollectionProperty
    private List<Raumverfuegbarkeit> raumverfuegbarkeiten;

    @PlanningEntityCollectionProperty
    private List<PlanungsEinheit> einheiten;

    @PlanningScore
    private HardMediumSoftScore score;

    public Stundenplanproblem(List<Lehrkraft> lehrkraefte,
                              List<Raum> raeume,
                              List<Zeitfenster> zeitfenster,
                              List<Lehrerverfuegbarkeit> lehrerverfuegbarkeiten,
                              List<Raumverfuegbarkeit> raumverfuegbarkeiten,
                              List<PlanungsEinheit> einheiten) {
        this.lehrkraefte = lehrkraefte;
        this.raeume = raeume;
        this.zeitfenster = zeitfenster;
        this.lehrerverfuegbarkeiten = lehrerverfuegbarkeiten;
        this.raumverfuegbarkeiten = raumverfuegbarkeiten;
        this.einheiten = einheiten;
    }
}