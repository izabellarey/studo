package de.cau.sp.stundenplanung.dto.accounts;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import de.cau.sp.stundenplanung.model.Rolle;

@AllArgsConstructor 
@Getter
@Setter
public class ProfilAnzeigenDTO {
    private String name;
    private String vorname;
    private String benutzername;
    private Rolle rolle;
    private boolean aktiv;
}