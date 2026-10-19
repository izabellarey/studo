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
 * Pausenregel(PausenID, RBID, JID, MindestpauseNachStunden, MindestdauerMinuten, Haerte, Gewicht)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Pausenregel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Jahrgangsstufe jahrgangsstufe;

    private int mindestpauseNachStunden;

    private int mindestdauerMinuten;

    @Enumerated(EnumType.STRING)
    private Haerte haerte;

    private int gewicht;

    private boolean aktiv;

}