package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Raumaenderung;

public interface RaumaenderungRepository extends CrudRepository<Raumaenderung, Long> {
}
