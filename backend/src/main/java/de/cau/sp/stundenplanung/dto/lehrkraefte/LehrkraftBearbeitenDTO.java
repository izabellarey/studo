package de.cau.sp.stundenplanung.dto.lehrkraefte;

import de.cau.sp.stundenplanung.dto.verfuegbarkeiten.VerfuegbarkeitEingabeDTO;
import de.cau.sp.stundenplanung.model.Anstellungsverhaeltnis;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import java.util.List;

@NoArgsConstructor 
@AllArgsConstructor
@Getter
@Setter
public class LehrkraftBearbeitenDTO {
    private String name;                 
    private String vorname;                 
    private Boolean aktiv;                    

    private Anstellungsverhaeltnis anstellungsverhaeltnis;  
    private int deputatsstunden;              
    private List<Long> fachIds;
    
    @Size(max = 3, message = "Maximal 3 Praeferenzen erlaubt")               
    private List<Long> praeferenzIds;

    private Long lerngruppeId; // falls Klassenlehrer
    private List<VerfuegbarkeitEingabeDTO> verfuegbarkeiten;
}
