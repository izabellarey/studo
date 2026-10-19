package de.cau.sp.stundenplanung.dto.ereignis;

import de.cau.sp.stundenplanung.model.Wochentag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TagDTO {
    private Wochentag wochentag;
    private Long zeitslotId;
    private Long fachId;
}
