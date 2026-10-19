package de.cau.sp.stundenplanung.dto.faecher;

import de.cau.sp.stundenplanung.model.Raumart;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FachAnlegenDTO {
    private String kuerzel;
    private String name;
    private Raumart benoetigterRaumtyp;
}
