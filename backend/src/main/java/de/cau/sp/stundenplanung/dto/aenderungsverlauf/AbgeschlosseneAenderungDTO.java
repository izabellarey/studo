package de.cau.sp.stundenplanung.dto.aenderungsverlauf;

import de.cau.sp.stundenplanung.model.EreignisTyp;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbgeschlosseneAenderungDTO {

    private Long id;
    private EreignisTyp typ;            // für den Filter: AUSFALL, RAUMAENDERUNG, ZUSATZSTUNDE
    private String anzeigeTyp;           // Text fürs Badge, z.B. "Ausfall" oder "Vertretung" (beides EreignisTyp.AUSFALL)
    private String betroffeneEinheit;   // z.B. Klasse 9a (Physik)
    private String aenderung;          
    private String grund;               
    private String eingetragenVon;     
    private LocalDateTime zeitstempel;
    private boolean freistunde;
}