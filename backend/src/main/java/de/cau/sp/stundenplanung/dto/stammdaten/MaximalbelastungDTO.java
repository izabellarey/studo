package de.cau.sp.stundenplanung.dto.stammdaten;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor 
public class MaximalbelastungDTO {
    private Long id;
    private int maxStundenTag;
    private int maxStundenWoche;
    private Long jahrgangsstufeId;
    private boolean aktiv;
}
