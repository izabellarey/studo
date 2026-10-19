package de.cau.sp.stundenplanung.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stundenplan(STID, Startdatum, Enddatum, Status, Zeitstempel, Schuljahr, Halbjahr, ErstellerNID)
 * Die Liste unterrichtsstunden erzeugt die Verknuepfungstabelle Stundenzuordnung(STID, UID).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Stundenplan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String schuljahr;

    private int halbjahr;

    private LocalDate startdatum;

    private LocalDate enddatum;

    @Enumerated(EnumType.STRING)
    private PlanStatus status;

    private Long generationId;

    private Integer variante;

    private int bewertungsScore;

    private int kompromisse;

    @ManyToOne
    private Account ersteller;

    //Idee: jede Stunde bezieht sich auf einen Stundenplan und Stundenplan gehört zu einer Klasse
    //Kein Stundenplan-Objekt für Lehrkräfte, sondern nur Abfrage:
    //"Alle Unterrichtsstunden, bei denen ich lehrkraft bin und die zu einem aktiven Stundenplan gehören"
    //Siehe Unterrichtsstunde-Model
    @OneToMany(mappedBy = "stundenplan")
    private List<Unterrichtsstunde> unterrichtsstunden;

    //Dann kann das auch auf nullable = false
    @ManyToOne(optional = true)
    @JoinColumn(name = "lerngruppe_id", nullable = false)
    private Lerngruppe lerngruppe;

}