package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/** BRAUCHT MAN DIE KLASSE??? */


/**
 * Stundenkontingent(SKID, KlassenID, FachID, Wochenstunden, WunschLehrerID)
 * Eingabe fuer die Planerzeugung.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Stundenkontingent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Lerngruppe lerngruppe;

    @ManyToOne
    private Fach fach;

    @Positive
    private int wochenstunden;

    @ManyToOne
    private Lehrkraft wunschlehrkraft;

}