package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ausfall(EID, LehrerID, Grund)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Ausfall extends Ereignis {

    @ManyToOne
    private Lehrkraft lehrkraft;

    private String grund;

}