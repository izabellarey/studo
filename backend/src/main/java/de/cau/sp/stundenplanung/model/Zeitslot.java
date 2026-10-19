package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Zeitslot(ZID, Stundennummer, Start, Ende, Pause)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Zeitslot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int stundennummer;

    private LocalTime start;

    private LocalTime ende;
    //KANN WEG, Pausen nicht mehr als Zeitslot
    private boolean pause;

}