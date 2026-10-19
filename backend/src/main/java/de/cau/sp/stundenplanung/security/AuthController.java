package de.cau.sp.stundenplanung.security;
import de.cau.sp.stundenplanung.dto.security.LoginRequest;
import de.cau.sp.stundenplanung.dto.security.LoginResponse;
import de.cau.sp.stundenplanung.model.Account;
import de.cau.sp.stundenplanung.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountRepository accountRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.benutzername(), request.passwort())
        );

        Account nutzer = accountRepository.findByBenutzername(request.benutzername()).orElseThrow();
        //nutzer aktiv?
        String token = jwtService.generateToken(nutzer.getBenutzername(), nutzer.getRolle().name());

        return ResponseEntity.ok(new LoginResponse(token, nutzer.getName(), nutzer.getVorname(), nutzer.getRolle().name()));
    }
}
