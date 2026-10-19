package de.cau.sp.stundenplanung.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /*http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable())); // für h2-console

                */
                http
                        .cors(cors -> {})
                    .csrf(csrf -> csrf.disable()) // stateless REST-API, kein CSRF-Risiko
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                        .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/actuator/health").permitAll()
                                .requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/stammdaten/stundenraster").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/lehrkraftansicht/**").hasAuthority("ROLE_LEHRKRAFT")
                                .requestMatchers(HttpMethod.GET,"/api/schueleransicht/**").hasAuthority("ROLE_SCHUELER")
                                .requestMatchers(HttpMethod.GET, "/api/raeume/**").hasAnyAuthority("ROLE_LEHRKRAFT", "ROLE_PLANUNG")
                                .requestMatchers("/api/**").hasAuthority("ROLE_PLANUNG")
                                .anyRequest().authenticated())
                    .headers(headers -> headers.frameOptions(frame -> frame.disable())) // nur wegen h2
                    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();

    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
