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
 * Maximalbelastung(MaxID, RBID, JID, LID, MaxStundenTag, MaxStundenWoche, MaxAufeinanderfolgend, Haerte, Gewicht)
 * jahrgangsstufe ODER lehrkraft ist gesetzt; keines von beiden bedeutet: gilt fuer alle.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Maximalbelastung {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Jahrgangsstufe jahrgangsstufe;

    private Integer maxStundenTag;

    private Integer maxStundenWoche;

    @Enumerated(EnumType.STRING)
    private Haerte haerte;

    private int gewicht;

    private boolean aktiv;

}