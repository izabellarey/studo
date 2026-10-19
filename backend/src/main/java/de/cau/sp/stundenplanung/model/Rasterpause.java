package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rasterpause(PauseID, RasterID, NachStunde, DauerMinuten)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Rasterpause {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Stundenraster stundenraster;

    private int nachStunde;

    private int dauerMinuten;

}
