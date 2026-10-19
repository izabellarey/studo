package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Stundenraster;

public interface StundenrasterRepository extends CrudRepository<Stundenraster, Long> {
}
