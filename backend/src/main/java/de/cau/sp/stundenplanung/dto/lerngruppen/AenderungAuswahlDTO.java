package de.cau.sp.stundenplanung.dto.lerngruppen;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AenderungAuswahlDTO {
    private RaumAlternativeDTO gewaehlterRaum;
    private LehrkraftAlternativeDTO gewaehlteLehrkraft;
}
