package de.cau.sp.stundenplanung.dto.aenderungsverlauf;

import de.cau.sp.stundenplanung.model.EreignisTyp;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Eine offene Aenderung mit Bearbeiten- und Erledigt-Knopf.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OffeneAenderungDTO {

    private Long id;
    private EreignisTyp typ;           // Ausfall, Raumsperrung oder Zusatzstunde
    private String titel;              
    private String beschreibung;
    private Long ereignisId;          
    private Long unterrichtsstundeId;  
    private Long klassenstufeId;
    private Long klasseId;
    private LocalDateTime erstelltAm;
}
