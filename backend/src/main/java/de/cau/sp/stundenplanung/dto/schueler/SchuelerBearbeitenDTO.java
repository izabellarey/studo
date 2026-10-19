package de.cau.sp.stundenplanung.dto.schueler;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor
@Getter 
@Setter 

public class SchuelerBearbeitenDTO {
    private String name;
    private String vorname;
    private Boolean aktiv;
    private LocalDate austrittsdatum;

    private Long klassenId;
}
