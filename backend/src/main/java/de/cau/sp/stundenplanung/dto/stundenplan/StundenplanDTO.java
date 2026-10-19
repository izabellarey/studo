package de.cau.sp.stundenplanung.dto.stundenplan;

import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StundenplanDTO {
    private String schuljahr;
    private int halbjahr;
    private String lerngruppeBezeichnung;
    private LocalDate gueltigStart;
    private LocalDate gueltigEnde;
    private String erstellerVorname;
    private String erstellerNachname;
    private List<UnterrichtsstundeDTO> unterrichtsstunde;

}
