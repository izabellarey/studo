package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rahmenbedingungen(RBID, Bezeichnung, Aktiv)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Rahmenbedingung {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String bezeichnung;

    private boolean aktiv;

}