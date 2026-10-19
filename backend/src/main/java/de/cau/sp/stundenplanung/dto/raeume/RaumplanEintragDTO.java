package de.cau.sp.stundenplanung.dto.raeume;

import de.cau.sp.stundenplanung.model.Wochentag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
@Getter
@Setter
@AllArgsConstructor
public class RaumplanEintragDTO {
    private Wochentag wochentag;
    private int stundennummer;
    private LocalTime start;
    private LocalTime ende;
    private String fachName;
    private String lehrkraftKuerzel;
    private String lerngruppeKuerzel;
    private boolean geaendert;
    private boolean freistunde;
}
