package de.cau.sp.stundenplanung.dto.stundenplan;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GenerierungStatusDTO {
    private String status;   // z.B. "LAEUFT", "FERTIG", "FEHLER"
}
