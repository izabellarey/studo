package de.cau.sp.stundenplanung.dto.accounts;


import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@AllArgsConstructor 
@Getter
@Setter
public class AccountAnzeigenDTO {
    private Long id;
    private String name;
    private String vorname;
    private String benutzername;
    private boolean aktiv;
}
