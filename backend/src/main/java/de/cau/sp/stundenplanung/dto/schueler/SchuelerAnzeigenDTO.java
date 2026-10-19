package de.cau.sp.stundenplanung.dto.schueler;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter 
@Setter 
public class SchuelerAnzeigenDTO {
    private Long id;
    private String name;
    private String vorname;
    private String benutzername;
    private Long klasseId;
    private String klasse;
    private String klassenraum;
    private boolean aktiv;
}
