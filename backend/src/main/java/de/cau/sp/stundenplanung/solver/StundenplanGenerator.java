package de.cau.sp.stundenplanung.solver;

import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.constructionheuristic.ConstructionHeuristicPhaseConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.MoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.composite.UnionMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.factory.MoveListFactoryConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.ChangeMoveSelectorConfig;
import ai.timefold.solver.core.config.heuristic.selector.move.generic.SwapMoveSelectorConfig;
import ai.timefold.solver.core.config.localsearch.LocalSearchPhaseConfig;
import ai.timefold.solver.core.config.phase.PhaseConfig;
import ai.timefold.solver.core.config.solver.PreviewFeature;
import ai.timefold.solver.core.config.solver.SolverConfig;

import de.cau.sp.stundenplanung.solver.model.Stundenplanproblem;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StundenplanGenerator {

    /** Zufallsstartwerte, die pro Generierung parallel ausprobiert werden. */
    private static final long[] SEEDS = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L};

    /** Wie viele der erfolgreichen Läufe (beste Punktzahl zuerst) gespeichert werden. */
    private static final int ANZAHL_VARIANTEN = 3;

    private final StundenplanproblemLader lader;
    private final StundenplanSchreiber schreiber;
    private final SolverConfig solverConfig;

    public record LaufErgebnis(long variante, long dauerMs, String score, boolean gespeichert) {
    }

    private record LaufAusgang(long seed, Stundenplanproblem loesung, long dauerMs, boolean gueltig) {
    }

    private record LaufResultat(Stundenplanproblem loesung, long dauerMs) {
    }

    @PostConstruct
    void ergaenzeBlockverschiebungszug() {
        if (solverConfig.getPhaseConfigList() != null) {
            return; // schon konfiguriert (z.B. per solverConfig.xml) - nicht ueberschreiben
        }
        solverConfig.withPreviewFeature(PreviewFeature.NEIGHBORHOODS);

        List<MoveSelectorConfig> zuege = new ArrayList<>();
        zuege.add(new ChangeMoveSelectorConfig());
        zuege.add(new SwapMoveSelectorConfig());
        zuege.add(new SwapMoveSelectorConfig().withVariableNameIncludes("zeitfenster"));
        zuege.add(new MoveListFactoryConfig().withMoveListFactoryClass(LehrkraftBlockMoveListFactory.class));
        UnionMoveSelectorConfig alleZuege = new UnionMoveSelectorConfig(zuege);

        List<PhaseConfig> phasen = new ArrayList<>();
        phasen.add(new ConstructionHeuristicPhaseConfig());
        phasen.add(new LocalSearchPhaseConfig().withMoveSelectorConfig(alleZuege));
        solverConfig.setPhaseConfigList(phasen);
    }

    public List<LaufErgebnis> generieren(Long generationId) {
        List<LaufAusgang> alle = laeufeParallelRechnen(SEEDS);

        List<LaufResultat> erfolgreiche = new ArrayList<>();
        for (LaufAusgang ausgang : alle) {
            if (ausgang.gueltig()) {
                erfolgreiche.add(new LaufResultat(ausgang.loesung(), ausgang.dauerMs()));
            }
        }

        if (erfolgreiche.isEmpty()) {
            throw new IllegalStateException("Keiner der " + SEEDS.length
                    + " Läufe erfüllt die harten Regeln. Es wird nichts gespeichert.");
        }

        erfolgreiche.sort(Comparator.comparing((LaufResultat r) -> r.loesung().getScore()).reversed());

        int anzahl = Math.min(ANZAHL_VARIANTEN, erfolgreiche.size());
        List<LaufErgebnis> ergebnisse = new ArrayList<>();
        for (int i = 0; i < anzahl; i++) {
            LaufResultat resultat = erfolgreiche.get(i);
            int variante = i + 1;
            schreiber.speichern(resultat.loesung(), generationId, variante);
            ergebnisse.add(new LaufErgebnis(variante, resultat.dauerMs(), resultat.loesung().getScore().toString(), true));
        }
        log.info("Generierung {} fertig: {} von {} Läufen gespeichert", generationId, anzahl, SEEDS.length);
        return ergebnisse;
    }

    private List<LaufAusgang> laeufeParallelRechnen(long[] seeds) {
        ExecutorService pool = Executors.newFixedThreadPool(
                Math.min(seeds.length, Runtime.getRuntime().availableProcessors()));
        try {
            List<CompletableFuture<LaufAusgang>> futures = new ArrayList<>();
            for (long seed : seeds) {
                futures.add(CompletableFuture.supplyAsync(() -> laufMitSeed(seed), pool));
            }
            return futures.stream().map(CompletableFuture::join).toList();
        } finally {
            pool.shutdown();
        }
    }

    private LaufAusgang laufMitSeed(long seed) {
        SolverConfig laufConfig = solverConfig.copyConfig().withRandomSeed(seed);
        Stundenplanproblem problem = lader.laden();
        log.info("Seed {} gestartet: {} Einheiten", seed, problem.getEinheiten().size());

        long begonnen = System.currentTimeMillis();
        Stundenplanproblem loesung = SolverFactory.<Stundenplanproblem>create(laufConfig)
                .buildSolver()
                .solve(problem);
        long dauer = System.currentTimeMillis() - begonnen;

        boolean gueltig = loesung.getScore().hardScore() >= 0;
        if (!gueltig) {
            log.warn("Seed {} verworfen, er verletzt harte Regeln: {}", seed, loesung.getScore());
        } else {
            if (loesung.getScore().mediumScore() < 0) {
                log.warn("Seed {} enthält {} Kompromisse bei organisatorischen Regeln",
                        seed, -loesung.getScore().mediumScore());
            }
            log.info("Seed {} fertig nach {} ms, Punktzahl {}", seed, dauer, loesung.getScore());
        }
        return new LaufAusgang(seed, loesung, dauer, gueltig);
    }
}