package de.cau.sp.stundenplanung.dto.accounts;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor 
@Getter
@Setter
public class AccountBearbeitenDTO {
    private String name;
    private String vorname;
    private Boolean aktiv;
}
