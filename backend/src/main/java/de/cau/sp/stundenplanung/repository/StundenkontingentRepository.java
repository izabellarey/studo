package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Stundenkontingent;

public interface StundenkontingentRepository extends CrudRepository<Stundenkontingent, Long> {
}
