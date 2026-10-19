package de.cau.sp.stundenplanung.repository;

import org.springframework.data.repository.CrudRepository;

import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.model.Rolle;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {
    //public List<Account> findName(){}
        List<Account> findByRolle(Rolle rolle);
        List<Account> findByAktiv(boolean aktiv);
        List<Account> findByRolleAndAktiv(Rolle rolle, boolean aktiv);
        Optional<Account> findByBenutzername(String benutzername);
}
