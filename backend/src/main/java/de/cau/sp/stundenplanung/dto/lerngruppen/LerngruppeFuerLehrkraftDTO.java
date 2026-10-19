package de.cau.sp.stundenplanung.dto.lerngruppen;


import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LerngruppeFuerLehrkraftDTO {
    private String bezeichnung;
    private int jahrgangsstufe;
    private String klassenzimmer;
}
