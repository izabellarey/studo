package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Pflichtfachvorgabe;

public interface PflichtfachvorgabeRepository extends CrudRepository<Pflichtfachvorgabe, Long> {
}
