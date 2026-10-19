package de.cau.sp.stundenplanung.repository;

import de.cau.sp.stundenplanung.model.Raumart;
import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Raum;


import java.util.List;

public interface RaumRepository extends CrudRepository<Raum, Long> {
    List<Raum> findByArt(Raumart art);
}
