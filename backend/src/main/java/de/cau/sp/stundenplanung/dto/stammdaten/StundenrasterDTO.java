package de.cau.sp.stundenplanung.dto.stammdaten;

import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StundenrasterDTO {

    private Long id;
    private LocalTime schulbeginn;
    private int stundendauer;       // Minuten je Unterrichtsstunde
    private int stundenProTag;
    private List<RasterpauseDTO> pausen;
    private List<ZeitslotDTO> zeitslots;   // aus den Werten oben berechnet, nur zur Anzeige
}