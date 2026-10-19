package de.cau.sp.stundenplanung.dto.lerngruppen;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LerngruppeUebersichtDTO {
    private Long id;
    private String bezeichnung;
    private int jahrgangsnummer;
}