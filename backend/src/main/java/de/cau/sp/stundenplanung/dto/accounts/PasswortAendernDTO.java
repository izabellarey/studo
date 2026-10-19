package de.cau.sp.stundenplanung.dto.accounts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswortAendernDTO {
    private String altesPasswort;
    private String neuesPasswort;
}