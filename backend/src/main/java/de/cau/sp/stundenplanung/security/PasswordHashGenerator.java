package de.cau.sp.stundenplanung.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Reines Entwickler-Werkzeug, kein Teil der eigentlichen Anwendung.
 * Generiert BCrypt-Hashes fuer Testdaten, z.B. zum direkten Einfuegen
 * in SQL-Insert/Update-Statements in der H2-Konsole.
 *
 * Aufruf: mehrere Klartext-Passwoerter als Argumente uebergeben,
 * z.B. in IntelliJ unter Run-Configuration -> Program arguments:
 *   test123 test456 admin789
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Bitte mind. ein Klartext-Passwort als Argument uebergeben.");
            System.out.println("Beispiel: test123 test456");
            return;
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        for (String klartext : args) {
            String hash = encoder.encode(klartext);
            System.out.println(klartext + " -> " + hash);
        }
    }
}
