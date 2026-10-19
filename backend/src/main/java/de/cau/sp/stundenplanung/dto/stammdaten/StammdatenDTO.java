package de.cau.sp.stundenplanung.dto.stammdaten;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StammdatenDTO {

    private SchulprofilDTO schulprofil;
    private List<ZeitslotDTO> stundenraster;
    private List<PausenregelDTO> pausenregeln;
    private List<PflichtfachvorgabeDTO> pflichtfaecher;
    private List<VertretungsprioritaetDTO> vertretungsprioritaet;
}
