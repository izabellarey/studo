package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Blockregel;
import de.cau.sp.stundenplanung.model.Fach;
import de.cau.sp.stundenplanung.model.Jahrgangsstufe;
import java.util.Optional;

public interface BlockregelRepository extends CrudRepository<Blockregel, Long> {
    Optional<Blockregel> findByFachAndJahrgangsstufe(Fach fach, Jahrgangsstufe jahrgangsstufe);
}
