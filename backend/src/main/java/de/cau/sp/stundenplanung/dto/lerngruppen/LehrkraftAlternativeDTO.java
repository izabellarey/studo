package de.cau.sp.stundenplanung.dto.lerngruppen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LehrkraftAlternativeDTO {
    private String vorname;
    private String nachname;
    private Long lehrkraftId;
    private boolean fachlehrkraft;
}
