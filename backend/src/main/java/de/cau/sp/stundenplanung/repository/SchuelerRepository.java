package de.cau.sp.stundenplanung.repository;

import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.model.Lerngruppe;
import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Schueler;
import java.util.List;
import java.util.Optional;

public interface SchuelerRepository extends CrudRepository<Schueler, Long> {
    List<Schueler> findByLerngruppe_Jahrgangsstufe_NummerBetween(int von, int bis);
    List<Schueler> findByAccount_Aktiv(boolean aktiv);
    List<Schueler> findByLerngruppe_Jahrgangsstufe_NummerBetweenAndAccount_Aktiv(int von, int bis, boolean aktiv);
    Optional<Schueler> findByAccount(Account account);
    List<Schueler> findByLerngruppe(Lerngruppe lerngruppe);
}
