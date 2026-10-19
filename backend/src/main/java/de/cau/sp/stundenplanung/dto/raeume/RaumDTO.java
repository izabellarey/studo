package de.cau.sp.stundenplanung.dto.raeume;

import de.cau.sp.stundenplanung.model.Raumart;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RaumDTO {
    private Long id;
    private String bezeichnung;
    private Raumart art;
    private int kapazitaet;

}
