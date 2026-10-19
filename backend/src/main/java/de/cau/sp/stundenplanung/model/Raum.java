package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Raum(RID, Bezeichnung, Art, Kapazitaet)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Raum {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String bezeichnung;

    @Enumerated(EnumType.STRING)
    private Raumart art;

    @Positive
    private int kapazitaet;

}