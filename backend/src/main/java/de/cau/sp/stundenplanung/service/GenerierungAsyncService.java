package de.cau.sp.stundenplanung.service;
import de.cau.sp.stundenplanung.model.Generierungslauf;
import de.cau.sp.stundenplanung.model.GenerierungsStatus;
import de.cau.sp.stundenplanung.repository.GenerierungslaufRepository;
import de.cau.sp.stundenplanung.solver.StundenplanGenerator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter 
@RequiredArgsConstructor 
@Service
public class GenerierungAsyncService {

    private final GenerierungslaufRepository generierungslaufRepository;
    private final StundenplanGenerator stundenplanGenerator;

    @Async
    public void generierungAsyncStarten(Long generationId) {
        try {
            stundenplanGenerator.generieren(generationId);
            Generierungslauf lauf = generierungslaufRepository.findById(generationId)
                .orElseThrow(() -> new RuntimeException("Generierungslauf nicht gefunden"));
            lauf.setStatus(GenerierungsStatus.FERTIG);
            lauf.setFertigAm(LocalDateTime.now());
            generierungslaufRepository.save(lauf);

        } catch (Exception e) {
            System.err.println("Generierung fehlgeschlagen fuer generationId=" + generationId + ": " + e.getMessage());
            e.printStackTrace();

            generierungslaufRepository.findById(generationId).ifPresent(lauf -> {
                lauf.setStatus(GenerierungsStatus.FEHLER);
                generierungslaufRepository.save(lauf);
            });
        }
    }
}