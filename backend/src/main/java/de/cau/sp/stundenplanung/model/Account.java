package de.cau.sp.stundenplanung.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Account(NID, Name, Vorname, Passwort, Rolle, Aktiv, Benutzername)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String benutzername;

    @NotBlank
    private String name;

    @NotBlank
    private String vorname;

    @NotBlank
    private String passwort;

    @Enumerated(EnumType.STRING)
    private Rolle rolle;

    private boolean aktiv;
}