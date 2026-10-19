package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Zusatzstunde;

public interface ZusatzstundeRepository extends CrudRepository<Zusatzstunde, Long> {
}
