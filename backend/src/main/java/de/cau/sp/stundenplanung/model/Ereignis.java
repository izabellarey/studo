package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ereignis(EID, Typ, Startdatum, Enddatum, Erstellungsdatum, Status, AutorID)
 * Oberklasse von Raumaenderung, Ausfall und Zusatzstunde (JOINED: je eine eigene Tabelle).
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public class Ereignis {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private EreignisTyp typ;

    private LocalDate startdatum;

    private LocalDate enddatum; //nullable, also optional, oder lieber nur bei AUSFALL und RAUMÄNDERUNG?

    private LocalDateTime erstellungsdatum;

    @ManyToOne
    private Account autor;

    @OneToMany(mappedBy = "ausloeser")
    private List<Aenderung> ausgeloesteAenderungen;

}