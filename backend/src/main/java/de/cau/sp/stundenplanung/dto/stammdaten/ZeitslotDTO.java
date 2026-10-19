package de.cau.sp.stundenplanung.dto.stammdaten;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZeitslotDTO {

    private Long id;            
    private int stundennummer;
    private LocalTime start;
    private LocalTime ende;
    //Kann weg!
    //private boolean pause;      // true = Pausenslot, kein Unterricht
}
