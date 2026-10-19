package de.cau.sp.stundenplanung.dto.stammdaten;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PflichtfachvorgabeDTO {

    private Long fachId;
    private String fachkuerzel;                // aus Fach, z.B. "MA"
    private String fachname;                   // aus Fach, z.B. "Mathematik"
    private boolean hauptfach;
    private List<StufenvorgabeDTO> stufen;     // eine Angabe pro Jahrgangsstufe
}
