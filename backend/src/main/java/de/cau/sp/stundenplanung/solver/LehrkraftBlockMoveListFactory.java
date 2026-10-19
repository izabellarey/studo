package de.cau.sp.stundenplanung.solver;

import ai.timefold.solver.core.impl.heuristic.selector.move.factory.MoveListFactory;
import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningSolutionMetaModel;
import ai.timefold.solver.core.preview.api.domain.metamodel.PlanningVariableMetaModel;
import ai.timefold.solver.core.preview.api.move.Move;
import ai.timefold.solver.core.preview.api.move.builtin.Moves;

import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Lerngruppe;
import de.cau.sp.stundenplanung.solver.model.PlanungsEinheit;
import de.cau.sp.stundenplanung.solver.model.Stundenplanproblem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Erzeugt Züge, die alle Unterrichtsstunden eines kompletten Fach-Klasse-Blocks auf einmal einer
 * anderen Lehrkraft zuweisen.
 * Ohne diesen Zug kann der Solver einen Deputat-Ausgleich zwischen zwei Lehrkräften oft nicht
 * finden: Ändert er nur eine einzelne Unterrichtsstunde, haben die übrigen Stunden desselben
 * Fach-Klasse-Blocks weiterhin die alte Lehrkraft – das verletzt sofort den harten Constraint
 * "eineLehrkraftJeFach". Ein einzelner Zwischenschritt sieht für lokale Suche deshalb immer
 * schlechter aus, egal wie gut das Ergebnis danach wäre, und wird nie freiwillig gegangen.
 * Dieser Zug ändert alle betroffenen Stunden gleichzeitig, sodass der Zwischenschritt entfällt.
 */
public class LehrkraftBlockMoveListFactory implements MoveListFactory<Stundenplanproblem> {

    private static final PlanningVariableMetaModel<Stundenplanproblem, PlanungsEinheit, Lehrkraft> LEHRKRAFT_VARIABLE =
            PlanningSolutionMetaModel.of(Stundenplanproblem.class, PlanungsEinheit.class)
                    .genuineEntity(PlanungsEinheit.class)
                    .basicVariable("lehrkraft", Lehrkraft.class);

    @Override
    public List<? extends Move<Stundenplanproblem>> createMoveList(Stundenplanproblem solution) {
        Map<Lerngruppe, Map<Fach, List<PlanungsEinheit>>> bloecke = new LinkedHashMap<>();
        for (PlanungsEinheit einheit : solution.getEinheiten()) {
            bloecke.computeIfAbsent(einheit.getLerngruppe(), k -> new LinkedHashMap<>())
                    .computeIfAbsent(einheit.getFach(), k -> new ArrayList<>())
                    .add(einheit);
        }

        List<Move<Stundenplanproblem>> zuege = new ArrayList<>();
        for (Map<Fach, List<PlanungsEinheit>> jeFach : bloecke.values()) {
            for (List<PlanungsEinheit> block : jeFach.values()) {
                zuege.addAll(zuegeFuerBlock(block));
            }
        }
        return zuege;
    }

    private List<Move<Stundenplanproblem>> zuegeFuerBlock(List<PlanungsEinheit> block) {
        List<Move<Stundenplanproblem>> zuege = new ArrayList<>();
        if (block.isEmpty()) {
            return zuege;
        }
        for (Lehrkraft kandidat : block.get(0).getMoeglicheLehrkraefte()) {
            boolean hatSchonAlle = block.stream().allMatch(e -> kandidat.equals(e.getLehrkraft()));
            if (hatSchonAlle) {
                continue;
            }
            List<Move<Stundenplanproblem>> teilzuege = new ArrayList<>();
            for (PlanungsEinheit einheit : block) {
                teilzuege.add(Moves.change(LEHRKRAFT_VARIABLE, einheit, kandidat));
            }
            zuege.add(Moves.compose(teilzuege));
        }
        return zuege;
    }
}