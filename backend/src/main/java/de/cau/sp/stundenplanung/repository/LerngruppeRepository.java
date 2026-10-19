package de.cau.sp.stundenplanung.repository;

import de.cau.sp.stundenplanung.model.Jahrgangsstufe;
import de.cau.sp.stundenplanung.model.Raum;
import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Lehrkraft;
import de.cau.sp.stundenplanung.model.Lerngruppe;

import java.util.List;

public interface LerngruppeRepository extends CrudRepository<Lerngruppe, Long> {
    Lerngruppe findByKlassenleitung(Lehrkraft lehrkraft);
    long countByJahrgangsstufeAndAktivTrue(Jahrgangsstufe jahrgangsstufe);
    List<Lerngruppe> findByJahrgangsstufeIdAndAktivTrue(Long jahrgangsstufeId);
    Lerngruppe findByKlassenzimmer(Raum klassenzimmer);
    List<Lerngruppe> findAllByAktivTrue();

}
