package de.cau.sp.stundenplanung.dto.stammdaten;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SchulprofilDTO {

    private String bundesland;  
    private String schulform;   
    private String name;  
}
