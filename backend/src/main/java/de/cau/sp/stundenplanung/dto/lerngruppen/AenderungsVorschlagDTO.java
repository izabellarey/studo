package de.cau.sp.stundenplanung.dto.lerngruppen;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AenderungsVorschlagDTO {
    private List<RaumAlternativeDTO> raumalternativen;
    private List<LehrkraftAlternativeDTO> lehrkraftalternativen;
}
