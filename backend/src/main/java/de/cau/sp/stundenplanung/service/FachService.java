package de.cau.sp.stundenplanung.service;

import de.cau.sp.stundenplanung.dto.faecher.FachAnlegenDTO;
import de.cau.sp.stundenplanung.dto.faecher.FachUebersichtDTO;
import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.repository.FachRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FachService {

    private final FachRepository fachRepository;

    public List<FachUebersichtDTO> alleFaecher() {
        List<Fach> faecher = new ArrayList<>();
        fachRepository.findAll().forEach(faecher::add);

        List<FachUebersichtDTO> result = new ArrayList<>();
        for (Fach f : faecher) {
            FachUebersichtDTO dto = new FachUebersichtDTO();
            dto.setId(f.getId());
            dto.setKuerzel(f.getKuerzel());
            dto.setName(f.getName());
            dto.setBenoetigterRaumtyp(f.getBenoetigterRaumtyp()); 
            result.add(dto);
        }
        return result;
    }

    public FachUebersichtDTO fachAnlegen(FachAnlegenDTO dto) {
        if (fachRepository.findByKuerzel(dto.getKuerzel()).isPresent()) {
            throw new RuntimeException("Ein Fach mit diesem Kuerzel existiert bereits");
        }

        Fach fach = new Fach();
        fach.setKuerzel(dto.getKuerzel());
        fach.setName(dto.getName());
        fach.setBenoetigterRaumtyp(dto.getBenoetigterRaumtyp());
        fach.setHauptfach(false); // ueber die API angelegte Faecher sind nie Hauptfach

        Fach gespeichert = fachRepository.save(fach);

        FachUebersichtDTO result = new FachUebersichtDTO();
        result.setId(gespeichert.getId());
        result.setKuerzel(gespeichert.getKuerzel());
        result.setName(gespeichert.getName());
        result.setBenoetigterRaumtyp(gespeichert.getBenoetigterRaumtyp());
        return result;
    }
}
