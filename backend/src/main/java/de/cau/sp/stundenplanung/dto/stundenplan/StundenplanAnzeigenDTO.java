package de.cau.sp.stundenplanung.dto.stundenplan;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import de.cau.sp.stundenplanung.model.PlanStatus;

import de.cau.sp.stundenplanung.dto.UnterrichtsstundeDTO;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StundenplanAnzeigenDTO {
    private Long id;
    private PlanStatus status;
    private Integer variante;
    private List<UnterrichtsstundeDTO> unterrichtsstunden;
}
