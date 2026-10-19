package de.cau.sp.stundenplanung.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lerngruppe(KID, Bezeichnung, JID, Schueleranzahl, KlassenleitungID, KlassenzimmerID, Art)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Lerngruppe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String bezeichnung;

    @ManyToOne
    private Jahrgangsstufe jahrgangsstufe;

    private int schueleranzahl;

    @OneToOne
    private Lehrkraft klassenleitung;

    @OneToOne
    private Raum klassenzimmer;

    @Enumerated(EnumType.STRING)
    private LerngruppenArt art;

    private boolean aktiv;

}