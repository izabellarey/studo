package de.cau.sp.stundenplanung.dto.lerngruppen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JahrgangsstufeDTO {
    private Long id;
    private int nummer;
    private int anzahlKlassen;
    //private boolean planmaeßig;
    //private int anzahlOffeneVertretungen;
}
