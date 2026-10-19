package de.cau.sp.stundenplanung.solver.model;

import ai.timefold.solver.core.api.domain.common.PlanningId;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Lerngruppe;
import de.cau.sp.stundenplanung.model.Raum;
import de.cau.sp.stundenplanung.model.Wochentag;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@PlanningEntity
@Getter
@Setter
@NoArgsConstructor
public class PlanungsEinheit {

    @PlanningId
    private Long id;


    private Lerngruppe lerngruppe;
    private Fach fach;

    /** 1 für eine Einzelstunde, 2 für eine Doppelstunde. */
    private int dauer = 1;
    private Integer maxStundenTag;
    private int stundenProTag;

    @ValueRangeProvider(id = "moeglicheLehrkraefte")
    private List<Lehrkraft> moeglicheLehrkraefte;

    @ValueRangeProvider(id = "moeglicheRaeume")
    private List<Raum> moeglicheRaeume;

    @ValueRangeProvider(id = "moeglicheZeitfenster")
    private List<Zeitfenster> moeglicheZeitfenster;

    @PlanningVariable(valueRangeProviderRefs = "moeglicheLehrkraefte")
    private Lehrkraft lehrkraft;

    @PlanningVariable(valueRangeProviderRefs = "moeglicheRaeume")
    private Raum raum;

    @PlanningVariable(valueRangeProviderRefs = "moeglicheZeitfenster")
    private Zeitfenster zeitfenster;

    public PlanungsEinheit(Long id, Lerngruppe lerngruppe, Fach fach, int dauer,
                           Integer maxStundenTag, int stundenProTag,
                           List<Lehrkraft> moeglicheLehrkraefte,
                           List<Raum> moeglicheRaeume, List<Zeitfenster> moeglicheZeitfenster) {
        this.id = id;
        this.lerngruppe = lerngruppe;
        this.fach = fach;
        this.dauer = dauer;
        this.maxStundenTag = maxStundenTag;
        this.stundenProTag = stundenProTag;
        this.moeglicheLehrkraefte = moeglicheLehrkraefte;
        this.moeglicheRaeume = moeglicheRaeume;
        this.moeglicheZeitfenster = moeglicheZeitfenster;
    }

    public Wochentag wochentag() {
        return zeitfenster.getWochentag();
    }

    public int stunde() {
        return zeitfenster.getZeitslot().getStundennummer();
    }

    public int letzteStunde() {
        return stunde() + dauer - 1;
    }

    public boolean belegt(int stundennummer) {
        return stundennummer >= stunde() && stundennummer <= letzteStunde();
    }

    public int letzteVormittagsstunde() {
        return (stundenProTag + 1) / 2;
    }

    public int ersteRandstunde() {
        return stundenProTag - 1;
    }

    public int prio() {
        if (lerngruppe.getJahrgangsstufe() == null) {
            return 1;
        }
        return Math.max(1, 20 - lerngruppe.getJahrgangsstufe().getPrio());
    }

    @Override
    public String toString() {
        return lerngruppe.getBezeichnung() + " " + fach.getKuerzel()
                + (dauer > 1 ? " (Doppelstunde)" : "")
                + " @ " + (zeitfenster == null ? "-" : zeitfenster.schluessel());
    }
}