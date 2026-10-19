package de.cau.sp.stundenplanung.service;


import de.cau.sp.stundenplanung.model.*;
import de.cau.sp.stundenplanung.repository.GenerierungslaufRepository;
import de.cau.sp.stundenplanung.repository.StundenplanRepository;
import de.cau.sp.stundenplanung.repository.UnterrichtsstundeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor  
@Getter 
@Setter 
@Service 
public class GenerierungAnlegenService {
    
    private final GenerierungslaufRepository generierungslaufRepository;
    private final StundenplanRepository stundenplanRepository;
    private final UnterrichtsstundeRepository unterrichtsstundeRepository;

    @Transactional 
    public Long generierungAnlegen(){
        //Sicherheits-Check, falls Planung erneut auf Generieren klickt
        List<Generierungslauf> alleLaeufe = new ArrayList<>();
        generierungslaufRepository.findAll().forEach(alleLaeufe::add);

        for (Generierungslauf lauf : alleLaeufe) {
            if (lauf.getStatus() == GenerierungsStatus.LAEUFT) {
                throw new RuntimeException("Es laeuft bereits eine Generierung, bitte warten Sie bis diese abgeschlossen ist");
            }
        }

        //Alte, fertige/fehlerhafte, aber nicht veroeffentlichte Entwuerfe komplett verwerfen
        for (Generierungslauf lauf : alleLaeufe) {
            if (lauf.getStatus() == GenerierungsStatus.FERTIG || lauf.getStatus() == GenerierungsStatus.FEHLER) {
                List<Stundenplan> alleZuDieserGeneration = stundenplanRepository.findByGenerationId(lauf.getId());

                List<Stundenplan> nurEntwuerfe = new ArrayList<>();
                for (Stundenplan plan : alleZuDieserGeneration) {
                    if (plan.getStatus() == PlanStatus.ENTWURF) {
                        nurEntwuerfe.add(plan);
                    }
                }

                for (Stundenplan plan : nurEntwuerfe) {
                    unterrichtsstundeRepository.deleteAll(plan.getUnterrichtsstunden());
                }

                stundenplanRepository.deleteAll(nurEntwuerfe);
                generierungslaufRepository.delete(lauf);
            }
        }

        //Jetzt Lauf starten
        Generierungslauf lauf = new Generierungslauf();
        lauf.setStatus(GenerierungsStatus.LAEUFT);
        lauf.setGestartetAm(LocalDateTime.now());
        Generierungslauf gespeicherterLauf = generierungslaufRepository.save(lauf);

        return gespeicherterLauf.getId();
    }
}
