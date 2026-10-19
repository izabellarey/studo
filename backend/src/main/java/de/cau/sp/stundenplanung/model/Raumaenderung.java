package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raumaenderung(EID, RaumID)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Raumaenderung extends Ereignis {

    @ManyToOne
    private Raum raum;

}