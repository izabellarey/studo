package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Ereignis;

public interface EreignisRepository extends CrudRepository<Ereignis, Long> {
}
