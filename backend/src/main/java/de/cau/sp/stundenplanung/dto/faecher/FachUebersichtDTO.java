package de.cau.sp.stundenplanung.dto.faecher;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import de.cau.sp.stundenplanung.model.Raumart;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FachUebersichtDTO {
    private Long id;
    private String kuerzel;
    private String name;
    private Raumart benoetigterRaumtyp;
}