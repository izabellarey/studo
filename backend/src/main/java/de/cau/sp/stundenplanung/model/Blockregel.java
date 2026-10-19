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
 * Blockregel(BlockID, RBID, FachID, JID, Haerte, Gewicht)
 * Ein Eintrag bedeutet: dieses Fach wird in diesem Jahrgang als Doppelstunde unterrichtet.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Blockregel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Fach fach;

    @ManyToOne
    private Jahrgangsstufe jahrgangsstufe;

    @Enumerated(EnumType.STRING)
    private Haerte haerte;

    private int gewicht;

    private boolean aktiv;

}