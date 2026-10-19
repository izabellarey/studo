package de.cau.sp.stundenplanung.dto.dashboard;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private long anzahlOffeneEreignisse;                   
    private List<AktuelleAenderungDTO> aktuelleAenderungen;
}

