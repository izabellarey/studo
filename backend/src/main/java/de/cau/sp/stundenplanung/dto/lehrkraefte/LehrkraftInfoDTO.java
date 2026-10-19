package de.cau.sp.stundenplanung.dto.lehrkraefte;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LehrkraftInfoDTO {
    private Long id;
    private String vorname;
    private String nachname;
    private String kuerzel;
}
