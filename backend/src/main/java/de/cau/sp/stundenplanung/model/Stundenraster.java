package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Stundenraster(RasterID, SchulID, Schulbeginn, Stundendauer, StundenProTag) */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Stundenraster {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Schule schule;

    private LocalTime schulbeginn;

    /** in Minuten */
    private int stundendauer;

    private int stundenProTag;

}