package de.cau.sp.stundenplanung.dto.stammdaten;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StufenvorgabeDTO {

    private Long id;
    private Long jahrgangsstufeId;
    private Integer jahrgangsnummer;
    private Integer stundenanzahl;
    private boolean doppelstundenBevorzugt;
    private boolean aktiv;
}