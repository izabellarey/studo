package de.cau.sp.stundenplanung.dto.stammdaten;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RasterpauseDTO {

    private Long id;               
    private int nachStunde;
    private int dauerMinuten;
}
