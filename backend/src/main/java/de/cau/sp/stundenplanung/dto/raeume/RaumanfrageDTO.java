package de.cau.sp.stundenplanung.dto.raeume;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RaumanfrageDTO {
    private Long zeitslotId;
    private Long fachId;
    private LocalDateTime startdatum;
}

