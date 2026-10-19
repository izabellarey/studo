package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Jahrgangsstufe;

import java.util.Optional;

public interface JahrgangsstufeRepository extends CrudRepository<Jahrgangsstufe, Long> {
    Optional<Jahrgangsstufe> findFirstByNummerGreaterThanOrderByNummerAsc(int nummer);}
