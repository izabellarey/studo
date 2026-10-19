package de.cau.sp.stundenplanung.dto.security;

public record LoginResponse(String token, String name, String vorname, String rolle) { //account.getVorname(), account.getNachname()

}
