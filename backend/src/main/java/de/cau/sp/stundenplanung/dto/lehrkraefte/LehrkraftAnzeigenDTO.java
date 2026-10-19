package de.cau.sp.stundenplanung.dto.lehrkraefte;
import java.util.List;

import de.cau.sp.stundenplanung.dto.faecher.FachUebersichtDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LerngruppeFuerLehrkraftDTO;
import de.cau.sp.stundenplanung.dto.lerngruppen.LerngruppeUebersichtDTO;
import de.cau.sp.stundenplanung.dto.verfuegbarkeiten.VerfuegbarkeitFuerLehrkraftDTO;
import de.cau.sp.stundenplanung.model.Anstellungsverhaeltnis;
import de.cau.sp.stundenplanung.model.Fach;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LehrkraftAnzeigenDTO {
    private Long id;
    private String name;                
    private String vorname;  
    private String benutzername;                
    private boolean aktiv;       

    private String kuerzel;                   
    private Anstellungsverhaeltnis anstellungsverhaeltnis;  
    private int deputatsstunden;              
    private List<FachUebersichtDTO> faecher;
    private List<LerngruppeUebersichtDTO> praeferenzen;

    private LerngruppeFuerLehrkraftDTO lerngruppe;
    private List<VerfuegbarkeitFuerLehrkraftDTO> verfuegbarkeiten;

}
