package de.cau.sp.stundenplanung.solver.model;

import de.cau.sp.stundenplanung.model.Wochentag;
import de.cau.sp.stundenplanung.model.Zeitslot;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Zeitfenster {

    private Wochentag wochentag;
    private Zeitslot zeitslot;

    public String schluessel() {
        return wochentag + "-" + zeitslot.getStundennummer();
    }

    @Override
    public String toString() {
        return schluessel();
    }
}