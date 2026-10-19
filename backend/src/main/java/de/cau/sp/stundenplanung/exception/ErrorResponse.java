package de.cau.sp.stundenplanung.exception;

import java.time.Instant;
import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}