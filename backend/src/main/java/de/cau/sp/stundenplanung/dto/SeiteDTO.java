package de.cau.sp.stundenplanung.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeiteDTO<T> {

    private List<T> inhalt;
    private int seite;            // beginnt bei 0
    private int groesse;          // Einträge pro Seite
    private long gesamtElemente;
    private int gesamtSeiten;
}
