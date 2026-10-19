package de.cau.sp.stundenplanung.dto.stammdaten;
import de.cau.sp.stundenplanung.model.Jahrgangsstufe;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PausenregelDTO {
    
    private Long id;
    private int mindestpauseNachStunden;
    private int mindestdauerMinuten;
    private Long jahrgangsstufeId;
    private boolean aktiv;
}
