package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Fach(FID, Kuerzel, Raumtyp, Name)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id") //Wichtig, sonst funktioniert contains() nicht wie erwartet (siehe lehrkraftPasstZuFach() in LerngruppeService)
public class Fach {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String kuerzel;

    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    private Raumart benoetigterRaumtyp;

    private boolean hauptfach;
}