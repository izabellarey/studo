package de.cau.sp.stundenplanung.repository;

import de.cau.sp.stundenplanung.model.Generierungslauf;
import org.springframework.data.repository.CrudRepository;

public interface GenerierungslaufRepository extends CrudRepository<Generierungslauf, Long> {
}
