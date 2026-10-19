package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lehrerverfuegbarkeit(LID, ZeitID, Typ, Wochentag, Startdatum, Enddatum)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Lehrerverfuegbarkeit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Lehrkraft lehrkraft;

    @ManyToOne
    private Zeitslot zeitslot;

    @Enumerated(EnumType.STRING)
    private Wochentag wochentag;

    @Enumerated(EnumType.STRING)
    private VerfuegbarkeitsTyp typ;

    private LocalDate startdatum;

    private LocalDate enddatum;

}