package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Pausenregel;

public interface PausenregelRepository extends CrudRepository<Pausenregel, Long> {
}
