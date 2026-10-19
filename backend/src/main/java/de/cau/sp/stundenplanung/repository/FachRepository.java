package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Fach;

import java.util.Optional;

public interface FachRepository extends CrudRepository<Fach, Long> {
    Optional<Fach> findByKuerzel(String kuerzel);
}
