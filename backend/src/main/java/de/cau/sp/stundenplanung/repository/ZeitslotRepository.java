package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Zeitslot;

import java.util.List;

public interface ZeitslotRepository extends CrudRepository<Zeitslot, Long> {
    List<Zeitslot> findByStundennummerBetweenOrderByStundennummerAsc(int von, int bis);
}
