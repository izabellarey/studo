package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lehrer(LID, NID, Kuerzel, Anstellungsverhaeltnis, Deputatsstunden)
 * faecher entspricht der Relation Ausbildung(LID, FID).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Lehrkraft {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private Account account;

    @NotBlank
    private String kuerzel;

    @Enumerated(EnumType.STRING)
    private Anstellungsverhaeltnis anstellungsverhaeltnis;

    private int deputatsstunden;

    @ManyToMany
    private List<Fach> faecher;

    @ManyToMany
    private List<Lerngruppe> praeferenzen;

}