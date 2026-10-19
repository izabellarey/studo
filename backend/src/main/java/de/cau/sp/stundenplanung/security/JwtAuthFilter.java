package de.cau.sp.stundenplanung.security;

import de.cau.sp.stundenplanung.model.Rolle;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Kein Bearer-Token vorhanden:
        // Request normal weiterlaufen lassen.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // JWT wird hier GENAU EINMAL geparst und validiert.
            Claims claims = jwtService.parseToken(token);

            String username = claims.getSubject();
            String roleString = claims.get("role", String.class);

            if (username == null || roleString == null) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Ungültiger Token"
                );
                return;
            }

            Rolle rolle = Rolle.valueOf(roleString);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(
                                    AuthorityMapper.toAuthority(rolle)
                            )
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Ungültiger Token"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}