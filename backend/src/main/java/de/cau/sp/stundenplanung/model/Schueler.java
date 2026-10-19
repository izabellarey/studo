package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Schueler(SID, NID, Eintrittsdatum, Austrittsdatum)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Schueler {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private Account account;

    private LocalDate eintrittsdatum;

    private LocalDate austrittsdatum;

    @ManyToOne 
    private Lerngruppe lerngruppe;
}