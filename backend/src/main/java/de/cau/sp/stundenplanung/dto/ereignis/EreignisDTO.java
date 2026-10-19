package de.cau.sp.stundenplanung.dto.ereignis;

import de.cau.sp.stundenplanung.model.EreignisStatus;
import de.cau.sp.stundenplanung.model.EreignisTyp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import de.cau.sp.stundenplanung.model.Raumart;
import de.cau.sp.stundenplanung.model.Zeitslot;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EreignisDTO {

    private Long id;
    private EreignisTyp typ;                 
    private LocalDate startdatum;

    private LocalDate enddatum; //hinzugefügt
    private boolean ganztag; //für AUSFALL, RAUMÄNDERUNG
    private Long startzeitId; //nur bei AUSFALL, RAUMÄNDERUNG, für Zusatzstunde entsprechender Zeitslot
    private Long endzeitId; //s.o.

    //private EreignisStatus status; //noch wichtig? wir haben ob Änderung offen ist oder nicht
    private LocalDateTime erstellungsdatum;
    private Long autorId; //hier eher ID von Ersteller, wird ja nur im Header mitgegeben

    private Long lehrkraftId;                // nur bei AUSFALL und benutzername statt ID?
    private Long raumId;                     // nur bei RAUMÄNDERUNG, dropdown
    private Long lerngruppeId;               // nur bei ZUSATZSTUNDE,
    private Long fachId;                    //ZUSATZSTUNDE
    private String grund;                   //AUSFALL, rauslassen?
}
