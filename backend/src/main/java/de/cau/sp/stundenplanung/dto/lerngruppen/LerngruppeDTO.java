package de.cau.sp.stundenplanung.dto.lerngruppen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LerngruppeDTO {
    private Long lerngruppeId;
    private String bezeichnung;
    private String lehrkraftVorname;
    private String lehrkraftNachname;
    private int schueleranzahl;
    private String klassenzimmer;

}
