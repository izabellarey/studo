package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Zusatzstunde(EID, KlassenID)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Zusatzstunde extends Ereignis {

    @ManyToOne
    private Lerngruppe lerngruppe;

    @ManyToOne
    private Lehrkraft lehrkraft;

    @ManyToOne
    private Fach fach;

    @ManyToOne
    private Zeitslot zeitslot;

    @ManyToOne
    private Raum raum;



    //neue GET request und dann verfügbare Zeitslots anzeigen,
    // oder dem User überlassen und Lehrer und Klassenstundenplan
    // anzeigen lassen oder gar nix an Service anbieten?
}