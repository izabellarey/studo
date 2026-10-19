package de.cau.sp.stundenplanung.security;

import de.cau.sp.stundenplanung.model.Rolle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthorityMapperTest {
    @Test
    void mapsRoleToSpringAuthority() {
        assertEquals("ROLE_LEHRKRAFT", AuthorityMapper.toAuthority(Rolle.LEHRKRAFT).getAuthority());
        assertEquals("ROLE_SCHUELER", AuthorityMapper.toAuthority(Rolle.SCHUELER).getAuthority());
        assertEquals("ROLE_PLANUNG", AuthorityMapper.toAuthority(Rolle.PLANUNG).getAuthority());
    }
}
