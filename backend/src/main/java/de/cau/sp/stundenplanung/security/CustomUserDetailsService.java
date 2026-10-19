package de.cau.sp.stundenplanung.security;

import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Account benutzer = accountRepository.findByBenutzername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Nicht gefunden"));

        return User.builder()
                .username(benutzer.getBenutzername())
                .password(benutzer.getPasswort())
                .authorities(
                        AuthorityMapper.toAuthority(benutzer.getRolle())
                )
                .disabled(!benutzer.isAktiv())
                .build();
    }
}