package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Raumverfuegbarkeit;
import de.cau.sp.stundenplanung.model.Zeitslot;
import java.util.List;

public interface RaumverfuegbarkeitRepository extends CrudRepository<Raumverfuegbarkeit, Long> {
    List<Raumverfuegbarkeit> findByZeitslot(Zeitslot zeitslot);
}
