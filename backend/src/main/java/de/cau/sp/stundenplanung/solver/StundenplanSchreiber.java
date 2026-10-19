package de.cau.sp.stundenplanung.solver;

import de.cau.sp.stundenplanung.model.Lerngruppe;
import de.cau.sp.stundenplanung.model.PlanStatus;
import de.cau.sp.stundenplanung.model.Stundenplan;
import de.cau.sp.stundenplanung.model.Unterrichtsstunde;
import de.cau.sp.stundenplanung.model.Zeitslot;
import de.cau.sp.stundenplanung.repository.StundenplanRepository;
import de.cau.sp.stundenplanung.repository.UnterrichtsstundeRepository;
import de.cau.sp.stundenplanung.repository.ZeitslotRepository;
import de.cau.sp.stundenplanung.solver.model.PlanungsEinheit;
import de.cau.sp.stundenplanung.solver.model.Stundenplanproblem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StundenplanSchreiber {

    private final StundenplanRepository stundenplanRepository;
    private final UnterrichtsstundeRepository unterrichtsstundeRepository;
    private final ZeitslotRepository zeitslotRepository;

    private record Zeitraum(String schuljahr, int halbjahr) {
    }

    private static Zeitraum aktuellerZeitraum() {
        LocalDate heute = LocalDate.now();
        int monat = heute.getMonthValue();
        int jahr = heute.getYear();

        if (monat >= 3 && monat <= 8) {
            return new Zeitraum(schuljahrLabel(jahr), 1);
        }
        int zieljahr = monat >= 9 ? jahr + 1 : jahr;
        return new Zeitraum(schuljahrLabel(zieljahr - 1), 2);
    }

    private static String schuljahrLabel(int startjahr) {
        return startjahr + "/" + String.format("%02d", (startjahr + 1) % 100);
    }

    @Transactional
    public int speichern(Stundenplanproblem loesung, long generationId, int variante) {
        int bewertung = Math.toIntExact(loesung.getScore().softScore());
        int kompromisse = Math.toIntExact(-loesung.getScore().mediumScore());
        Zeitraum zeitraum = aktuellerZeitraum();
        Map<Integer, Zeitslot> slots = new HashMap<>();
        zeitslotRepository.findAll().forEach(z -> slots.put(z.getStundennummer(), z));

        Map<Lerngruppe, List<PlanungsEinheit>> jeGruppe = loesung.getEinheiten().stream()
                .collect(Collectors.groupingBy(PlanungsEinheit::getLerngruppe,
                        LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Lerngruppe, List<PlanungsEinheit>> eintrag : jeGruppe.entrySet()) {
            Stundenplan plan = new Stundenplan();
            plan.setLerngruppe(eintrag.getKey());
            plan.setSchuljahr(zeitraum.schuljahr());
            plan.setHalbjahr(zeitraum.halbjahr());
            plan.setStatus(PlanStatus.ENTWURF);
            plan.setGenerationId(generationId);
            plan.setVariante(variante);
            plan.setBewertungsScore(bewertung);
            plan.setKompromisse(kompromisse);
            stundenplanRepository.save(plan);

            for (PlanungsEinheit einheit : eintrag.getValue()) {
                for (int nummer = einheit.stunde(); nummer <= einheit.letzteStunde(); nummer++) {
                    Zeitslot slot = slots.get(nummer);
                    if (slot == null) {
                        throw new IllegalStateException("Kein Zeitslot für Stunde " + nummer);
                    }
                    unterrichtsstundeRepository.save(zuUnterrichtsstunde(einheit, plan, slot));
                }
            }
        }
        return jeGruppe.size();
    }

    private Unterrichtsstunde zuUnterrichtsstunde(PlanungsEinheit einheit, Stundenplan plan,
                                                  Zeitslot slot) {
        Unterrichtsstunde stunde = new Unterrichtsstunde();
        stunde.setWochentag(einheit.wochentag());
        stunde.setZeitslot(slot);
        stunde.setRaum(einheit.getRaum());
        stunde.setLehrkraft(einheit.getLehrkraft());
        stunde.setFach(einheit.getFach());
        stunde.setLerngruppe(einheit.getLerngruppe());
        stunde.setStundenplan(plan);
        return stunde;
    }
}