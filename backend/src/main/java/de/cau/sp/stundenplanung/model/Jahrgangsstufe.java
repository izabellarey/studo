package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Jahrgangsstufe(JID, Prio, Nummer)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Jahrgangsstufe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int nummer;

    private int prio;

}