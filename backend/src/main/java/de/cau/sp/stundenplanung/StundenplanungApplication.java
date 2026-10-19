package de.cau.sp.stundenplanung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class StundenplanungApplication {

    public static void main(String[] args) {
        SpringApplication.run(StundenplanungApplication.class, args);
    }
}