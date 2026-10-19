package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pflichtfachvorgaben(PFID, RBID, JID, FID, stundenanzahl, Haerte, Gewicht)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Pflichtfachvorgabe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Jahrgangsstufe jahrgangsstufe;

    @ManyToOne
    private Fach fach;

    private int stundenanzahl;

    @Enumerated(EnumType.STRING)
    private Haerte haerte;

    private int gewicht;

    private boolean aktiv;

}