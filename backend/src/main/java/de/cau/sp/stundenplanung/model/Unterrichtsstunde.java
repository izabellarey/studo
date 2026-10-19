package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unterrichtsstunde(UID, Wochentag, RaumID, LehrerID, FachID, LerngruppeID, ZeitID)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Unterrichtsstunde {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Wochentag wochentag;

    @ManyToOne
    private Zeitslot zeitslot;

    @ManyToOne
    private Raum raum;

    @ManyToOne
    private Lehrkraft lehrkraft;

    @ManyToOne
    private Fach fach;

    @ManyToOne
    private Lerngruppe lerngruppe;

    //Idee: jede Stunde bezieht sich auf einen Stundenplan und Stundenplan gehört zu einer Klasse
    //Kein Stundenplan-Objekt für Lehrkräfte, sondern nur Abfrage:
    //"Alle Unterrichtsstunden, bei denen ich lehrkraft bin und die zu einem aktiven Stundenplan gehören"
    //Siehe Stundenplan-Model
    @ManyToOne
    @JoinColumn(name = "stundenplan_id")
    private Stundenplan stundenplan;

}