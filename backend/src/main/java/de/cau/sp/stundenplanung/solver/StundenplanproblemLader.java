package de.cau.sp.stundenplanung.solver;

import de.cau.sp.stundenplanung.model.Blockregel;
import de.cau.sp.stundenplanung.model.Lehrerverfuegbarkeit;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Maximalbelastung;
import de.cau.sp.stundenplanung.model.Raum;
import de.cau.sp.stundenplanung.model.Raumverfuegbarkeit;
import de.cau.sp.stundenplanung.repository.BlockregelRepository;
import de.cau.sp.stundenplanung.repository.LehrerverfuegbarkeitRepository;
import de.cau.sp.stundenplanung.repository.LehrkraftRepository;
import de.cau.sp.stundenplanung.repository.MaximalbelastungRepository;
import de.cau.sp.stundenplanung.repository.RaumRepository;
import de.cau.sp.stundenplanung.repository.RaumverfuegbarkeitRepository;
import de.cau.sp.stundenplanung.solver.model.PlanungsEinheit;
import de.cau.sp.stundenplanung.solver.model.Stundenplanproblem;
import de.cau.sp.stundenplanung.solver.model.Zeitfenster;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Component
@RequiredArgsConstructor
public class StundenplanproblemLader {

    private final LehrkraftRepository lehrkraftRepository;
    private final RaumRepository raumRepository;
    private final LehrerverfuegbarkeitRepository lehrerverfuegbarkeitRepository;
    private final RaumverfuegbarkeitRepository raumverfuegbarkeitRepository;
    private final BlockregelRepository blockregelRepository;
    private final MaximalbelastungRepository maximalbelastungRepository;
    private final EinheitenGenerator einheitenGenerator;

    @Transactional(readOnly = true)
    public Stundenplanproblem laden() {
        List<Lehrkraft> lehrkraefte = lehrkraftRepository.findByAccountAktivTrue();
        for (Lehrkraft l : lehrkraefte) {
            l.getFaecher().size();
            l.getPraeferenzen().size();
        }

        List<Raum> raeume = alle(raumRepository.findAll());
        List<Lehrerverfuegbarkeit> lehrerzeiten = alle(lehrerverfuegbarkeitRepository.findAll());
        List<Raumverfuegbarkeit> raumzeiten = alle(raumverfuegbarkeitRepository.findAll());

        List<Blockregel> blockregeln = new ArrayList<>();
        for (Blockregel b : blockregelRepository.findAll()) {
            if (b.isAktiv()) {
                blockregeln.add(b);
            }
        }
        List<Maximalbelastung> belastungen = new ArrayList<>();
        for (Maximalbelastung m : maximalbelastungRepository.findAll()) {
            if (m.isAktiv()) {
                belastungen.add(m);
            }
        }

        int stundenProTag = einheitenGenerator.stundenProTag();
        List<Zeitfenster> zeitfenster = einheitenGenerator.zeitfensterErzeugen();
        List<Zeitfenster> blockfenster = einheitenGenerator.blockfensterErzeugen(zeitfenster);
        List<PlanungsEinheit> einheiten = einheitenGenerator.einheitenErzeugen(
                lehrkraefte, raeume, blockregeln, belastungen, zeitfenster, blockfenster, stundenProTag);

        return new Stundenplanproblem(lehrkraefte, raeume, zeitfenster,
                lehrerzeiten, raumzeiten, einheiten);
    }

    private static <T> List<T> alle(Iterable<T> quelle) {
        List<T> liste = new ArrayList<>();
        quelle.forEach(liste::add);
        return liste;
    }
}