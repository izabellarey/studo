package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Schule;

public interface SchuleRepository extends CrudRepository<Schule, Long> {
}
