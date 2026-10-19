package de.cau.sp.stundenplanung.dto.verfuegbarkeiten;

import java.time.LocalDate;

import de.cau.sp.stundenplanung.model.VerfuegbarkeitsTyp;
import de.cau.sp.stundenplanung.model.Wochentag;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VerfuegbarkeitFuerLehrkraftDTO {
    private Long zeitslotId;
    private int stundennummer;
    private Wochentag wochentag;
    private VerfuegbarkeitsTyp typ;
    private LocalDate startdatum;
    private LocalDate enddatum;
    private Boolean ganztaegig;
}
