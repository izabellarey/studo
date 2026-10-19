package de.cau.sp.stundenplanung.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Aenderung(AID, UID, Datum, Zeitstempel, RaumNeu, LehrerNeu, FachNeu, Freistunde)
 * ausloeser entspricht der Relation Ausloeser(EID, AID).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Aenderung {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Unterrichtsstunde unterrichtsstunde;

    private LocalDate datum;

    private LocalDateTime zeitstempel;

    @ManyToOne
    private Raum raumNeu;

    @ManyToOne
    private Lehrkraft lehrkraftNeu;

    @ManyToOne
    private Fach fachNeu;

    @ManyToOne
    private Zeitslot zeitslotNeu;

    @ManyToOne
    private Lerngruppe lerngruppeNeu;

    @Enumerated(EnumType.STRING)
    private Wochentag wochentagNeu;

    private boolean freistunde;

    @ManyToOne
    private Ereignis ausloeser;

    private boolean offen; //gibt an, ob die Änderung bereits bestätigt wurde oder noch bestätigt werden mus


}
