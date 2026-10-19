package de.cau.sp.stundenplanung.dto.stammdaten;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VertretungsprioritaetDTO {

    private Long jahrgangsstufeId;
    private Integer nummer;   
    private Integer prio;               // wird aus der Listenposition abgeleitet
}
