package de.cau.sp.stundenplanung.repository;

import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.model.Schueler;
import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Lehrkraft;
import java.util.Optional;
import java.util.List;

public interface LehrkraftRepository extends CrudRepository<Lehrkraft, Long> {

    Optional<Lehrkraft> findByKuerzel(String kuerzel);
    List<Lehrkraft> findByAccount_Aktiv(boolean aktiv);
    Optional<Lehrkraft> findByAccount(Account account);
    List<Lehrkraft> findByAccountAktivTrue();
}
