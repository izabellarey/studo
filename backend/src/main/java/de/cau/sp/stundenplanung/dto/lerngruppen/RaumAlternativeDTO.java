package de.cau.sp.stundenplanung.dto.lerngruppen;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RaumAlternativeDTO {
    private String bezeichnung;
    private Long raumId;
    private boolean fachraum;
}
