package de.cau.sp.stundenplanung.security;

import de.cau.sp.stundenplanung.model.Rolle;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class AuthorityMapper {

    private AuthorityMapper() {
    }

    public static SimpleGrantedAuthority toAuthority(Rolle rolle) {
        return new SimpleGrantedAuthority("ROLE_" + rolle.name());
    }
}
