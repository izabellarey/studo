package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Rasterpause;
import de.cau.sp.stundenplanung.model.Stundenraster;
import java.util.List;

public interface RasterpauseRepository extends CrudRepository<Rasterpause, Long> {
    List<Rasterpause> findByStundenraster(Stundenraster stundenraster);
}
