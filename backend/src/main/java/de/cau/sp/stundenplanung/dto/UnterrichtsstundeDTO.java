package de.cau.sp.stundenplanung.dto;

import de.cau.sp.stundenplanung.model.EreignisTyp;
import de.cau.sp.stundenplanung.model.Wochentag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnterrichtsstundeDTO {
    private Long id;
    private Wochentag wochentag;
    private int stundennummer;
    private LocalTime start;
    private LocalTime ende;
    private String fachName;
    private String raumBezeichnung;
    private String lehrkraftKuerzel;
    private String lerngruppeKuerzel;
    private boolean geaendert;
    private boolean freistunde;
    private boolean offeneAenderung;
    private EreignisTyp grund;
}
