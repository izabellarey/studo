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
 * Raumverfuegbarkeit(RID, ZID, Typ, Wochentag)
 */



@Entity
@Getter
@Setter
@NoArgsConstructor
public class Raumverfuegbarkeit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Raum raum;

    @ManyToOne
    private Zeitslot zeitslot;

    @Enumerated(EnumType.STRING)
    private Wochentag wochentag;

    @Enumerated(EnumType.STRING)
    private VerfuegbarkeitsTyp typ;

}