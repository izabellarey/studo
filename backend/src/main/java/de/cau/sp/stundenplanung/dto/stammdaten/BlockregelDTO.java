package de.cau.sp.stundenplanung.dto.stammdaten;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor 
public class BlockregelDTO {
    private Long id;
    private Long fachId;
    private Long jahrgangsstufeId;
    private boolean aktiv;
}
