package de.cau.sp.stundenplanung.dto.dashboard;

import de.cau.sp.stundenplanung.model.EreignisTyp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AktuelleAenderungDTO {

    private Long id;
    private EreignisTyp typ;      // steuert ob rot gelb usw.
    private String titel;
    private String beschreibung;
    private boolean offen; //gibt an, ob Änderung noch bearbeitet werden muss oder nicht
    private Long klassenstufeId;
    private Long klasseId;

    // Für Lehrer / Schüleransicht
    public AktuelleAenderungDTO(Long id, EreignisTyp typ, String titel, String beschreibung, boolean offen) {
        this(id, typ, titel, beschreibung, offen, null, null);
    }
}
