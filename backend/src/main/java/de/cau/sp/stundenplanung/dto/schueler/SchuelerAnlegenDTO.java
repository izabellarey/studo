package de.cau.sp.stundenplanung.dto.schueler;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import lombok.AllArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor
@Getter 
@Setter 
public class SchuelerAnlegenDTO {
    private String name;                    
    private String vorname;
    private String passwort;                             
    private LocalDate eintrittsdatum;

    private Long klassenId;

}
